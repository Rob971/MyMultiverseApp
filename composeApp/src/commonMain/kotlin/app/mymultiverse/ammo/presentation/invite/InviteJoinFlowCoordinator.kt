package app.mymultiverse.ammo.presentation.invite

import app.mymultiverse.ammo.data.invite.InviteRedirectEvents
import app.mymultiverse.ammo.data.invite.InviteRedirectUrls
import app.mymultiverse.ammo.data.invite.InviteSessionStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InviteEmailMismatchContext(
    val invitedEmail: String,
    val householdName: String,
)

sealed interface InviteJoinAcceptState {
    data object Idle : InviteJoinAcceptState

    data object Accepting : InviteJoinAcceptState

    data class Failed(
        val error: InviteJoinAcceptError,
        val mismatchContext: InviteEmailMismatchContext? = null,
    ) : InviteJoinAcceptState

    data class Succeeded(val householdName: String) : InviteJoinAcceptState
}

enum class InviteJoinAcceptError {
    EmailMismatch,
    Generic,
}

class InviteJoinFlowCoordinator(
    private val inviteSessionStore: InviteSessionStore,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val householdRepository: HouseholdRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val logger: AppLogger,
    private val scope: CoroutineScope,
) {
    private val _pendingInviteToken = MutableStateFlow(inviteSessionStore.getPendingInviteToken())
    val pendingInviteToken: StateFlow<String?> = _pendingInviteToken.asStateFlow()

    private val _acceptState = MutableStateFlow<InviteJoinAcceptState>(InviteJoinAcceptState.Idle)
    val acceptState: StateFlow<InviteJoinAcceptState> = _acceptState.asStateFlow()

    private var acceptInFlight = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            InviteRedirectEvents.urls.collect(::handleInviteRedirect)
        }
        consumeColdStartRedirect()
    }

    fun consumeColdStartRedirect() {
        InviteRedirectEvents.consumePending()?.let(::handleInviteRedirect)
    }

    fun handleInviteRedirect(url: String) {
        val token = InviteRedirectUrls.parseToken(url) ?: return
        logger.breadcrumb("invite_token_received")
        persistPendingToken(token)
    }

    fun clearPendingInvite() {
        inviteSessionStore.clearPendingInviteToken()
        _pendingInviteToken.value = null
        _acceptState.value = InviteJoinAcceptState.Idle
        acceptInFlight = false
    }

    fun clearAcceptSuccess() {
        if (_acceptState.value is InviteJoinAcceptState.Succeeded) {
            _acceptState.value = InviteJoinAcceptState.Idle
        }
    }

    fun dismissAcceptFailure() {
        if (_acceptState.value is InviteJoinAcceptState.Failed) {
            _acceptState.value = InviteJoinAcceptState.Idle
            acceptInFlight = false
        }
    }

    fun retryAfterEmailMismatch(signOut: suspend () -> Unit) {
        scope.launch {
            dismissAcceptFailure()
            signOut()
        }
    }

    fun acceptPendingInviteIfNeeded() {
        val token = _pendingInviteToken.value?.takeIf { it.isNotBlank() } ?: return
        if (acceptInFlight || _acceptState.value is InviteJoinAcceptState.Accepting) return

        acceptInFlight = true
        _acceptState.value = InviteJoinAcceptState.Accepting
        scope.launch {
            try {
                logger.breadcrumb("invite_accept_started")
                val preview = completeJoinFromToken(token)
                logger.breadcrumb("invite_accept_ok household=${preview.householdName.take(32)}")
                _acceptState.value = InviteJoinAcceptState.Succeeded(preview.householdName)
            } catch (throwable: Throwable) {
                if (_acceptState.value !is InviteJoinAcceptState.Failed) {
                    logger.recordError(
                        tag = "InviteJoinFlow",
                        message = "accept_pending_invite_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _acceptState.value = InviteJoinAcceptState.Failed(
                        error = InviteJoinAcceptError.Generic,
                    )
                }
            } finally {
                acceptInFlight = false
            }
        }
    }

    private suspend fun completeJoinFromToken(token: String): HouseholdInvitePreview {
        val preview = collaborationRepository.previewInvite(token).getOrElse { throwable ->
            clearPendingInvite()
            throw throwable
        }
        collaborationRepository.acceptInvite(preview.inviteId).getOrElse { throwable ->
            val error = mapAcceptError(throwable)
            if (error == InviteJoinAcceptError.EmailMismatch) {
                _acceptState.value = InviteJoinAcceptState.Failed(
                    error = error,
                    mismatchContext = InviteEmailMismatchContext(
                        invitedEmail = preview.inviteeEmail,
                        householdName = preview.householdName,
                    ),
                )
            }
            throw throwable
        }
        clearPendingInvite()
        householdRepository.refreshMembership()
            .onSuccess { status -> activateNutritionSessionIfActive(status) }
        return preview
    }

    private fun persistPendingToken(token: String) {
        inviteSessionStore.setPendingInviteToken(token)
        _pendingInviteToken.value = token
        if (_acceptState.value !is InviteJoinAcceptState.Succeeded) {
            _acceptState.value = InviteJoinAcceptState.Idle
        }
        acceptInFlight = false
    }

    private suspend fun activateNutritionSessionIfActive(status: HouseholdMembershipStatus) {
        val householdId = (status as? HouseholdMembershipStatus.Active)?.household?.id ?: return
        runCatching { sessionCoordinator.activateHousehold(householdId) }
    }

    private fun mapAcceptError(throwable: Throwable): InviteJoinAcceptError =
        when {
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.INVITE_EMAIL_MISMATCH,
                throwable.message,
            ) -> InviteJoinAcceptError.EmailMismatch
            else -> InviteJoinAcceptError.Generic
        }
}
