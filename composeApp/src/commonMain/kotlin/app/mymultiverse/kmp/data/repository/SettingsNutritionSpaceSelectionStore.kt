package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsNutritionSpaceSelectionStore(
    private val settings: Settings,
) : NutritionSpaceSelectionStore {
    private val activeSpaceId = MutableStateFlow(
        settings.getStringOrNull(KEY_ACTIVE_SPACE_ID)?.takeIf { it.isNotBlank() },
    )

    override fun observeActiveSpaceId(): Flow<String?> = activeSpaceId.asStateFlow()

    override suspend fun setActiveSpaceId(spaceId: String) {
        settings.putString(KEY_ACTIVE_SPACE_ID, spaceId)
        activeSpaceId.value = spaceId
    }

    override suspend fun clearActiveSpaceId() {
        settings.remove(KEY_ACTIVE_SPACE_ID)
        activeSpaceId.value = null
    }

    private companion object {
        const val KEY_ACTIVE_SPACE_ID = "nutrition_active_space_id"
    }
}
