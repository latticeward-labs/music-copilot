# PH10-MIN-001 — Implementation-ready Compose Accessibility Handoff

**Issue:** #17 Phase 7 · **Source:** Issue #16 PH10-MIN-001 · **Status:** SPECIFIED / RUNTIME OPEN (do not close the original finding)

## Expected Jetpack Compose structure

For every one of the 45 canonical app-visible states, follow the linked individual accessibility disposition in ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md. Each has explicit order, TalkBack labels for existing actions, non-color status, 100/150/200% text scaling expectations and focus return.

1. Use the app heading semantics on the single visible title. No duplicate decorative artwork label when title/artist already spoken.
2. Existing Material3 buttons should expose appropriate role/label and a minimum 48×48dp touch region, INCLUDING invisible hit slop; Driving control >=64dp, Mic >=72dp, Exit >=56dp. Do not claim visual icon size is hit-target size.
3. Compose stateDescription for service-observed Playing, Paused, Completed, No Track, disabled action, Permission Denied, Scanning, Voice Listening/Processing/Confirming/Executed/Failed. Convey status with text and shape/icon, never color only.
4. For a changing error use polite liveRegion only when the message settles. Never announce continuous scan/progress/position updates every frame. Do not repeat one-sentence TTS through a duplicate TalkBack live announcement.
5. Voice is an overlay on the initiating Player/Driving, not a new destination. Provide a distinct paneTitle, accessible one/two immutable candidate option labels sourced from active Confirming.options, and separate Neither/Cancel. The one confirmation and one Retry budgets cannot be reset by UI recomposition.
6. Use isTraversalGroup and traversalIndex only to fix demonstrably incorrect semantics traversal; avoid hardcoded absolute index that diverges at 200% reflow. Prefer natural semantic structure.
7. When dismissing Voice restore focus to initiating Mic or a still-existent manual control. When leaving Driving, restore last valid origin; fallback Home/Library. Do not steal focus from native Android dialogs.
8. With fontScale 1.0/1.5/2.0, do not truncate state/error/disclosure/control labels. Use Text with sp, responsive layout and LazyColumn/verticalScroll for content. Keep Exit/Cancel/Manual/Retry visible or immediately reachable; avoid hardcoded text-card heights. Artwork shrinks first.
9. System/window insets are respected. A physical TalkBack swipe/keyboard focus path must match the contract rather than merely drawing correctly.

## Reference code (illustrative, not compiled or run)

    @Composable
    fun AccessiblePlaybackAction(
        label: String,
        enabled: Boolean,
        minTouch: Dp,
        onClick: () -> Unit
    ) {
        Button(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.defaultMinSize(
                minWidth = minTouch,
                minHeight = minTouch
            )
        ) {
            Text(label, softWrap = true)
        }
    }

Use Material controls' built-in semantics first; set stateDescription and supplemental labels only when needed. Avoid redundant contentDescription on a Text label that TalkBack already reads. Do not clearAndSetSemantics across independent actionable children.

    Text(
        text = playbackStateLabel,
        modifier = Modifier.semantics {
            stateDescription = playbackStateLabel
            liveRegion = LiveRegionMode.Polite
        }
    )

This status example is a pattern, not a license to announce every playback tick; deduplicate transitions at the state owner. Pane/focus restoration must use current supported Compose APIs and be exercised on Android 10/12/14+.

## Required tests and evidence

- On each of the 45 states, Compose UI-test semantics snapshots for heading, stateDescription, action labels, selected/disabled status and touch hitbox. Distinguish boundsInRoot from actual minimum touch-target interaction.
- 100/150/200% fontScale screenshots and manual traversal at narrow phone widths; no clipping, hidden action or overlapped controls.
- On Voice: initial focus, one/two option traversal, Neither/Cancel order, restoring origin after Cancel, ignoring stale generation; TTS failure does not change Executed.
- On Driving: initial Exit reachability, No Track invalid controls, manual actions and 200% scaling without nested menu.
- Native system surface entry/return restores app focus and checks actual permission/service state.

**PH10-MIN-001 remains OPEN until real implementation and test evidence is reviewed.** A spec pass in Issue #17 never substitutes for it.

## Official Android references

- https://developer.android.com/develop/ui/compose/accessibility/semantics
- https://developer.android.com/develop/ui/compose/accessibility/traversal
- https://developer.android.com/develop/ui/compose/touch-input/focus
- https://developer.android.com/develop/ui/compose/touch-input/focus/request-focus
- https://developer.android.com/develop/ui/compose/accessibility/scalable-content
