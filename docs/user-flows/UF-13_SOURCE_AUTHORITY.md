# UF-13 Source Authority

**Decision date:** 19 August 2026  
**Language rule:** Only English files in the official `MSM2.rar` package govern UF-13. Persian translations are excluded from normative decisions.

## Authority order

1. `Music_Copilot_PRD_v1_EN_Clarified.pdf`
   - FR-09: like a track and play it from a simple Liked Songs screen (P2).
   - v1 includes simple Liked Songs; advanced playlists/social, backend, account, sync, streaming are excluded.
2. `Music_Copilot_MVP_v1_EN.pdf`
   - Simple Like is P2 and included only if Sprint capacity remains.
   - Liked Songs has Empty and Ready states; User Data is local without account/backend/sync.
3. `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
   - S-09, D-027, UF-13 catalog row, FR-09 traceability, and optional-scope sequencing.
4. Accepted architectural constraints in the same English package:
   - ADR-0002 for service-owned playback.
   - ADR-0003 for local app-owned Likes, MediaStore authority, reconciliation, migration, and privacy boundaries.
   - ADR-0006 for no backend/account/sync/streaming.
5. Current GitHub Issue #3 and downstream Issues #6/#15/#16/#17 for repository governance and handoffs. GitHub may clarify current project state but cannot expand the locked PRD/MVP scope.

## Controlled decisions

- Keep UF-13 visibly P2 and independently removable.
- Keep the surface simple: Empty/Ready, play local liked tracks, like/unlike.
- Treat Like as persistent app-owned local intent while MediaStore/access remains authoritative for playability.
- Reconcile missing/inaccessible media safely and never present it as playable.
- Hand playback to the service/controller; UI does not own the queue/player.
- Add no telemetry event and no network/account behavior.

## Non-authoritative or unresolved

- Persian PRD/MVP files.
- Figma styling/prototypes.
- Assumed sort order, database schema, retention duration, or migration version not stated by the sources.
- Production implementation claims or physical-device behavior not evidenced in GitHub.
