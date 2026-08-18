# Cluster E - Source Authority and Controlled Resolutions

**Scope:** UF-11 Interruption/Audio Route; UF-12 Settings/Privacy  
**Date:** 18 August 2026  
**Tracking:** Issue #3; implementation Issues #8/#12; release QA Issue #13; architecture Issue #16  
**Status:** Controlled source baseline for Cluster E review

## Authority order

1. English records inside the supplied `MSM2.rar` archive.
2. Accepted ADRs and current GitHub product records where they do not conflict with the English archive.
3. Derived Cluster E flow, state, traceability, executable reference-contract, and review records in this change.

The Persian PRD/MVP attachments and translated snapshots are excluded from decision authority. The reviewed archive SHA-256 is `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## English records used

- `Music_Copilot_PRD_v1_EN_Clarified.pdf`
- `Music_Copilot_MVP_v1_EN.pdf`
- `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`
- `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`
- ADR-0002 Service-Owned Media3 Playback
- ADR-0003 Local-First Data Boundaries
- ADR-0004 Replaceable Voice Provider
- ADR-0006 No Backend, Account, or Streaming

## Fixed UF-11 contract

- Playback Service/MediaSession remains the sole playback authority through calls, focus, routes, corrupt items, and UI recreation.
- A call or transient focus loss pauses or ducks according to one typed policy; Voice capture/confirmation is cancelled before it can execute a stale command.
- Resume is conditional only: playback was playing before interruption, focus is restored, the same valid route remains, and no manual pause occurred.
- Permanent focus loss never auto-resumes.
- Wired-headset or Bluetooth disconnect pauses immediately and prevents unintended phone-speaker playback.
- Reconnect never auto-resumes. The user must explicitly resume.
- Repeated, reordered, or duplicate callbacks are idempotent and cannot create duplicate playback.
- A corrupt item is skipped once in a controlled way; if no playable item remains, the queue ends/pauses with a concise error.
- UI, notification, lock screen, Driving Mode, and system controls render the refreshed service snapshot rather than maintaining a second player state.

## Fixed UF-12 contract

- Settings is a non-driving destination for Bluetooth suggestion setup, permission/provider recovery, privacy explanation, consent, and data controls.
- A selected Bluetooth device enables only a P1 suggestion; connection never opens Driving Mode automatically.
- Returning from Android Settings rechecks the relevant permission/provider condition.
- Cloud speech remains disabled until separate consent plus provider/destination, purpose, and retention disclosure are complete; decline keeps manual controls.
- Clearing requires explicit confirmation and affects only selected app-owned short-history/diagnostic categories.
- Likes, settings, consent records, library projection, queue/live playback, and user media files are never silently erased by history/diagnostic clearing.
- If an approved external diagnostic processor stores eligible data, its deletion action must be represented and failures remain visible/retryable; local success must not falsely imply remote deletion.
- Exact retention periods are not invented in UX or code. Collection requiring a period remains disabled until an approved, positive policy is supplied by Privacy/#16.
- Telemetry is allowlist-only and non-blocking; prohibited personal content is rejected before any adapter.

## Controlled resolutions

| Topic | Resolution | Downstream boundary |
|---|---|---|
| Transient focus/call recovery | Record whether playback was playing; pause/cancel Voice; resume only after the four D-018 guards pass. | OEM callback behavior and final policy adoption: #8/#13/#16. |
| Duck event | A typed duck-capable focus event may duck active playback; call and route-loss paths pause. Any terminal transition clears ducking. | Exact Android callback integration: #8/#16. |
| Route identity | A disconnect invalidates the pre-interruption route generation and clears resume eligibility. Reconnect is safe-paused only. | Physical Pixel/Samsung/Xiaomi and 3+ head units: #13. |
| Corrupt item | One controlled skip to an already validated next item; otherwise end/paused queue. | Media implementation remains #7/#8 and QA #13. |
| Clear scope | Only `SHORT_HISTORY`, `LOCAL_DIAGNOSTICS`, and approved `PROVIDER_DIAGNOSTICS` are selectable. | Production repositories/adapters: #12/#16. |
| Retention | Named categories require approved positive durations; missing/invalid values disable collection. | Privacy approval and enforceable storage: TS-10/#12/#16. |
| Consent | Separate, revocable, mode-specific; revocation disables future cloud STT and routes deletion per approved provider contract. | Legal/provider copy and implementation: #12/#16. |

## Truth boundary

Host/AOSP reference tests may close deterministic policy, idempotency, protected-category, allowlist, and fail-closed retention contracts. They cannot establish OEM telephony/focus callback order, real route leakage, a legal processor statement, remote provider deletion, or vehicle behavior. Those claims require Issues #8/#12/#13/#16 and are not fabricated here.
