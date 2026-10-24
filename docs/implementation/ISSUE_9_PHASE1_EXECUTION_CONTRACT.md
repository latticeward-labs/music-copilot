# Issue #9 — P01 GitHub execution, delivery and evidence contract

**Date:** 2026-10-24 · **Phase:** P01 · **Gate type:** GitHub governance and documentation ONLY (no app implementation, testing, APK or CI).  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/9  
**Execution branch:** `feature/9-driving-mode-bluetooth-suggestion` (existing branch reused, not recreated).  
**Reference `main` source SHA:** `674d0cf19a897dad44b59729fb0afa2c635d802d` at P01 entry.  
**P00-committed branch SHA:** `332da9e1788739c0b2a71e0e958b0f5641f676e7`; branch was 6 commits ahead / 0 behind main, with four Issue-9 documentation paths only.  
**Authority archive:** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (12 English PDFs; cross-checked in P00).  
**Normative references:** [PRD](../PRD.md) FR-04 (P0), FR-07 (P1); [MVP](../MVP.md) US-04, P0 Driving, cuttable P1 BT hint; [Architecture](../SOFTWARE_ARCHITECTURE.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [ADR-0001..0008](../adr/README.md); UF-07, UF-11, UF-12; WF-07/WF-10 and GS-04/GS-05; [Issue #17 Screen handoff](../handoff/SCREEN_SPECS.md), [component sizes](../handoff/COMPONENT_SPECS.md), [126-state action matrix](../handoff/STATE_ACTION_MATRIX.md).

## 1. Branch and PR topology (actual, not assumed)

1. **Only one Issue #9 feature branch:** `feature/9-driving-mode-bluetooth-suggestion`. All phases P00–P12 and eventual P13 final build are integrated on this branch. Do not create one branch/APK per phase; do not modify `main` during P01.
2. **Source base:** latest `main` includes #7/#8 merged production and #16/#17 accepted architecture/design. `main` SHA above is an audit snapshot, **not a permanent future merge base**. Before final PR, re-fetch main and compare.
3. **Important policy discrepancy:** [CONTRIBUTING.md](../../CONTRIBUTING.md) says “use `develop` for integration,” but `develop` was **1,065 commits behind `main` (and 0 ahead)** in an authenticated comparison at P01. Current work must not silently rebase onto stale develop, lose merged #7/#8 changes, or claim a repository policy exception already approved. **Proposed target is `main`**, subject to *explicit owner/reviewer acceptance of the project-specific deviation at P13*, not a P01 merge authorization.
4. **Repository governance:** GitHub rulesets returned 403 (“Upgrade to GitHub Pro or make repository public”); branch-protection details returned 403 (“Resource not accessible by integration”). Therefore **enforcement not verified**; no fictitious verified protection/review approvals. The repository is private and remains so. A GitHub Project V2 `Ready` field was **not directly verified**; Issue DoR availability and owner instruction justify P01 preparation only, not a false UI field assertion.
5. **PR is P13/downstream only:** create a single scoped PR for completed combined implementation; use the [Issue #9 PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-9-driving-mode-bluetooth-suggestion.md). No PR, merge, closeout, self-approval or protected-branch rule change occurs in P01. Preferred eventual squash merge only when evidence/owner closeout decisions permit.

## 2. Non-negotiable phase execution and QA restrictions

- **P00–P12:** static source audits, pure design/policies, Kotlin/Compose production changes and test-*source authoring* as phases require. **Zero execution:** no Gradle task, JVM/JUnit, Android instrumented test, emulator, ADB, Lint/Detekt, local app build, GitHub Actions workflow, APK output or installed-app demo in these phases. Code-review/JSON validation by readback is a static activity; it is **not** a runtime test.
- **Documentation commits:** use `[skip ci]`; **do not dispatch** any Actions workflow. Existing workflows may contain repository-wide triggers; commit-message skip is a prevention measure for supported push/pull_request runs, not a guarantee about all possible workflows. Verify independently if CI usage is ever claimed.
- **P13 only:** build the **one cumulative installable debug APK** from the final P00–P12 branch head. Use local `:app:assembleDebug` or one narrowly limited build-only Actions workflow; no `test`, `check`, `lint`, `connectedAndroidTest` or other hidden test task. A compilation failure is **BUILD_FAIL** requiring a source fix and a build-only retry; do not distribute an unbuilt or guessed APK. Publish **one identifiable successful candidate** with exact actual APK SHA-256, source commit, provenance/link.
- **Owner handset QA after P13:** owner personally executes M01–M46 on the **identified APK hash**, parked/stationary for car cases, sends observed results, which are recorded case-by-case. No owner response exists yet. No automatic PASS from authored source or build success.
- **Automated/field QA:** five currently defined technical groups stay `NOT_RUN` until actual separately authorized execution; one Honor/phone/headset does not substitute for Pixel/Samsung/Xiaomi or a vehicle head unit. GS-04 live spoken “Next song” requires #10 Voice; if absent on final APK record `BLOCKED`, not PASS. GS-05 actual in-vehicle route test requires a parked vehicle/head unit and must never be extrapolated from earbuds.
- **Closeout:** issue remains open and AC checkboxes **unchecked** in P01. Only an explicit user acceptance after evidenced P13 handset feedback can request PR/Merge/Close. Any approved MVP exception must identify deferred tests/vehicle/provider/independent review in a follow-up Issue, not retroactively invent execution.

## 3. P0/P1 scope, architecture and privacy contract

### P0 — always required
- One WF-07 Driving Compose surface, **Playing / Paused / No Track**, 4 fixed transports **Previous, Play/Pause, Next, Seek back 10 seconds**, separate large Mic, title/artist/artwork or placeholders, immediate Exit/Back. The current accepted `PlaybackNavigationPolicy` defines 10-second Seek back; do not silently substitute a new value.
- Explicit entry only from approved Library/Home and Full Player surfaces; remember latest non-Driving origin; deterministic Home/Library fallback. Exiting must not stop background playback. UI process recreation re-observes complete `PlaybackGateway.snapshot` and controller readiness; UI never owns media queue or player.
- Minimum interactive area 48dp; #17 Driving targets **64dp transport, 72dp Mic, 56dp Exit**; 20dp insets, high contrast, correct TalkBack order, 200% font reflow. No keyboard, deep navigation, long list, intrusive motion, settings or permission modal while Driving. Copy says attention to the road remains the driver's responsibility; do not promise driving safety.
- All playback commands go through existing service-owned Media3 authority, typed `PlaybackGateway.execute`/ACK; preserve #8 call/focus/route safe Pause, no BT reconnect autoplay, notification Play/media card app-open/AirPods Play fixes.
- Voice button/handoff is **not permission to claim real STT works**. Voice is an overlay with `VoiceOrigin.Driving`, not new NavHost destination; deny/unavailable status gives truthful manual fallback; only #10 authentic provider and consent/permission rules can activate real recognition.

### P1 — independent feature flag / cleanly removable
- Selected **already paired** eligible Bluetooth audio device may *suggest* entering Driving **after user opt-in**. Neither connection nor dismissal automatically starts screen, audio, Voice, foreground Activity or disruptive full-screen alert. Prompt must provide **Enter** / **Not now**, foreground-safe; no spam or stale signal.
- Store selection/preferences locally in DataStore; never collect raw BT device identifier, MAC address or local song metadata in analytics/crash logs. Respect Android API 29–30 and Android 12+ `BLUETOOTH_CONNECT` boundary; permissions requested contextually outside Driving. No scan, location request, automatic pairing or Android Auto.
- If P1 is missing, unstable, permission denied, or too costly, **delete/disable the chooser + observer + suggestion together** before sacrificing P0 Manual Driving, playback or exit. `selectedBluetoothHint` existing DataStore string is not a completed chooser/event monitor.
- **Cutline:** all P1 adapters/providers must depend inward on Driving entry contracts, not turn P0 Driving into a consumer of Bluetooth APIs, nor modify playback Audio Focus/route recovery.

## 4. Planned source ownership and file boundaries (no P01 production edits)

| Phase | Allowed scoped surface | Ownership / prohibition |
|---|---|---|
| P02 | `core/application/driving/*`, pure presentation policy, authored `app/src/test/*` | Project a `PlaybackSnapshot`, no ExoPlayer, no UI queue ownership |
| P03 | `app/navigation/AppNavHost.kt`, `Destination.kt`, nav origin helper; bounded Library/FullPlayer entry callbacks | Exactly one NavHost route; no auto-entry and no new Voice destination |
| P04–P05 | `feature/driving/*`, approved theme/string/resource files | HF-07 Playing/Paused/No Track, status/error, one layer |
| P06 | Driving command adapter/VM → existing `playback/api` and `feature/player/PlayerViewModel` where genuinely needed | Reuse serialized service commands. Any `playback/service`, `media3` or #8 interruption changes require a documented narrow regression reason and review, NOT wholesale rewrites |
| P07 | `feature/driving/*` with existing `voice/api` / app Voice bindings | Honest PTT capability/overlay seam; #10 owns real provider execution |
| P08 | Driving UI semantics/strings + authored UI/a11y tests | Source only; no instrumented test execution |
| P09 | Settings UI, DataStore preference adapters, manifest/permission boundary only if justified | Paired-audio selection and opt-in outside Driving; no scan/location by default |
| P10 | Narrow Android Bluetooth event adapter + Application eligibility policy | Selected+connected match, OS permissions, dedupe, stale revoke; do not own audio routes |
| P11 | Foreground nonblocking suggestion UI / explicit action entry | Never auto-launch Activity or playback; P1-only separable |
| P12 | Cumulative source audit, synthetic test *source*, trace/QA docs | No test execution; no release APK |
| P13 | APK *build-only* handoff, owner evidence ledger, downstream PR/merge decision | APK source/bytes hash authoritative; no fake technical results |

**Forbidden scope without a separate approved change:** backend, account, network music catalog, streaming, always-listening wake phrase, Android Auto/CarPlay, driving-motion detection, telemetry containing raw audio/transcript/media paths/Bluetooth identifiers, duplicated Media3 Player or queue, code unrelated to Issue #9. Preserve unmodified #7/#8 behavior by default. Every exception must be documented as a scoped source diff and linked to an Issue #9 AC.

## 5. Traceability, milestone and status vocabulary

- [Machine-readable evidence index](ISSUE_9_EVIDENCE_INDEX.json) maps **P00–P13, AC01–AC14, M01–M46 and five technical groups**, with FR/UF/WF/GS/ADR/code ownership per AC. Exact case text remains authoritative in [M01–M46 owner checklist](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md).
- **P00:** `PASS_STATIC_SOURCE_AND_READINESS_AUDIT_NO_TESTS`; proof [P00 report](ISSUE_9_PHASE0_READINESS_AUDIT.md). **P01:** `PASS_CONTRACT_ONLY` only when GitHub files read back and Issue #9 updated. **P02–P12:** `PLANNED_NOT_EXECUTED` until individually implemented and audited. **P13:** `PLANNED_NOT_EXECUTED`.
- **Acceptance:** `NOT_VERIFIED` until corresponding production integration and valid evidence, not merely contract/source completion. **Manual test:** `NOT_RUN` before actual owner-device observation. **Technical:** `NOT_RUN` before actual engineering test execution. Later use `PASS`, `FAIL`, `BLOCKED`, `N/A` with grounded case notes, exact APK hash and platform.
- **Governance PASS `!==` product PASS:** P01 proves scope/trace/evidence discipline only. Documentation reads, Git file SHA, and Issue edit are not an Android build, runtime test, feature implementation, human-signature review or field validation.
- **P02 entry gate:** P00 passed static source audit; P01 file paths, freeze authority, owner-only APK agreement, production surface limits, P1 cutline, all 14 ACs/46 M/5 technical QA mapped, no duplicate branch/PR, no new workflows/Actions, issue remains open. P02 defines a pure service-snapshot → Driving-state/action projection with source-authored but unrun test cases.

## 6. Risks, gates and rollback

| Risk | Prevention | Delivery gate |
|---|---|---|
| Diverged integration base / old CONTRIBUTING guidance | Track actual develop/main divergence and demand owner/reviewer project-specific base decision before PR | P13 |
| Unsafe auto-play on BT connect / leaked speaker audio | Never modify #8 route authority accidentally; P1 is suggestion only | P06/P10–P12, owner M25/M26/M39/M41 |
| Controller stale/optimistic UI | Only gateway verified ACK and authoritative connection/snapshot | P02/P06, M09–M15/M18–M21 |
| Voice #10 not actually wired | Separate microphone affordance from real provider outcomes | P07, M16 and M45 |
| Accessibility or Exit at 200% | Approved large touch sizes, Compose reflow and TalkBack focus | P08, M27–M32 |
| P1 delaying/regressing core | Delete P09–P11 Bluetooth-only source/manifest/Settings options, keep P03–P08 P0 intact | P12 and M42 |
| Evidence inflation | Snapshot/reporter/provenance immutable, no inferred PASS and explicit waived debt tracking | P13 owner closeout |

**Final P01 disposition (subject to live read-back):** repository/phase contract defined, evidence mapping and draft PR template committed; **P02 NEXT**. No source-runtime acceptance or code build/testing is granted by this contract.
