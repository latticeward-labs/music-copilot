package com.latticeward.musiccopilot.spike

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object SyntheticMedia {
    fun insertSilentWav(context: Context): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "mc-spike-${UUID.randomUUID()}.wav")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MusicCopilotSpike")
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
            put(MediaStore.Audio.Media.DURATION, 1_000)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val uri = checkNotNull(
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values),
        )
        resolver.openOutputStream(uri, "w")!!.use { it.write(oneSecondSilentWav()) }
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
            null,
            null,
        )
        return uri
    }

    fun delete(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    private fun oneSecondSilentWav(): ByteArray {
        val sampleRate = 8_000
        val channels = 1
        val bitsPerSample = 16
        val dataSize = sampleRate * channels * (bitsPerSample / 8)
        return ByteBuffer.allocate(44 + dataSize)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                put("RIFF".encodeToByteArray())
                putInt(36 + dataSize)
                put("WAVE".encodeToByteArray())
                put("fmt ".encodeToByteArray())
                putInt(16)
                putShort(1.toShort())
                putShort(channels.toShort())
                putInt(sampleRate)
                putInt(sampleRate * channels * (bitsPerSample / 8))
                putShort((channels * (bitsPerSample / 8)).toShort())
                putShort(bitsPerSample.toShort())
                put("data".encodeToByteArray())
                putInt(dataSize)
                repeat(dataSize) { put(0.toByte()) }
            }
            .array()
    }
}
