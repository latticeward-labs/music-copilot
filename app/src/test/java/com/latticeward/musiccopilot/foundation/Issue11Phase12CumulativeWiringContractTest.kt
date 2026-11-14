package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P12 — cumulative source-wiring regression fixtures.
 *
 * AUTHOR_ONLY: never run or compile in P12. Textual checks complement the
 * P02-P11 functional fake JUnit/instrumented/Compose fixtures; they cannot
 * prove Kotlin compilation, thread correctness, hardware safety, STT success,
 * Media3 acceptance, or the real APK. Those gates belong to P13.
 */
class Issue11Phase12CumulativeWiringContractTest {
    private fun main(path: String): String =
        File("src/main/java/com/latticeward/musiccopilot", path).readText()

    private fun root(path: String): String = File("../", path).readText()

    private val coordinator get() = main("voice/session/DefaultVoiceSessionCoordinator.kt")
    private val app get() = main("app/di/ApplicationBindings.kt")

    @Test fun oneApplicationScopedCoordinatorIsExportedAsTheOnlyVoiceOwner() {
        assertTrue(app.contains("private val voiceOwner: DefaultVoiceSessionCoordinator"))
        assertTrue(app.contains("val voiceSessionCoordinator: VoiceSessionCoordinator = voiceOwner"))
    }

    @Test fun productionVoiceReadsOnlyRealLocalMoonshineAndOfflineTts() {
        val binding = main("app/di/VoiceBindings.kt")
        assertTrue(binding.contains("MoonshineSileroSpeechToTextEngine(context, dispatchers)"))
        assertTrue(binding.contains("AndroidOfflineTextToSpeechEngine(context)"))
        assertFalse(binding.contains("CloudSpeechToTextEngine"))
    }

    @Test fun appGraphFeedsRealLibraryRepositoryNotSongTitleFakeToArtist() {
        val app = this.app
        val data = main("app/di/DataBindings.kt")
        assertTrue(data.contains("RoomLibraryRepository("))
        assertTrue(app.contains("artistLibrary = libraryRepository"))
        assertFalse(app.contains("artistLibrary = Fake"))
    }

    @Test fun sixIntentGrammarIsBoundedAndTransportMapperIsStillSeparate() {
        val phrase = main("voice/intent/PlayArtistPhrasePolicy.kt")
        val mapper = main("voice/intent/RestrictedVoiceIntentMapper.kt")
        assertTrue(phrase.contains("const val APPROVED_PREFIX = \"play songs by\""))
        assertTrue(phrase.contains("MAX_ARTIST_SLOT_CHARS = 80"))
        assertTrue(mapper.contains("PLAY_ARTIST"))
        assertFalse(phrase.contains("SpotifyPlayback"))
    }

    @Test fun artistResolverRunsOnCpuDispatcherNotComposeMain() {
        assertTrue(coordinator.contains(
            "withContext(Dispatchers.Default) { resolver.resolve(recognized.slot) }"))
        assertTrue(coordinator.contains("if (!isCurrent(id, generation, number)) return"))
    }

    @Test fun roomArtistLookupExcludesBlankMetadataAndDoesNotSearchSongTitles() {
        val dao = main("data/room/LibraryDao.kt")
        val sql = dao.substring(dao.indexOf("suspend fun voiceArtistGroupCount"),
            dao.indexOf("suspend fun albumCount"))
        assertTrue(sql.contains("normalizedArtist"))
        assertTrue(sql.contains("artist IS NOT NULL"))
        assertFalse(sql.contains("normalizedTitle"))
    }

    @Test fun localRepositoryUsesRevisionAndPermissionEpochForArtistReads() {
        val repository = main("data/repository/RoomLibraryRepository.kt")
        assertTrue(repository.contains("override suspend fun lookupArtistGroups("))
        assertTrue(repository.contains("override suspend fun artistTrackRefs("))
        assertTrue(repository.contains("accessEpoch.get() == epoch"))
        assertTrue(repository.contains("source.hasAccess()"))
    }

    @Test fun roomIndexProvidesBoundedPagedArtistRefs() {
        val projection = main("data/room/LibraryProjection.kt")
        assertTrue(projection.contains("dao.voiceArtistPage(request.limit, request.offset)"))
        assertTrue(projection.contains("dao.voiceArtistMemberRefs("))
        assertTrue(projection.contains("database.withTransaction"))
    }

