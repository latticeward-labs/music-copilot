# Issue #11 — P08: Player/Driving one-step Compose confirmation UI and accessibility

**Date:** 2026-11-14  
**Branch:** `feature/11-play-artist-one-step-ambiguity`  
**Phase status:** `SOURCE_IMPLEMENTED_NOT_COMPILED_NOT_RUN`  
**Owner restriction:** P00–P12: **NO app build, APK, Gradle, Kotlin/JVM tests, Android instrumentation, lint, CI/Actions, simulator, emulator, phone tests or PASS claims**. P13 is the sole integrated build and owner QA phase. No PR/merge/Issue close.

## 1. Authority and relevant source inspection

The exact attached owner artifact `MSM2(1)(1).rar` was reopened, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, containing 12 English PDFs. P08 references:
- `Music_Copilot_MVP_v1_EN.pdf` — Voice State Machine (Idle → Listening → Processing → Confirming → Executed/Failed), screens 7/8, Driving dark/high-contrast/one-layer/48dp+ controls and two-similar-artist short confirmation.
- `Music_Copilot_PRD_v1_EN_Clarified.pdf` — Driving command and ambiguity rules, no text input or deep navigation, accessibility contrast/scaled text/TalkBack; avoid incorrect auto-execution.
- `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` §§7–11 — confirmation exactly one short question and one answer chosen from at most two artist candidates; unclear/declined selection returns manual controls, not a second dialog or guessed playback.
- `ADR-0002_Service_Owned_Media3_Playback.pdf` — single service-owned playback, UI does not own Player or queue.
- `ADR-0004_Replaceable_Voice_Provider.pdf` — explicit Push-to-Talk, no wake/listen while waiting for an option, manual fallback.
- Live GitHub controlling plan `docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md` P08: Compose shared `VoiceSessionPanel`, option buttons, Neither/Cancel, token-scoped coordinator, TalkBack/RTL/touch/focus, same origin, NO UI lookup.

The P08 implementation extends the existing Issue #10 WF-08 voice panel and Issue #9 Driving/Full Player screens. No new navigation destination or independent overlay host is created.

## 2. Committed source changes

| Path | P08 implementation |
|---|---|
| [`core/application/voice/VoiceOverlayPresentationPolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoiceOverlayPresentationPolicy.kt) | Maps an actual `VoiceSessionState.Confirming` into `VoiceOverlayStatus.ConfirmationRequired`, not legacy `ConfirmationUnavailable`; state has priority over an old permission-dialog presentation to avoid hiding one active confirmation. No retry or permission continuation |
| [`feature/voice/VoiceConfirmationActionPolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceConfirmationActionPolicy.kt) | Pure, event-time UI predicate: latest coordinator StateFlow is Confirming with same opaque token and expected Player/Driving origin, and the originating screen remains active. No Room/MediaStore or execution authority |
| [`feature/voice/VoiceSessionPanel.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceSessionPanel.kt) | Shared one-question UI using **only** `VoiceSessionState.Confirming.options`. One/two full-width artist selection buttons, Neither and Cancel, unique TalkBack content descriptions; no second STT turn, free-text entry, result list, title/URI query, or guessed artist. Optional callbacks are **nullable**, disabling missing implementations instead of showing fake clickable actions. Presentation-only token-keyed rapid-tap latch; coordinator P07 remains authoritative |
| [`feature/player/FullPlayerScreen.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt) | Passes option, Neither and token-scoped Cancel callbacks into existing PlayerVoiceControls/VoiceSessionPanel. System Back and header Back first dismiss the same current confirmation via token; no screen exit or playback stop |
| [`feature/driving/DrivingModeScreen.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt) | Same shared panel. During Confirming, show options at the **top** of the scrollable body, suppress artwork/long recovery metadata and transport actions underneath, preserve pinned Exit button. System Back is token-scoped; controls remain large with no typing/menus. Legacy non-confirming Driving path is still present |
| [`app/navigation/AppNavHost.kt`](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | Live current `voiceSessionCoordinator.state.value` check at actual tap for each Player or Driving action, plus active origin/route permission/foreground predicates. Calls the same `confirm(token,index)`, `neither(token)` or `cancelConfirmation(token)` on the **single app-scoped coordinator**; no new Media3 command or STT call |
| [`res/values/strings.xml`](../../app/src/main/res/values/strings.xml) | Eight **unique**, local English one/two artist question, explanation, option TalkBack, Neither and Cancel labels. One resource per ID (duplicate definitions eliminated in source review). No raw transcript or media path in strings |
| [`Issue11Phase8ArtistConfirmationUiTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase8ArtistConfirmationUiTest.kt) | **24 synthetic JVM source-authored JUnit cases, zero run**, covering state/origin/token guards, accessibility/source structure, distinct resource keys and no UI media ownership |
| [P08 source QA register](ISSUE_11_P08_SOURCE_EVIDENCE_AND_QA_REGISTER.json) | Exact GitHub source hashes, machine-readable `NOT_RUN` inventory and remaining validation |

All changes are on `feature/11-play-artist-one-step-ambiguity` with `[skip ci]` commit markers. The Issue #10 five transport mapping/dispatcher, P03 Room query, P04 resolver, P05 admission, P06 service ACK, service Media3 and player queue were not edited in P08.

## 3. Exact UX/state contract

