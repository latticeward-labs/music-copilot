# Issue #12 — P11 Weekly Measurement Dashboard | Product/QA/Privacy Contract

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Phase status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`; all source files and authored QA cases were committed to GitHub; NO generator was executed, NO HTML was rendered/verified in a browser, NO unittest/Gradle/compilation/CI/Actions/APK/device test was run.  
**Archive provenance:** Original user upload `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. The original English PDFs were directly reopened and checked, including a visual inspection of MVP printed p.10 and PRD p.10. Governing source hierarchy: PRD -> MVP -> System Architecture -> approved ADRs -> GitHub Issue #12 P01–P10 source/decisions. Do not treat later engineering interpretation as an approved source instruction.

## 1. Product objective: three questions **at a glance**

The English MVP, **printed page 10**, explicitly requires: **"Is it used? Does it work correctly? Does the user return?"** Its allowed non-audio data table and privacy-boundary box forbid raw audio, private file titles, precise location, contact information and unnecessary identifiers. The English PRD printed pp.9–10 establishes the exact numerical thresholds, North Star, eight event families and consent/delete requirements. Original MVP printed p.9 provides four indicator bands (not a universal traffic-light policy). Original System Architecture p.17 and ADR-0003/0004/0005/0006 require privacy-filtered, non-authoritative quality events without introducing an account, streaming or cloud backend.

P11 delivers **one offline, printable, responsive static HTML dashboard** with these exact panels:

| Panel | At-a-glance Product/QA content | Required caveat |
|---|---|---|
| **USED** — Is it used? | Real enrolled vs consented participant counts, actual W1/W2 Driving starts, legitimate Driving+Voice sessions, distinct W1/W2 active users, **NS01 for both anchored weeks** | Sessions are NOT unique people; `voice_button_pressed` is not a true successful action; a session with voice is not automatically a correct Voice session |
| **WORKS** — Does it work correctly? | Q01 direct correct result, F01 verified correction, G01 wrong execution, P01 voice physical speech-end response P95, P02 local ACK P95, S01 crash-free started Driving journeys, S02 **critical incidents per 100 starts**, event missingness/unknowns | Validated intended-vs-executed result, complete denominators, real calibrated acoustic/visible boundaries and real critical severity classification required; `command_executed` alone cannot establish accuracy |
| **RETURNS** — Does the user return? | R01 weekly W1→W2 return, R02 enrolled-participant Driving reuse, repeat participation, actual enrolled sample size, UTC W1/W2 window, head-unit diversity | R01 denominator = **W1-active participants**; R02 denominator = **all eligible enrolled Beta participants**; no hidden per-install/user ID or session join derived from eight events |

Each KPI appears with **ID, source-locked threshold and strict comparison, window, numerator, denominator, UNKNOWN, provenance and evidence verdict**. Both Week 1 and Week 2 are explicitly visible; the cross-week metrics are separately labeled `W1+W2`. The dashboard includes a coverage/authorization section and a separate listing of the **eight v1.0 event family counts (observational only)**. No green status on missing, synthetic, unconsented, unreconciled or otherwise unqualified evidence; no spurious denominators.

## 2. Delivered repo files (not runtime-validated)

| Repository path | Purpose |
|---|---|
| `tools/quality/issue12_dashboard_core.py` | Strict closed-vocabulary validated aggregate input, source locked P10-compatible numerator/denominator math, UTC W1/W2 anchoring, strict P95 histogram boundary, source approval flags, quality BLOCKED/GO/ITERATE/STOP review candidate |
| `tools/quality/issue12_dashboard_render.py` | Responsive 3-panel HTML layout, explicit evidence statuses, provenance, summary tables and real/synthetic warnings, printable output, HTML escaping, strict static Content Security Policy; **NO JavaScript/network/fonts/images/tracking** |
| `tools/quality/issue12_weekly_dashboard.py` | Stdlib-only local CLI, explicit input and output, strict JSON duplicate-key and nonfinite rejection, file size limit, secure no-overwrite owner-private output and optional validated machine-readable summary |
| `tools/quality/fixtures/issue12_dashboard_SYNTHETIC.json` | Fully **FABRICATED** QA/UX demo values, NOT actual Music Copilot pilot results. Dataset carries `dataset_kind=SYNTHETIC`, `evidence_source=SYNTHETIC_FIXTURE`, all review flags FALSE, issues #80/#82 blockers and missingness to prove fail-closed behavior |
| `tools/quality/test_issue12_weekly_dashboard.py` | **31 authored pure-Python unittest contract cases, zero executed**: strict formula thresholds and P95 2500/1000 equality, 14-day W1/W2 denominator coherence, privacy/error rejection, Blocked/Go-Candidate, anonymous events, HTML escaping and file safety |
| `docs/implementation/ISSUE_12_DASHBOARD_SPEC.md` | This authoritative execution/status/privacy and dashboard input specification |

