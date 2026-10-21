# Issue #5 — Phase 4: Two approved Onboarding slides (WF-02)

**Date:** 2026-10-16. **Authority:** user's explicit P00–P14 phase names; same 12-PDF MSM2 archive (SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`) and accepted repository PRD/MVP, UF-01, WF-02, HF-02 and Issue #16/#17 design contracts. This P04 is **TWO ONBOARDING PAGES**, not a native permission adapter. Historical phase names from the initial plan are superseded by the user's explicit 15-phase order; prior commits/reports are not rewritten.

## Implemented changes

- `feature/onboarding/FirstUseScreen.kt`: true source-aligned Compose layout for **exactly two** approved educational pages, both using a single Onboarding route/State Machine.
- Slide 1 (WF-02/HF-02): `Back`, `Welcome`, `LOCAL MUSIC`, accessible local illustration/placeholder, `Play local music. No account needed.`, `Your library stays on this device.`, 2-dot pager, `Continue`. No automatic grant request.
- Slide 2: `Back`, `Welcome`, `Skip`, `OPTIONAL VOICE`, `6 supported commands`, `Voice starts only after your tap.`, `Skip keeps all manual controls.`, 2-dot pager, `Practice Voice`, `Finish without Voice`. Concise road-attention safety language from UF-01 is retained.
- `Practice Voice` is **visible but disabled** and explicitly labelled as deferred to **P09**; no inert enabled tap and no accidental microphone dialog. Future native action must use the P09 explicit practice/permission gate.
- `Continue` checkpoints slide 2 through the real DataStore repository **after ACK**, `Back` persists slide 1, `Skip` and `Finish without Voice` persist completion only after ACK, failed writes show Retry/Back, pending writes block repeated taps. No new Android permission requests or Voice/Playback authority.
- MaterialTheme reads existing Night Signal color/text tokens without forbidden `feature → app.theme` imports. Source-owned `Welcome/Back` app bar and CMP-22-style dot progress; written TalkBack `Page 1 of 2`/`Page 2 of 2` content descriptions and semantic heading. App-owned click targets >=48dp. Scroll and slide-specific scroll reset to avoid hiding second-slide heading after first-slide scrolling; safe insets and wrap content aim to support larger fonts.
- Static illustration elements are synthetic local-device placeholders, **not imported static reference PNGs** or real library/track content. No telemetry/audio/transcripts/track metadata/network/account fields introduced.

## Evidence contract

| Gate | Requirement | Result |
|---|---|---|
| Visual hierarchy and exact product copy | Compare implementation resources with approved HF-02 frame text/ordering | **PASS** — exact approved resource copy + 8 JVM cases in [#38872429334](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429334) |
| State machine + persistence | Unit/JVM assertions on ACK, Back, Skip/Finish, stale result, no automatic permission | **PASS** — [real Android/Kotlin CI #38872429334](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429334) and post-documentation [#38872782861](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872782861) SUCCESS |
| Accessibility semantics | semantic headings, pager content labels, 48dp actions, disabled practice, scroll at 200% | **PASS on tested API29 emulator scope** — [#38873215678](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678), including 200% two-slide reflow screenshots; broader P12 TalkBack/other OS QA still due |
| Real Android UI | Emulator API29 fresh/Skip/Back and force-stop, screenshot and OS grants; optional font_scale=2.0 check | **PASS** — [Android 10 run #38873215678](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678), real UI hierarchy and taps, screenshot + APK artifact #12034777234 |
| Privacy / architecture / regression | fail-closed source guards, P03 regression, Lint/Detekt, JVM, APK | **PASS** — real JVM/Gradle and privacy/architecture/design checks in [#38872429334](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429334) |
| Wider Android/TalkBack | independent device OS/API/TalkBack and visual sign-off | deferred P12–P13 (NOT claimed) |

## Scope exclusions

P05 handles **media permission request**, P06 handles native Granted/Denied/Permanently Denied, P07 Settings/revoked, P08 actual Library Readiness, P09 six-command practice and microphone, P10 Notification/Cloud STT, P11 lifecycle/privacy, P12 Accessibility sign-off, P13 physical QA and Golden Scenario, P14 final 13-AC audit/PR/merge/close. Source design review is AI-assisted unless documented otherwise. Issue #5 13 original acceptance checkboxes stay unchecked pending integrated review.

## CI/QA ledger

**P04 PASS / COMPLETE (within WF-02 two-page scope).** [JVM/Gradle/Android CI #38872429334](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429334) SUCCESS on UI implementation `d996b7671346cc337deb7e5e62998c898af0ffcd`: 8 new JUnit tests, existing regression tests, source governance, architecture/privacy/token checks, Spotless, Detekt, Lint, built APK. [P04 post-doc CI #38872782861](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872782861) SUCCESS. [Real Android 10 Emulator #38873215678](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678) SUCCESS on final UI/workflow head `eb57eac9c6830bd854726963c33f0fcdb1af82e5`; screenshot/API29 evidence is in [artifact #12034777234](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678/artifacts/12034777234). This last commit differs from the JVM-tested code only in workflow/script and documentation, **not** in app bytecode/resources.

### Preserved failure and recovery evidence

The [initial P04 emulator run #38872429339](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429339) failed due to reading the `enabled` value of a Text child within a disabled Compose button; this is not authoritative for its clickable parent. Replaced brittle tree-attribute assertion with a real tap attempt followed by strict same-screen/no microphone-grant assertions. After a successful follow-up [run #38873155172](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873155172), the latest run #38873215678 also captured `slide_1.png`, `slide_2.png`, `reflow_200pct_slide_1.png` and `reflow_200pct_slide_2.png`, checked persisted Skip/first-use completion across relaunch, and verified no automatic media/mic grants. No failures are relabeled PASS.

**Out of scope, still open:** native media permission dialog and rationale (P05), native outcomes (P06), Android Settings/revocation (P07), library readiness (P08), six-command practice and mic (P09), notifications/cloud STT (P10), comprehensive lifecycle/privacy (P11), independent TalkBack/accessibility QA (P12), physical device Golden Scenario (P13), final 13/13 Issue #5 acceptance/PR/merge (P14).
