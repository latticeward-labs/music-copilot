# WF-10_SETTINGS_BLUETOOTH_UPDATED — Settings: Bluetooth / Updated

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-10_SETTINGS_BLUETOOTH_UPDATED.svg](WF-10_SETTINGS_BLUETOOTH_UPDATED.svg)  
**Package:** `WF-10`  
**Priority:** **P1 / Removable preference**  
**Ownership:** App + platform/provider boundary

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-12 |
| Canonical State IDs | `CE-20, CE-16, CE-17` |
| Golden Scenario | — (not on GS-01..05 path) |
| Screen owner | UF-12 · Settings/Privacy |
| Recovery owner | UF-12 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Save or remove an eligible Bluetooth suggestion preference without auto-entering Driving Mode.
- **Primary action:** Save selection; Remove is explicit.
- **Visible source of truth:** Eligible platform devices plus locally stored selected preference.
- **Recovery:** Permission/unavailable routes to explicit outside-driving recovery; failure is textual.
- **Persistence/recreation:** Selected identity stays local and is excluded from telemetry.

## Navigation

- **Entry:** P1 Bluetooth setup selected outside Driving Mode.
- **Exit:** Acknowledged update → Settings; failure retains previous value.
- **Back:** Cancels unsaved changes.

## Accessibility

- **TalkBack order:** P1 label → Bluetooth heading → selected device → Save/Remove → result.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Connection alone never launches Driving Mode; platform identity remains protected.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
