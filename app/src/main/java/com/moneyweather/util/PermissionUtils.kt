package com.moneyweather.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {
    companion object {
        val REQUEST_CODE_STORAGE_PERMISSION = 2001

        fun isGrantedPermission(context: Context, permissions: Array<String>): Boolean {
            var granted = true
            for (i in permissions.indices) {
                if (context.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            return granted
        }

        fun isGrantedLocationPermission(context: Context, permissions: Array<String>): Boolean {
            for (i in permissions.indices) {
                if (context.checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                    return true
                }
            }
            return false
        }

        fun movePermissionSetting(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }

        fun isAllowedPhoneStatePermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isGrantedPermission(context, arrayOf(Manifest.permission.READ_PHONE_STATE))
            } else {
                true
            }
        }

        fun needsStoragePermission(): Boolean {
            return Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.P
        }

        fun hasStoragePermission(context: Context): Boolean {
            return if (needsStoragePermission()) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Android 10 이상은 권한 필요 없음
            }
        }

        fun requestStoragePermission(activity: Activity, requestCode: Int) {
            if (needsStoragePermission()) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestCode
                )
            }
        }
    }
}