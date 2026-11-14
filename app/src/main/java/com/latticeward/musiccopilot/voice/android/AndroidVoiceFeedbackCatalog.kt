package com.latticeward.musiccopilot.voice.android

import android.content.Context
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackKey

/** English-first Android resource boundary, ready for future locale values. */
class AndroidVoiceFeedbackCatalog(context: Context) : VoiceFeedbackCatalog {
    private val app = context.applicationContext

    override fun textFor(key: VoiceFeedbackKey): String = app.getString(when (key) {
        VoiceFeedbackKey.Play -> R.string.i10_feedback_play
        VoiceFeedbackKey.Pause -> R.string.i10_feedback_pause
        VoiceFeedbackKey.Next -> R.string.i10_feedback_next
        VoiceFeedbackKey.Previous -> R.string.i10_feedback_previous
        VoiceFeedbackKey.SeekBack -> R.string.i10_feedback_seekback
        VoiceFeedbackKey.Timeout -> R.string.i10_feedback_timeout
        VoiceFeedbackKey.NotRecognized -> R.string.i10_feedback_notrecognized
        VoiceFeedbackKey.Unavailable -> R.string.i10_feedback_unavailable
        VoiceFeedbackKey.ArtistPlaying -> R.string.i11_feedback_artist_playing
        VoiceFeedbackKey.ArtistConfirmOne -> R.string.i11_feedback_artist_confirm_one
        VoiceFeedbackKey.ArtistConfirmTwo -> R.string.i11_feedback_artist_confirm_two
        VoiceFeedbackKey.ArtistNotFound -> R.string.i11_feedback_artist_not_found
        VoiceFeedbackKey.ArtistUnsupported -> R.string.i11_feedback_artist_unsupported
        VoiceFeedbackKey.ArtistDeclined -> R.string.i11_feedback_artist_declined
        VoiceFeedbackKey.ArtistCanceled -> R.string.i11_feedback_artist_canceled
        VoiceFeedbackKey.ArtistStale -> R.string.i11_feedback_artist_stale
        VoiceFeedbackKey.ArtistUnavailable -> R.string.i11_feedback_artist_unavailable
        VoiceFeedbackKey.ArtistPlaybackFailure -> R.string.i11_feedback_artist_playback_failure
    })
}