No app permissions, Android UI screen, Compose flow, DataStore table, Room database, Media3 handler, Voice intent mapper, STT/VAD model behavior, P03 outbound event schema or Quality upload transport are changed in P11. The standalone Product/QA report generator is **not a product analytics backend**. P12 separately owns the in-app privacy Settings and deletion UI.

## 3. Fully offline future invocation — **DO NOT RUN BEFORE P14**

The following are documented usage instructions, **not commands executed by this phase**. Python 3 standard library only; run from repository root after owner approval in the final validation phase.

```bash
python3 tools/quality/issue12_weekly_dashboard.py \
  --input tools/quality/fixtures/issue12_dashboard_SYNTHETIC.json \
  --output /tmp/music-copilot-weekly-SYNTHETIC.html \
  --summary-output /tmp/music-copilot-weekly-SYNTHETIC-report.json
```

The fixture produces a local printable HTML sheet with an unmissable **SYNTHETIC DEMONSTRATION ONLY** warning and `DEMO_ONLY` global decision. Its 50 sample participants, example events, dates, sessions and accuracy counts are imaginary, and the generator must NOT treat them as a recruited 50-person Beta or a passing lab trial. **No HTML/report output file was actually generated in P11**, by the explicit project no-execution-before-P14 rule.

To work with a real future pilot, a separately authorized Beta owner provides a minimal, **already reconciled anonymous aggregate JSON** with independently verified denominators and recorded review/consent evidence. The generator **cannot** itself access devices, fetch records, prove signatures or certify whether human-entered declarations are genuine. Any operator-set approval bit remains **operator-declared** and should be independently audited; even a complete file yields at most `GO_REVIEW_CANDIDATE`, never an automatic release GO.

Outputs are explicitly user-selected file paths, created with owner-only permissions (`0600`); existing files are never overwritten; no input/output JSON is printed to error logs. The CLI rejects duplicate JSON keys, NaN/Infinity, additional fields, oversized data (>64 KiB), unapproved vocabularies, negative or oversized counts and invalid UTC periods. The HTML renderer escapes every interpolated value and uses a local-only, no-script/no-network CSP. The optional `--summary-output` serializes **only validated reporting fields**, not the original payload.

## 4. Strict aggregate input schema, identity minimization

Root object, **exact fields only**:

```json
{
  "schema": "music-copilot-weekly-aggregate-v1",
  "dataset_kind": "SYNTHETIC",
  "evidence_source": "SYNTHETIC_FIXTURE",
  "pilot_t0_utc": "2026-01-01T00:00:00Z",
  "cohort": {
    "qualified_enrolled": 50, "consented": 46, "withdrawn": 1,
    "distinct_vehicle_models": 3, "days_observed": 14,
    "week1_active": 40, "week2_active": 29
  },
  "review": {
    "qa": false, "product": false, "privacy": false, "retention": false,
    "cohort_reconciled": false, "intent_labels": false, "voice_boundary": false,
    "local_ack_coverage": false, "player_severity": false,
    "crash_reconciled": false
  },
  "coverage": {
    "queue_drops": 2, "ring_evictions": 5,
    "missing_participant_reports": 7, "unreconciled_attempts": 4,
    "invalid_unresolved": 1, "deduplicated": 3
  },
  "weeks": ["See strict W1/W2 objects in the checked-in SYNTHETIC fixture"],
  "cross_week": {"R01": {"yes": 25, "no": 11, "unknown": 4},
                 "R02": {"yes": 24, "no": 21, "unknown": 5}},
  "open_blockers": ["LAB_VOICE_GATE_ISSUE80", "ARTIST_QA_ISSUE82"]
}
```

