# Issue #12 — P09 Local Diagnostics Storage, Retention, Clear and Provider Gate

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN` — source and QA test sources committed; NO Gradle, compile, JVM/instrumentation/device tests, CI, Actions, APK, PR or merge.  
**Owner-uploaded archive:** exactly `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; opened original English PDF sources, not an inferred filename.

## 1. Hierarchy of authority and what is NOT specified

- **English PRD v1 Clarified, printed p.10:** collect *only* quality/hypothesis-required data, explain permission/consent, **the user can delete history and diagnostic data**, no raw audio storage/transmission, distinct cloud-STT disclosure/retention. The eight v1 events are immutable without review.
- **English MVP v1 EN, printed pp.9–10:** non-audio diagnostics require **informed user consent**; exact approved coarse events/fields; do not collect private titles, precise location, contacts or unnecessary IDs; weekly dashboard three questions. Its table lists seven event rows, whereas the English PRD and architecture explicitly list eight including `intent_failed`.
- **English ADR-0003 revised, pp.2–4:** local-first does not make telemetry an authoritative product data store. Existing user MediaStore media, rebuildable Room library index, DataStore settings, approved user history and diagnostics have distinct owners. Users can clear applicable diagnostic data, never delete user-owned media in the process. Telemetry adapters receive **approved coarse events only**, not arbitrary domain/media entities.
- **English technical blueprint, printed pp.34–39:** typed event → strict privacy filter → non-authoritative adapter; clearable app-owned data; no backend/account. Exact historical and diagnostic retention durations and the final analytics/crash provider remain explicitly **OPEN approval decisions** (System Architecture printed p.23; Phase-0 D-026). `intent_failed` blueprint v1 field disagreement remains separately documented in P03/P06.
- **English ADR-0004/0005/0006**: no raw audio or transcripts in analytics, no arbitrary speech metadata, optional quality-service outage cannot impede playback or privacy-safe failure recovery, and no account/backend/sync in the MVP.
- **Approved GitHub UF-12** (`docs/user-flows/UF-12_SETTINGS_PRIVACY.md`): clearing history and diagnostics are **separate explicitly selected categories**; confirm then clear only selected app-owned data, **preserve likes/settings/consents/library/queue/live playback and user media**. Future external provider deletion cannot be marked complete from a local clear. No user-facing consent/deletion UI exists yet for Issue #12; **P12 owns that UI and must verify completion/failure handling**.
- Existing Issue #12 P02–P08: eight P03 externally allowed event families; internal Issue #11 ninth family forbidden for export; P04 independent diagnostics authorization with **four production approvals FALSE**; P05–P08 source-owned event admission; P06 typed bounded nonblocking channel; P07 crash enum (RAM/NoOp) and P08 volatile Driving ticket.

**No source grants permission to invent a TTL (for example “7/14/30/90 days”), device/user installation identifier, cloud processor, crash SDK, daily export, remote endpoint, per-user cohort key, or auto-on telemetry.** This phase therefore adds real **bounded process-memory source code** with a disabled production selector; it does NOT implement a disk database, upload/export scheduler or arbitrary retention duration.

## 2. P09 storage decision — RAM only, capture OFF in current build

