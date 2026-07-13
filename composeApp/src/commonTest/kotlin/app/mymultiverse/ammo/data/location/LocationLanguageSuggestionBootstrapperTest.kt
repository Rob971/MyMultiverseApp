package app.mymultiverse.ammo.data.location

import app.mymultiverse.ammo.domain.location.DeviceRegion
import app.mymultiverse.ammo.domain.location.FakeDeviceRegionService
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import app.mymultiverse.ammo.presentation.di.FakeLanguageManager
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationLanguageSuggestionBootstrapperTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // ── isFirstLaunch ────────────────────────────────────────────────────

    @Test
    fun isFirstLaunch_trueWhenNoKeyInSettings() {
        val bootstrapper = makeBootstrapper(MapSettings(), FakeDeviceRegionService())
        assertTrue(bootstrapper.isFirstLaunch())
    }

    @Test
    fun isFirstLaunch_falseWhenKeyAlreadyStored() {
        val settings = MapSettings().apply {
            putString(SupportedAppLanguages.SETTINGS_KEY, "it")
        }
        val bootstrapper = makeBootstrapper(settings, FakeDeviceRegionService())
        assertFalse(bootstrapper.isFirstLaunch())
    }

    // ── needsLocationPermissionForLanguage ───────────────────────────────

    @Test
    fun needsLocationPermission_trueForItalyFirstLaunch() {
        val bootstrapper = makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(localeCountry = "IT"),
        )
        assertTrue(bootstrapper.needsLocationPermissionForLanguage())
    }

    @Test
    fun needsLocationPermission_falseForNonItaly() {
        val bootstrapper = makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(localeCountry = "FR"),
        )
        assertFalse(bootstrapper.needsLocationPermissionForLanguage())
    }

    @Test
    fun needsLocationPermission_falseWhenLanguageAlreadyStored() {
        val settings = MapSettings().apply {
            putString(SupportedAppLanguages.SETTINGS_KEY, "it")
        }
        val bootstrapper = makeBootstrapper(
            settings,
            FakeDeviceRegionService(localeCountry = "IT"),
        )
        assertFalse(bootstrapper.needsLocationPermissionForLanguage())
    }

    @Test
    fun needsLocationPermission_falseForNullCountryCode() {
        val bootstrapper = makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(localeCountry = null),
        )
        assertFalse(bootstrapper.needsLocationPermissionForLanguage())
    }

    // ── bootstrapIfFirstLaunch ───────────────────────────────────────────

    @Test
    fun firstLaunch_withKnownRegion_appliesDetectedLanguage() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("FR"))

        makeBootstrapper(settings, regionService, languageManager)
            .bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("fr", languageManager.currentLanguage.value)
        assertEquals(1, regionService.callCount)
    }

    @Test
    fun firstLaunch_inCampania_appliesNapolitan() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("IT", "Campania"))

        makeBootstrapper(MapSettings(), regionService, languageManager)
            .bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("nap", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_inNapoli_appliesNapolitan() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(DeviceRegion("IT", "Napoli")),
            languageManager,
        ).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("nap", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_italyOtherRegion_appliesItalian() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(DeviceRegion("IT", "Lombardia")),
            languageManager,
        ).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("it", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_italyNoAdminArea_appliesItalian() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(DeviceRegion("IT", adminArea = null)),
            languageManager,
        ).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("it", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_regionServiceReturnsNull_appliesEnglishFallback() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(regionToReturn = null),
            languageManager,
        ).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("en", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_regionServiceThrows_appliesEnglishFallback() = runTest(testDispatcher) {
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        makeBootstrapper(
            MapSettings(),
            FakeDeviceRegionService(throwOnCall = RuntimeException("Location unavailable")),
            languageManager,
        ).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("en", languageManager.currentLanguage.value)
    }

    @Test
    fun subsequentLaunch_withLanguageAlreadyStored_doesNotDetect() = runTest(testDispatcher) {
        val settings = MapSettings().apply { putString(SupportedAppLanguages.SETTINGS_KEY, "de") }
        val languageManager = FakeLanguageManager("de")
        val regionService = FakeDeviceRegionService(DeviceRegion("FR"))

        makeBootstrapper(settings, regionService, languageManager).bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("de", languageManager.currentLanguage.value)
        assertEquals(0, regionService.callCount)
    }

    @Test
    fun calledTwice_detectsOnlyOnce() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("ES"))
        val bootstrapper = makeBootstrapper(settings, regionService, languageManager)

        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        settings.putString(SupportedAppLanguages.SETTINGS_KEY, languageManager.currentLanguage.value)

        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals(1, regionService.callCount)
        assertEquals("es", languageManager.currentLanguage.value)
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private fun makeBootstrapper(
        settings: MapSettings,
        regionService: FakeDeviceRegionService,
        languageManager: FakeLanguageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE),
    ) = LocationLanguageSuggestionBootstrapper(
        settings = settings,
        languageManager = languageManager,
        deviceRegionService = regionService,
        scope = TestScope(testDispatcher),
    )
}
