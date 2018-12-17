package de.devmil.paperlaunch.locale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.devmil.paperlaunch.service.LauncherOverlayService

class LocaleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action != LocaleConstants.ACTION_FIRE_SETTING) {
            return
        }
        if(!intent.hasExtra(LocaleConstants.EXTRA_BUNDLE)) {
            return
        }
        val bundle = intent.getBundleExtra(LocaleConstants.EXTRA_BUNDLE)
        if(!LocaleBundle.isValid(bundle)) {
            return
        }
        val localeBundle = LocaleBundle.from(bundle)

        //delegate the intended action to the LauncherOverlayService
        if(localeBundle.isEnabled) {
            LauncherOverlayService.play(context)
        } else {
            LauncherOverlayService.pause(context)
        }
    }
}
