# Issue #11 — P08 Compose one-step artist confirmation in Player & Driving

**Date:** 2026-11-14  
**Repository:** \`latticeward-labs/music-copilot\`  
**Branch:** \`feature/11-play-artist-one-step-ambiguity\`  
**Status:** \`SOURCE_IMPLEMENTED_UNCOMPILED_UNTESTED\`. All tests **NOT_RUN**, no Gradle, lint, Kotlin compiler, UI instrumentation, Compose Preview runtime, GitHub Actions, emulator, device run or APK. No merge/close. **Never treat source-authored assertions as PASS.**

## 1. Source authority

The exact attached \`MSM2(1)(1).rar\` (SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`; 12 English PDF specifications) and existing GitHub branch were inspected.

- \`Music_Copilot_PRD_v1_EN_Clarified.pdf\` §04 Product Experience: **ONE GLANCE, ONE DECISION**, four large Driving controls, only one short ambiguity question, error does not interrupt current playback, manual control reachable in one touch. Its ambiguity example asks about two similar artists.
- \`Music_Copilot_MVP_v1_EN.pdf\` user story **US-06**: low confidence/multiple matches ⇒ one-step confirmation; Driving shows essential controls and an immediate Exit; six intents include **PLAY_ARTIST**.
- \`ADR-0001_Native_Android_Platform_Revised.pdf\` §8.2: Jetpack Compose, accessible semantics, scalable text and adequate touch targets; UI must not own playback/voice providers.
- \`ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf\` §§5–9: deterministic single confirmation of up to two artist candidates, no multi-turn request, unknown/unsupported safe fallback.
- GitHub \`docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md\`, P08: shared \`VoiceSessionPanel\` on Player and Driving; \`onConfirm(token,index)\` linked to Coordinator; keyboard, TalkBack, RTL, original-route preservation, **no new library lookup or queue**.

## 2. Source changes

| Path | Scope |
|---|---|
| [\`core/application/voice/VoiceOverlayPresentationPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoiceOverlayPresentationPolicy.kt) | New \`ConfirmationRequired\` projection. Real \`VoiceSessionState.Confirming\` overrides stale permission-rationale/pending presentation; one/two options, no retry/second microphone turn; CancelCapture exit. Previous Issue #10 projection branches preserved. |
| [\`feature/voice/VoiceSessionPanel.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceSessionPanel.kt) | Reuses one existing inline Compose component for both origins. Renders question, 1–2 full-width vertical artist buttons from the **current Coordinator-projected option list**, Neither and token-scoped Cancel; accessible labels/TalkBack/heading/polite live region/focus outline, response-in-progress guard; no Room query, ExoPlayer or new Voice owner. Button min height: Player **48dp**, Driving confirmation **64dp** via existing \`MusicSize\` design tokens. |
| [\`feature/voice/VoiceConfirmationActionPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceConfirmationActionPolicy.kt) | Pure event-time guard: latest \`VoiceSessionState.Confirming\`, token identity, recorded origin (Player or Driving), origin screen currently active. Not an alternative Coordinator authority. |
| [\`feature/player/FullPlayerScreen.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt) | Routes Select/Neither/Cancel through its existing inline \`PlayerVoiceControls\` to shared panel and callback. Android Back while confirming cancels the one pending choice, **not the Player or Media3 service**; existing open/close focus handling remains. |
| [\`feature/driving/DrivingModeScreen.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt) | Same shared panel and callbacks. While Confirming, places artist choices **near top** of the scrollable Driving body before artwork/status details, while the explicit **Driving Exit remains pinned**; nonessential metadata/transport controls are hidden in this state; Android Back dismisses pending choice, not Driving route. Full-width vertical options allow 150%–200% fonts and RTL without clipping from a two-column row. |
| [\`app/navigation/AppNavHost.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | Both origin routes wire \`onVoiceConfirm(token,index)\`, \`onVoiceNeither(token)\`, \`onVoiceCancelConfirmation(token)\` to the **single app-scoped VoiceSessionCoordinator**. At callback time read \`coordinator.state.value\`, token, recorded origin, active destination/permission/route; dismiss/replaced origin cannot act. No navigation destination, local database query, Media3 call or STT request in UI action callbacks. |
| [\`app/src/main/res/values/strings.xml\`](../../app/src/main/res/values/strings.xml) | Eight English, localization-ready question/action/TalkBack strings, including artist label and one-based index for screen-reader context. Only UI displays private local label; no transcript/log/telemetry persistence. |
| [\`Issue11Phase8ArtistConfirmationUiContractTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase8ArtistConfirmationUiContractTest.kt) | **27 new JUnit source methods written, zero executed**. Pure projection/event-guard cases plus source-boundary/accessibility/route/strings checks. Real TalkBack and 200%-font behavior still unverified. |

No change to the existing five #10 phrase dictionary, active transport dispatch, library Room/MediaStore SQL, STT provider, \`ArtistResolver\`, \`ArtistDecisionPolicy\`, P06 Media3 ACK use case, \`MusicPlaybackService\`, ExoPlayer, queue owner, privacy/telemetry sink or network.

## 3. Source contract and UI response matrix

| Origin and state | UI / action | Authority / result |
|---|---|---|
| Player or Driving + \`Listening\`/\`Processing\` | Existing generic Voice status and Cancel; no option buttons | Existing #10 behavior unchanged |
| Player + one valid artist option | Inline one-question prompt with one full-width selection, Neither and Cancel | \`VoiceSessionState.Confirming.options\` only; no extra library lookup |
| Player + two options | Two ordered full-width artist choices, Neither, Cancel | Zero-based indices passed unchanged with current token |
| Driving + one/two options | Same panel with larger 64dp minimum choices, prominent early in scroll body; permanent Driving Exit stays visible | One-glance bounded decision; never routes to Search or opens a second Voice screen |
| Valid matching token and current origin, Select(index) | Event-time UI guard calls \`voiceSessionCoordinator.confirm(token,index)\` once | **P07** consumes current snapshot/token atomically; \`Processing\` until **P06 service ACK**; UI does **not** claim Executed on click |
| Neither | \`voiceSessionCoordinator.neither(token)\` | P07 terminal negative result, \`canRetry=false\`, no second listening and no queue action |
| Cancel or Android Back | \`voiceSessionCoordinator.cancelConfirmation(token)\` | P07 resets to Idle and same origin controls; no new navigation or queue mutation |
| Wrong/expired token, hidden origin, stale route, expired generation | UI rejects callback; P07 also validates session/revision/claim | No service command; stale click cannot dismiss a newer valid token |
| Permission lost, activity stops, route change | Existing application lifecycle cancels Voice; UI guard refuses no-longer-owned token | Existing service may continue playback independently |
| Accepted selection but lost/invalid Media3 ACK | P07 shows Processing/Failed, not optimistic Executed | P09 improves wording; P13 verifies physical service behavior |

**Important implementation boundary:** UI does not own the token, only passes the token already exposed by \`VoiceSessionState.Confirming\`. The coordinator alone verifies selection/generation/revision, consumes one answer and sends at most one service command. The source-level UI rapid-tap guard is auxiliary, not a substitute for P07/P06 idempotency. Dismissing a UI **after** an accepted Media3 command cannot retroactively unsend it; P10/P13 must validate interruption races.

## 4. Accessibility, distraction and privacy constraints

- Use Android Material3 Button/OutlinedButton semantics, \`contentDescription\` including artist name and explicit option ordering, keyboard focus outline and **polite** status announcement. Android's focus and TalkBack control remains native; no forced focus theft upon every Compose recomposition.
- Choices flow in a vertical column, full width with wrapping text, keeping native layout direction (RTL support; no \`LayoutDirection.Ltr\` overrides). Driving Exit remains in the fixed header and the body scrolls for narrow screens or large type.
- Accessible target sizing uses existing \`MusicSize.touchTargetMinDp=48dp\`, \`MusicSize.drivingControlMinDp=64dp\`; nonconfirmation voice exit retains its approved screen-specific minimum.
- Artist metadata may be displayed **only as choice labels** and read to the screen reader. It is not logged, persisted in DataStore, passed to analytics, resolved by another ViewModel or transcribed again. VoiceSessionState diagnostics remain redacted from P07.
- No wrong action or fake success from a single tap. The actual state remains Processing until P06 verifies Media3 service ACK.

## 5. QA evidence and limitations

[Machine-readable P08 source-evidence and QA register](ISSUE_11_P08_SOURCE_EVIDENCE_AND_QA_REGISTER.json) enumerates **27 authored synthetic JUnit cases** with \`NOT_RUN\` status. There were **zero** executed JVM/Compose/Android tests, static checker runs, Gradle builds, APKs or phone trials.

P13 owner-run QA must explicitly cover: one option/two options/Neither/Cancel, one-step answer and duplicate taps, timeout token expiry, Back in each origin, Driving fixed Exit, TalkBack correct artist/index reading, button focus traversal, minimum 48/64dp targets, RTL language/layout, 150% and 200% text, long artist names, permission revocation, stale token after route switch, confirmation displayed while previous track continues or is paused, and ACK-dependent executed state.

**P09 handoff:** Short artist-specific spoken feedback (including NotFound and service error), absent TTS behavior and localized fallback copy. The P08 English visual strings are not claimed to replace P09's short voice feedback design. **P10:** lifecycle/phone/BT race checks; **P11:** telemetry allowlist; **P12:** aggregate integration fixtures; **P13 only:** cumulative build/APK and owner phone QA.

**Outstanding Issue #80:** voice STT quality debt remains open. P08 cannot improve actual recognition accuracy; prior owner-reported Issue #10 lab was 24/50 (48%) against 90% target.

**P08 exit:** source committed on existing branch, owner test/merge/acceptance still pending.
