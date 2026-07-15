package app.mymultiverse.ammo.data.manager

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository
import app.mymultiverse.ammo.domain.repository.AuthRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncedAiAssistantSettingsTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private fun TestScope.makeSettings(
        initialRemoteKey: String = "",
        initialLocalKey: String = "",
        initialAuthState: AuthState = AuthState.Unauthenticated,
        remoteFailure: Throwable? = null,
    ): Triple<SyncedAiAssistantSettings, FakeRemote, FakeAuthRepo> {
        val fakeRemote = FakeRemote(storedKey = initialRemoteKey, failure = remoteFailure)
        val fakeAuth = FakeAuthRepo(initialAuthState)
        val localSettings = MapSettings().also {
            if (initialLocalKey.isNotBlank()) it.putString(SettingsAiAssistantSettings.KEY, initialLocalKey)
        }
        // backgroundScope: its coroutines are cancelled (not failed) at test end → no UncompletedCoroutinesError
        val sut = SyncedAiAssistantSettings(
            local = SettingsAiAssistantSettings(settings = localSettings),
            remote = fakeRemote,
            authRepository = fakeAuth,
            scope = backgroundScope,
        )
        return Triple(sut, fakeRemote, fakeAuth)
    }

    @Test
    fun setGeminiApiKey_updatesLocalImmediately() = runTest(dispatcher) {
        val (sut, _, _) = makeSettings()

        sut.setGeminiApiKey("my-key")

        assertEquals("my-key", sut.geminiApiKey.value)
    }

    @Test
    fun setGeminiApiKey_pushesToRemote() = runTest(dispatcher) {
        val (sut, remote, _) = makeSettings()

        sut.setGeminiApiKey("my-key")
        advanceUntilIdle()

        assertEquals("my-key", remote.storedKey)
    }

    @Test
    fun clearGeminiApiKey_clearsLocalImmediately() = runTest(dispatcher) {
        val (sut, _, _) = makeSettings(initialLocalKey = "existing-key")

        sut.clearGeminiApiKey()

        assertEquals("", sut.geminiApiKey.value)
    }

    @Test
    fun clearGeminiApiKey_pushesEmptyToRemote() = runTest(dispatcher) {
        val (sut, remote, _) = makeSettings(initialRemoteKey = "old-key", initialLocalKey = "old-key")

        sut.clearGeminiApiKey()
        advanceUntilIdle()

        assertEquals("", remote.storedKey)
    }

    @Test
    fun signIn_syncsPullsRemoteKeyToLocal_whenLocalIsEmpty() = runTest(dispatcher) {
        val (sut, _, fakeAuth) = makeSettings(initialRemoteKey = "remote-key")

        fakeAuth.signIn()
        advanceUntilIdle()

        assertEquals("remote-key", sut.geminiApiKey.value)
    }

    @Test
    fun signIn_remoteKeyOverwritesStaleLocalKey() = runTest(dispatcher) {
        val (sut, _, fakeAuth) = makeSettings(
            initialRemoteKey = "updated-key",
            initialLocalKey = "old-local-key",
        )

        fakeAuth.signIn()
        advanceUntilIdle()

        assertEquals("updated-key", sut.geminiApiKey.value)
    }

    @Test
    fun signIn_doesNotClearLocalKey_whenRemoteIsEmpty() = runTest(dispatcher) {
        val (sut, _, fakeAuth) = makeSettings(
            initialRemoteKey = "",
            initialLocalKey = "local-only-key",
        )

        fakeAuth.signIn()
        advanceUntilIdle()

        assertEquals("local-only-key", sut.geminiApiKey.value)
    }

    @Test
    fun signIn_syncDoesNotFail_whenRemoteThrows() = runTest(dispatcher) {
        val (sut, _, fakeAuth) = makeSettings(
            initialLocalKey = "safe-key",
            remoteFailure = RuntimeException("network error"),
        )

        fakeAuth.signIn()
        advanceUntilIdle()

        assertEquals("safe-key", sut.geminiApiKey.value)
    }

    @Test
    fun setKey_doesNotFail_whenRemoteThrows() = runTest(dispatcher) {
        val (sut, _, _) = makeSettings(remoteFailure = RuntimeException("offline"))

        sut.setGeminiApiKey("new-key")
        advanceUntilIdle()

        assertEquals("new-key", sut.geminiApiKey.value)
    }

    @Test
    fun refreshFromRemote_pullsKeyAfterTransientFailure() = runTest(dispatcher) {
        val remote = FakeRemote(storedKey = "remote-key", failuresBeforeSuccess = 2)
        val fakeAuth = FakeAuthRepo(AuthState.Unauthenticated)
        val sut = SyncedAiAssistantSettings(
            local = SettingsAiAssistantSettings(settings = MapSettings()),
            remote = remote,
            authRepository = fakeAuth,
            scope = backgroundScope,
        )

        sut.refreshFromRemote()
        advanceUntilIdle()

        assertEquals("remote-key", sut.geminiApiKey.value)
        assertEquals(3, remote.fetchCount)
    }

    @Test
    fun secondSignInWithSameUser_doesNotTriggerSync() = runTest(dispatcher) {
        val (_, remote, fakeAuth) = makeSettings(initialRemoteKey = "key")

        fakeAuth.signIn()
        advanceUntilIdle()
        val firstFetchCount = remote.fetchCount

        fakeAuth.signIn()
        advanceUntilIdle()

        assertEquals(firstFetchCount, remote.fetchCount, "Second sign-in with same user must not re-fetch")
    }

    @Test
    fun differentUserSignIn_triggersFreshSync() = runTest(dispatcher) {
        val (_, remote, fakeAuth) = makeSettings(initialRemoteKey = "key")

        fakeAuth.signIn(userId = "user-a")
        advanceUntilIdle()
        val firstFetchCount = remote.fetchCount

        fakeAuth.signIn(userId = "user-b")
        advanceUntilIdle()

        assertTrue(remote.fetchCount > firstFetchCount, "New user sign-in must trigger a fresh remote sync")
    }

    private class FakeRemote(
        storedKey: String = "",
        private val failure: Throwable? = null,
        private val failuresBeforeSuccess: Int = 0,
    ) : AiSettingsRemoteRepository {
        var storedKey: String = storedKey
        var fetchCount: Int = 0
        private var remainingFailures = failuresBeforeSuccess

        override suspend fun getGeminiApiKey(): Result<String> {
            fetchCount++
            if (failure != null) return Result.failure(failure)
            if (remainingFailures > 0) {
                remainingFailures--
                return Result.failure(RuntimeException("transient"))
            }
            return Result.success(storedKey)
        }

        override suspend fun upsertGeminiApiKey(key: String): Result<Unit> {
            if (failure != null) return Result.failure(failure)
            storedKey = key.trim()
            return Result.success(Unit)
        }

        override suspend fun clearGeminiApiKey(): Result<Unit> {
            if (failure != null) return Result.failure(failure)
            storedKey = ""
            return Result.success(Unit)
        }
    }

    private class FakeAuthRepo(
        initialState: AuthState = AuthState.Unauthenticated,
    ) : AuthRepository {
        private val _authState = MutableStateFlow(initialState)
        override val authState: StateFlow<AuthState> = _authState.asStateFlow()

        fun signIn(userId: String = "fake-user") {
            _authState.value = AuthState.Authenticated(
                AuthUser(id = userId, email = "$userId@example.com", displayName = "Test"),
            )
        }

        override suspend fun restoreSession() = Unit
        override suspend fun signInWithEmail(email: String, password: String) = Result.success(Unit)
        override suspend fun signUpWithEmail(email: String, password: String, displayName: String?) = Result.success(Unit)
        override suspend fun sendEmailOtp(email: String) = Result.success(Unit)
        override suspend fun verifyEmailOtp(email: String, code: String) = Result.success(Unit)
        override suspend fun signInWithGoogle() = Result.failure<Unit>(UnsupportedOperationException())
        override suspend fun signInWithApple() = Result.failure<Unit>(UnsupportedOperationException())
        override suspend fun signOut() { _authState.value = AuthState.Unauthenticated }
        override suspend fun exportPersonalData() = Result.success("{}")
        override suspend fun deleteAccount() = Result.success(Unit)
    }
}