    @Test fun artistResolutionFailsClosedForOversizedCandidateIndex() {
        val resolver = main("voice/intent/ArtistResolver.kt")
        assertTrue(resolver.contains("const val MAX_SCANNED_ARTISTS = 5_000"))
        assertTrue(resolver.contains("if (page.totalCount > MAX_SCANNED_ARTISTS)"))
        assertFalse(resolver.contains("return AppResult.Success(ArtistResolution.NotFound(0))"))
    }

    @Test fun exactFuzzyHistoryPolicyIsDeterministicAndVersioned() {
        val scorer = main("voice/intent/ArtistMatchPolicy.kt")
        val resolver = main("voice/intent/ArtistResolver.kt")
        assertTrue(scorer.contains("NORMALIZATION_VERSION"))
        assertTrue(scorer.contains("SCORER_VERSION"))
        assertTrue(scorer.contains("isSeparated"))
        assertTrue(resolver.contains("ArtistRankAccumulator("))
    }

    @Test fun artistAdmissionNeverTurnsMissingMoonshineConfidenceIntoDirect() {
        val admission = main("voice/intent/ArtistDecisionPolicy.kt")
        val confidence = main("voice/intent/ArtistConfidencePolicy.kt")
        assertTrue(admission.contains("ArtistProviderConfidence.Missing"))
        assertTrue(admission.contains("ArtistConfidencePolicy.permitsDirect"))
        assertTrue(confidence.contains("providerConfidence"))
    }

    @Test fun separateArtistBuildConfigDoesNotReplaceFiveTransportConfidence() {
        val gradle = root("app/build.gradle.kts")
        val bindings = main("app/di/VoiceBindings.kt")
        assertTrue(gradle.contains("VOICE_ARTIST_DIRECT_CONFIDENCE_THRESHOLD"))
        assertTrue(gradle.contains("VOICE_DIRECT_CONFIDENCE_THRESHOLD"))
        assertTrue(bindings.contains("BuildConfig.VOICE_ARTIST_DIRECT_CONFIDENCE_THRESHOLD"))
        assertTrue(bindings.contains("BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD"))
    }

    @Test fun localMediaStoreIsRecheckedBeforeArtistServiceDispatch() {
        val usecase = main("voice/session/PlayArtistPlaybackUseCase.kt")
        assertTrue(usecase.contains("library.get(ref)"))
        assertTrue(usecase.contains("sameRevision(claim.expectedLibraryRevision)"))
        assertTrue(usecase.contains("playback.execute(command)"))
        assertTrue(usecase.contains("validAck(outcome.value, before, accepted)"))
    }

    @Test fun onlyServiceOwnedMedia3MayMutateArtistQueue() {
        val usecase = main("voice/session/PlayArtistPlaybackUseCase.kt")
        assertTrue(usecase.contains("PlaybackCommand.ReplaceQueue("))
        assertFalse(usecase.contains("ExoPlayer.Builder"))
        assertFalse(usecase.contains("MediaPlayer("))
        val binding = main("app/di/ApplicationBindings.kt")
        assertTrue(binding.contains("PlaybackBindings.controller("))
    }

    @Test fun explicitOneStepConfirmationKeepsOnePrivateTokenAndNoSecondListen() {
        assertTrue(coordinator.contains("private var pendingArtist: ArtistPending? = null"))
        assertTrue(coordinator.contains("VoiceCandidateToken(UUID.randomUUID().toString())"))
        assertTrue(coordinator.contains("override fun confirm(token: VoiceCandidateToken"))
        assertTrue(coordinator.contains("override fun neither(token: VoiceCandidateToken"))
        assertTrue(coordinator.contains("override fun cancelConfirmation(token: VoiceCandidateToken"))
    }

    @Test fun confirmationClaimIsConsumedBeforeLaunchingServiceJob() {
        val code = coordinator
        val start = code.indexOf("override fun confirm(token: VoiceCandidateToken")
        val end = code.indexOf("override fun neither(token:", start)
        assertTrue(start >= 0 && end > start)
        val section = code.substring(start, end)
        assertTrue(section.indexOf("dispatchClaimed = true") <
            section.indexOf("ownerScope.launch(generation)"))
    }

    @Test fun media3ReceiptMustBeCorrelatedWithTheIssuedCommand() {
        val ctrl = main("playback/controller/PlaybackControllerConnection.kt")
        val receipt = main("playback/command/VoiceCommandReceiptPolicy.kt")
        assertTrue(ctrl.contains("PlaybackWireProtocol.acknowledges(command, result.extras)"))
        assertTrue(receipt.contains("receiptId == command.commandId.value"))
    }

