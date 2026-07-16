package app.mymultiverse.ammo.domain.coroutines

import kotlinx.coroutines.CancellationException

/**
 * Like [runCatching] but re-throws [CancellationException] so structured concurrency is
 * never broken.  Replace every `runCatching { suspendCall() }.onFailure { … }` pattern
 * that wraps suspend code that must respect coroutine cancellation.
 *
 * Never call bare `runCatching` in suspend functions — it catches CancellationException
 * and silently turns a cancelled coroutine into a `Result.failure`.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
