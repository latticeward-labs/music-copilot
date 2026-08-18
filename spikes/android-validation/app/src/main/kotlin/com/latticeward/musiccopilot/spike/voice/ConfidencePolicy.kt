package com.latticeward.musiccopilot.spike.voice

/**
 * Conservative, spike-only candidate values for TS-09. Issue #16 owns final adoption.
 * Remote configuration may tune only directThreshold within the fixed range.
 */
data class ConfidencePolicy(
    val directThreshold: Double,
    val ambiguityThreshold: Double = AMBIGUITY_THRESHOLD,
) {
    init {
        require(directThreshold in MIN_DIRECT_THRESHOLD..MAX_DIRECT_THRESHOLD)
        require(ambiguityThreshold in 0.0..<directThreshold)
    }

    companion object {
        const val BUILD_DEFAULT_DIRECT_THRESHOLD = 0.90
        const val MIN_DIRECT_THRESHOLD = 0.80
        const val MAX_DIRECT_THRESHOLD = 0.98
        const val AMBIGUITY_THRESHOLD = 0.72

        fun resolve(remoteValue: Double?, lastValidValue: Double?): Resolution {
            val remote = remoteValue?.takeIf(::isValidDirectThreshold)
            if (remote != null) {
                return Resolution(ConfidencePolicy(remote), Source.REMOTE_VALID)
            }

            val lastValid = lastValidValue?.takeIf(::isValidDirectThreshold)
            if (lastValid != null) {
                return Resolution(ConfidencePolicy(lastValid), Source.LAST_VALID)
            }

            return Resolution(
                ConfidencePolicy(BUILD_DEFAULT_DIRECT_THRESHOLD),
                Source.BUILD_DEFAULT,
            )
        }

        private fun isValidDirectThreshold(value: Double): Boolean =
            value.isFinite() && value in MIN_DIRECT_THRESHOLD..MAX_DIRECT_THRESHOLD
    }

    data class Resolution(val policy: ConfidencePolicy, val source: Source)

    enum class Source {
        REMOTE_VALID,
        LAST_VALID,
        BUILD_DEFAULT,
    }
}

data class ArtistMatchPolicy(
    val directThreshold: Double = 0.88,
    val plausibleThreshold: Double = 0.72,
    val minimumSeparation: Double = 0.08,
) {
    init {
        require(plausibleThreshold in 0.0..<directThreshold)
        require(directThreshold in 0.0..1.0)
        require(minimumSeparation in 0.0..1.0)
    }
}