The above JSON is **a schema illustration, not an executable alternate fixture**, since `weeks` here is intentionally abbreviated. The exact runnable future input fixture at `tools/quality/fixtures/issue12_dashboard_SYNTHETIC.json` has **two complete week objects**, `index=1` and `index=2`.

Every week contains exactly:
- `sessions`: `started,completed,verified_crashed,unknown,voice_driving_sessions,voice_adoption_unknown` with `started=completed+verified_crashed+unknown` and nonovercounted Voice sessions. The `voice_driving_sessions` figure is descriptive presence, not an independently verified successful command.
- `counts`: **NS01,Q01,F01,G01,S01**, each with `yes,no,unknown`. `NS01.eligible` must equal the authoritative `cohort.week{1|2}_active` count; `S01.eligible` must equal **every started Driving session**; unknown/crashed journeys cannot be counted clean. Verified distinct-user NS01 successes cannot exceed Voice-linked sessions.
- `latency`: `P01,P02` with `below_target,at_or_above,unknown,boundary`. P01 accepts `PROXY_VAD_TO_COMPOSE_DRAW`, `PROXY_ANDROID_END_TO_COMPOSE_DRAW`, `UNAVAILABLE` or externally vetted `VERIFIED_ACOUSTIC_TO_PERCEIVABLE`. P02 accepts `PARTIAL_UI_TO_ACK`, `UNAVAILABLE` or `VERIFIED_ALL_ELIGIBLE_LOCAL_ACK`. **A proxy boundary NEVER certifies the official P95**. Do not export raw elapsed samples or times, only aggregate strictly-below and at-or-over bins.
- `critical_errors`: `incidents,started_verified,started_unknown`, whose latter two **must sum to session started**, even if there are zero errors observed. `incidents` counts critically classified service error *episodes*, not number of distinct people or percent of sessions touched.
- `observed_events`: exactly the **eight** English PRD/Architecture allowlisted v1.0 families `driving_mode_started`, `voice_button_pressed`, `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`, `player_error`, `session_completed`; nonnegative aggregate counts **ONLY**. They are NOT ground truth or user counts. In particular, the inherited Issue #11 ninth `voice_intent_outcome` is NEVER an export field.

`cross_week.R01` eligible count MUST equal `cohort.week1_active`; returned people cannot exceed actual W2-active population. `cross_week.R02` eligible count MUST equal **all qualified enrolled**, not all session starts or only W1 participants. Every YES/NO/UNKNOWN carries its own traceable owner-provided source and reported denominator, not an inferred event-cohort join.

**Disallowed at EVERY schema level:** session/user/installation IDs, personal media IDs/titles/artists/paths, transcripts/voice/audio, local/device location, Bluetooth device names/addresses, contacts, IPs, free-text logs, individual event arrays and external URLs. Exact nested key allowlists reject any arbitrary additional fields, even if disguised inside a row; the terminal never prints the rejected data. No new stable participant key is secretly added to support R01 or R02.

## 5. Frozen v1.0 targets; same arithmetic as P02/P10