| Boundary | Implementation and engineering choice | Status / provenance |
|---|---|---|
| Event source | P05–P08 typed event from authoritative navigation/Voice/Media3 boundaries | Source implemented, uncompiled |
| Consent before source enqueue | `DiagnosticsCollectionAuthorization.canCollect()` checks user diagnostics preference + Ready + copy + retention + adapter + Legal approval + revoke latch | P04; all production approvals **FALSE** |
| Source queue | App-scoped `Channel<PendingDiagnostic>` with capacity **64**, best-effort `trySend`; no suspend, Media3 call or network | P06 inherited queue; now an in-memory epoch protects explicit erase |
| Queue-loss accounting | Saturating, local-only `AtomicLong` for `trySend` overflow; resets on clear | New P09 engineering coverage, **not** a user-success/error metric |
| Worker | One app-scoped `Dispatchers.Default` drainer, serialization around local/NoOp publish and clear; drop stale epoch | P06/P09, independent of user app result |
| Sanitization | `AllowlistedTelemetryPrivacyGate` validates typed events before `TelemetrySink.tryEmit`; schema v1.0 exact eight families/keys/closed values | P03 unchanged; Issue #11 ninth rejected |
| Extra local adapter guard | `BoundedMemoryDiagnosticsStore` re-validates canonical version, exact keys, fixed fields AND **semantic values by re-projecting through the same P03 privacy gate**; no forged arbitrary string persisted in RAM | P09 defense in depth |
| Local record buffer | `ArrayDeque<SanitizedTelemetryEvent>`, default cap **128**, configurable hard cap **1024**, oldest evicted on overflow | Engineering **memory cap**, not Product-approved retention period |
| Coarse summary | `LocalDiagnosticsSummary`: retained count, accepted-since-clear count, evicted-oldest count, source-queue-drop count, per-family counters only | No per-utterance fields/identities; **not** a validated weekly cohort |
| Physical persistence | **NONE**: no Room table/DAO, DataStore event key, SharedPreferences event array, disk file, WAL, queue backup or Android Backup payload | **NO PERMANENT RETENTION ALLOWED** before review |
| Retention lifetime | **Until process ends, consent denial/revoke, explicit user clear or cap eviction — whichever first** | Not a time-based TTL; this is a conservative P09 stopgap, NOT approval for future disk/long-lived history |
| At-rest encryption/backups | **Not applicable to P09 event observations**, because no event data is written to disk | If P09 follow-up ever adds persistence: new Privacy review, Android backup exclusion, key protection, TTL, migration and deletion proof required |
| Local aggregation read | `summaryIfAuthorized()` returns coarse totals only when authorized; optional internal `snapshotSanitizedForLocalProjection()` feeds future P10 quality analysis in-process | P10 must preserve missingness and sample provenance |
| External telemetry | No analytics SDK, request, host, credential, HTTP client, job, uploader or account | ADR-0006; NO external provider |
| Provider selector | `LocalDiagnosticsProviderMode.NoOp` or `MemoryOnly` in `QualityBindings`, **explicit reviewed source selection**, no remotely controlled or user-preference implicit switch | **NoOp selected for both debug/release; cannot be switched by stored Granted alone** |
| Crash evidence | P07 `NoOpCrashEvidenceSink` and one closed in-process `SafeCrashCategory` remain separate, also clearable | Never a ninth v1 exported analytics event |
| P08 Driving recovery marker | **No disk marker.** In-process-only ticket; process death UNKNOWN | Pending approved retention/Crash-free join proposal; P09 does NOT fabricate a viable cross-process marker |

**Product truth:** Although a working RAM-only adapter is written, **no data is currently retained** because the actual app graph selects `NoOpTelemetrySink` and all diagnostics authorization approval flags remain FALSE. Even a device preference `DiagnosticsConsent.Granted` does NOT enable P09 collection. This is deliberate and must never be described as an observed successful collector.

## 3. Explicit deletion versus consent revocation — race-safe contract

Two separate commands, each preserving all unrelated user state:

**A. `ApplicationBindings.clearLocalDiagnostics(): Boolean`** — the future UF-12 Clear Diagnostic Data action, after a user confirmation in P12.
1. Under a short in-memory `diagnosticsDeliveryLock`, increment a private generation `diagnosticsEpoch` (not a user/session ID and never stored/exported).
2. Drain pending P06 `Channel<PendingDiagnostic>` with `tryReceive`; never print event attributes.
3. Reset all P09 ring events and aggregate counters, and the source queue-overflow counter.
4. Clear P07 in-memory crash evidence, P06 explicit manual-correction ticket and P08 ephemeral Driving diagnostic ticket to prevent later resurrection of pre-clear attribution.
5. Return success only if the in-memory erase completed; an exception produces FALSE so P12 can show Retry. Repeated clear is idempotent.
6. **Do not** alter user files, MediaStore, Room library/likes/history, Bluetooth selection, Preferences, CloudSpeechConsent, DiagnosticsConsent, playback queue, Media3 or Android permissions. Clearing is NOT consent revocation: later authorized events (if future approved) may be captured from new user actions.

