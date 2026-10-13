package com.latticeward.musiccopilot.core.model

/** Opaque IDs: never use as telemetry identity or expose personal URI/path values. */
@JvmInline value class CommandId(val value: String)
@JvmInline value class MediaRef(val value: String)
@JvmInline value class VoiceSessionId(val value: Long)
@JvmInline value class VoiceCandidateToken(val value: String)

