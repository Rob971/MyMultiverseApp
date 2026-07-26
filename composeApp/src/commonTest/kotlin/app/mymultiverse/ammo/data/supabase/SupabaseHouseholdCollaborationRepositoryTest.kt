package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.observability.TestObservability
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseHouseholdCollaborationRepositoryTest {

    @Test
    fun removeMember_usesAuthorizedRpcContract() = runTest {
        val invocations = mutableListOf<Pair<String, JsonObject>>()
        val repository = repository(invocations)

        repository.removeMember("member-123").getOrThrow()

        assertEquals(1, invocations.size)
        assertEquals("remove_household_member", invocations.single().first)
        assertEquals("member-123", invocations.single().second["p_member_id"]?.jsonPrimitive?.content)
    }

    @Test
    fun declineInvite_usesAuthorizedRpcContract() = runTest {
        val invocations = mutableListOf<Pair<String, JsonObject>>()
        val repository = repository(invocations)

        repository.declineInvite("invite-456").getOrThrow()

        assertEquals(1, invocations.size)
        assertEquals("decline_household_invite", invocations.single().first)
        assertEquals("invite-456", invocations.single().second["p_invite_id"]?.jsonPrimitive?.content)
    }

    private fun repository(
        invocations: MutableList<Pair<String, JsonObject>>,
    ): SupabaseHouseholdCollaborationRepository {
        val client = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "test-key",
        ) {}
        return SupabaseHouseholdCollaborationRepository(
            client = client,
            logger = TestObservability.logger,
            invokeMutationRpc = { name, parameters ->
                invocations += name to parameters
            },
        )
    }
}