| Metric | Denominator, formula basis | Locked target | Dashboard evidence rule |
|---|---|---|---|
| NS01 W1/W2 | Correct Voice Driving participant count / DISTINCT active people in that SAME W | **>=50%** | Verified owner cohort+QA intended action |
| Q01 | Correct QA-labelled direct attempts / ALL eligible direct attempts (ASR no-match and retries INCLUDED) | **>=85%** | Both intended-action truth and service ACK, not just `command_executed` |
| F01 | Explicitly attributable corrections / ALL eligible evaluated Voice attempts | **<15%** | A missing correction event is UNKNOWN, not no-correction |
| G01 | Independently verified wrong executions / ALL eligible intended action attempts | **<3%** | Wrong ACK result is a wrong action; no silently inferred 0% |
| R01 | W1-active person returned in W2 / W1-active people | **>=30%** | Approved distinct-person/week aggregate, no exported stable ID |
| R02 | Enrolled participants with >=2 actual Driving entries / ALL eligible enrolled | **>=50%** | Real owner-confirmed session entries, not UI rotation |
| S01 | Verified crash-free completed Driving journeys / ALL eligible starts, including crashes/unknown | **>=99%** | Incomplete/process-death UNKNOWN, never counted clean |
| S02 | Verified critical Player incidents ×100 / ALL started Driving sessions | **<1 per 100 starts** | Playback/QA-approved criticality code mapping, not percent |
| P01 | Nearest-rank P95 speech-end→perceivable terminal response | **strict <2500ms** | Calibrated actual acoustic speech-end and physical/audible feedback, NOT VAD+Compose proxy |
| P02 | Nearest-rank P95 local accepted command→real Media3 ACK | **strict <1000ms** | All eligible local source paths independently reconciled, not only visible Player/Driving taps |

Percentage is `100×YES/(YES+NO+UNKNOWN)`; `D=0` => `NOT_MEASURABLE`. Any incomplete denominator, operator-only nonverified source, withdrawal, missing approval, unresolved voice/wrong-action labels, dropped event, unreconciled or invalid sample, not-fully-consented 50/14/3 protocol, or open blocker => `NOT_VERIFIED`, never a GREEN headline. A complete independent QA-reviewed true count can compare the unrounded value: **exactly 15% F01 fails, exactly 3% G01 fails, exactly 99% S01 passes**. No fabricated "safe" rounding to 85 or 99.

For P95, `n=below_target+at_or_above` and `rank=ceil(0.95×n)`; iff `rank<=below_target`, full-source-calibrated result meets the strict threshold. **At 2500ms Voice or 1000ms local, the sample is in `at_or_above`, not `below_target`**. Unknown/incomplete attempts never disappear to inflate latency. The display shows a threshold classification/nearest-rank sample composition, **not a fabricated exact percentile duration**. For S02, numerator may exceed started sessions because one session may have more than one distinct critical incident.

### MVP indicator bands

The English MVP p.9 specifies only four Green/Yellow/Red bands: Driving reuse (Green >=50, Yellow 30–49), Command success (Green >=85, Yellow 70–84), Correction (Green <15, Yellow 15–25), Crash-free (Green >=99, Yellow 97–98.9). These are **not a license to introduce Yellow/Red thresholds for other KPIs**. The generated report shows strict evidence verdicts `PASS,FAIL,NOT_VERIFIED,NOT_MEASURABLE` and does NOT imply a new product acceptance threshold, confidence score or minimum sample beyond the actual Beta protocol.

## 6. Security, provenance, completeness and decision boundary

**Input source states:** `SYNTHETIC_FIXTURE`, `OBSERVED_EVENTS_ONLY`, `NOT_PROVIDED` are never validated Beta success sources. `QA_VERIFIED_AGGREGATE` and `CONTROLLED_BETA_VERIFIED_AGGREGATE` can yield metric candidate PASS/FAIL only if Product, QA, Privacy and retention are operator-declared approved, all required QA annotations/metric-specific calibration reviewed, the full 50-person/14-day/3+ model protocol is independently declared complete and coverage is reconciled. But the program has NO means to authenticate these flags. Its output explicitly says **operator-declared / independent review required**. A real, signed-off Beta SOP/ground truth and consent ledger must exist outside the app metrics; P11 does NOT invent such a ledger.

**Coverage:** `queue_drops,ring_evictions,missing_participant_reports,unreconciled_attempts,invalid_unresolved` must all be zero for candidate KPI verification. `deduplicated` is reported separately because dropped duplicates do not imply missing eligible independent units when their dedup method is reviewed. A nonzero unknown per metric blocks its PASS/FAIL verdict. Missing voice acoustic timing, no actual P0 crash corroboration and missing re-contact flags remain UNKNOWN; cannot infer a clean journey or successful command just because a diagnostic was not observed.

