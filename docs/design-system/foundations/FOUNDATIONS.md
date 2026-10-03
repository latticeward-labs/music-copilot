# Music Copilot Design Foundations — Issue #17 Phase 2

- **Product:** Music Copilot MVP v1.0
- **Issue:** [#17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Phase:** 2 — Visual Direction and Design Foundations
- **Date:** 3 October 2026
- **Status:** **PASS / COMPLETE at Design-Foundation level**
- **Execution branch:** `issue-17/phase-2-design-foundations`
- **Phase 1 baseline:** [Issue #17 Source Manifest](../ISSUE_17_SOURCE_MANIFEST.md) — PASS / SOURCE-FROZEN
- **Attached English source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Machine-readable tokens:** [TOKENS.json](TOKENS.json)
- **Visual review board:** [FOUNDATIONS_BOARD.png](FOUNDATIONS_BOARD.png)
- **Validation:** [FOUNDATIONS_VALIDATION.md](FOUNDATIONS_VALIDATION.md)

> Engineering truth lives in this Markdown file and `TOKENS.json`. `FOUNDATIONS_BOARD.png` is visual review evidence only and cannot override exact token/specification values.

## 1. Scope and authority

Phase 2 defines the shared visual language that all later Issue #17 high-fidelity artifacts must use. It does **not** create or approve final screen layouts.

### Source-locked constraints

The following are inherited from the attached English PRD/MVP, Accepted ADRs, approved User Flows/Golden Scenarios, completed Issue #15 Wireframes and final Issue #16 Software Architecture:

- Android 10+ native product.
- Jetpack Compose implementation target.
- Local authorized music; no backend/account/streaming scope expansion.
- Playback/queue/position remain MediaSessionService-owned; UI is an observer/command sender.
- Push-to-Talk Voice, bounded deterministic intents, no open conversation or wake word.
- Voice is an overlay/presentation state, not a separate navigation destination.
- Minimum interactive target: **48dp × 48dp**.
- Text must scale/reflow to **200%** without clipping critical actions.
- Sufficient contrast, TalkBack labels/order and non-color-only state communication.
- Driving Mode: one layer, immediate Exit, no typing, no nested/deep navigation, no long lists, limited motion.
- Driving Mode shows the essential current playback state plus fixed manual controls, Mic and Exit.
- Error/recovery must preserve reachable manual control.
- Android/system-owned dialogs and surfaces remain system handoffs, not invented app screens.
- Raw audio/transcript and prohibited personal-media metadata do not enter analytics/repository evidence.
- Liked Songs remains P2/removable.

### Phase-2 visual decisions

The source package intentionally does not prescribe brand colors, a final type scale, corner radii, elevation values or motion timing. Phase 2 therefore makes the following **reversible visual-design decisions** without changing product behavior:

- Visual direction name: **Night Signal**.
- Dark-first visual baseline for Issue #17 high-fidelity work.
- Neutral charcoal/navy surfaces with a bright cyan primary accent, violet secondary accent and distinct success/warning/error roles.
- Android-system sans-serif typography (Roboto/system fallback) to avoid a custom-font runtime dependency.
- 4dp spacing baseline with Compose-ready semantic spacing aliases.
- Rounded but restrained shape language; larger rounding is reserved for high-emphasis controls/sheets.
- Short, bounded transitions; Driving Mode animation duration is capped at 160ms for ordinary state transitions.
- Album artwork never drives the app theme dynamically in MVP v1.0.
- No user-facing theme selector is introduced by this design decision because one is not part of the approved MVP scope.

## 2. Visual direction — Night Signal

**Goal:** make the interface readable at a glance, calm during playback, and especially low-distraction in Driving Mode.

The visual language is:

- **dark and low-glare:** deep neutral backgrounds, restrained elevation and limited decorative contrast;
- **signal-focused:** cyan highlights indicate primary interactive emphasis; semantic success/warning/error colors remain distinct;
- **content-first:** album artwork and track metadata carry music identity; chrome stays visually quiet;
- **state-truthful:** Playing, Paused, No Track, Listening, Processing, Confirming, Executed, Failed and recovery states are expressed with text + shape/icon, never color alone;
- **motion-light:** transitions clarify state change but are not decorative.

## 3. Theme behavior

### 3.1 Approved baseline

Issue #17 high-fidelity artifacts use a **dark visual baseline**.

| Role | Token | Value |
|---|---|---|
| App background | `color.semantic.background` | `#090D12` |
| Base surface | `color.semantic.surface` | `#111720` |
| Variant surface | `color.semantic.surfaceVariant` | `#18212C` |
| Raised surface | `color.semantic.surfaceRaised` | `#202A36` |
| System bars | `color.semantic.systemBar` | `#090D12` |

Status/navigation bar icons should use the light-icon treatment against this dark baseline. This is a presentation decision only; it does not introduce a new navigation surface.

### 3.2 No implicit light-theme requirement

The English PRD/MVP do not require a light-theme feature or a theme switcher. Phase 2 therefore does not invent one. A later reviewed product/design change may add another theme mode, but it cannot be assumed by current Issue #17 artifacts.

## 4. Color system

### 4.1 Core semantic roles

| Role | Value | Usage |
|---|---|---|
| Background | `#090D12` | App canvas / system bars |
| Surface | `#111720` | Standard cards/containers |
| Surface Variant | `#18212C` | Secondary containers, controls |
| Surface Raised | `#202A36` | Elevated sheets/selected control surfaces |
| Primary | `#67D8FF` | Primary actions, focus/active emphasis |
| On Primary | `#031117` | Text/icon on Primary |
| Primary Container | `#143646` | Selected/active container |
| On Primary Container | `#C7F2FF` | Content on Primary Container |
| Secondary | `#A9A1FF` | Secondary highlight, voice/auxiliary emphasis |
| On Secondary | `#0E0B1B` | Content on Secondary |
| Success | `#76E0A3` | Successful execution/status |
| Warning | `#FFD166` | Caution/temporary attention |
| Error | `#FF7088` | Error/failure/destructive emphasis |
| Error Container | `#3A1720` | Error-state surface |
| Text Primary | `#F5F7FA` | Main text |
| Text Secondary | `#C8D0DA` | Supporting text |
| Text Muted | `#9AA5B3` | Lower-emphasis metadata |
| Border | `#2B3746` | Dividers/outlines |
| Focus Ring | `#A2EBFF` | Keyboard/accessibility focus indication |
| Disabled Content | `#7C8998` | Disabled labels/icons |
| Scrim | `#000000B8` | Modal/overlay scrim |

### 4.2 Contrast contract

At minimum:

- normal readable text uses a **4.5:1 or greater** contrast target;
- large text and essential graphical controls use **3:1 or greater**;
- disabled elements may use lower visual emphasis but must still expose correct semantics to TalkBack;
- Driving Mode intentionally targets stronger contrast than the minimum whenever practical.

Validated Phase-2 pairs are recorded in `TOKENS.json` and `FOUNDATIONS_VALIDATION.md`.

### 4.3 Color never acts alone

Selected, disabled, loading, Empty, error, permission and Voice states must include at least one additional cue: written state, icon/glyph, shape/border or explicit action label.

## 5. Typography

The implementation baseline uses Android system sans-serif / Roboto fallback and `sp` units. No bundled custom font is required.

| Role | Size | Line height | Weight | Use |
|---|---:|---:|---:|---|
| `display` | 32sp | 40sp | 700 | Rare high-emphasis onboarding/empty headline |
| `titleLarge` | 24sp | 32sp | 700 | Main screen/player title |
| `titleMedium` | 20sp | 28sp | 600 | Section/header |
| `bodyLarge` | 16sp | 24sp | 400 | Primary reading text |
| `bodyMedium` | 14sp | 20sp | 400 | Supporting text/metadata |
| `labelLarge` | 14sp | 20sp | 600 | Buttons/tabs/actions |
| `labelMedium` | 12sp | 16sp | 600 | Compact status/caption |
| `drivingTitle` | 28sp | 36sp | 700 | Track/status headline in Driving Mode |
| `drivingMeta` | 18sp | 26sp | 500 | Artist/supporting Driving Mode metadata |
| `drivingState` | 16sp | 24sp | 600 | Playing / Paused / No Track label |

Rules:

- Do not use fixed-height text containers for body/title copy.
- At 200% font scale, primary/recovery actions remain reachable; text may wrap and containers may grow vertically.
- Ellipsis is acceptable only for non-critical metadata when the owning Wireframe permits it; state labels and recovery actions must not disappear.
- Driving Mode does not use marquee/auto-scrolling text.

## 6. Spacing and grid

### 6.1 Spacing scale

The system uses a 4dp baseline:

`4, 8, 12, 16, 24, 32, 40, 48, 64dp`

Semantic aliases are defined in `TOKENS.json`.

### 6.2 Reference geometry

Issue #15's approved low-fidelity reference remains:

- reference viewport: **360 × 800dp**;
- app bar: **56dp**;
- bottom navigation: **64dp when present**;
- Android system chrome remains system-owned.

High-fidelity layout must remain responsive and cannot assume a fixed physical pixel density.

### 6.3 Grid

- compact-phone reference: **4 columns**;
- baseline gutter: **12dp**;
- ordinary horizontal screen margin: **16dp**;
- Driving Mode horizontal margin: **20dp** where the approved wireframe structure permits it;
- baseline vertical rhythm: **4dp**.

These values style the approved Wireframe structure; they do not authorize moving or adding navigation destinations.

## 7. Shape / radius

| Token | Radius |
|---|---:|
| `none` | 0dp |
| `xs` | 4dp |
| `sm` | 8dp |
| `md` | 12dp |
| `lg` | 16dp |
| `xl` | 24dp |
| `pill` | 999dp |

Default cards/search fields use 12–16dp radii. Large circular/pill shapes are reserved for prominent controls such as microphone actions or compact pills, not decorative repetition.

## 8. Elevation

Elevation is intentionally shallow in the dark theme:

| Level | Value | Use |
|---|---:|---|
| `level0` | 0dp | Canvas/flat sections |
| `level1` | 1dp | Quiet control/card separation |
| `level2` | 3dp | Raised control/card |
| `level3` | 6dp | Overlay/sheet |
| `level4` | 12dp | Highest temporary modal layer |

Elevation never substitutes for an explicit focus/state cue.

## 9. Iconography

- Default icon visual size: **24dp**.
- Player high-emphasis icons: **32dp**.
- Driving Mode primary-control icon: **36dp**.
- Visual stroke target: approximately **2dp** for outline icons.
- Hit target is defined independently and never shrinks to the visual glyph size.
- Semantic icons require a TalkBack label unless the icon is explicitly decorative.
- Play and Pause use distinct glyph + state/action label semantics.
- Use a consistent Android/Material-compatible rounded icon geometry or local vector equivalent; do not mix unrelated icon families in the same surface.

## 10. Artwork treatment

- Album/track artwork uses a **1:1 aspect ratio**.
- Default high-fidelity artwork corner radius: **12dp**.
- Full Player may scale artwork responsively within the approved Wireframe hierarchy.
- Driving Mode artwork remains subordinate to state/title/manual controls and may reduce in size under 200% text/reflow.
- Missing artwork uses a neutral local placeholder with a music glyph and accessible label where needed.
- No blur-heavy background, flashing treatment or metadata-derived dynamic theme is introduced in MVP v1.0.
- Personal media artwork must not be copied into analytics/evidence payloads.

## 11. Motion

| Motion token | Duration | Purpose |
|---|---:|---|
| `instant` | 0ms | Immediate state replacement when motion adds no value |
| `fast` | 100ms | Press/selection acknowledgement |
| `standard` | 160ms | Normal control/state transition |
| `deliberate` | 220ms | Overlay/sheet entrance/exit |
| `maxNonCritical` | 300ms | Absolute cap for non-critical decorative transition |

Rules:

- **Driving Mode ordinary transition cap: 160ms**.
- No parallax, bouncing navigation, decorative looping animation or marquee in Driving Mode.
- Voice Listening may use a restrained pulse only if the written `Listening` state remains the authoritative cue.
- When system animations are disabled/reduced, state meaning and action availability remain complete.
- Motion never delays authoritative playback/Voice acknowledgement.

## 12. Interaction states

### Default
Use normal semantic surface/content roles.

### Pressed
Use `state.pressedOverlay = #FFFFFF1A` over the current container, duration `motion.fast`.

### Focused
Use a **2dp `color.semantic.focusRing`** outline with a 2dp visual offset where geometry allows. Focus order follows task order, not drawing order.

### Selected
Use `primaryContainer` + `onPrimaryContainer`, and include a label/check/state indicator so selection is not color-only.

### Disabled
Use `disabledContainer` + `disabledContent`; action semantics report disabled and, when behavior requires, the visible screen provides the reason/recovery.

### Error
Use `error` / `errorContainer` plus written error/status and a recovery action where available. Error red alone is never sufficient.

### Loading
Loading uses written state plus bounded progress/indicator. It must not look identical to disabled.

## 13. Accessibility foundation

Every later high-fidelity screen/component must satisfy:

1. **Touch:** all interactive targets ≥48×48dp.
2. **Driving:** primary fixed Driving Mode controls ≥64dp targets; Mic target ≥72dp; Exit target ≥56dp where geometry permits.
3. **TalkBack:** content descriptions/semantics and logical reading/focus order are documented.
4. **200% text:** reflow without clipping primary/recovery actions.
5. **Contrast:** readable text/control contrast targets validated against semantic background roles.
6. **Non-color-only:** text + icon/shape/state cue.
7. **Focus:** explicit visible focus treatment for keyboard/switch/accessibility navigation.
8. **Motion:** state meaning survives reduced/disabled animation.
9. **System boundary:** Android/system-owned surfaces are announced/described as handoffs, not recreated as app UI.

Actual runtime Compose semantics, focus traversal and device-specific reflow remain downstream implementation QA proof under `PH10-MIN-001`; this phase supplies the design specification.

## 14. Driving Mode foundation — non-negotiable

The approved WF-07 behavior controls.

### Fixed layer

Driving Mode remains a single stable layer with:

- current state: **Playing / Paused / No Track**;
- title;
- artist;
- artwork when available;
- four fixed primary playback controls:
  - Previous
  - Play / Pause
  - Next
  - Seek Back
- microphone control;
- immediate Exit.

### Geometry

- each primary playback target: **≥64dp**;
- Mic target: **≥72dp**;
- Exit target: **≥56dp**;
- no interactive target below **48dp**.

### Forbidden in active Driving Mode

- typing;
- nested/deep navigation;
- long result lists;
- permission prompts;
- Settings/consent surfaces;
- multi-step menus;
- distracting animation;
- auto-entering Driving Mode;
- auto-resuming playback solely because the route reconnects.

### Voice interaction

Mic opens the WF-08 Voice overlay over the initiating surface. It does not navigate to a new destination. Failure/Cancel returns to the initiating manual controls.

## 15. Voice-state visual foundation

WF-08 states use the same system but each includes a written state label:

- **Listening:** Primary/Cyan emphasis + listening label; capture/duck state is not represented only by animation.
- **Processing:** neutral/secondary progress treatment + processing label.
- **Confirming:** one or two large immutable candidate options plus Neither/Cancel; never a keyboard or long list.
- **Executed:** success role + short acknowledgement; playback truth still comes from service state.
- **Failed / Retry:** error role + one explicit Retry when allowed; manual fallback remains visible/reachable.

No raw audio/transcript is persisted or embedded in design evidence.

## 16. Compose-ready mapping

Token names are intentionally semantic so they can map into a Compose theme layer without embedding product behavior:

- `color.semantic.*` → Compose color scheme / extended Music Copilot colors.
- `typography.*` → Compose `Typography` or an app-specific extension for Driving roles.
- `spacing.*` → immutable `MusicSpacing`.
- `radius.*` → `Shapes` / app-specific shape tokens.
- `elevation.*` → component tonal/shadow elevation values.
- `motion.*` → shared animation specs.
- `size.*` → hit-target/icon/layout constants.

Material 3 is already used by the repository's Android validation spike, so the token naming is compatible with Material 3/Compose mapping. This does not make Material 3 a new product capability.

## 17. Phase 2 exit criteria

- [x] Dark-theme behavior defined without inventing a theme-setting feature.
- [x] Semantic color roles and contrast contract defined.
- [x] Typography scale defined in sp with 200% reflow rules.
- [x] Spacing/grid/layout reference defined in dp.
- [x] Radius and elevation scales defined.
- [x] Iconography rules defined.
- [x] Artwork treatment defined.
- [x] Motion durations/limits defined.
- [x] Default/Pressed/Focused/Selected/Disabled/Error/Loading state rules defined.
- [x] Accessibility foundation defined.
- [x] Driving Mode non-negotiables and control minimums defined.
- [x] Voice-state visual foundation defined.
- [x] Exact machine-readable values committed in `TOKENS.json`.
- [x] Repository visual review board produced as `FOUNDATIONS_BOARD.png`.
- [x] No final high-fidelity screen is claimed complete by this phase.

**Phase 2 decision: PASS / COMPLETE at Design-Foundation level.**