**B. `ApplicationBindings.revokeDiagnosticsConsent()`** — immediate stop + erase before persistence:
1. **Synchronously** call P04 `requestImmediateRevocation()`; denied access is effective even before DataStore changes.
2. Call `clearLocalDiagnostics()` synchronously. Any pending event from the old epoch is dropped by the worker, and a concurrent worker cannot later reinsert old P09 observations past the erase boundary.
3. Persist `DiagnosticsConsent.Revoked` in the existing distinct DataStore preference asynchronously, with P04's existing failure limitations unchanged. P12 must disclose and remediate a failed preference write; production approvals still OFF.
4. An observer of `preferences` and `loadState` invokes the same erase when an independent change transitions authorization to DENIED, including loading/failed state or direct consent denial. The local buffer itself also fails closed **and purges** on any unauthorized read/write.
5. Any future external processor deletion needs an **additional provider-specific** action, documented SLA, confirmation/retry and truthful UI state; P09 currently has NO processor and **claims no remote deletion**.

**Race guarantee & scope:** The UI/Voice callback does only a P04 permission check + bounded `trySend`; sanitizer, local-buffer insert, Provider and clear coordination happen in app-owned memory/off the playback path. An already-in-flight event cannot reappear after a completed clear because the collector and clear share a short serialization boundary and enqueued observations carry a pre-clear epoch. New eligible actions AFTER clear may create new events under a future positively authorized mode. This does not create or import a stable installation/session identifier.

**Privacy caveat:** `clearLocalDiagnostics()` exists as a headless app API; **P12 does not yet provide the UF-12 user-visible selection/confirmation, failure reporting or vendor-clear states**. The presence of an API is not acceptance of that screen requirement.

## 4. Local-only snapshot and data-quality caveats

The RAM store retains **at most 128** most recent sanitized records by default. The per-family summary retains saturating accepted totals across cap evictions for the **current process and clear epoch**. Its counts are *not* sufficient to prove:
- N distinct weekly users, 14-day Beta cohort enrollment or weekly return;
- that a Speech-to-Text recognition corresponds to actual user intent;
- true wrong-execution/correction false negatives;
- exact speech-end-to-response P95;
- crash-free sessions after process death;
- complete Journey start→finish joins with zero exported session IDs;
- a 7/14-day history that survives Android process kill.

If queue cap 64 rejects an event, `sourceQueueDropsSinceClear` increases (saturated). If ring cap evicts an older sanitized event, `evictedOldestSinceClear` increases. P10 must mark missing details and dropped denominator contributions UNKNOWN, not silently call missing events PASS or 0 errors. If consent, approval, or preference readiness prevents collection, `summaryIfAuthorized()` returns null: **NULL ≠ zero observed errors or perfect uptime**.

**No network export:** The snapshot API exposes only a coarse in-memory `LocalDiagnosticsSummary` to future P10/P12 internal callers after authorization. This is **not an upload/export mechanism**; the user-flow UI, a controlled aggregate sharing mechanism, and any Beta cohort evidence require explicit Product/Privacy review and later phases. A user cannot infer or claim remote deletion because no endpoint exists.

## 5. Governance decision log — nothing falsely approved

| Decision / approval | Current fact | Effect of not having approval |
|---|---|---|
| Independent non-audio diagnostics consent | P04 enum exists; **no approved P12 notice & controls** | NO capture |
| Privacy notice scope/version | **NOT SIGNED OFF** | Privacy gate remains OFF |
| Exact event retention duration/expiry policy, TTL, cap and clock | **NOT SIGNED OFF**. RAM default 128 is an engineering safety bound only | NO durable storage / Beta KPI coverage |
| Persistent recovery marker for prior-process Driving crash | **NOT APPROVED** | No cross-process marker; crash-free per journey UNKNOWN |
| Purge contract after account/data export or app uninstall | **No account/backend**; local RAM dies with process; P12 UI still pending | No vendor-level deletion claim |
| External processor or SDK, destination, SLA, DPA/data residence | **NONE SELECTED / NO LEGAL REVIEW** | NO remote sender, uploader, crash SDK or transport |
| Per-family approved `intent_failed` schema discrepancy | English technical blueprint vs P03 implementation still **OPEN** | Review required before real collection |
| P08 duration buckets, ACK-only command-count meanings | **ENGINEERING INTERPRETATIONS** | Product/QA signoff before GO |
| Consent regrant after revocation | Requires a reviewed explicit UI + persisted decision + safe reset sequence | No P09 auto-regrant or hidden collection |
| Privacy-safe metrics, weekly retention and exact P95 | **P10 NOT STARTED** | No dashboard green success/release decision |
| Beta disclosure and Settings clear UI | **P12 NOT STARTED** | No user-facing claim that clear has been verified |
| Device tests, builds, P0 privacy and playback regressions | **P14 owner QA PENDING** | No PASS, PR merge, close or release GO |

