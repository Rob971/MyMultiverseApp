package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import kotlinx.datetime.Instant

internal fun NutritionWeekDataRow.updatedAtEpochMilliseconds(): Long =
    updatedAt
        ?.let { raw -> runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrNull() }
        ?: Long.MIN_VALUE

internal fun List<NutritionWeekDataRow>.latestForDataKind(dataKind: String): NutritionWeekDataRow? =
    filter { it.dataKind == dataKind }
        .maxWithOrNull(compareBy { it.updatedAtEpochMilliseconds() })
