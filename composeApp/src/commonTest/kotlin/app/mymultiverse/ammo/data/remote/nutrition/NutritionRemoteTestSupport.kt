package app.mymultiverse.ammo.data.remote.nutrition

/**
 * For fakes whose tests do not exercise the version precondition. It states explicitly
 * that a conditional write is a plain upsert that succeeds, so no fake leans on an
 * interface default. Fakes that test concurrency implement real versioned CAS instead.
 */
internal suspend fun NutritionRemoteDataSource.pushViaUpsert(
    householdId: String,
    weekKey: String,
    dataKind: String,
    payload: String,
    expectedVersion: Long?,
): PushResult {
    upsert(householdId, weekKey, dataKind, payload)
    return PushResult.ok((expectedVersion ?: 0L) + 1)
}
