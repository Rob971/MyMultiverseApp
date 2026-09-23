package app.mymultiverse.ammo.presentation.invite

import app.mymultiverse.ammo.data.invite.InviteRedirectEvents
import app.mymultiverse.ammo.data.invite.InviteRedirectUrls
import app.mymultiverse.ammo.data.invite.InviteSessionStore
import app.mymultiverse.ammo.data.observability.AppLogger
import kotlinx.coroutines.CancellationException
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
            } catch (e: CancellationException) {
                // Coroutine cancelled (e.g. user navigated away) — reset silently, don't show failure.
                _acceptState.value = InviteJoinAcceptState.Idle
                throw e
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
            // Clear the token only on server-authoritative terminal rejections. A transient
            // failure (network, timeout, unknown) must retain it so a retry still reaches the
            // same invitation instead of silently dropping the user into creation.
            if (isTerminalInviteError(throwable) || isAlreadyReconciledInviteError(throwable)) {
                clearPendingInvite()
            }
            throw throwable
        }

        val acceptError = collaborationRepository.acceptInvite(preview.inviteId).exceptionOrNull()
        if (acceptError != null) {
            if (mapAcceptError(acceptError) == InviteJoinAcceptError.EmailMismatch) {
                _acceptState.value = InviteJoinAcceptState.Failed(
                    error = InviteJoinAcceptError.EmailMismatch,
                    mismatchContext = InviteEmailMismatchContext(
                        invitedEmail = preview.inviteeEmail,
                        householdName = preview.householdName,
                    ),
                )
                throw acceptError
            }

            if (isAlreadyReconciledInviteError(acceptError)) {
                // The invite was already accepted (e.g. elsewhere) or the invitee already
                // belongs to a household. Reconcile through membership rather than surfacing
                // a failure: success only if the active membership is the INVITED household.
                clearPendingInvite()
                val status = householdRepository.refreshMembership().getOrNull()
                if (status is HouseholdMembershipStatus.Active && status.household.id == preview.householdId) {
                    activateNutritionSessionIfActive(status)
                    return preview
                }
            }

            throw acceptError
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

    private fun isTerminalInviteError(throwable: Throwable): Boolean =
        CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_NOT_FOUND, throwable.message) ||
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_EXPIRED, throwable.message) ||
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_DECLINED, throwable.message)

    private fun isAlreadyReconciledInviteError(throwable: Throwable): Boolean =
        CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_ALREADY_ACCEPTED, throwable.message) ||
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITEE_HOUSEHOLD_ALREADY_ACTIVE, throwable.message)
}
