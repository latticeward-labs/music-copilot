# Cluster B Android Spike Harness

**Tracking:** Issue #3; informs Issue #16  
**Scope:** TS-01, TS-02, TS-06, TS-08  
**Classification:** Focused pre-architecture technical spike; not production implementation  
**Product foundation (Issue #4):** Not started  

## Purpose

The repository's Delivery Workflow blocks the production Android foundation until Issue #16 is approved. Issue #16 simultaneously requires focused technical spikes where lifecycle, Media3, MediaStore, and restore evidence is missing. The isolated project at `spikes/android-validation/` resolves that boundary without bypassing the implementation gate.

## What the harness can prove

- An installable Android 10+ project can own ExoPlayer and MediaSession in one service.
- Multiple controllers can observe one authoritative queue.
- MediaStore can be queried using synthetic app-owned audio without logging personal metadata.
- A rebuildable reconciliation core can remove stale rows, deduplicate, and process a 5,000-item logical corpus.
- A strict versioned snapshot can be written atomically, fail closed, and be published only as Paused after validation.
- AOSP API 29/31/34 instrumentation paths can run repeatably in CI.

## What it cannot prove by itself

- Samsung or Xiaomi OEM behavior, real notification permission presentation, task-removal policy, or head-unit behavior.
- Real process-loss rows that require controlled device execution and immutable traces.
- Product UI, navigation, Wireframe feasibility, final persistence technology, or final module boundaries.
- Numeric `T_PREVIOUS`; the harness exposes an unambiguous previous-item transport probe only.
- Android/QA/Product/Design/Privacy sign-off.

## Evidence rule

CI output is build and AOSP-emulator evidence. It becomes protocol evidence only when copied into an immutable, redacted result bundle matching `results/README.md`, with exact commit/build/device identity and required reviewer decisions. Physical Samsung and Xiaomi rows remain mandatory.
