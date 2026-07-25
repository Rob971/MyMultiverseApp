package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.domain.sync.NetworkConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Test/dev fallback when no platform monitor is registered. */
class AlwaysOnlineNetworkConnectivityMonitor : NetworkConnectivityMonitor {
    override fun observeOnline(): Flow<Boolean> = flowOf(true)

    override fun isOnline(): Boolean = true
}
