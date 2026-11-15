# Issue #12 — P02 Measurement Dictionary v1.0 & Frozen Beta Thresholds

**Date:** 2026-11-15
**Status:** P02 SOURCE/DOCUMENTATION COMPLETE; PRODUCT/QA/PRIVACY INTERPRETATION APPROVALS PENDING; NOT BUILT/TESTED
**Branch:** feature/12-privacy-safe-telemetry-measurement
**Archive:** exact owner-uploaded MSM2(1)(1).rar, SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8 (RAR5, 12 English PDFs extracted; relevant PDF pages visually checked).
**Primary authority:** Music_Copilot_PRD_v1_EN_Clarified.pdf (PDF pp.9–10); Music_Copilot_MVP_v1_EN.pdf (PDF pp.2,7–11).
**Secondary authority:** Music_Copilot_System_Architecture_MVP_v1_EN.pdf pp.16–17; ADR-0003/0004/0005/0006; repository [PRD](../PRD.md), [MVP](../MVP.md), [Analytics and Privacy](../ANALYTICS_AND_PRIVACY.md), [P01 audit](ISSUE_12_SOURCE_AND_GAP_AUDIT.md), and [Issue #12](https://github.com/latticeward-labs/music-copilot/issues/12).

**Evidence labels:** LOCKED_PRODUCT means explicitly prescribed by approved English PRD/MVP; ENGINEERING_DEFINITION means implementation interpretation not stated verbatim in those sources; REQUIRES_REVIEW means no actual Product/QA/Privacy approval is asserted. No new event fields, analytics provider or product scope are authorized by this dictionary.

## 1. Locked metrics and targets

| ID | Metric | Exact locked target | Source |
|---|---|---|---|
| NS01 | Successful Driving session with at least one Voice command (North Star) | >= 50% of weekly users | PRD p.10 |
| Q01 | Direct-command success | >= 85% | PRD p.10, MVP pp.2,9 |
| F01 | Manual correction required after a command | < 15% | PRD p.10, MVP p.9 |
| G01 | Incorrect command execution | < 3% | PRD p.10 |
| R01 | Weekly return | >= 30% | PRD p.10 |
| R02 | Driving Mode reuse | >= 50% | PRD p.10, MVP pp.2,9 |
| P01 | P95 Voice response *after end of speech* | < 2.5 seconds | PRD p.9, MVP pp.2,4 |
| P02 | P95 local command response | < 1 second | PRD p.9 |
| S01 | Crash-free journeys/sessions | >= 99% | PRD p.9, MVP pp.2,9,11 |
| S02 | Critical Player errors per 100 sessions | < 1 per 100 sessions | PRD p.9 |
| LAB01 | Controlled Voice laboratory recognition | >= 90% | MVP p.7; Issue #10 QA |
| BETA01 | Closed Beta | 50 qualified drivers, 14 days, 3+ vehicle/head-unit models | MVP p.9 |
| D01 | Weekly dashboard | Answer: Is it used? Does it work correctly? Does the user return? in one view | MVP p.10 |

No P02 implementation may silently change these targets. In particular, exactly 15% manual correction is NOT a pass; exactly 3% wrong executions is NOT a pass; exactly 2.500 seconds Voice P95 is NOT a pass; exactly 99% crash-free IS a pass.

### 1.1 MVP printed indicator bands, preserved

| Metric | Green | Yellow | Red |
|---|---|---|---|
| Driving reuse | >=50% | 30%–49% | <30% |
| Direct-command success | >=85% | 70%–84% | <70% |
| Manual correction | <15% | 15%–25% | >25% |
| Crash-free session | >=99% | 97%–98.9% | <97% |

**Engineering continuous-interval interpretation (not a source quote):** reuse: [30%,50%) yellow; success: [70%,85%) yellow; manual correction: [15%,25%] yellow; crash-free: [97%,99%) yellow. Decimal gaps such as 84.9% or 98.95% must not be rounded up to Green. Report full precision and flag the interval convention for Product review; source-target boundaries always prevail. The PRD does NOT prescribe yellow/red bands for weekly return, wrong execution, response P95, critical errors or North Star, so do not invent them.

## 2. Units and authoritative source-of-truth

| Unit | Exact P02 operational definition (ENGINEERING_DEFINITION) | Truth owner |
|---|---|---|
| Enrolled Beta participant | Unique, actually recruited qualified pilot participant with informed non-audio diagnostic consent at enrollment. Target 50 is NOT proof that 50 have enrolled. Document opt-outs/withdrawals separately. | Beta owner controlled aggregate roster, not telemetry device/user ID |
| Week-active user | Participant with at least one verified interactive use in an anchored 7-day window, counted once, not one per launch/session; a background service heartbeat alone is not activity. | Consent-governed local participant-week flag or Beta owner evidence |
| Driving session | Starts at one authoritative accepted inactive→active Driving transition. Ends only at legitimate owner-confirmed exit; Activity rotation, recomposition and backgrounding are not new starts/ends. | Application/Quality session owner, not screen composition |
| Voice attempt | One admitted explicit PTT or Retry request owned by the single Voice coordinator. One new accepted Retry is a new attempt; one-step artist confirmation remains within the ORIGINAL attempt. | VoiceSessionCoordinator |
| Eligible direct command | One intended single-utterance command executable without disambiguation; failed/no-match ASR remains in ground-truth-labelled trial denominator, not dropped due to missing recognized intent. | Controlled QA request labels or explicit consented validated feedback |
| Successful execution | Correct intended action actually acknowledged by service-authoritative playback, with evidence of correct resulting outcome; optimistic UI or recognition alone does not prove it. | Media3 Playback service ACK + QA truth |
| Verified correction | Explicitly observed user correction attributed to a preceding command. A normal manual skip, absence of a tap, or silent app use is not automatic evidence. | Approved QA/UX attribution evidence |
| Wrong execution | Explicit verified mismatch between intended and executed action, even if service successfully ACKed the WRONG command. | QA-labelled intended/actual comparison |
| Crash | Corroborated app fatal or serious Player crash; a caught typed player error is a separate nonfatal error; unexplained OS kill/process death is UNKNOWN, not clean completion. | Approved privacy-safe crash/session classifier |
| Missing/unknown | Incomplete, unconsented, erased, unobservable, unreconciled or ambiguous evidence. Never default to 0 bad, 100% good, or successful execution. | Quality accounting |

Valid outcome classification: VERIFIED_TRUE, VERIFIED_FALSE, UNKNOWN, NOT_APPLICABLE or INVALID. Count de-duplicated eligible units; separately expose duplicates, invalid records and loss. No exported stable person/device/installation/session identifiers, artist/title/transcript/media path, Bluetooth identity, contact, location or raw voice.

## 3. Time windows and denominator populations

1. The 14-day pilot starts at a **real immutable UTC start instant T0**, assigned only when Beta actually starts. Period is half-open [T0, T0+14×24h). There is **no** asserted pilot start or telemetry sample today.
2. W1 = [T0, T0+7×24h), W2 = [T0+7×24h, T0+14×24h). These are *anchored pilot weeks*, not arbitrary ISO Mondays. Internal calculations use UTC; the UI can show user-local dates if it ALSO discloses original UTC range. Session entry counts use time of authoritative start; command events use their actual occurrence windows.
3. Keep **enrolled**, **consenting and still observed**, **W1 active**, **W2 active**, **withdrawn**, **unknown/not reached** and **actual source n** separately. Never quietly replace a metric's population denominator with only people whose telemetry was conveniently available.
4. Denominator for the PRD North Star is **weekly active users**, while Driving Mode reuse in MVP p.2 refers to **Beta users** using Driving more than once. These are intentionally different denominator populations; never substitute.
5. Pairing week1/week2 is handled by minimal consented on-device paired booleans/counters or approved Beta-owner aggregate responses. No backend/account/sync and **no outgoing stable participant key**. An identifiable roster, if needed for recruiting, is separate from the app telemetry dataset and requires the owner's lawful controls.
6. No retroactive telemetry before consent. Following withdrawal, respect the actual deletion decision; retain only lawful aggregate recruitment totals, and record unobservable individual outcomes as UNKNOWN. If the denominator cannot be recovered consistently, status is NOT_MEASURABLE.

## 4. Full operational metric dictionary (formula, scope, missingness)

For n>0, percentage = 100 × N/D. For D=0 the result is NOT_MEASURABLE (not 0%).

### NS01 — successful-Voice Driving weekly-user North Star

- **N:** Number of distinct W-active enrolled participants with at least ONE confirmed Driving session in W containing ≥1 genuinely correct Voice command, service-ACKed and not contradicted by verified correction/wrong execution.
- **D:** Number of distinct eligible W-active participants in W (not number of sessions and not 50 by default).
- Count each participant once per W, even if they have several successful sessions; a started session with unproven command result is UNKNOWN, not automatically successful.
- **Interpretation caveat:** English PRD says "Successful driving session with at least one voice command / at least 50% of weekly users" without defining whether "successful" means session completion or correct voice action. The above conservative user-level success definition is ENGINEERING_DEFINITION / REQUIRES_PRODUCT_REVIEW.

### Q01 — direct-command success, >=85%

- **N:** QA-labelled, intended direct-command Voice attempts with actual correct service-acknowledged requested result **and no verified required manual correction**.
- **D:** All eligible QA-labelled **attempted** direct commands, including ASR no-match, rejected/timeout and wrong-action trials. Distinct Retry attempts count separately but ambiguity **one-step confirmation is not a second command**.
- Distinguish five direct control intents and unambiguous PLAY_ARTIST. Confirmed ambiguous interactions are a separate cohort, not mixed into direct metric. Unknown intended intent classification cannot be silently excluded to inflate success; show unclassified count and NOT_VERIFIED on telemetry-only reports.
- The eight external events cannot independently prove the user's requested intent or lack of correction. Non-observation of correction is not proof of success. Real controlled QA or explicitly validated consented evidence is mandatory for conclusive 85% claims.

### F01 — manual correction, <15%

- **N:** Voice-command attempts for which a corrective manual user action is **explicitly attributable** to that prior attempt, count each attempt at most once.
- **D:** All eligible evaluated Voice command attempts, with evaluated true/false/unknown attribution status. An ordinary Play/Next touch may be unrelated; do not infer correction automatically. Confirmation "Neither", cancellation and re-entry are not by themselves a manual correction.
- Attribution window needs explicit Product/UX acceptance: the PDFs specify **no time limit**. P02 recommends defining next-intent/session boundary plus a bounded follow-up interval in P06/P13, but leaves the exact interval **OPEN** rather than inventing a source number. Until fixed, the automated rate is PROVISIONAL, QA-labelled evidence only.

### G01 — incorrect execution, <3%

- **N:** Explicitly verified *wrong requested action* executions (not no-match, no action or an expected refusal); preserve any severe P0 incident separately.
- **D:** All eligible attempted Voice commands with independently verified intent-and-result status; keep unverified attempts as UNKNOWN against the declared full eligible population in conservative bounds.
- Do **not** report 0% from missing reports; even a real command_executed event can represent the wrong action. A serious wrong playback/route incident can block Beta independently of the numeric <3% rate.

### R01 — weekly return, >=30%

- **N:** Distinct eligible participants active in BOTH W1 and W2.
- **D:** Distinct eligible participants active in W1 (not the full 50 and not W2 alone).
- No W1 use means not in W1 retention denominator but remains visible in enrollment/activation figures. Unavailable W2 outcome is UNKNOWN, not a non-return without reliable evidence. Use on-device paired summary with consent; **no exported stable user ID**.

### R02 — Driving Mode reuse, >=50%

- **N:** Distinct eligible enrolled participants with ≥2 independently confirmed Driving entries/sessions in 14 days.
- **D:** All eligible enrolled Beta participants in the frozen pilot cohort, not only weekly active users.
- Recomposition, foreground/background transitions and app rotation are NOT another session. A legitimate later explicit re-entry is a second session; missing logs/unconsented or deleted outcomes UNKNOWN. Also display a second descriptive *cross-week* reuse measure separately because MVP hypothesis refers to next-week return.
- **ENGINEERING_DEFINITION / Product review:** source says "use Driving Mode more than once" but does not specify minimum time between sessions.

### S01 — crash-free journey/session, >=99%

- **N:** Confirmed completed journeys with sufficient reliable evidence showing **no** fatal app crash or serious Player crash while active.
- **D:** **Every confirmed started eligible Driving journey** = verified crash-free + verified crashed + UNKNOWN/incomplete. This yields a **conservative lower bound**; an incomplete/process-killed session cannot be called crash-free. Show known-outcome fraction separately, never substitute for D.
- Typed recoverable PlayerError is **not automatically a fatal crash**. An OS process death without authenticated fatal evidence is UNKNOWN, not a confirmed crash. Genuine observed fatal crashes stay in D as failures even without a session_completed event.
- English PRD calls them crash-free "journeys"; MVP says "without a serious Player crash" and "Crash-free Session". Our conservative Driving journey interpretation is ENGINEERING_DEFINITION / QA review.

### S02 — critical Player errors, <1 per 100 sessions

- **N:** Deduplicated service-authoritative **critical** playback-error incidents according to an approved, fixed typed-code severity mapping.
- **D:** Number of eligible confirmed started sessions for the observation interval.
- **Rate = 100 × N/D critical errors per 100 sessions**, NOT percentage of sessions having an error (multiple different critical incidents can occur). No evidence = UNKNOWN, not zero critical errors. No unapproved arbitrary Throwable.message or metadata export. Severity mapping requires Playback/QA review.

### P01 — Voice-response latency P95, <2.5 seconds

- **Start:** monotonic *end-of-speech* marker from observed VAD/audio endpoint **when available**, NOT PTT tap, STT start or inferred timeout.
- **End:** first verified **user-perceivable terminal feedback** after completion or failure, e.g. a visible final Voice result **when actually displayed**, or audible TTS onset **when verifiable**. For Executed the service must have already ACKed the correct action; an intermediate Listening/Processing/Confirming UI label is NOT a terminal response.
- **Duration:** t_terminal_user_feedback − t_speech_end; negative/clock-reset/unknown endpoint → UNKNOWN. Track audio vs UI endpoint coverage; do not conflate service ACK alone, silent hidden UI, or TTS dispatch with audible delivery. Exact terminal modality definition **requires Product/Voice/QA signoff**.
- Include valid failed/timeout terminal outcomes as well as successes; do not select only fast successes. Canceled before speech, no-speech or unrecoverable missing timer is NOT_APPLICABLE/UNKNOWN as reasoned and shown separately, never 0ms.
- For n valid samples, **nearest-rank P95** = ascending sorted sample at 1-based rank ceil(0.95×n). Requirement is **strictly <2500ms**. Exactly 2500ms fails. Compute statistic **on device from transient monotonic values**; export/store only approved coarse aggregate/bins, not raw per-command durations, transcripts or timestamps.
- The #11 P11 diagnostic bucket times **STT attempt start→terminal result**: Fast <=1000ms; Expected 1001–2500ms; Slow >2500ms. These are NOT P01 speech-end→perceivable-response timing. The Expected bucket includes 2500ms and cannot prove strict <2500ms nor reconstruct a numerical P95.
- **Illustrative math only, NOT a performed test:** durations [800, 1000, 2400, 2500]ms => rank ceil(0.95×4)=4 => P95=2500ms => FAIL. Missing/slow attempts must not disappear to inflate speed.

### P02 — local-command P95, <1 second

- **Start:** authoritative accepted local/manual control input. **End:** service-observed ACK of resulting playback state. A UI animation is not the final ACK.
- Keep rejected/missing ACK in separate error/unobserved counts; do not silently select only fast successful operations. Use locally collected transient monotonic durations for P95, not raw per-user export. Product target remains strictly <1000ms.

### LAB01 — lab Voice success, >=90% (not Beta 85%)

- Numerator verified correct actual execution in controlled labelled fixed trial corpus; denominator all explicitly scheduled trials including ASR failure/no-match. Report per-intent and aggregate, no fabricated attempt-by-attempt results.
- **Known actual inherited #80 result:** Issue #10 user-reported L01–L05 aggregate **24/50=48%**, against **45/50=90%** gate → **FAIL**. Source Issue #80 remains OPEN. Issue #82 artist ambiguity/QA follow-up remains OPEN, independent of this dictionary.
- Experimental QA sample must be kept **separate** from real 14-day Beta cohort; a production analytics dashboard cannot turn a failed lab gate into PASS.

## 5. Unknown, loss and decision-safe arithmetic

Maintain, for each eligible metric, D = known_positive + known_negative + unknown, plus a separately reconciled INVALID/DUPLICATE ledger. Do not silently shrink D to observed successes to make results attractive. Unknown reasons include consent unavailable/withdrawn, privacy-gate rejection, sink dropped, missing service ACK, aborted/unobservable timing, incomplete/crashed journey without classification, a missing intended action, uncertain correction and roster attrition.

- **Higher-is-better threshold >=T:** optimistic upper = 100×(known_positive+unknown)/D; conservative lower = 100×known_positive/D. Only if **lower >= T** is target met irrespective of unknown, subject to quality and provenance approval; if **upper < T** target fails; otherwise **INDETERMINATE**.
- **Lower-is-better threshold <T:** minimum possible bad = 100×known_bad/D; maximum = 100×(known_bad+unknown)/D. Only if **maximum < T** can it be shown as met; if **minimum >= T** it fails; else **INDETERMINATE**.
- **Percentiles:** report valid n, untimed/timeout n, censored n and reasons. A quick P95 from a selected fast subset is **PARTIAL**, not a Green verdict. If capture cannot prove response modality/speech-end, show NOT_VERIFIED.
- **Zero denominators:** NOT_MEASURABLE, never 0% errors/100% success. Do not report 50 enrolled, 3 tested vehicles, consent grants or any empirical rate before actual measured evidence.
- **Coverage reporting:** enrolled/consented/active/observed/withdrawn counters; expected→recorded PTT vs terminal; accepted command→ACK/fail/unknown; session start→completed/crashed/unknown; accepted sanitizer→delivered/drop; missing start/end speech; dedup counts. Quality failures never block playback, Voice or safe route recovery.
- **Sample/CI:** show n/D and raw counts. Optional Wilson 95% binomial interval for actual *person-level* independent observations (center = (p+z²/(2D))/(1+z²/D), halfwidth = z×sqrt(p(1-p)/D+z²/(4D²))/(1+z²/D), z=1.95996398454). Statistical uncertainty is **descriptive**, NOT a new PRD threshold; 50-user cohorts and multiple correlated sessions deserve explicit small-sample caution. Never generate CI for fictional data.
- **Quality gate:** the English PDFs specify **no numeric maximum drop rate, consent coverage percentage, timed sample minimum or Wilson bound**. Do not fabricate them. Before GO, Product+QA review actual denominator provenance, material missingness, dedup, source-of-truth, P95 endpoint availability, participant/vehicle diversity and privacy. Without sufficient evidence show INSUFFICIENT_EVIDENCE or BLOCKED, not GREEN.

## 6. Approved eight event families vs actual inference capability

| Official v1.0 event | Valid contribution | Cannot prove alone |
|---|---|---|
| driving_mode_started | confirmed entry source, Bluetooth yes/no | distinct participant, successful action |
| voice_button_pressed | admitted PTT rate, coarse playback state | utterance existed, command accuracy |
| intent_detected | mapped intent and confidence bucket | correctly understood user intention |
| intent_failed | typed error class | no wrong execution occurred |
| command_executed | ACKed action + latency bucket | intent was the *requested* action, speech-end P95 |
| manual_correction | explicitly attributed correction category | no unreported corrections |
| player_error | coarse error and route class | fatal crash or crash-free proof |
| session_completed | verified completed duration/count | absent crashes, per-user return |

The approved product **external allowlist is exactly eight families** as in English PRD p.10, System Architecture p.17 and Issue #12. English MVP p.10 prints seven telemetry table rows (omits intent_failed); this is a documented source discrepancy, **not permission to remove intent_failed**. Code from Issue #11 contains a ninth local voice_intent_outcome serializable diagnostic; it is NOT approved as a ninth external v1.0 family. P03 must keep it internal/nonexportable or seek formal Privacy/Product review. No export of stable IDs or added fields to make retention arithmetic easier.

**Critical feasibility finding:** eight event types alone CANNOT definitively measure wrong execution, manual-correction false negatives, unique-user weekly return, exact P95, real crash-free coverage or verified intended-command success. P05–P10 require authoritative local aggregation and/or consented independent Beta QA evidence. Until those signals actually exist, the relevant dashboard entries must say NOT_VERIFIED; **do not falsely derive success from counting intent_detected or command_executed**.

## 7. P02 locked dashboard contract and deferred review decisions

**USED:** pilot enrollment/consent/activation, Driving sessions, sessions with Voice, NS01 weekly active user denominator, entry/Voice events and data-quality flags.

**WORKS:** Q01 direct-command verified %, F01 attributable correction %, G01 verified wrong execution %, Voice P95 after speech-end, local command P95, Player errors/100 sessions, crash-free journeys with unknown crashes, unresolved P0 incidents and coverage.

**RETURNS:** R01 W1→W2 return %, R02 14-day driving reuse among enrolled users, repeated Driving participants, raw numerator/denominator, missingness.

Every metric must display metric ID, numerator/denominator, source, UTC observation window, evidence state and LOCKED_PRODUCT threshold without changing comparison operators. No dashboard green if substantive UNKNOWN/NOT_VERIFIED/consent privacy issues prevent an evidence-based conclusion. Severe safety incidents override the traffic-light color.

**Explicit pending decisions, NOT falsely approved:**

| Decision | Current precise default/handoff | Status / owner |
|---|---|---|
| Weekly "active" | Interactive use, not process heartbeat; UTC two anchored weeks | ENGINEERING_DEFINITION / Product review |
| NS01 "successful" session | ≥1 correct service-ACKed Voice command in confirmed Driving | ENGINEERING_DEFINITION / Product review |
| Reuse counting | ≥2 genuine accepted Driving entries per enrolled user during 14 days | ENGINEERING_DEFINITION / Product review |
| Correction attribution | Only observed attributable action; exact follow-up time limit *not in sources* | BLOCKED automated proof / Product+UX |
| Incorrect execution | Known requested vs actually executed action; no false-zero | Requires independent QA/source / Voice+QA |
| Voice response endpoint | User-perceivable terminal UI/audio; speech-end origin, strict 2500ms | Product+Voice+QA signoff before verified P95 |
| Crash-free unit/interpretation | Driving session starts, complete clean vs verified crash vs UNKNOWN | QA+Product review |
| Data retention / provider | No approved TTL, storage vendor, upload or external processor | BLOCKED / Product+Privacy+Legal |
| Diagnostic consent | Separate non-audio opt-in, not existing cloud STT consent | P04 implementation + Privacy signoff |
| Numeric telemetry-loss tolerance | **Not defined by English PRD/MVP**; do not invent | Requires Product+QA approval before Beta GO |
| Ninth internal event | No external permission for voice_intent_outcome | P03 Product+Privacy disposition |
| Inherited #80/#82 | Failed lab gate and artist ambiguity remain transparent | OPEN P0 QA follow-ups |

## 8. Delivery mapping and exit register

- **P03** will freeze the eight-event schema and closed enum/bucket names; **P04** consent; **P05–P06** authoritative Driving/PTT and Voice outcome hooks; **P07–P08** actual crash/session lifecycle; **P09** bounded local consented storage/deletion; **P10** this exact metric dictionary with unknown accounting and monotonic timing; **P11** one-view dashboard; **P12** user deletion/consent education; **P13** *write only, never run* QA cases for thresholds, 2500ms equality, missingness and privacy; **P14 final only** one integrated APK and owner-reported phone QA.
- [x] Product source truth and exact cutoff directions captured from PDFs.
- [x] Four MVP source traffic-light bands preserved, decimal interpretation clearly identified.
- [x] Units, numerator, denominator, eligibility, 14-day anchored week windows and consent coverage specified.
- [x] Direct vs ambiguous, Retry, Player ACK vs UI, Voice response vs STT internal latency, crash vs incomplete and wrong execution vs manual correction distinguished.
- [x] Unknown/missing/loss conservative bounds, sample-size caveat and privacy-safe local measurement feasibility documented.
- [x] All missing Product/QA/Privacy/legal approvals visibly gated, rather than claimed to be granted.
- [ ] Product/QA/Privacy human signoff: **NOT PROVIDED**.
- [ ] Implemented metric engine, emitted event capture, schema validation, manual QA, Gradle/CI, APK, live dashboard: **NOT DONE / NOT RUN**.

**P02 RESULT: DOCUMENTED_SOURCE_ONLY.** All 14 acceptance criteria of Issue #12 remain NOT_VERIFIED. Issue #12 remains OPEN. This phase does not authorize PR merge, external analytics, or changes to Voice/Media3 business logic.
