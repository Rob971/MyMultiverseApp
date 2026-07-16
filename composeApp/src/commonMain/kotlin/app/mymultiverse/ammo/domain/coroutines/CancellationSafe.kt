package app.mymultiverse.ammo.domain.coroutines

import kotlinx.coroutines.CancellationException

/**
 * Cancellation-safe replacement for [runCatching] in suspend contexts.
 *
 * Standard [runCatching] catches **all** Throwables, including [CancellationException].
 * A cancelled coroutine becomes `Result.failure(CancellationException)`, silently breaking
 * structured concurrency and making cancelled calls look like business failures.
 *
 * This function is `suspend inline` so the lambda can itself call suspend functions, and the
 * try/catch is inlined at the call site where the coroutine context is available.
 */
@Suppress("NOTHING_TO_INLINE")
suspend inline fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
