package com.latticeward.musiccopilot.app.di

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import com.latticeward.musiccopilot.core.application.permission.AndroidPermissionGateway
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.PermissionCapability
import com.latticeward.musiccopilot.core.application.permission.PermissionPlatformPolicy
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction

/**
 * P02/P03 read-only Android adapter. OS grant truth only; historical
 * request count is UNKNOWN, so this adapter can never declare permanent denial.
 * P05/P07 supply lifecycle-safe native launchers and request history.
 */
class ReadOnlyAndroidPermissionGateway(private val activity: Activity) : AndroidPermissionGateway {
    override suspend fun observe(capability: PermissionCapability):
        AppResult<NativePermissionObservation> {
        val permission = PermissionPlatformPolicy.manifestPermission(capability, Build.VERSION.SDK_INT)
            ?: return AppResult.Success(NativePermissionObservation(
                granted = true, requestedBefore = false, shouldShowRationale = false))
        return AppResult.Success(NativePermissionObservation(
            granted = activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED,
            requestedBefore = false,
            shouldShowRationale = activity.shouldShowRequestPermissionRationale(permission),
        ))
    }

    override suspend fun request(capability: PermissionCapability):
        AppResult<NativePermissionObservation> = AppResult.Failure(
            AppError.Permission(
                AppErrorCodes.PERMISSION_DENIED,
                Recoverability.USER_ACTION_REQUIRED,
                RecoveryAction.ManualFallback,
            )
        )

    override suspend fun openSettings(): AppResult<Unit> = AppResult.Failure(
        AppError.Permission(
            AppErrorCodes.PERMISSION_DENIED,
            Recoverability.USER_ACTION_REQUIRED,
            RecoveryAction.ManualFallback,
        )
    )
}