    @Test fun noArtistPlaybackSuccessCanBeAnnouncedBeforeP06Ack() {
        val code = coordinator
        assertTrue(code.contains("if (sent.value == PlayArtistDecision.Executed)"))
        assertTrue(code.contains("if (ack.value == PlayArtistDecision.Executed)"))
        assertTrue(code.contains("if (publishExecuted(id, origin, number, generation))"))
        assertTrue(code.contains("VoiceFeedbackKey.ArtistPlaying"))
    }

    @Test fun fullPlayerAndDrivingBindToOneCoordinatorWithEventTimeTokenChecks() {
        val nav = main("app/navigation/AppNavHost.kt")
        assertTrue(nav.contains("VoiceConfirmationActionPolicy.mayRespond("))
        assertTrue(nav.contains("voiceSessionCoordinator?.confirm(token, index)"))
        assertTrue(nav.contains("voiceSessionCoordinator?.neither(token)"))
        assertTrue(nav.contains("voiceSessionCoordinator?.cancelConfirmation(token)"))
        assertTrue(nav.contains("VoiceOrigin.Driving"))
        assertTrue(nav.contains("VoiceOrigin.Player"))
    }

    @Test fun visibleArtistOptionsComeOnlyFromImmutableCoordinatorState() {
        val panel = main("feature/voice/VoiceSessionPanel.kt")
        val state = main("voice/api/VoiceSessionCoordinator.kt")
        assertTrue(panel.contains("confirming.options.forEach { option ->"))
        assertTrue(panel.contains("responseSubmitted"))
        assertTrue(state.contains("VoiceSessionState.Confirming(redacted)"))
        assertFalse(panel.contains("RoomDatabase"))
    }

    @Test fun lifecyclePermissionFocusAndRouteInvalidationStillOwnVoice() {
        val app = main("app/MusicCopilotApplication.kt")
        val coord = coordinator
        assertTrue(app.contains("PlaybackVoiceInterruptionReason.ROUTE_INVALIDATED"))
        assertTrue(app.contains("PlaybackVoiceInterruptionReason.FOCUS_INVALIDATED"))
        assertTrue(coord.contains("isCurrentAndAuthorizedLocked("))
        assertTrue(coord.contains("stt.cancel(id)"))
        assertTrue(coord.contains("tts.stop(id)"))
    }

    @Test fun noInternetAndTelemetryOnlyAcceptClosedTypedOutcomes() {
        val quality = main("app/di/QualityBindings.kt")
        val gate = main("quality/privacy/AllowlistedTelemetryPrivacyGate.kt")
        val manifest = root("app/src/main/AndroidManifest.xml")
        assertTrue(quality.contains("NoOpTelemetrySink()"))
        assertTrue(gate.contains("voice_intent_outcome"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(coordinator.contains("tryRecordVoiceOutcome(recognition"))
    }

    @Test fun p13WorkflowOnlyAcceptsExplicitRequestMarkerOnFeatureBranch() {
        val workflow = root(".github/workflows/issue-11-p13-one-cumulative-apk.yml")
        assertTrue(workflow.contains("feature/11-play-artist-one-step-ambiguity"))
        assertTrue(workflow.contains(".github/issue11-p13-apk-request.txt"))
        assertFalse(File("../.github/issue11-p13-apk-request.txt").exists())
    }

    @Test fun p13WorkflowIncludesRealMoonshineAndSherpaWithoutTestsOrLint() {
        val workflow = root(".github/workflows/issue-11-p13-one-cumulative-apk.yml")
        assertTrue(workflow.contains("sherpa-onnx-moonshine-tiny-en-quantized-2026-02-27"))
        assertTrue(workflow.contains("sherpa-onnx-2.0.0.aar"))
        assertTrue(workflow.contains("sha256sum -c -"))
        assertTrue(workflow.contains(":app:assembleDebug"))
        assertFalse(workflow.contains(":app:test"))
        assertFalse(workflow.contains(":app:lint"))
        assertFalse(workflow.contains("connectedAndroidTest"))
    }

    @Test fun p13OwnerQaMatrixIsSourceDeclaredAsNotRunUntilOwnerTests() {
        val matrix = root("docs/implementation/ISSUE_11_P12_P13_OWNER_PHONE_QA_CHECKLIST_FA.md")
        assertTrue(matrix.contains("M01"))
        assertTrue(matrix.contains("M48"))
        assertTrue(matrix.contains("T01"))
        assertTrue(matrix.contains("T10"))
        assertTrue(matrix.contains("L01"))
        assertTrue(matrix.contains("L06"))
        assertTrue(matrix.contains("NOT_RUN"))
    }
}
