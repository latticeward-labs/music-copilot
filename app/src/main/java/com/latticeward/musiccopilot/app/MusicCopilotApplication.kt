package com.latticeward.musiccopilot.app

import android.app.Application

/**
 * Android process entry point.
 *
 * Phase 2 intentionally creates no Media3 Player, Voice owner, repository,
 * or background work here. Lifetime-aware composition is introduced in Phase 5.
 */
class MusicCopilotApplication : Application()
