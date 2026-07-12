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

@OptIn(ExperimentalCoroutinesApi::class)
class LocationLanguageSuggestionBootstrapperTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun firstLaunch_withKnownRegion_appliesDetectedLanguage() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("FR"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("fr", languageManager.currentLanguage.value)
        assertEquals(1, regionService.callCount)
    }

    @Test
    fun firstLaunch_inCampania_appliesNapolitan() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("IT", "Campania"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("nap", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_inNapoli_appliesNapolitan() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("IT", "Napoli"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("nap", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_italyOtherRegion_appliesItalian() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("IT", "Lombardia"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("it", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_regionServiceReturnsNull_appliesEnglishFallback() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(regionToReturn = null)

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("en", languageManager.currentLanguage.value)
    }

    @Test
    fun firstLaunch_regionServiceThrows_appliesEnglishFallback() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(
            throwOnCall = RuntimeException("Location unavailable"),
        )

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("en", languageManager.currentLanguage.value)
    }

    @Test
    fun subsequentLaunch_withLanguageAlreadyStored_doesNotDetect() = runTest(testDispatcher) {
        val settings = MapSettings().apply {
            putString(SupportedAppLanguages.SETTINGS_KEY, "de")
        }
        val languageManager = FakeLanguageManager("de")
        val regionService = FakeDeviceRegionService(DeviceRegion("FR"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )
        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals("de", languageManager.currentLanguage.value)
        assertEquals(0, regionService.callCount)
    }

    @Test
    fun calledTwice_detectsOnlyOnce() = runTest(testDispatcher) {
        val settings = MapSettings()
        val languageManager = FakeLanguageManager(SupportedAppLanguages.DEFAULT_CODE)
        val regionService = FakeDeviceRegionService(DeviceRegion("ES"))

        val bootstrapper = LocationLanguageSuggestionBootstrapper(
            settings = settings,
            languageManager = languageManager,
            deviceRegionService = regionService,
            scope = testScope,
        )

        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        // Simulate: after first call, language key is now in settings
        settings.putString(SupportedAppLanguages.SETTINGS_KEY, languageManager.currentLanguage.value)

        bootstrapper.bootstrapIfFirstLaunch()
        advanceUntilIdle()

        assertEquals(1, regionService.callCount)
        assertEquals("es", languageManager.currentLanguage.value)
    }
}
