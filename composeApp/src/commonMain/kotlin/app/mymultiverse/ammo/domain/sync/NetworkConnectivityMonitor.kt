package app.mymultiverse.ammo.domain.sync

import kotlinx.coroutines.flow.Flow

/**
 * Observes device network reachability so nutrition sync can retry when connectivity returns.
 */
interface NetworkConnectivityMonitor {
    fun observeOnline(): Flow<Boolean>

    fun isOnline(): Boolean
}
