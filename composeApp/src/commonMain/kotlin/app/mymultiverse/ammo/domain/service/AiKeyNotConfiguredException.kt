package app.mymultiverse.ammo.domain.service

/**
 * Thrown by [NutritionAiAssistantService] implementations when the user has not
 * yet configured a Gemini API key and the requested operation requires one.
 *
 * Screen models catch this exception to show a polite setup prompt rather than
 * a generic error, directing the user to Account & settings.
 */
class AiKeyNotConfiguredException : Exception("ai_key_not_configured")
