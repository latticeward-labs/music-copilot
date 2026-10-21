package com.latticeward.musiccopilot.core.application.permission

import com.latticeward.musiccopilot.core.result.AppResult

/**
 * Fakeable OS permission port. Implementations never record grants in DataStore
 * and must not call request/openSettings except after a user event.
 * Native ActivityResult registration belongs to P05; Settings to P07.
 */
interface AndroidPermissionGateway {
    suspend fun observe(capability: PermissionCapability): AppResult<NativePermissionObservation>
    suspend fun request(capability: PermissionCapability): AppResult<NativePermissionObservation>
    suspend fun openSettings(): AppResult<Unit>
}
