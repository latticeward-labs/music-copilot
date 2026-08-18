# TS-04 - Conditional Audio Focus and Call Recovery

**Question:** Can one deterministic policy permit logical recovery without surprise playback?  
**Result:** **PASS at executable reference-policy level; physical/OEM matrix remains #8/#13**

## Executable contract

- Normalize system callbacks before applying policy.
- Snapshot prior Playing only once per active interruption.
- Cancel Voice and clear ducking on call/pause paths.
- Resume only with: prior Playing, focus restored, unchanged valid route generation, and no manual Pause.
- Permanent loss, route change, manual Pause, already-Paused entry, and stale/duplicate callback all remain Paused.
- Effects are idempotent and service-owned.

## Automated rows

| Row | Expected |
|---|---|
| transient loss while Playing -> gain | exactly one Pause then one guarded Resume |
| transient loss while Paused -> gain | no Resume |
| manual Pause during interruption -> gain | no Resume |
| permanent loss -> gain | no Resume |
| route generation changes before gain | no Resume |
| duplicate loss/gain | no duplicate effects |
| active Voice when call begins | Voice cancelled before playback recovery |

## Non-claim

Host tests prove policy determinism, not Android/OEM callback availability/order. Pixel/stock, Samsung, Xiaomi, real calls, and vehicle traces remain mandatory downstream evidence.
