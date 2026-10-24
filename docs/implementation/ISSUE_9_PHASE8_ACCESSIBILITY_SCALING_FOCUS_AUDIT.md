# Issue #9 — P08 Driving accessibility, responsive text and focus source audit

**Date:** 2026-10-24 · **Gate:** `PASS_SCOPED_A11Y_SOURCE_AND_11_COMPOSE_TESTS_AUTHORED_NOT_RUN` · **Next:** P09 WF-10 optional paired Bluetooth device preference.

## Source authority, unchanged MVP

- User-uploaded `MSM2(1)(1).rar` SHA256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, same source as P00–P07; clarified PRD / Android MVP, ADR-0002/0004/0007/0008, UF-07 / UF-11, GS-04 / GS-05.
- [Playing HF-07 frame](../high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.md), [Paused HF-07 frame](../high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PAUSED.md), [No Track HF-07 frame](../high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_NO_TRACK.md).
- [CC-05 a11y contract](../high-fidelity/phase-7/app-visible/CC-05.md), [CC-06 a11y contract](../high-fidelity/phase-7/app-visible/CC-06.md), [CC-07 a11y contract](../high-fidelity/phase-7/app-visible/CC-07.md); [P03 origin navigation audit](ISSUE_9_PHASE3_DRIVING_NAVIGATION_AUDIT.md), [P07 Voice owner](ISSUE_9_PHASE7_VOICE_MIC_HANDOFF_AUDIT.md).
- The user mandated **NO build or executed tests** during P00–P12. This audit is static source inspection only, not a TalkBack/device/Compose runtime verification.

## Exact P08 source changes

| Path | Source-level behavior |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt` | Refactor root to fixed top header (DRIVING MODE/Exit) and bounded scrollable content. Exit cannot be scrolled out of view by 200% font or long errors. All main controls are still separate buttons, no ancestor action click or nested navigation. Playback status before title+artist, decorative artwork excluded from TalkBack semantics. Real source-derived stateDescription, Polite liveRegion for semantic status/recovery, localizable action labels and sanitized disabled reasons. Keyboard/Switch focus uses Night Signal focus ring; Mic/Voice inline heading receives focus once when overlay opens and focus returns to Mic after closing when enabled. Responsive artwork priority 160dp → 96dp at 150% → 72dp at 200%; four controls reflow 4 across or 2x2; NoTrack retains immediate Exit, no fake Play Last. Action minimums: transport 64dp, Mic 72dp, Exit 56dp, Voice Cancel/Continue/Retry 56dp. |
| `app/src/main/res/values/strings.xml` | 17 new explicit localizable `i9_a11y_*` TalkBack labels and disabled reasons; Play vs Pause is dynamic and semantic, seek says `Seek backward ten seconds` rather than just `-10s`. Safe wording without raw URI, song title, route identifier, voice token, score or exact position. |
| `app/src/androidTest/java/com/latticeward/musiccopilot/Issue9Phase8DrivingAccessibilityUiTest.kt` (**NEW**) | 11 authored Compose/instrumentation tests, **zero executed**. Exercises Playing, Paused, NoTrack, 64/72/56dp targets, 150%/200% font, fixed Exit with active Voice, disabled action explanation, inline Voice rationale/Cancel, true Listening vs no false success, pending non-color-only text, and Mic → Voice → Mic focus restoration. Fixtures contain only empty synthetic in-memory library and opaque test Voice IDs. |

## Precise accessibility reading order

1. Heading `DRIVING MODE` (heading semantics).
2. Always-visible `Exit` actionable button, spoken `Exit Driving Mode immediately`; fixed outside the scrolling body, target >=56dp. When Voice is active, Exit cancels Driving-owned voice via root before returning to origin.
3. Truthful textual playback state PLAYING / PAUSED / NO TRACK (stateDescription, polite live region on actual authoritative state, no per-position-tick announcement).
4. If current track valid, a *single* related title+artist semantics group; if No Track, no invented title/artist/artwork, instead explicit browse explanation. ARTWORK placeholder decorative and not a competing TalkBack focus node.
5. Previous, Play/Pause, Next, Seek Back 10 seconds: four **separate** 64dp+ button targets and full spoken action labels. `Button.enabled=false` stays announced as disabled; `stateDescription` tells the sanitized controller/pending/unavailable reason.
6. One distinct Mic/Voice 72dp+ action or live permission/Voice overlay. Unsupported local STT displays textual fallback rather than a fake microphone capture. The Voice overlay heading takes focus when opened; inline Cancel/Retry/Continue remain >=56dp, and after closing the focus requester returns to Mic only if truly available.
7. Single-layer road attention note and short text. Android permission UI belongs to the OS, not a fake Compose system screen.

## Responsive and ergonomic source constraints

- At normal scale 4 controls remain one row if available width >=340dp. At system fontScale >=1.5 or narrow width, the four controls occupy two rows without reducing their >=64dp target; label style remains sp-based and action semantics names are full even where visible copy is short.
- The header is outside the body's `verticalScroll`, with safe drawing insets and 20dp side margins. No fixed-height content panel clips enlarged status or error messages. Artwork shrinks before controls. Reading order does not depend on colors or visual icons.
- `MusicColors` are the approved Night Signal dark theme with light foreground and bright status/focus tokens. P08 does not override other screen colors or design tokens. **Actual contrast/200% OEM screenshots/TalkBack speech/focus order are NOT verified**, and must be measured on phone.
- No new keyboard, queue, song lists, Android Auto, location, call details, arbitrary network audio, Voice recorder, service, route changes, or DataStore/Room additions.

## Authored test inventory and P13 owner checklist mapping

| Scope | Authored evidence | P13 owner proof (all NOT_RUN) |
|---|---|---|
| 64/72/56dp and fixed Exit | UI tests 1, 5, 6, 10 | M27, M29 |
| TalkBack Playing/Paused/No Track action semantics | UI tests 1, 2, 3, 4 | M28 |
| Font scaling 100%, 150%, 200% / viewport reflow | UI tests 5, 6 | M29 |
| Voice overlay/recovery state/cancel and focus return | UI tests 7, 8, 11 | M16, M28–M30; GS-04 real STT requires Issue #10 |
| Non-color pending/error text, road notice, one layer | UI test 9 and semantic source inspection | M30, M31, M32 |

## Phase gate and evidence truth

- [x] Uploaded RAR SHA re-checked; official HF/WF states and CC-05/06/07 accessibility specs matched.
- [x] Only existing WF-07/Voice route modified; no new NavHost destination, Player, Voice coordinator, Android manifest change, BT action, permission grant, backend or automatic capture.
- [x] Fixed Exit, independent action semantics, status/disabled descriptions, decorative artwork omission, focus restoration, responsive layout and approved tap target sizes in GitHub source.
- [x] 11 new `androidTest` Compose test methods authored, **0 executed**.
- [ ] Compile, Gradle/JUnit/UI tests, instrumentation, Lint, CI/Actions, emulator, device TalkBack/voice and 200% screenshots: **NOT_RUN**.
- [ ] Product acceptance: **14 NOT_VERIFIED**. Phone **46 NOT_RUN**, technical groups **5 NOT_RUN**, APK **NOT_BUILT**. No PR, merge or Issue closure.
- [ ] P09–P11 optional P1 Bluetooth preference/suggestion and P13 one cumulative APK/owner testing remain pending. Actual Voice artist confirmation/TTS and GS-04 end-to-end remain external Issues #10/#11.

**Disposition: P08 source-only gate complete; P09 next. NOT a runtime accessibility PASS.**