`VoiceSessionState.Confirming(sessionId, origin, token, options)` is produced **only** by P07 coordinator. It contains one or two original artist identity display labels with indices 0 or 0/1. The UI never queries songs or artists again, normalizes names itself, fetches new matches or manufactures a fallback choice. Display text is local user music metadata; it remains ephemeral in Compose, redacted in `toString()` and excluded from telemetry.

- **One candidate**: short question such as “Play songs by this artist?”, one full-width artist button, Neither, Cancel.
- **Two candidates**: short question “Which local artist did you mean?”, two separate full-width option buttons with unambiguous TalkBack ordinal labels, Neither, Cancel.
- **Select option 0/1**: the UI sends only the current token and option index; P07 validates original private candidate, generation, permission and revision and consumes exactly one answer. The UI immediately disables all four response buttons after its first callback as a usability guard; the P07 lock is the real idempotency guarantee. The app publishes `Executed` only after P06 Media3 ACK.
- **Neither**: token-scoped terminal refusal, no playback command and no second question. **Cancel/Back**: token-scoped cancel to Idle, no queue mutation; a stale UI tap must not cancel another voice generation. A Driving Exit is separately controlled by the existing root navigation policy and still cannot stop the Player.
- **Expired/stale token, origin dismissed, route switched, permission revoked**: the event-time gate discards the stale callback, while P07/P10 generation and service checks remain authoritative. No silent guessed selection or re-recording.
- **Before P08 wiring** (e.g., legacy preview or fixture): nullable handlers give visibly **disabled** selection controls rather than clickable no-op actions; the ordinary Voice Back fallback can still dismiss the overlay.

The legacy `ConfirmationUnavailable` status remains as a distinct enum for compatibility but a valid live Confirming state does **not** project to it.

## 4. Accessibility and Driving safety

1. **Touch sizes:** Player artist, Neither and Cancel actions use `MusicSize.touchTargetMinDp`. Driving artist options and Neither use the larger `MusicSize.drivingControlMinDp` (theme-defined 64dp), while Cancel uses `MusicSize.drivingExitMinDp`. All choices are full width, never side-by-side. The Driving body scrolls while the safety Exit remains pinned in its own header.
2. **Text scaling and RTL:** long names are rendered in full-width, vertically stacked native Compose Material3 buttons without truncating to an index-only label; text alignment and button semantics use normal Compose layout mirroring, and screens expose their own focus outline.
3. **TalkBack/keyboard/switch:** one polite live-region question heading, each option gets a unique “Select artist [display label], option n of count” description. Neither and Cancel each have distinct meaningful descriptions. Focus is requested on panel **open/close**, not on each Player update, and no modal focus trap or second Voice screen was introduced.
4. **One layer:** still the original FullPlayerScreen/DrivingModeScreen with one shared `VoiceSessionPanel`; normal transport UI is hidden while the Drive voice panel is active. Safe-road reminder remains visible below the scrollable confirmation body.
5. **No overlay authority:** presentation code contains no Media3 Player constructor, no Room artist scan, no playback command and no transcript log. It only delegates a typed token/index to the coordinator.

**Unverified:** actual keyboard focus order, screen-reader behavior, color contrast on specific devices, landscape/short-height scrolling, 150%/200% text scaling and right-to-left rendering remain **P13 manual-device QA**, not a claimed P08 PASS. Product copy/TTS expansion for errors/Not Found is P09.

## 5. Source-authored QA inventory and remaining gates

**Consolidated P08 authored QA:** the branch also contains a complementary 27-case source-only fixture suite, [`Issue11Phase8ArtistConfirmationUiContractTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase8ArtistConfirmationUiContractTest.kt). Together with this audit's 24-case suite, **51 authored JVM methods remain NOT_RUN, 0 compiled and 0 verified PASS**. Both suites are indexed individually in [the consolidated P08 evidence register](ISSUE_11_P08_SOURCE_EVIDENCE_AND_QA_REGISTER.json). Overlapping cases are intentional source coverage, not 51 independent real-device executions.



The new P08 authored fixture file contains **24 @Test methods**, all **NOT_RUN**:
- Positive and negative token/origin/hidden-screen admission, old-token rejection after Processing/Executed, Idle/Listening rejections.
- `Confirming` projection over Idle/Rationale/AwaitingAndroid permission states, one/two options and redacted diagnostics.
- Shared UI source, privacy/no lookup, disabled unbound actions, 48dp+ target contracts, polite TalkBack, focus handling, Driving options above artwork with fixed Exit, Player Back selection cancellation, root current-state gating.
- Resource uniqueness and no new Media3 or recording dependencies.

Source review checks are **not** compile/unit tests. All prior P02–P07 authored fixtures remain NOT_RUN. The Issue #11 acceptance checklist still records **zero verified PASS**; M01–M48, T01–T10 and L01–L06 remain NOT_RUN. P13 only may build one integrated APK and collect the owner's actual phone observations.

**P09 handoff:** brief English one-sentence TTS for Confirming and result/not-found/recovery; no spoken TTS success before actual service ACK; local/offline fallback. **P10/P12:** lifecycle/route and integrated full state/callback verification.

**Independent quality debt:** [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) is OPEN; prior owner-reported #10 microphone lab 24/50=48% versus the 90% target. UI/Compose changes do not repair voice STT accuracy.

**P08 disposition:** SOURCE IMPLEMENTED ON BRANCH, verification pending until P13. No APK, merge or Issue close.