**Blockers:** The closed code list includes inherited actual unresolved #80 `LAB_VOICE_GATE_ISSUE80`, #82 `ARTIST_QA_ISSUE82`, privacy, timing, serious wrong-execution, crash and Player safety risks. Any supplied blocker prevents KPI Green and any automatic GO, regardless of seemingly good aggregate values. The user-reported **24/50=48% controlled lab** is a separately inherited Issue #80 finding, not a new P11 test or fabricated Beta observation.

**Decision evidence, NOT release authority:**
- `DEMO_ONLY`: all synthetic inputs, regardless of their numbers.
- `INSUFFICIENT_EVIDENCE`: incomplete real recruitment/consent/protocol, missing sources, QA validation, data coverage or calibrated thresholds; no new Beta start fabricated.
- `BLOCKED`: protocol-evaluable but a known unresolved issue/safety blocker remains; no GO.
- `GO_REVIEW_CANDIDATE`: real (operator-declared) complete 50/14/3 protocol, every locked metric meets target and no blockers; **a human must still verify evidence and authorize release**.
- `ITERATE_REVIEW_CANDIDATE`: complete reviewed evidence shows at least one missed target requiring technical improvement.
- `STOP_REVIEW_CANDIDATE`: complete reviewed evidence shows failure of weekly return AND driving reuse AND both-week North Star/direct command effectiveness; this is a **conservative engineering interpretation** of the PRD v1.0 qualitative STOP description, not a newly approved automated stop policy.

**No automatic release GO/STOP, no automatic merge, no closed issue.** Human Product/QA/Privacy/Legal review owns all release decisions and must confirm which Beta evidence categories have actually been reviewed.

## 7. QA source authored, never executed

The new `tools/quality/test_issue12_weekly_dashboard.py` authors **31** `unittest` method cases: synthetic watermark/no green; USED/WORKS/RETURNS in one sheet; immutable UTC weeks; distinct NS/R01/R02 denominators; S01/S02 crash reconciliation; strict Q01/F01/G01 threshold boundaries; 2500/1000ms nearest rank; VAD proxy always unverified; source unknown, event/queue/eviction/consent/truncated Beta blocked; no user/media/voice identifiers; no nine-family export; JSON key duplication, nonfinite values/overflow; HTML escaping/no scripts/remote assets; and output file privacy/overwrite semantics. Fixtures are **synthetic test inputs only, zero tests run**. Do not mark source assertions as PASS. Phase 13 will reconcile all P03–P12 authored test suites before the **single** final Phase 14 APK/test handoff.

## 8. Deferred requirements and acceptance dependencies

- Current Android production P04 collection approvals `false`, P09 production sink `NoOp`; P11 does NOT enable analytics capture, upload, external processing, crash SDK or silently generate a weekly file.
- No real 50-person pilot enrollment, 14-day T0, 3+ vehicle-model evidence, actual Privacy-signed scope, approved non-audio consent and retention, true correction/wrong-action labels, acoustic P95, complete local command coverage, critical Player code severity map or crash-free cross-process join have been supplied.
- Exact eight outward events lack weekly unique-user/return join; the generator does **not** reconstruct or store private IDs to pretend this is solved. Controlled Beta owner must supply independently approved anonymous aggregate/participant-week evidence for future real reports.
- This is Product/QA offline reporting, not a new in-app player screen or product backend. P12 owns user diagnostics Settings copy, consent revocation and actual local data-clearing UI. P13 is SOURCE TEST WRITING ONLY. P14 is the single integrated APK build, after which the owner conducts device QA and authorizes merge/closure.
- **All 14 Issue #12 acceptance criteria remain `NOT_VERIFIED` and Issue #12 stays OPEN.** Phase P11 recorded as `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`, not an operational telemetry/weekly dashboard PASS.

**Next phase:** P12 Settings, deletion, education and retention UX, preserving MediaStore and Media3 authority while distinguishing app-owned diagnostics delete from optional cloud STT consent/remote-provider deletion.
