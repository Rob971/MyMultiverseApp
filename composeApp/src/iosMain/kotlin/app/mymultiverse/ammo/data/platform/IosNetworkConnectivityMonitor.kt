package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.sync.NetworkConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * iOS connectivity monitoring is not wired yet; assume online so shared sync logic can run.
 */
class IosNetworkConnectivityMonitor : NetworkConnectivityMonitor {
    override fun observeOnline(): Flow<Boolean> = flowOf(true)

    override fun isOnline(): Boolean = true
}
