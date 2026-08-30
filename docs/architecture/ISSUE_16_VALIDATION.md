# Issue #16 — Validation and Test Architecture

- **Phase:** 8 — Test Architecture and Validation
- **Status:** **PASS / COMPLETE / VALIDATION-ARCHITECTURE-READY**
- **Date:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Source package:** user-attached English `MSM2(2).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Controlling architecture:** [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md), [Phase 6 implementation contracts](IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Phase 7 runtime policy](RUNTIME_POLICIES.md)
- **Controlling ADRs:** [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), with ADR-0001..0006 preserved by the [ADR register](../adr/README.md)
- **Product QA/privacy evidence:** [QA and Closed Beta Plan](../QA_AND_BETA_PLAN.md), [Analytics and Privacy](../ANALYTICS_AND_PRIVACY.md)
- **Repeatable machine gate:** [`scripts/validate_issue16_architecture.py`](../../scripts/validate_issue16_architecture.py) executed by [`issue-16-architecture-validation.yml`](../../.github/workflows/issue-16-architecture-validation.yml)

## 1. Phase 8 decision

The Issue #16 architecture now has an implementation-facing validation model. Every release-blocking architecture path has a named test seam and a concrete recovery assertion. Unit, integration, UI/service, recreation, interruption, Voice, privacy and field/Beta seams are separated so Issue #4 can build production tests without inventing a new owner, fake boundary or recovery family.

This phase also adds a repeatable PR validation gate. It performs repository-relative link validation, standalone/inline Mermaid source parity, traceability coverage, Architecture-ID coverage and branch scope checks. A separate CI job parses and renders all eight architecture Mermaid sources with Mermaid CLI.

**No Persian/Farsi PRD or MVP is used as Phase 8 authority.** The attached English package, Accepted ADRs and approved GitHub evidence remain the authority chain.

This phase defines and validates the test architecture. It does **not** falsely claim that production Issue #4 code, OEM hardware, phone-call behavior, three real vehicles or Closed Beta execution already exist.

---

## 2. Validation levels and ownership

| Level | Purpose | Primary seams | Required environment | Phase 8 meaning |
|---|---|---|---|---|
| **Unit** | Deterministic policy and reducer correctness | mapper, queue, seek, Previous, interruption reducer, Voice state machine, privacy gate | JVM/local deterministic fakes | Must be fast and exhaustive for policy boundaries |
| **Integration** | Boundary contract correctness | MediaSessionService/Controller, MediaStore/Room/DataStore, STT adapter | JVM where possible; Android instrumentation where platform behavior is required | Proves ports and concrete adapters agree |
| **UI/Service** | Observer/authority synchronization | Compose/ViewModel ↔ PlaybackGateway ↔ MediaSessionService | Android instrumentation | Proves UI never becomes playback truth |
| **Lifecycle/Recreation** | P0 continuity and P1 boundary | Activity recreation, controller reconnect, service continuity, cold restore | Android instrumentation/process test harness | Proves P0 active and P1 paused restoration remain distinct |
| **System recovery** | Safety under failure/interruption | permission, corrupt media, call/focus/route, Voice timeout | deterministic fake gateways + Android integration | Every failure must end in an approved recovery terminal |
| **Device/OEM** | Platform/OEM compatibility | Android 10/12/14+, Pixel/Samsung/Xiaomi | emulator plus physical devices | Required downstream execution matrix |
| **Field/Beta** | Real route/head-unit behavior | Bluetooth, calls, road noise, three head units | physical vehicle/head-unit | Required downstream gate; not fabricated in Phase 8 |

### Test-double rule

Test doubles are placed at existing public ports; they may not create a new architecture boundary.

Allowed examples:

- fake `PlaybackGateway` / fake Player adapter;
- fake `SpeechToTextEngine` and `TextToSpeechEngine`;
- fake `LibraryRepository`, MediaStore scanner and capability gateway;
- in-memory Room;
- in-memory/fake DataStore-facing stores;
- fake Audio Focus/route/call gateways;
- virtual clock/test dispatcher;
- fake `TelemetrySink`.

Forbidden test design:

- a fake UI-owned Player that changes ownership;
- a fake repository that becomes live queue truth;
- a test-only Voice owner separate from `VoiceSessionCoordinator`;
- bypassing `TelemetryPrivacyGate` to make analytics tests easier;
- allowing P1 persisted state to behave as live playback authority.

---

## 3. Unit-test seams

### UT-01 — Deterministic Intent Mapper

**Seam:** pure `DeterministicIntentMapper` plus fixed corpus and deterministic candidate inputs.

Required assertions:

- only the six approved MVP executable intents are emitted;
- unknown/unsupported input produces typed no-match/unsupported behavior;
- missing/invalid provider confidence never direct-executes;
- direct execution respects the approved confidence policy;
- mapper output alone never mutates playback;
- normalization is deterministic for identical inputs;
- no open-ended execution decision is introduced.

**Recovery assertion:** ambiguous/not-found/unsupported input preserves current playback and routes to bounded confirmation or manual fallback; it never guesses a command.

### UT-02 — Queue reducer / command policy

**Seam:** queue policy/`PlaybackCommandProcessor` with fake Player and deterministic `MediaRef` list.

Required assertions:

- `ReplaceQueue` validates non-empty playable input and index bounds;
- Next moves only to a valid next item;
- queue revision changes only on authoritative queue mutation;
- command order is preserved;
- accepted commands are never silently dropped;
- capacity 32 overflow returns typed `PLAYBACK_BUSY`;
- rejected mutation keeps the last confirmed queue/snapshot.

**Recovery assertion:** invalid command or unavailable item cannot create an optimistic queue; corrupt/unplayable handling uses controlled skip and ends Safe Paused/queue-end when no valid item remains.

### UT-03 — Seek policy

**Seam:** pure seek calculation plus fake Player duration.

Required assertions:

- Voice/manual seek-back is `-10_000 ms`;
- target never becomes negative;
- target is clamped to verified duration when known;
- seeking unsupported media returns typed rejection;
- seeking does not change queue ownership.

**Recovery assertion:** invalid/unsupported seek leaves last confirmed position/state intact and exposes typed recovery rather than fabricated success.

### UT-04 — Previous policy

**Seam:** pure Previous policy with current position/index.

Required assertions:

- position `> 3_000 ms` restarts current item;
- position `<= 3_000 ms` moves to the previous valid item when available;
- first-item edge cannot produce an invalid index;
- policy result still executes through the serialized Playback authority.

**Recovery assertion:** no valid previous target keeps a truthful current/paused state; it does not fabricate a queue item.

### UT-05 — Privacy filter

**Seam:** typed `TelemetryEvent` → `TelemetryPrivacyGate`.

Required assertions:

- approved event families/properties pass only after schema validation;
- unknown event/property is rejected;
- arbitrary/free-form map or JSON is rejected;
- raw audio, transcript, artist/album/folder/title, filesystem path, content URI, MediaStore ID, search query/results, library contents, queue contents, precise position/location, contacts/call identity, Bluetooth/vehicle identity and credentials/tokens are rejected;
- coarse typed error/route/intent/confidence/latency/duration/count values remain allowed only by schema;
- sanitizer output contains no prohibited value.

**Recovery assertion:** privacy rejection drops/contains the diagnostic event; product behavior and Playback continue unchanged.

### UT-06 — Interruption reducer

**Seam:** pure interruption policy with prior-playing, focus, route-generation and manual-pause inputs.

Required assertions:

- automatic resume only when all four ADR-0007 guards pass;
- permanent focus loss results in Safe Paused;
- route disconnect invalidates prior resume eligibility;
- manual Pause wins;
- duplicate/stale events are idempotent.

**Recovery assertion:** failed guard stays Safe Paused/Manual Resume; reconnect alone never auto-resumes.

### UT-07 — Voice session state machine

**Seam:** `VoiceSessionCoordinator` with virtual time, fake STT/TTS and fake PlaybackGateway.

Required assertions:

- exactly one active generation;
- listening timeout is six seconds;
- only one explicit Retry is available;
- maximum attempts are two;
- stale generation/token result is ignored;
- only current pending confirmation may execute;
- success state follows playback acknowledgement;
- TTS failure does not roll back acknowledged playback.

**Recovery assertion:** first eligible failure permits one Retry; second failure is terminal for that session and returns to manual fallback.

---

## 4. Integration-test seams

### IT-01 — MediaSessionService ↔ Player contract

**Environment:** Android instrumentation or Media3-compatible service harness.

Exercise:

`PlaybackCommand -> PlaybackCommandProcessor -> MediaSessionService -> Player/MediaSession -> PlaybackSnapshot`

Assertions:

- only service lifecycle constructs/owns the production Player and MediaSession;
- commands execute sequentially;
- authoritative snapshot changes after accepted mutation;
- service remains independent of Activity/ViewModel lifecycle;
- terminal Stop/cleared queue/release behavior follows ADR-0007.

Recovery:

- player error is translated to typed `MediaItem`/`Playback` error;
- no second Player is created;
- corrupt item enters controlled skip logic.

### IT-02 — MediaController / PlaybackGateway contract

**Environment:** real MediaController connection to test service.

Assertions:

- connection state moves through `Disconnected/Connecting/Ready/RecoverableError`;
- complete snapshot is atomically replaced before `Ready`;
- app command and external/system command converge on the same session authority;
- timeout/rejection does not fabricate success;
- last confirmed snapshot remains presentation context during reconnect.

Recovery:

- failed controller connection exposes typed Controller failure and Retry/reconnect;
- system-surface absence is not interpreted as playback Stop.

### IT-03 — Room / derived library projection

**Environment:** in-memory Room plus repository.

Assertions:

- Room is rebuildable projection, not media-existence authority;
- scan normalization produces deterministic rows/search tokens;
- rebuild clears/reconstructs derived projection without modifying user media;
- released app-owned records use explicit migrations;
- live queue never moves into Room.

Recovery:

- corrupt derived index triggers `Rebuild`;
- failed mutation preserves last confirmed app-owned state.

### IT-04 — MediaStore / LibraryRepository

**Environment:** Android instrumentation with controlled test media and capability gateway.

Assertions:

- MediaStore is canonical existence/identity truth;
- reconcile inserts/updates/removes stale projection;
- stale `MediaRef` is revalidated;
- permission/source truth changes invalidate stale `Ready`.

Recovery:

- denial/revocation returns truthful unavailable/recoverable library state;
- retry/Settings return performs a fresh platform re-check;
- stale Room data is not shown as current permission truth.

### IT-05 — STT adapter lifecycle

**Environment:** fake STT for deterministic contract tests plus Android speech capability probe for platform adapter availability/cancellation.

Assertions:

- recognition starts only after push-to-talk + current permission/capability check;
- coroutine/session cancellation cancels provider work where supported;
- provider result maps into the same deterministic mapper path;
- transcript/confidence remain ephemeral;
- remote speech path cannot start without its separate approved gate.

Recovery:

- unavailable/language/provider failure becomes typed Voice failure and returns to one-Retry/manual fallback policy;
- no provider result bypasses confirmation or directly owns playback.

### IT-06 — DataStore restoration contract

**Environment:** temporary/in-memory DataStore-facing `RestorableQueueStore`.

Assertions:

- versioned snapshot only;
- stable re-query references, no title/artist/path payload;
- corrupt/future schema rejected;
- missing items dropped after MediaStore re-query;
- position clamped;
- live MediaSession wins;
- valid cold restore prepares Paused only.

Recovery:

- invalid snapshot is ignored/discarded safely;
- no snapshot creates autoplay.

### IT-07 — Quality adapter fault isolation

**Environment:** privacy gate plus sink that returns false/throws internally/has unavailable configuration.

Assertions:

- product path never waits for telemetry;
- sink failure is contained;
- optional configuration failure uses approved local defaults;
- no quality adapter owns Player, Voice, permission, route or persistence truth.

Recovery:

- diagnostics may be dropped;
- Playback, Voice cancellation, permission recovery and route safety continue.

---

## 5. Required UI, lifecycle and recovery tests

### ST-01 — UI / Service synchronization

Procedure:

1. start playback through the public command path;
2. observe service acknowledgement;
3. compare UI/ViewModel projection with `PlaybackSnapshot`;
4. issue an external MediaController command;
5. compare app UI, notification/lock/external projection with the same authoritative revision.

PASS assertions:

- UI never displays Playing before acknowledgement;
- all observer surfaces converge on service state;
- no UI-owned queue or position mutation exists.

Recovery assertion:

- temporary controller/system-surface loss shows reconnect/recoverable state without changing actual service playback truth.

### ST-02 — UI recreation and controller reconnect

Procedure:

1. establish Playing state;
2. recreate Activity/destination;
3. disconnect/recreate controller;
4. reconnect to existing service;
5. verify complete snapshot replacement before `Ready`.

PASS assertions:

- Player instance is not recreated by UI;
- queue/current item/position/revision are not reconstructed from UI state;
- Playback continues when legitimate;
- snapshot is internally coherent.

Recovery assertion:

- reconnect failure is typed Controller recovery; no fabricated Playing/Stopped state.

### ST-03 — Process loss / P1 cold restoration

This is intentionally separate from ST-02.

Procedure:

1. create a valid P1 restorable snapshot;
2. simulate process-loss/cold-start boundary;
3. ensure no live session exists;
4. restore through MediaStore validation.

PASS assertions:

- P0 process-death survival is not claimed;
- snapshot is non-authoritative;
- valid restore is Paused only;
- corrupt/future schema or missing media is handled safely.

Recovery assertion:

- invalid restore falls to Library/Safe Paused and never autoplays.

### ST-04 — Corrupt-file recovery

Procedure:

1. queue valid A, corrupt B, valid C;
2. trigger B playback failure;
3. service translates failure to typed error;
4. controlled skip seeks next valid item.

PASS assertions:

- no infinite skip loop;
- queue authority remains service-owned;
- C can continue if valid;
- if no valid item remains, state becomes queue-end/Safe Paused.

Recovery assertion:

- user receives truthful recoverable state; no crash and no unintended output route change.

### ST-05 — Permission denial and revocation

Cases:

- initial denial;
- permanent denial;
- Settings handoff and return;
- permission revoked after prior `Ready`;
- provider capability lost.

PASS assertions:

- grant is never cached as current truth;
- return always re-checks Android/provider;
- MediaStore reconciliation invalidates stale Room projection;
- Voice permission loss cancels active Voice generation.

Recovery assertion:

- Retry/Not now/Open Settings/Manual Only follows the approved state; stale data never fabricates readiness.

### ST-06 — Call, Audio Focus and Route

Use deterministic fake gateways for repeatable integration and physical execution downstream.

Cases:

- transient focus loss;
- permanent focus loss;
- incoming call;
- duck request;
- manual Pause during interruption;
- wired/Bluetooth disconnect;
- route reconnect;
- duplicate/stale callback order.

PASS assertions:

- four resume guards are enforced;
- disconnect immediately pauses and cancels Voice;
- route generation invalidates stale resume;
- reconnect alone does not auto-resume;
- no unintended speaker playback is accepted.

Recovery assertion:

- unsafe/failed guard terminates Safe Paused with Manual Resume.

### ST-07 — Voice timeout, one Retry and ambiguity

Cases:

- direct intent;
- ambiguous two candidates;
- not found;
- unsupported phrase;
- six-second timeout;
- first provider failure;
- second failure;
- stale callback after cancellation.

PASS assertions:

- one bounded confirmation;
- no low-confidence/ambiguous guessing;
- exactly one explicit Retry;
- second attempt failure is terminal;
- stale result cannot mutate Voice or playback;
- playback success must be acknowledged before TTS success state.

Recovery assertion:

- failed/terminal Voice returns immediately to approved manual controls/fallback while preserving actual playback.

### ST-08 — Privacy / Quality failure isolation

Cases:

- every allowlisted event;
- unknown event/property;
- each prohibited-field family;
- unavailable analytics/crash sink;
- malformed/unavailable optional configuration.

PASS assertions:

- prohibited content is rejected before sink;
- diagnostics failure is non-authoritative;
- local/manual P0 functions remain available.

Recovery assertion:

- drop/NoOp is the Quality recovery; Playback must not Pause/Stop because analytics or optional configuration failed.

---

## 6. Architecture-path validation matrix

The controlling [Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md) defines AP-01..AP-12. Phase 8 turns each catalog Test/Recovery cell into an executable seam.

| Architecture path | Priority role | Minimum Phase 8 seams | Mandatory recovery assertion | Gate |
|---|---|---|---|---:|
| **AP-01 Bootstrap and education** | P0 | startup reducer; migration-failure contract; fake DataStore; recreation/resume | bounded Retry/resume; incomplete startup never fabricated as complete | PASS |
| **AP-02 Permission/capability** | P0 | IT-04 + ST-05; fake capability/permission gateway | denial/revocation -> truthful Retry/Settings/fresh re-check; unavailable provider -> manual path | PASS |
| **AP-03 Library/search** | P0 | IT-03 + IT-04; normalization/reconciliation tests | retry/rebuild/reconcile; stale selection/index rejected | PASS |
| **AP-04 Playback/queue** | P0 + separable P1 restore | UT-02/03/04 + IT-01/02/06 + ST-03/04 | rejected command preserves truth; corrupt bounded skip; invalid restore never autoplays | PASS |
| **AP-05 Background/system controls** | P0 + separable P1 restore | IT-01/02 + ST-01/02/03 | reconnect; unavailable surface != playback failure; P1 restore Paused only | PASS |
| **AP-06 Driving Mode** | P0 + separable P1 hint | Driving reducer/navigation + ST-06/ST-07 | immediate Exit; truthful Playing/Paused/No Track; route/Voice failure returns manual/safe state | PASS |
| **AP-07 Direct Voice** | P0 | UT-01/07 + IT-05 + ST-07 | typed Voice failure cancels safely and returns initiating manual surface | PASS |
| **AP-08 Ambiguity/not-found** | P0 | UT-01/07 + fixed candidate corpus + ST-07 | one confirmation; bound candidate only; not-found/unsupported never guesses | PASS |
| **AP-09 Timeout/retry** | P0 | UT-07 virtual clock + ST-07 | six-second timeout; one Retry; second failure terminal/manual fallback | PASS |
| **AP-10 Call/focus/route/corrupt safety** | P0 | UT-06 + ST-04/ST-06 | guarded resume only; permanent/unsafe -> Safe Paused; route reconnect no auto-resume; corrupt bounded skip | PASS |
| **AP-11 Settings/privacy/clearing** | P0-referenced privacy/control + separable P1 preference | UT-05 + IT-03/06/07 + ST-05/08 | preserve last confirmed setting; clear result truthful; provider unavailable/manual; Quality failure contained | PASS |
| **AP-12 Liked Songs** | **P2 removable**; appears in P0 non-regression rows only to prove isolation | feature-disabled P0 non-regression + stale/idempotency policy | disabling/removing P2 cannot break P0/P1; mutation failure preserves confirmed state | PASS / non-blocking |

**Important AP-12 interpretation:** the machine traceability parser sees AP-12 in rows whose Priority text contains P0 because those rows prove P2 isolation/non-regression. This does not promote Liked Songs to P0. AP-12 remains P2/removable exactly as the source matrix states.

### P0 PASS condition

For every architecture path referenced by a P0 traceability row:

- at least one Test seam exists;
- a non-empty Recovery assertion exists;
- the architecture owner is unchanged;
- no test needs a new product boundary.

Result: **0 P0-referenced Architecture paths missing Test or Recovery coverage.**

---

## 7. Traceability-row machine audit

The repeatable validator parses Markdown tables instead of trusting a hand-written summary.

Validated rules:

1. Architecture catalog contains exactly `AP-01..AP-12`.
2. Every Architecture ID referenced by traceability rows exists in the catalog.
3. The document-declared traceability-row count matches parsed rows.
4. Every parsed traceability row containing Architecture IDs has a non-empty `Recovery` and `Test` cell.
5. Every P0-referenced Architecture path has a catalog-level Test and Recovery contract.
6. No unknown Architecture ID is accepted.

Pre-publication CI execution measured:

- Architecture catalog: **12/12**
- Architecture IDs referenced: **12/12 known**
- Traceability rows parsed: **199**
- Rows whose Priority contains P0: **174**
- Traceability rows missing Test or Recovery: **0**
- Unknown Architecture IDs: **0**

The validation workflow is configured on PR synchronization, so the same audit is rerun after this Phase 8 artifact is added.

---

## 8. Link audit

Machine scope:

- `docs/SOFTWARE_ARCHITECTURE.md`;
- all top-level Markdown files under `docs/architecture/`;
- all Markdown ADR files under `docs/adr/`.

Rules:

- repository-relative Markdown targets must resolve in the checkout;
- fragments do not create a separate file target;
- external HTTP links are outside the filesystem-resolution test;
- link failures fail the PR validation job.

Pre-publication execution:

- relative links checked: **220**
- unique local targets: **134**
- broken relative links: **0**

Historical Wireframe link audits remain useful upstream evidence, but Phase 8 does not substitute them for this architecture-specific audit.

**Result:** **PASS**.

---

## 9. Mermaid validation

Two layers are enforced.

### 9.1 Structural/source-parity validation

The Python gate verifies:

- all eight required standalone `.mmd` sources exist;
- every source begins with a recognized Mermaid diagram declaration;
- standalone files contain no Markdown fence;
- `subgraph`/`end` structure and quote balance are sane;
- `docs/SOFTWARE_ARCHITECTURE.md` contains exactly eight inline Mermaid blocks;
- each standalone `.mmd` source is byte-equivalent, after outer whitespace normalization, to one inline block.

Required files:

1. `01-system-context.mmd`
2. `02-container-component-architecture.mmd`
3. `03-dependency-direction.mmd`
4. `04-state-ownership.mmd`
5. `05-playback-ownership-lifecycle.mmd`
6. `06-navigation-voice-overlay.mmd`
7. `07-critical-data-flows.mmd`
8. `08-privacy-telemetry-boundary.mmd`

### 9.2 Mermaid CLI parse/render

The GitHub Actions `mermaid-parse` job runs Mermaid CLI against every `0*.mmd`, renders an SVG and verifies exactly eight non-empty SVG outputs.

CI execution generated **eight Mermaid charts successfully**.

**Mermaid parse/render failures:** `0`.

**Result:** **8/8 PASS**.

---

## 10. Device and OS validation matrix

The source QA matrix is retained without inventing exact physical device models that have not been supplied.

### 10.1 Android OS matrix

| OS family | API reference | Automated/reference gate | Physical gate |
|---|---:|---|---|
| Android 10 | API 29 | service/controller, permission/media, recreation, Voice boundary smoke | required downstream |
| Android 12 | API 31 | same P0 architecture suite | required downstream |
| Android 14+ | API 34+ | same P0 architecture suite; include current supported 14+ target during execution | required downstream |

The existing Android spike workflow already preserves AOSP instrumentation slots for API 29/31/34 as evidence infrastructure. Phase 8 requires the production Issue #4 suite to inherit the same OS-family coverage rather than treating spike execution as production proof.

### 10.2 OEM matrix

| OEM family | Required minimum physical representation | Critical focus |
|---|---|---|
| Pixel / Android Stock | at least one supported physical Pixel, build recorded | reference lifecycle, MediaSession, permission behavior |
| Samsung / One UI | at least one supported Samsung, build recorded | background/service, notification/system controls, audio route |
| Xiaomi / MIUI | at least one supported Xiaomi, build recorded | OEM background/service behavior, route/reconnect, permission behavior |

Coverage may overlap OS and OEM requirements; exact model/OS/build must be recorded when the test is executed. Phase 8 does not invent device names or claim unperformed physical runs.

### 10.3 Field/Beta vehicle/head-unit gate

Maintain three distinct slots:

| Gate ID | Required evidence at execution | Mandatory scenarios |
|---|---|---|
| **VHU-01** | vehicle/head-unit make/model, phone/build, Bluetooth profile, date/reviewer | Play/Pause/Next/Previous, screen-off, call/focus, disconnect/reconnect, Driving/Voice |
| **VHU-02** | different vehicle/head-unit configuration with same evidence fields | same P0 field suite |
| **VHU-03** | third distinct vehicle/head-unit configuration with same evidence fields | same P0 field suite |

PASS requires **3+ real vehicle/head-unit models** in Field/Beta evidence. Placeholder IDs are architecture test slots, not fabricated vehicle evidence.

Critical field assertions:

- Bluetooth disconnect immediately pauses;
- no unintended speaker playback;
- reconnect alone never auto-resumes;
- notification/head-unit/app surfaces remain synchronized;
- call/focus behavior follows the four-guard policy;
- medium road noise and similar-artist Voice cases preserve the approved no-guess behavior.

---

## 11. Scope-creep controls

### 11.1 Machine control

The CI validator compares the Issue #16 branch with frozen baseline `281e1abda39a78ee0d80051c154ed6eb4af33361`.

Allowed changed paths are documentation plus the dedicated validation script/workflow. Any unrelated non-document/non-validation path fails the gate.

Pre-publication machine result:

- changed files inspected: **24**
- unauthorized paths: **0**
- semantic out-of-scope keyword hits requiring classification: **7**
- hits classified as explicit exclusion/prohibition/future-after-Beta context: **7**
- unreviewed positive scope additions: **0**

### 11.2 Manual semantic control

The seven machine hits were manually reviewed. They are negative governance references, including forbidden generic backend/account/streaming coupling, forbidden open-ended/LLM Voice expansion, prohibited credentials/account identifiers, explicit platform/scope exclusions and a future-after-Beta discovery note. None introduces an MVP feature.

Manual scope rules remain:

- **No backend, account, cloud sync, streaming or online catalog is introduced.**
- **No iOS, Android Auto or CarPlay implementation is introduced.**
- **No wake word, always-listening Voice, open-ended LLM execution, humming recognition or recommendations are introduced.**
- P2 Liked Songs remains removable/non-blocking.
- Optional speech/quality providers remain replaceable/non-authoritative and do not add a product backend.

**Manual scope-creep findings:** `0`.

---

## 12. Existing spike evidence versus production validation

Existing spike/evidence harnesses can seed Issue #4 test implementations, especially for Media3 service ownership, MediaStore round-trip/reconciliation, Voice capability/session policy, privacy/data policy, interruption policy and restoration.

However:

- spike test success is **not** relabeled as production architecture-test success;
- Phase 8 defines the required production seams and assertions;
- Issue #4 must implement the tests against production contracts;
- Field/Beta must provide physical OEM/vehicle evidence separately.

This preserves the evidence/non-claim boundary used throughout Issue #16.

---

## 13. Issue #4 implementation test order

The Android foundation should establish tests in this order:

1. leaf unit tests: mapper, queue, seek, Previous, privacy, interruption and Voice state machine;
2. Data integration: MediaStore + Room + DataStore restoration;
3. Playback service/controller integration;
4. UI/service synchronization and recreation/reconnect;
5. corrupt media + permission denial/revocation recovery;
6. call/focus/route deterministic integration;
7. Voice timeout/retry/ambiguity integration;
8. privacy/Quality failure isolation;
9. API 29/31/34+ automated matrix;
10. Pixel/Samsung/Xiaomi physical matrix;
11. three Vehicle/Head-unit Field/Beta gate.

A failed field gate cannot be “fixed” by changing an Accepted architecture invariant locally; material ownership/lifecycle changes return to ADR governance.

---

## 14. Phase 8 acceptance audit

| User-requested requirement | Resolution | Result |
|---|---|---:|
| Mapper unit seam | UT-01 | PASS |
| Queue unit seam | UT-02 | PASS |
| Seek unit seam | UT-03 | PASS |
| Previous unit seam | UT-04 | PASS |
| Privacy filter unit seam | UT-05 | PASS |
| MediaSessionService integration seam | IT-01 | PASS |
| MediaController integration seam | IT-02 | PASS |
| Room integration seam | IT-03 | PASS |
| MediaStore integration seam | IT-04 | PASS |
| STT integration seam | IT-05 | PASS |
| UI/Service synchronization | ST-01 | PASS |
| recreation/reconnect | ST-02 + ST-03 | PASS |
| corrupt-file recovery | ST-04 | PASS |
| permission denial/revocation | ST-05 | PASS |
| Call/Audio Focus/Route | ST-06 | PASS |
| Voice timeout/one-retry/ambiguity | ST-07 | PASS |
| Android 10/12/14+ matrix | Section 10.1 | PASS — matrix registered |
| Pixel/Samsung/Xiaomi matrix | Section 10.2 | PASS — matrix registered |
| 3 Vehicle/Head-unit Field/Beta gate | Section 10.3 | PASS — gate retained, execution downstream |
| Link audit | CI Python gate | PASS |
| Mermaid validation | structural parity + Mermaid CLI render | PASS |
| Architecture ID coverage | AP-01..AP-12 machine audit | PASS |
| all 199 traceability rows | parser coverage | PASS |
| machine scope-creep control | diff + semantic classifier | PASS |
| manual scope-creep control | Section 11.2 | PASS |

---

## 15. Phase 8 PASS gate

Required condition:

> **Every P0 Architecture path has at least one Test seam and one Recovery assertion.**

Machine and manual review result:

- architecture catalog: **12/12 present**;
- traceability rows: **199/199 parsed**;
- traceability rows missing Test/Recovery: **0**;
- P0-referenced Architecture paths missing Test/Recovery: **0**;
- unknown Architecture IDs: **0**;
- broken architecture/ADR relative links: **0**;
- Mermaid standalone sources: **8/8 present**;
- Mermaid CLI parse/render: **8/8 PASS**;
- unauthorized branch scope paths: **0**;
- unreviewed positive scope-creep additions: **0**.

# **PHASE 8: PASS / COMPLETE / TEST-ARCHITECTURE-VALIDATED**

## 16. Non-claims and remaining gate

This Phase 8 PASS means the architecture test seams, recovery assertions and documentation-validation machinery are complete enough for implementation planning.

It does **not** mean:

- production Android Issue #4 tests have already been implemented/executed;
- Pixel/Samsung/Xiaomi physical runs are complete;
- three real Vehicle/Head-unit gates are complete;
- Closed Beta has run;
- provider/legal/retention approval exists;
- Issue #16 has final cross-role approval;
- Issue #4 is formally unblocked.

Issue #16 remains open until the remaining repository synchronization, cross-role reviews and final merge/exit gate pass.
