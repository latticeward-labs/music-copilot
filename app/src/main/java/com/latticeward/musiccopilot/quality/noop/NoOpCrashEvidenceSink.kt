package com.latticeward.musiccopilot.quality.noop

import com.latticeward.musiccopilot.quality.api.SafeCrashCategory
import com.latticeward.musiccopilot.quality.api.SafeCrashEvidenceSink

/** P07: external and persistent crash collection remain OFF by default. */
class NoOpCrashEvidenceSink : SafeCrashEvidenceSink {
    override fun tryRecord(category: SafeCrashCategory): Boolean = false
}
