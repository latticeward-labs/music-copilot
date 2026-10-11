# HF Frame Review — WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY

**Issue/phase:** #17 / 5 · **Source:** WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.png · **SHA-256:** ca841f97fefd04ccf020ea0d9e19ea013a1e5585d781766b1a755970f897479c
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CB-19, CE-12, CE-13, CE-14 · **UF:** UF-05 / UF-06 / UF-11 · **GS:** GS-02 / GS-03 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Now playing"
- "Queue"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "01:26"
- "03:42"
- "!  Track unavailable"
- "Media is untouched."
- "Continue with next"
- "Return to Library"

## Behavioral boundary

- **Entry:** Prepare/advance/selection meets corrupt or inaccessible media.
- **Primary action:** Continue with bounded next-item validation; Library is always reachable.
- **Exit:** Valid next → Playing/Paused; none → terminal Paused/Completed or Library.
- **Back:** Returns to Library/manual selection; no file deletion.
- **Truth:** Typed service failure plus MediaStore/index reconciliation.
- **Recovery:** One bounded forward skip; permission/reindex recovery when appropriate.
- **A11y / TalkBack:** Unavailable heading → concise explanation → Continue → Library.
- **System handoff:** No system dialog unless a separately verified permission recovery is needed.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
