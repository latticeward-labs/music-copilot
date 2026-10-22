package com.latticeward.musiccopilot.foundation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.app.navigation.Destination
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapEvidence
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoute
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoutePolicy
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.datastore.DataStorePreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException

/** JVM contracts and source integration checks; not instrumented device QA. */
class Issue5Phase3FirstLaunchTest {
    private fun evidence(
        loaded: Boolean = true,
        migrated: Boolean = true,
        failure: Boolean = false,
        finished: Boolean = false,
        grant: PermissionStatus = PermissionStatus.NotRequested,
        library: LibraryIndexStatus? = LibraryIndexStatus.Unavailable,
        fallback: Boolean = false,
    ) = BootstrapEvidence(loaded, migrated, failure, finished, grant, library, fallback)

    @Test fun coldStartCannotRouteFromUnloadedDefaults() {
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(evidence(loaded = false)))
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(evidence(migrated = false)))
        assertEquals(BootstrapRoute.RetryStartup, BootstrapRoutePolicy.project(evidence(failure = true)))
    }

    @Test fun freshInstallationAndResumedCheckpointRouteToOnboarding() {
        for (grant in listOf(PermissionStatus.Unknown, PermissionStatus.Granted, PermissionStatus.NotRequested)) {
            assertEquals(BootstrapRoute.Onboarding, BootstrapRoutePolicy.project(evidence(finished = false, grant = grant)))
        }
    }

    @Test fun returningUserMustObserveCurrentPermission() {
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.Unknown)))
        assertEquals(BootstrapRoute.PermissionRecovery, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.NotRequested)))
        assertEquals(BootstrapRoute.PermissionRecovery, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.Revoked)))
        assertEquals(BootstrapRoute.HomeLibrary, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.Granted)))
    }

    @Test fun noTrueGrantCanBeInferredFromReadyIndexOrPlayback() {
        assertEquals(BootstrapRoute.PermissionRecovery, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.PermanentlyDenied,
                library = LibraryIndexStatus.Ready)))
        assertEquals(BootstrapRoute.HomeLibrary, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.DeniedRequestable,
                fallback = true)))
    }

    @Test fun emptyOrErrorStillLeadsToTruthfulLibraryDestination() {
        for (status in listOf(LibraryIndexStatus.Empty, LibraryIndexStatus.Scanning,
            LibraryIndexStatus.RecoverableError, LibraryIndexStatus.Unavailable)) {
            assertEquals(BootstrapRoute.HomeLibrary, BootstrapRoutePolicy.project(
                evidence(finished = true, grant = PermissionStatus.Granted, library = status)))
        }
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(
            evidence(finished = true, grant = PermissionStatus.Granted, library = null)))
    }

    @Test fun approvedNavHostStartsWithBootstrapAndHasExactlyFourRoutes() {
        val nav = read("app/navigation/AppNavHost.kt")
        assertEquals(setOf(Destination.Bootstrap, Destination.Onboarding,
            Destination.PermissionRecovery, Destination.Library), Destination.activeInPhase3)
        assertEquals(6, Regex("composable\\(").findAll(nav).count())
        assertTrue(nav.contains("composable(Destination.Search.route)"))
        assertTrue(nav.contains("startDestination = Destination.Bootstrap.route"))
        assertTrue(nav.contains("LaunchedEffect(target)"))
        assertTrue(nav.contains("composable(Destination.Settings.route)"))
        assertFalse(nav.contains("composable(Destination.Driving.route)"))
        assertFalse(nav.contains("composable(Destination.FullPlayer.route)"))
    }

    @Test fun activityObservesRealRepositoryAndNativePermissionOnResume() {
        val activity = read("app/MainActivity.kt")
        assertTrue(activity.contains("preferencesRepository.loadState.collectAsStateWithLifecycle()"))
        assertTrue(activity.contains("storageStartState.collectAsStateWithLifecycle()"))
        assertTrue(activity.contains("checkSelfPermission(permission)"))
        assertTrue(activity.contains("override fun onResume()"))
        assertTrue(activity.contains("BootstrapRoutePolicy.project(evidence)"))
        assertFalse(activity.contains("requestPermissions("))
        assertFalse(activity.contains("MediaSession.Builder("))
    }

    @Test fun firstUseUiEmitsOnlyCheckedInWritesAndNoNativePrompt() {
        val screen = read("feature/onboarding/FirstUseScreen.kt")
        val stateOwner = read("feature/onboarding/FirstUseViewModel.kt")
        assertTrue(screen.contains("controller.dispatch(event, onComplete, onExit)"))
        assertTrue(stateOwner.contains("FirstUseMachine.reduce(machine, event)"))
        assertTrue(stateOwner.contains("PreferenceUpdate.SetOnboardingCheckpoint"))
        assertTrue(stateOwner.contains("PreferenceUpdate.SetOnboardingComplete(true)"))
        assertTrue(stateOwner.contains("FirstUseEvent.Saved(operationId)"))
        assertTrue(screen.contains("BackHandler"))
        assertTrue(screen.contains(".verticalScroll(scrollState)"))
        assertFalse(screen.contains("requestPermissions("))
        assertFalse(screen.contains("SpeechRecognizer"))
        assertFalse(screen.contains("MediaStoreScanner"))
    }

    @Test fun permissionRecoveryIsExplicitAndDoesNotLaunchSettingsOrDialog() {
        val nav = read("app/navigation/AppNavHost.kt")
        assertTrue(nav.contains("manualFallback = true"))
        assertTrue(nav.contains("onRefreshPermission"))
        assertFalse(nav.contains("ActivityResultContracts.RequestPermission"))
        assertFalse(nav.contains("ACTION_APPLICATION_DETAILS_SETTINGS"))
    }

    @Test fun foundationReconcileCannotRunBeforeConfirmedOnboarding() {
        val binding = read("app/di/ApplicationBindings.kt")
        assertTrue(binding.contains("fun scanAfterExplicitMediaGrant("))
        assertTrue(binding.contains("ContextCompat.checkSelfPermission(application, permissionName)"))
        val foreground = binding.substringAfter("fun onForeground()").substringBefore("override fun close()")
        assertFalse(foreground.contains("libraryRepository.reconcile()"))
        assertTrue(foreground.contains("controller.ensureConnected()"))
    }

    @Test fun noMigrationsAreDestructivelyResetWhenStorageCannotOpen() {
        val data = read("app/di/DataBindings.kt")
        assertTrue(data.contains("database.openHelper.readableDatabase"))
        assertFalse(data.contains("fallbackToDestructiveMigration("))
    }

    private fun read(path: String): String =
        File("src/main/java/com/latticeward/musiccopilot", path).readText()
}

