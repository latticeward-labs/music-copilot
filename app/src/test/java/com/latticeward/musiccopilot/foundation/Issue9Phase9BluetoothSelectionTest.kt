package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDevice
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDeviceSource
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioRead
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.AppPreferences
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.feature.settings.bluetooth.BluetoothSelectionFeedback
import com.latticeward.musiccopilot.feature.settings.bluetooth.BluetoothSelectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * #9 P09 authored source tests only. DO NOT execute JUnit/Gradle/CI at P09.
 * All device IDs are synthetic fixtures and must never be logged or exported.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue9Phase9BluetoothSelectionTest {
    private val first = "02:11:22:33:44:55"
    private val second = "06:AB:CD:EF:12:34"
    private val deviceA = PairedAudioDevice(first, "Synthetic car audio")
    private val deviceB = PairedAudioDevice(second, "Synthetic headphones")
    private val main = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(main) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun token(address: String = first, seed: Byte = 3) =
        requireNotNull(BluetoothSelectionToken.create(address, ByteArray(16) { seed }))

    @Test fun normalizationAcceptsAndUppercasesOnlyConventionalBtAddress() {
        assertEquals(first, BluetoothSelectionToken.normalizeAddress(" 02:11:22:33:44:55 "))
        assertEquals(second, BluetoothSelectionToken.normalizeAddress(second.lowercase()))
    }

    @Test fun malformedOrMissingMacIsNeverStored() {
        assertNull(BluetoothSelectionToken.create("not-a-device"))
        assertNull(BluetoothSelectionToken.create("0A:BB:CC:DD"))
        assertNull(BluetoothSelectionToken.create(""))
        assertNull(BluetoothSelectionToken.create(first, byteArrayOf(1)))
    }

    @Test fun tokenIsOpaqueDoesNotIncludeRawAddressOrDeviceName() {
        val value = token()
        assertTrue(value.startsWith("v1:"))
        assertFalse(value.contains(first))
        assertFalse(value.contains("Synthetic"))
    }

    @Test fun sameSavedSaltMatchesOnlySameAddressAfterRestart() {
        val value = token()
        assertTrue(BluetoothSelectionToken.matches(value, first.lowercase()))
        assertFalse(BluetoothSelectionToken.matches(value, second))
    }

    @Test fun perSelectionSaltsPreventIdentifierReuseAndBothStillMatch() {
        val one = token(seed = 2)
        val two = token(seed = 4)
        assertNotEquals(one, two)
        assertTrue(BluetoothSelectionToken.matches(one, first))
        assertTrue(BluetoothSelectionToken.matches(two, first))
    }

    @Test fun unknownVersionAndLegacyRawIdAreNeverTrusted() {
        assertFalse(BluetoothSelectionToken.matches(first, first))
        assertFalse(BluetoothSelectionToken.matches("v2:a:b", first))
        assertFalse(BluetoothSelectionToken.matches(null, first))
    }

    @Test fun invalidOrTamperedDigestFailsClosed() {
        val old = token()
        assertFalse(BluetoothSelectionToken.matches(old + "!", first))
        assertFalse(BluetoothSelectionToken.matches("v1::", first))
    }

    @Test fun savedPreferenceIsOnlyAHintNotAConnectedRoute() {
        assertTrue(BluetoothSelectionToken.isCurrentFormat(token()))
        assertFalse(BluetoothSelectionToken.isCurrentFormat(first))
        assertFalse(BluetoothSelectionToken.isCurrentFormat(null))
    }

    @Test fun viewModelDoesNotEnumerateOrSaveOnConstruction() = runTest(main) {
        val prefs = FakePrefs()
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA)))
        BluetoothSelectionViewModel(prefs, source)
        advanceUntilIdle()
        assertEquals(0, source.reads)
        assertEquals(0, prefs.updates)
    }

    @Test fun explicitRefreshStagesOnlyNoWriteUntilSave() = runTest(main) {
        val prefs = FakePrefs()
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA, deviceB)))
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.refresh()
        advanceUntilIdle()
        vm.stage(first)
        assertEquals(first, vm.ui.value.draftAddress)
        assertEquals(0, prefs.updates)
    }

    @Test fun explicitSaveRechecksFreshBondingAndStoresOnlyToken() = runTest(main) {
        val prefs = FakePrefs()
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA)))
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.refresh(); advanceUntilIdle()
        vm.stage(first); vm.save(); advanceUntilIdle()
        assertEquals(2, source.reads)
        val value = prefs.preferences.value.selectedBluetoothHint
        assertTrue(BluetoothSelectionToken.matches(value, first))
        assertEquals(BluetoothSelectionFeedback.Saved, vm.ui.value.feedback)
        assertEquals(1, prefs.updates)
    }

    @Test fun saveRejectsUnpairedDeviceAndKeepsPreviousPreference() = runTest(main) {
        val previous = token(second)
        val prefs = FakePrefs(initial = previous)
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA)))
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.refresh(); advanceUntilIdle(); vm.stage(first)
        source.response = PairedAudioRead.Available(emptyList())
        vm.save(); advanceUntilIdle()
        assertEquals(previous, prefs.preferences.value.selectedBluetoothHint)
        assertEquals(0, prefs.updates)
        assertEquals(BluetoothSelectionFeedback.DeviceChanged, vm.ui.value.feedback)
    }

    @Test fun revokedPermissionDuringSaveNeverCommits() = runTest(main) {
        val prefs = FakePrefs(initial = token(second))
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA)))
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.refresh(); advanceUntilIdle(); vm.stage(first)
        source.response = PairedAudioRead.PermissionRequired
        vm.save(); advanceUntilIdle()
        assertEquals(0, prefs.updates)
        assertTrue(BluetoothSelectionToken.matches(prefs.preferences.value.selectedBluetoothHint, second))
        assertEquals(BluetoothSelectionFeedback.PermissionLost, vm.ui.value.feedback)
    }

    @Test fun failedDataStoreSaveLeavesOldSelectionAndDisplaysError() = runTest(main) {
        val prior = token(second)
        val prefs = FakePrefs(initial = prior, fail = true)
        val source = FakeSource(PairedAudioRead.Available(listOf(deviceA)))
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.refresh(); advanceUntilIdle(); vm.stage(first); vm.save(); advanceUntilIdle()
        assertEquals(prior, prefs.preferences.value.selectedBluetoothHint)
        assertEquals(BluetoothSelectionFeedback.StorageUnavailable, vm.ui.value.feedback)
    }

    @Test fun removeSucceedsEvenWithBluetoothPermissionRevoked() = runTest(main) {
        val prefs = FakePrefs(initial = token())
        val source = FakeSource(PairedAudioRead.PermissionRequired)
        val vm = BluetoothSelectionViewModel(prefs, source)
        vm.remove(); advanceUntilIdle()
        assertNull(prefs.preferences.value.selectedBluetoothHint)
        assertEquals(0, source.reads)
        assertEquals(BluetoothSelectionFeedback.Removed, vm.ui.value.feedback)
    }

    @Test fun failedRemovalRetainsStoredSelectedDevice() = runTest(main) {
        val prior = token()
        val prefs = FakePrefs(initial = prior, fail = true)
        val vm = BluetoothSelectionViewModel(prefs, FakeSource(PairedAudioRead.Disabled))
        vm.remove(); advanceUntilIdle()
        assertEquals(prior, prefs.preferences.value.selectedBluetoothHint)
        assertEquals(BluetoothSelectionFeedback.StorageUnavailable, vm.ui.value.feedback)
    }

    @Test fun cancelDraftAndSettingsExitNeverSaveASelection() = runTest(main) {
        val prefs = FakePrefs()
        val vm = BluetoothSelectionViewModel(prefs,
            FakeSource(PairedAudioRead.Available(listOf(deviceA))))
        vm.refresh(); advanceUntilIdle(); vm.stage(first)
        vm.cancelDraft(); vm.dismiss()
        assertEquals(0, prefs.updates)
        assertNull(vm.ui.value.draftAddress)
    }

    @Test fun permissionsAndBluetoothOffReadCannotInventAudioDevices() = runTest(main) {
        val source = FakeSource(PairedAudioRead.PermissionRequired)
        val vm = BluetoothSelectionViewModel(FakePrefs(), source)
        vm.refresh(); advanceUntilIdle()
        assertTrue(vm.ui.value.devices.isEmpty())
        assertEquals(BluetoothSelectionFeedback.PermissionLost, vm.ui.value.feedback)
        source.response = PairedAudioRead.Disabled
        vm.refresh(); advanceUntilIdle()
        assertTrue(vm.ui.value.devices.isEmpty())
        assertEquals(BluetoothSelectionFeedback.BluetoothDisabled, vm.ui.value.feedback)
    }

    private class FakeSource(var response: PairedAudioRead) : PairedAudioDeviceSource {
        var reads = 0
        override suspend fun readPairedAudio(): PairedAudioRead {
            reads++
            return response
        }
    }

    private class FakePrefs(
        initial: String? = null,
        var fail: Boolean = false,
    ) : PreferencesRepository {
        private val mutable = MutableStateFlow(
            AppPreferences(false, initial, CloudSpeechConsent.NotAsked)
        )
        override val preferences: StateFlow<AppPreferences> = mutable
        override val loadState: StateFlow<PreferencesLoadState> =
            MutableStateFlow(PreferencesLoadState.Ready)
        var updates = 0
        override suspend fun reload(): AppResult<AppPreferences> =
            AppResult.Success(mutable.value)
        override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> {
            updates++
            if (fail) return AppResult.Failure(AppError.Storage(
                AppErrorCodes.STORAGE_WRITE_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
            if (update is PreferenceUpdate.SetBluetoothHint)
                mutable.value = mutable.value.copy(selectedBluetoothHint = update.value)
            return AppResult.Success(mutable.value)
        }
    }
}
