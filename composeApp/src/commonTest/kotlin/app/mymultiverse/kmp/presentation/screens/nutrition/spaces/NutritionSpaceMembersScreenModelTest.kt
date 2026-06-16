package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionSpaceMembersScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSpaceCollaborationRepository
    private lateinit var model: NutritionSpaceMembersScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSpaceCollaborationRepository()
        model = NutritionSpaceMembersScreenModel(
            collaborationRepository = repository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitAddPerson_withUnknownEmail_sendsInvite() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        model.openAddPersonDialog()
        model.onEmailChange("invite@example.com")
        model.onRoleChange(SpaceMemberRole.Viewer)

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(SpaceMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
    }

    @Test
    fun submitCreateEventGroup_withoutExpiry_setsError() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        advanceUntilIdle()
        model.openCreateGroupDialog()
        model.onGroupNameChange("Ski trip")
        model.onGroupLifecycleChange(GroupLifecycle.Event)

        model.submitCreateGroup("space-1")
        advanceUntilIdle()

        assertEquals(SpaceMembersError.EventExpiresRequired, model.uiState.value.error)
    }

    @Test
    fun submitCreateEventGroup_withExpiry_createsGroup() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        model.openCreateGroupDialog()
        model.onGroupNameChange("Ski trip")
        model.onGroupLifecycleChange(GroupLifecycle.Event)
        model.onGroupExpiresChange("2030-12-31")

        model.submitCreateGroup("space-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.groups.any { it.name == "Ski trip" })
        assertFalse(model.uiState.value.showCreateGroupDialog)
    }

    @Test
    fun refreshMembers_includesOwnerRow() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        advanceUntilIdle()

        val owner = model.uiState.value.members.single()
        assertEquals(SpaceMemberRole.Owner, owner.role)
        assertEquals("owner-owner", owner.id)
    }

    @Test
    fun submitAddGroupMember_addsToGroup() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        model.openManageGroupDialog("group-1")
        model.onGroupMemberEmailChange("friend@example.com")

        model.submitAddGroupMember()
        advanceUntilIdle()

        assertEquals(SpaceMembersSuccess.GroupMemberAdded, model.uiState.value.successMessageKey)
        assertEquals(1, model.uiState.value.groupMembers.size)
    }
}
