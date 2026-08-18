# Cluster C - Source Authority and Scope Boundary

**Cluster:** C - UF-07 Driving Mode  
**Date:** 18 August 2026  
**Tracking:** GitHub Issue #3  
**Controlled status:** PASS - authority and scope boundary fixed

## Authority order

1. The English files in the supplied `MSM2.rar` are the primary source baseline.
2. The current GitHub English mirrors and accepted ADR register provide current repository status and durable traceability.
3. Existing completed UF-04/UF-05/UF-06 records provide only the verified handoff into UF-07; they cannot expand Cluster C scope.
4. If a derived artifact conflicts with the English PRD or MVP, the English PRD/MVP win.

## English source register

| Source | Cluster C authority |
|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | FR-04 P0 Driving Mode content; FR-07 P1 manual/Bluetooth entry; safety rules; analytics contract |
| `Music_Copilot_MVP_v1_EN.pdf` | US-04; Playing/Paused/No Track; immediate exit; one-layer Driving rules; P1 suggestion-only boundary |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | S-07; D-011, D-012, D-021; Constraints 7-8; UF-07 catalog and traceability; telemetry allowlist |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | UI observes service-owned playback; Driving Mode presentation boundary; Issue #15/#16 downstream decisions |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | Compose/MediaSession responsibilities, privacy enforcement, accessibility and quality budgets |
| ADR-0001 | Native Android 10+ and Android lifecycle/system integration |
| ADR-0002 | Playback Service/MediaSession owns current item, queue, position, and command result |
| ADR-0003 | Local-first data boundaries and rebuildable local state |
| ADR-0004 | Replaceable voice provider boundary; microphone is a downstream UF-08 handoff here |
| ADR-0005 | Restricted deterministic intent mapping; no conversational or wake-word scope |
| ADR-0006 | No backend, account, streaming, catalog, sync, upload, or download |

## Current GitHub records used

- `docs/PRD.md` and `docs/MVP.md` as concise English repository mirrors.
- `docs/USER_FLOWS.md` and `docs/user-flows/PHASE_0_FOUNDATION.md` for the active Cluster C gate.
- `docs/adr/README.md` for the current Accepted status of ADR-0001 through ADR-0006.
- UF-05 and UF-06 only for verified Playing/Paused/No Track and return-surface handoffs.
- Issue #3 for the full User Flow acceptance boundary.

## Explicitly excluded as authority

Persian/Farsi PRD, MVP, translated summaries, older snapshots, design inspiration, and external mockups are not decision authorities for this Cluster C record. Figma is not a source, dependency, or exit criterion for the User Flow stage.

## Scope boundary

Cluster C approves the implementation-independent behavior of entering, using, and immediately leaving the one-layer Driving Mode. It does not approve final layout, visual styling, exact Android APIs, Bluetooth receiver/permission mechanics, final navigation implementation, ViewModel/use-case structure, or application code. Those remain for Issues #15, #16, and #9.

Cluster C does not execute voice recognition. The microphone action is a controlled handoff to UF-08/UF-10. Calls, Audio Focus, route loss, and speaker-leak protection hand off to UF-11.

## Authority decision

**PASS.** The UF-07 artifacts may be reviewed and completed against the English `MSM2.rar` baseline plus current GitHub status without using the Persian attachments.

