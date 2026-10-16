# Issue #5 — Phase 6: Granted, Denied and Settings-only recovery

**Basis:** User-uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md`, SHA-256 `32f8095cac6c4f85a9654bd006ca73e6c27f0db3644a5bd4adef847cf738276f`; MSM2 archive SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; accepted UF-02, GS-01, WF-03/HF-03 Granted/Denied/Permanently Denied, Issue #16 architecture and Issue #17 Handoff. Branch `feature/5-onboarding-permissions-recovery`, 2026-10-16. Issue #5 OPEN. No changes to `main`.

## Implemented exact design outcomes

| WF-03 frame | Source-truth entry | Actual Compose copy and recovery | Native handoff |
|---|---|---|---|
| `WF-03_PERMISSIONS_GRANTED` | Fresh `checkSelfPermission` after Android ActivityResult | **Access granted** / **Continue to the blocked action.** / Continue, Back and Not now | UI shows BEFORE scan. `PermissionViewModel.takeOneGrantedScan()` consumes authorized `StartScan` once on explicit Continue; Activity fresh OS recheck and repository Mutex/MediaStore grant checks protect actual scan. Back/Not now does not scan. |
| `WF-03_PERMISSIONS_DENIED` | Fresh denial with request still permitted or uncertain | **Access denied** / **Manual controls remain available.** / Try again and Not now (Back too) | Try again goes to approved media rationale; **never** automatically launches another OS dialog. Not now returns manual Library, no false Ready or Empty claim. |
| `WF-03_PERMISSIONS_PERMANENTLY_DENIED` | Repeated denied OS requests and rationale unavailable (Settings-only conservative presentation) | **Open Android Settings** / **Permission must be changed explicitly.** / Open Settings and Not now; native handoff/fresh-truth annotations | Only explicit button launches `ACTION_APPLICATION_DETAILS_SETTINGS` for this app; no Compose fake Settings surface, no forced repeat. On return a fresh permission snapshot updates UI without scanning. Full P07 three-way blocked-action Settings return, revocation reconciliation and lifecycle validation are **not** claimed here. |

## Important Android/OEM truth boundary

`shouldShowRequestPermissionRationale=false` alone **never** means permanent denial. `MediaOutcomePolicy` conservatively shows Requestable after the first denied prompt, even if rationale false. Only repeated explicit requests with `requestedBefore=true` AND no rationale produce a **Settings-only recovery UX**, not a privileged Android-provided permanent-denied Boolean. This avoids labeling fresh install as permanently blocked and never produces automated request loops. The Activity owns `ActivityResultContracts.RequestPermission`; pure domain policy and ViewModel do not request OS permissions. `SavedStateHandle` retains minimal non-sensitive request count only (no OS grants, files, MediaStore IDs or transcript).

## Integration and negative checks

- Three UI branches use existing `Destination.PermissionRecovery`, not new unapproved destinations.
- Exact approved English copy in Android string resources, Night Signal theme Material colors, semantic heading and polite live region, scrolling/safe insets, **48dp** minimum controls. Screenshots from emulator required for visual claims; code alone cannot prove TalkBack focus on all OEMs.
- Manual exits and Back available in all states; while native dialog pending, rationale does not enable duplicate requests. On user grant no scan until explicit Continue, then only one scan with fresh OS grant.
- No account/identity/cloud/voice/notification, no original media paths/titles in logs or telemetry. Media3 service retains player/queue; voice provider is untouched.
- `Open Settings` is an actual Android-owned Intent only after explicit tap, but **P07** is still responsible for three-way return/revocation handling and comprehensive lifecycle/focus audit; **P08** for full library readiness. No unreviewed Issue #5 AC checkbox was changed.

## Automated and real native evidence

- [P06 current Kotlin, JUnit/Lint/Detekt/Spotless/APK #38894487796](https://github.com/music-copilot-organization/music-copilot/actions/runs/38894487796): **SUCCESS** on implementation `9274854c55957d04cb90b221ff757e1c5b9a82d6` (8 dedicated P06 tests and repository regressions).
- [P05 regression JVM #38894487783](https://github.com/music-copilot-organization/music-copilot/actions/runs/38894487783): **SUCCESS** same implementation. P05 original native test workflow updated to assert granted result screen + explicit Continue before scanning.
- [P06 API29/API33 native three-branch test #38894791348](https://github.com/music-copilot-organization/music-copilot/actions/runs/38894791348): **PENDING actual conclusion**. This uses the same real Android permission controller and shell-installed synthetic OS `user-fixed` permission flag for a reproducible Settings-only state; never fakes an app grant. It captures screenshots, Android OS permission snapshots, APK, verifies native Settings handoff/Back and blocked scan policy.
- **Older unsuccessful runs are retained**, not relabeled green: [#38894019456](https://github.com/music-copilot-organization/music-copilot/actions/runs/38894019456) had a CI Bash-script environment error (`OUT` not retained across separate runner shell invocations); [#38894019279](https://github.com/music-copilot-organization/music-copilot/actions/runs/38894019279) was blocked by the `Uri.parse` KTX lint finding, corrected in `9274854c...`. Neither counts as native P06 PASS.

**Current decision:** P06 implementation and JVM gates PASS; **overall P06 native acceptance PENDING** until API29 and API33 jobs complete successfully, artifacts linked, and Issue #5 updated. Physical OEM tests and formal TalkBack focus audit belong P12–P13. Original Issue #5 13 final ACs remain unchecked; P07–P14 untouched.
