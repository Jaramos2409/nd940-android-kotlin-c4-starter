package com.udacity.project4.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(
        activityResultLauncher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) = activityResultLauncher.launch(permissions)

    fun checkIfShouldShowRequestRationale(
        activity: Activity,
        listOfPermissions: List<String>
    ): Boolean =
        listOfPermissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }

    fun arePermissionsGranted(context: Context, listOfPermissions: List<String>): Boolean =
        listOfPermissions.all { permission ->
            hasPermission(context, permission)
        }

}
