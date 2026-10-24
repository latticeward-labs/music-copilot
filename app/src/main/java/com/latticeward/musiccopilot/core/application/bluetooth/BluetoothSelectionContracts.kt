package com.latticeward.musiccopilot.core.application.bluetooth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Issue #9 P09 — optional, user-selected paired audio only.
 * Bluetooth permissions and physical eligibility are checked by the Android
 * source, not inferred from a persisted preference.
 */
data class PairedAudioDevice(
    /** RAM only; never persisted, logged, used for telemetry or in evidence. */
    val address: String,
    val displayName: String,
)

sealed interface PairedAudioRead {
    data class Available(val devices: List<PairedAudioDevice>) : PairedAudioRead
    data object PermissionRequired : PairedAudioRead
    data object Unsupported : PairedAudioRead
    data object Disabled : PairedAudioRead
    data object Unavailable : PairedAudioRead
}

/** Enumeration is allowed only on a deliberate Settings Open / Refresh / Save. */
interface PairedAudioDeviceSource {
    suspend fun readPairedAudio(): PairedAudioRead
}

/**
 * This marker lives in the existing private, no-backup Preferences DataStore.
 * Never store a raw MAC or device name. Version + per-selection random salt
 * allow re-identification *locally* from fresh paired-device truth after reboot,
 * without storing a reversible Bluetooth address.
 *
 * No MAC / salt / digest may leave this app via telemetry or GitHub.
 */
object BluetoothSelectionToken {
    private const val VERSION = "v1"
    private val addressShape = Regex("^[0-9A-F]{2}(:[0-9A-F]{2}){5}$")
    private const val SALT_BYTES = 16

    fun normalizeAddress(candidate: String): String? {
        val normalized = candidate.trim().uppercase(java.util.Locale.ROOT)
        return normalized.takeIf { addressShape.matches(it) }
    }

    fun create(
        address: String,
        salt: ByteArray = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) },
    ): String? {
        val normalized = normalizeAddress(address) ?: return null
        if (salt.size != SALT_BYTES) return null
        val digest = digest(salt, normalized)
        val encoder = Base64.getUrlEncoder().withoutPadding()
        return listOf(VERSION, encoder.encodeToString(salt),
            encoder.encodeToString(digest)).joinToString(":")
    }

    /** Unknown/legacy preference strings are NOT trusted as device identity. */
    fun matches(token: String?, candidate: String): Boolean {
        val normalized = normalizeAddress(candidate) ?: return false
        val fields = token?.split(':') ?: return false
        if (fields.size != 3 || fields[0] != VERSION) return false
        return try {
            val decoder = Base64.getUrlDecoder()
            val salt = decoder.decode(fields[1])
            val expected = decoder.decode(fields[2])
            salt.size == SALT_BYTES && expected.size == 32 &&
                MessageDigest.isEqual(expected, digest(salt, normalized))
        } catch (_: IllegalArgumentException) { false }
    }

    /** Stored identity is a lookup hint, never proof of audio route/connection. */
    fun isCurrentFormat(token: String?): Boolean =
        token != null && token.startsWith("$VERSION:") &&
            token.split(':').size == 3

    private fun digest(salt: ByteArray, normalized: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(normalized.toByteArray(Charsets.US_ASCII))
        return md.digest()
    }
}
