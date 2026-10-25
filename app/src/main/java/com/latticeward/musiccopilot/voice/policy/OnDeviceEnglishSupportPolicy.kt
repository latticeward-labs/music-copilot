package com.latticeward.musiccopilot.voice.policy

/**
 * Android API 33+ recognition-support metadata is provider evidence, not
 * permission or proof of an actual successful utterance. No model download
 * is triggered by this policy or by the voice adapters.
 */
enum class EnglishModelReadiness {
    Installed, DownloadRequired, Unsupported
}

object OnDeviceEnglishSupportPolicy {
    const val REQUIRED_LANGUAGE_TAG = "en-US"

    /** No implicit language substitution: "en" may cover generic English. */
    fun matchesRequestedEnglish(installedTag: String, requestedTag: String): Boolean {
        if (!isSupportedRequest(requestedTag)) return false
        val available = installedTag.trim().replace('_', '-')
        return available.equals(requestedTag, ignoreCase = true) ||
            available.equals("en", ignoreCase = true)
    }

    fun isSupportedRequest(languageTag: String): Boolean =
        languageTag.equals(REQUIRED_LANGUAGE_TAG, ignoreCase = true)

    fun evaluate(
        requestedTag: String,
        installed: List<String>,
        downloadable: List<String>,
        pending: List<String>,
    ): EnglishModelReadiness {
        if (!isSupportedRequest(requestedTag)) return EnglishModelReadiness.Unsupported
        if (installed.any { matchesRequestedEnglish(it, requestedTag) })
            return EnglishModelReadiness.Installed
        if ((downloadable + pending).any { matchesRequestedEnglish(it, requestedTag) })
            return EnglishModelReadiness.DownloadRequired
        return EnglishModelReadiness.Unsupported
    }
}
