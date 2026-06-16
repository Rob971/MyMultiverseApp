package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.AppTopic
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionSpacesScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun refreshFailure_setsLoadErrorWithoutEmptyCreateError() = runTest(testDispatcher) {
        val repository = FakeSharingSpaceRepository(
            refreshFailure = IllegalStateException("network"),
        )
        val model = model(repository)

        advanceUntilIdle()

        assertFalse(model.uiState.value.isLoading)
        assertEquals(NutritionSpacesError.Generic, model.uiState.value.loadError)
        assertEquals(null, model.uiState.value.createError)
    }

    @Test
    fun openCreateDialog_preservesLoadErrorButClearsCreateError() = runTest(testDispatcher) {
        val repository = FakeSharingSpaceRepository(
            refreshFailure = IllegalStateException("network"),
        )
        val model = model(repository)
        advanceUntilIdle()

        model.submitCreateSpace {}
        model.openCreateDialog()

        assertEquals(NutritionSpacesError.Generic, model.uiState.value.loadError)
        assertEquals(null, model.uiState.value.createError)
    }

    @Test
    fun submitCreateSpace_blankNameSetsCreateErrorOnly() = runTest(testDispatcher) {
        val model = model(FakeSharingSpaceRepository())
        advanceUntilIdle()

        model.openCreateDialog()
        model.submitCreateSpace {}

        assertEquals(null, model.uiState.value.loadError)
        assertEquals(NutritionSpacesError.NameRequired, model.uiState.value.createError)
    }

    @Test
    fun submitCreateSpace_successClosesDialogAndSelectsSpace() = runTest(testDispatcher) {
        val selectionStore = FakeNutritionSpaceSelectionStore()
        val model = model(FakeSharingSpaceRepository(), selectionStore)
        advanceUntilIdle()

        model.openCreateDialog()
        model.onCreateNameChange("Family")
        model.submitCreateSpace {}
        advanceUntilIdle()

        assertFalse(model.uiState.value.showCreateDialog)
        assertEquals("space-1", selectionStore.activeSpaceId.value)
        assertEquals("Family", model.uiState.value.spaces.single().name)
    }

    @Test
    fun submitCreateSpace_successStillWorksAfterRefreshError() = runTest(testDispatcher) {
        val selectionStore = FakeNutritionSpaceSelectionStore()
        val model = model(
            FakeSharingSpaceRepository(refreshFailure = IllegalStateException("network")),
            selectionStore,
        )
        advanceUntilIdle()

        model.openCreateDialog()
        model.onCreateNameChange("Offline family")
        model.submitCreateSpace {}
        advanceUntilIdle()

        assertFalse(model.uiState.value.showCreateDialog)
        assertEquals(NutritionSpacesError.Generic, model.uiState.value.loadError)
        assertEquals(null, model.uiState.value.createError)
        assertEquals("space-1", selectionStore.activeSpaceId.value)
    }

    private fun model(
        repository: FakeSharingSpaceRepository,
        selectionStore: FakeNutritionSpaceSelectionStore = FakeNutritionSpaceSelectionStore(),
    ): NutritionSpacesScreenModel =
        NutritionSpacesScreenModel(
            sharingSpaceRepository = repository,
            selectionStore = selectionStore,
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
        )
}

private class FakeSharingSpaceRepository(
    private val refreshFailure: Throwable? = null,
) : SharingSpaceRepository {
    private val spaces = MutableStateFlow<List<SharingSpace>>(emptyList())

    override fun observeNutritionSpaces(): Flow<List<SharingSpace>> = spaces.asStateFlow()

    override suspend fun createNutritionSpace(
        name: String,
        features: Set<NutritionSharingFeature>,
    ): Result<SharingSpace> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("space_name_required"))
        if (features.isEmpty()) return Result.failure(IllegalArgumentException("space_features_required"))

        val created = SharingSpace(
            id = "space-${spaces.value.size + 1}",
            topic = AppTopic.Nutrition,
            name = trimmed,
            ownerId = "owner-1",
            features = features,
        )
        spaces.value = spaces.value + created
        return Result.success(created)
    }

    override suspend fun refreshNutritionSpaces() {
        refreshFailure?.let { throw it }
    }
}

private class FakeNutritionSpaceSelectionStore : NutritionSpaceSelectionStore {
    val activeSpaceId = MutableStateFlow<String?>(null)

    override fun observeActiveSpaceId(): Flow<String?> = activeSpaceId.asStateFlow()

    override suspend fun setActiveSpaceId(spaceId: String) {
        activeSpaceId.value = spaceId
    }

    override suspend fun clearActiveSpaceId() {
        activeSpaceId.value = null
    }
}
