package de.devmil.paperlaunch.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import de.devmil.paperlaunch.R


object PermissionUtils {
    val REQUEST_DRAW_OVERLAY_PERMISSION = 10001

    fun checkOverlayPermission(context : Context) : Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        return Settings.canDrawOverlays(context)
    }

    fun checkOverlayPermissionAndRouteToSettingsIfNeeded(activity : Activity) : Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if(!checkOverlayPermission(activity)) {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(R.string.permission_request_title)
            alertDialog.setMessage(R.string.permission_request_description)
            alertDialog.setNeutralButton(R.string.permission_request_button_ok) { dialog, _ ->
                dialog.dismiss()
                launchOverlaySettings(activity)
            }
            alertDialog.setOnDismissListener { activity.finish() }
            alertDialog.show()
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun launchOverlaySettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName))
        activity.startActivityForResult(intent, REQUEST_DRAW_OVERLAY_PERMISSION)
    }
}