package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.NotificationPermissionPolicy
import com.latticeward.musiccopilot.core.application.permission.NotificationPurpose
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechConsentPolicy
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
import com.latticeward.musiccopilot.core.application.voice.RemoteSpeechDisclosure
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P10: no surplus permission, no cloud capture; user owns physical-phone QA. */
class Issue5Phase10NotificationCloudConsentTest {
    @Test fun currentMediaSessionOnlyNeverNeedsRuntimePostNotificationsOnSupportedVersions() {
        for (api in 29..36) {
            assertFalse(NotificationPermissionPolicy.currentMvpNeedsRuntimePrompt(api))
            assertFalse(NotificationPermissionPolicy.shouldRequest(
                api, NotificationPurpose.MediaSessionPlayback, true, true, true))
        }
        assertEquals(setOf(NotificationPurpose.MediaSessionPlayback),
            NotificationPermissionPolicy.activePurposes)
    }

    @Test fun futureNonExemptNotificationsNeedApi33ApprovalValueAndExplicitTap() {
        for (api in 29..32) assertFalse(NotificationPermissionPolicy.shouldRequest(
            api, NotificationPurpose.NonExemptFeature, true, true, true))
        for (api in 33..36) {
            assertFalse(NotificationPermissionPolicy.shouldRequest(
                api, NotificationPurpose.NonExemptFeature, false, true, true))
            assertFalse(NotificationPermissionPolicy.shouldRequest(
                api, NotificationPurpose.NonExemptFeature, true, false, true))
            assertFalse(NotificationPermissionPolicy.shouldRequest(
                api, NotificationPurpose.NonExemptFeature, true, true, false))
            assertTrue(NotificationPermissionPolicy.shouldRequest(
                api, NotificationPurpose.NonExemptFeature, true, true, true))
        }
    }

    @Test fun missingRemoteAdapterOrLegalOrDisclosureOrConsentOrDrivingFailClosed() {
        val complete = RemoteSpeechDisclosure(
            "synthetic-processor", "synthetic-destination", "transcription", "zero retention")
        fun decide(adapter:Boolean, legal:Boolean, disclosure:RemoteSpeechDisclosure?,
            consent:Boolean, driving:Boolean=false) = CloudSpeechConsentPolicy.evaluate(
                adapter, legal, disclosure, consent, driving)
        assertEquals(CloudSpeechGateDecision.DisabledNoApprovedAdapter,
            decide(false,true,complete,true))
        assertEquals(CloudSpeechGateDecision.DisabledNoLegalApproval,
            decide(true,false,complete,true))
        assertEquals(CloudSpeechGateDecision.DisabledNoDisclosure,
            decide(true,true,null,true))
        assertEquals(CloudSpeechGateDecision.DisabledNoDisclosure,
            decide(true,true,complete.copy(retention=""),true))
        assertEquals(CloudSpeechGateDecision.DisabledNoConsent,
            decide(true,true,complete,false))
        assertEquals(CloudSpeechGateDecision.DisabledWhileDriving,
            decide(true,true,complete,true,true))
        assertEquals(CloudSpeechGateDecision.Permitted,
            decide(true,true,complete,true,false))
    }

    @Test fun noLegacySavedConsentCanEnableMvpRemoteSpeech() {
        val binding = File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        val provider = File("src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt").readText()
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val activity = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        assertTrue(binding.contains("remoteAdapterApproved = false"))
        assertTrue(binding.contains("legalApproval = false"))
        assertTrue(binding.contains("disclosure = null"))
        assertTrue(binding.contains("SetCloudSpeechConsent(CloudSpeechConsent.Revoked)"))
        assertFalse(provider.contains("RemoteSpeechToTextEngine"))
        assertTrue(provider.contains("AndroidOnDeviceSpeechToTextEngine"))
        assertFalse(manifest.contains("android.permission.POST_NOTIFICATIONS"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(activity.contains("POST_NOTIFICATIONS"))
        assertFalse(activity.contains("NotificationManagerCompat.requestPermission"))
    }

    @Test fun dataStoreAlreadyPersistsOnlyLocalCloudConsentNotAndroidGrant() {
        val store = File("src/main/java/com/latticeward/musiccopilot/data/datastore/PreferencesDataStore.kt").readText()
        assertTrue(store.contains("cloud_speech_consent"))
        assertTrue(store.contains("SetCloudSpeechConsent"))
        assertTrue(store.contains("CloudSpeechConsent.NotAsked"))
        assertTrue(CloudSpeechConsent.entries.contains(CloudSpeechConsent.Revoked))
        assertFalse(store.contains("POST_NOTIFICATIONS"))
    }

    @Test fun settingsHasVisibleDisabledCloudStateAndRevokeOnly() {
        val nav = File("src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(nav.contains("R.string.p10_notification_exempt_status"))
        assertTrue(nav.contains("R.string.p10_cloud_stt_disabled"))
        assertTrue(nav.contains("onClick = onRevokeCloudSpeechConsent"))
        assertTrue(nav.contains("R.string.p10_cloud_consent_revoked"))
        assertTrue(strings.contains("Cloud speech recognition: disabled."))
        assertTrue(strings.contains("No additional notification permission request"))
    }
}
