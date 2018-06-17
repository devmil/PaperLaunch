package de.devmil.paperlaunch.locale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.devmil.paperlaunch.service.LauncherOverlayService

class LocaleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action != "com.twofortyfouram.locale.intent.action.FIRE_SETTING") {
            return
        }
        if(!intent.hasExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")) {
            return
        }
        val bundle = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")
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