private class IntermittentPreferencesDataStore : DataStore<Preferences> {
    var fail = true
    override val data: Flow<Preferences> = flow {
        if (fail) throw IOException("synthetic storage failure")
        emit(emptyPreferences())
    }
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(emptyPreferences())
}

class Issue5Phase3PreferencesLoadingTest {
    @Test fun failedReadDoesNotMasqueradeAsUnfinishedOnboardingAndUserMayRetry() = runBlocking {
        val store = IntermittentPreferencesDataStore()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = DataStorePreferencesRepository(store, TestDispatcherProvider(), scope)
            assertEquals(PreferencesLoadState.Failed, repository.loadState.value)
            assertFalse(repository.preferences.value.onboardingComplete)
            store.fail = false
            val result = repository.reload()
            assertTrue(result is AppResult.Success<*>)
            assertEquals(PreferencesLoadState.Ready, repository.loadState.value)
            assertFalse(repository.preferences.value.onboardingComplete)
        } finally { scope.cancel() }
    }

    @Test fun repeatedFailedReadRemainsRecoverableWithoutClaimingReady() = runBlocking {
        val store = IntermittentPreferencesDataStore()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = DataStorePreferencesRepository(store, TestDispatcherProvider(), scope)
            assertEquals(PreferencesLoadState.Failed, repository.loadState.value)
            assertTrue(repository.reload() is AppResult.Failure)
            assertEquals(PreferencesLoadState.Failed, repository.loadState.value)
        } finally { scope.cancel() }
    }
}
