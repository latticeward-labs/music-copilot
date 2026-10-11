# Issue #17 · Phase 8 — Executable Prototype Validation

**Date:** 11 October 2026  
**Scope:** repository-native static interactive prototype / five Golden Scenarios / required recovery branches.  
**Source archive SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## Verified static route/asset gates

| Gate | Verified | Result |
|---|---:|---|
| GS-01..GS-05 approved primary route connectivity | 5/5 | PASS |
| GS-listed recovery branches reachable | 35/35 (6 + 5 + 4 + 16 + 4) | PASS |
| Source-linked graph nodes | 61 | PASS |
| Real app-owned screen nodes using approved PNGs | 53 | PASS |
| Distinct Android/System-only boundary annotations, no mock screen | 8 | PASS |
| Approved-app-control image hotspots | 73 | PASS |
| Source-referenced UI images with valid 1080×2400 PNG headers | all | PASS |
| Missing, duplicate, unassigned route targets | 0 | PASS |
| Ineligible second confirmation/third Retry | 0 | PASS |
| Automatic playback on route reconnect | 0 | PASS |
| Deep Driving navigation to Queue/Search/Settings | 0 | PASS |

The 53 app nodes are **prototype interaction nodes**, not 53 newly introduced canonical app-visible State IDs. Multiple demo branches reuse the same canonical State ID. The original 126-state register (45 dedicated + 61 variants + 20 System-only) is not modified.

## Actual browser smoke run

Headless Chromium successfully clicked through eight browser-level review checks:

1. GS-01 onboarding/permission-system-boundary/scan/Ready.
2. GS-02 local search/result/selection/Playback Service acknowledgment.
3. GS-03 system-owned background/controller/MediaSession/return.
4. GS-04 explicit Driving/Mic/Listening/Processing/acknowledged Executed.
5. GS-04 STT attempt one timeout → one Retry → attempt two terminal → Manual, no third Retry.
6. GS-04 two-option confirmation and TTS error after acknowledged success.
7. GS-05 OS route disconnect → Safe Paused → OS reconnect → **explicit manual Play** → service acknowledgment.
8. Mobile-width 390×844 and interactive hotspot toggle; sanitized synthetic screenshot captured.

**Result:** 8/8 PASS, 0 browser test failures. Evidence: [PHASE_8_BROWSER_VALIDATION.json](PHASE_8_BROWSER_VALIDATION.json), [PHASE_8_BROWSER_REVIEW.png](PHASE_8_BROWSER_REVIEW.png). Passing GitHub Actions browser run: [38151567956](https://github.com/latticeward-labs/music-copilot/actions/runs/38151567956). Subsequent simultaneous workflow runs also passed the code/browser test steps; some failed only when committing identical generated evidence concurrently. The temporary one-time workflow was therefore removed after evidence capture and the final PR receives the ordinary repository architecture CI.

## Implementation and approval boundary

- No Media3 player, actual STT/TTS, microphone, permissions, Android UI or backend is executed by this static prototype.
- Platform-only states **never** render fake Android dialogs, Settings, notification, lock-screen or media key screens.
- Reviewer-only synthetic outcomes are outside the phone visual and cannot be interpreted as new app controls.
- No real personal song titles, raw audio, STT transcripts, precise location, devices or Voice candidate tokens are gathered or transmitted.
- Runtime Compose TalkBack/focus/200% text reflow (PH10-MIN-001), Android 10/12/14+, vehicle/head-unit testing, provider/legal consent and independent Product/Design/Android/QA/Accessibility/Privacy sign-offs remain pending.
- This is a PASS at the **clickable prototype and traceability reviewability** gate, not a claim that the entire Issue #17 is accepted.

Machine-level details: [PHASE_8_VALIDATION.json](PHASE_8_VALIDATION.json). Reproduction: [README.md](README.md), [validate.cjs](validate.cjs), [browser-smoke.cjs](browser-smoke.cjs).
