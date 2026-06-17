package app.mymultiverse.kmp.domain.platform

/**
 * Shares exported GDPR JSON via the platform share sheet (or clipboard fallback).
 */
interface PersonalDataExporter {
    fun shareJson(filename: String, content: String): Boolean
}
