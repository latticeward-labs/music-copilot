package com.latticeward.musiccopilot.playback.media3

import android.net.Uri
import androidx.media3.common.MediaItem
import com.latticeward.musiccopilot.playback.service.PreparedPlaybackQueue

/**
 * Converts a previously OS-verified local queue into Media3 items.
 * Only callable from the existing MusicPlaybackService's application looper.
 * This does not open streams, persist URIs, or own a second Player.
 */
object LocalMedia3QueueItems {
    fun from(proposal: PreparedPlaybackQueue): List<MediaItem> =
        proposal.entries.map { verified ->
            val uri = Uri.parse(verified.contentUri)
            require(uri.scheme == "content" && uri.authority == "media") {
                "Unsupported local-media source"
            }
            MediaItem.Builder()
                .setMediaId(verified.ref.value)
                .setUri(uri)
                .build()
        }
}