To enable even local MemoryOnly collection in future, Product/Privacy/Legal must authorize **both** P04 gating and concrete retention semantics, then engineer explicitly changes the source-controlled selector. A remote provider would require additional architecture/ADR and processor review; `QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED=false` and `QUALITY_CRASH_UPLOAD_ENABLED=false` are unchanged.

## 6. Committed files / untouched boundaries

| Path | P09 change |
|---|---|
| `quality/api/LocalDiagnosticsStore.kt` | Clearable local-only sink contract and minimal aggregate-only summary; source queue-loss count included |
| `quality/adapters/BoundedMemoryDiagnosticsStore.kt` | In-memory 128-ring, hard config cap, semantic gate re-validation, closed event-family counts, FIFO eviction, clear-on-denial and explicit erase |
| `app/di/QualityBindings.kt` | Explicit NoOp / MemoryOnly selector; default NoOp in debug and release, P04 gated sink retained |
| `app/di/ApplicationBindings.kt` | One in-process local adapter, 64-queue epoch+overflow count, authorization observer, serialized clear, in-RAM cleanup on revoke/close, coarse summary only when mode reviewed |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase9BoundedDiagnosticsStoreTest.kt` | **17 authored, NOT_RUN** scenarios: cap, overflow, summary, consent, wipe, forbidden semantic values, ninth family, no IDs |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase9DiagnosticsDeletionWiringTest.kt` | **15 authored, NOT_RUN** scenarios: DI default, P03/P04 wiring, clear/epoch races, consent observation, queue loss, separate cloud/MediaStore state, no vendor |
| `docs/implementation/ISSUE_12_P09_LOCAL_DIAGNOSTICS_RETENTION_AND_CLEAR.md` | This source/decision/retention/deletion/coverage log |
| `docs/implementation/ISSUE_12_PHASE_LEDGER.json` | P09 status + unresolved approvals; no fabricated PASS |

Unchanged: Media3 Session/Service, queue and playback gateway, Moonshine/VAD, P06 deterministic six-intent mapper, artist matching, Audio Focus/BT route recovery, CloudSpeechConsent, MediaStore files, Room library database, DataStore schema, P03 eight-family allowlist, P04 default-deny approvals and production BuildConfig NO-UPLOAD flags. No new app permissions or dependencies.

## 7. Quality gates and nonclaims

- [x] Exact source archive/PDFs reopened and contrasted with current #12 branch.
- [x] RAM-only bounded event adapter receives **already P03-sanitized** envelopes and independently revalidates canonical fields AND semantic values.
- [x] Separate explicit provider selector, default NoOp in both build types; no account/remote/SDK path.
- [x] Epoch-safe deletion and local crash/correction/Driving attribution wipe, with consent-denied auto-clear and idempotent clear API.
- [x] Per-family coarse count + queue-loss/old-record-eviction accounting for future P10 missingness.
- [x] **32 authored P09 JVM/source contract test methods, zero executed**.
- [ ] **NO compilation, lint, JVM/Android/unit tests, emulator or handset testing, Gradle/CI/Actions, APK or upload**.
- [ ] Product/Privacy/Legal retention, data processing, notice and consent UI **NOT APPROVED**; P12 human UX work pending.
- [ ] P09 is **source-implemented only**; production telemetry remains ZERO because collection is disabled, not because quality/usage was measured.
- [ ] P10–P14 incomplete, all 14 Issue #12 acceptance criteria remain `NOT_VERIFIED`. No PR, merge, Issue closure or Beta GO.

**Next: P10** exact measurement/data-quality projection, verified speech-end time boundary and denominators. `Issue #12` remains OPEN; inherited open #80/#82 must not be silently marked PASS.
