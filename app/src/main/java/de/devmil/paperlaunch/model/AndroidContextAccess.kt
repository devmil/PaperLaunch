package de.devmil.paperlaunch.model

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import de.devmil.paperlaunch.utils.AppMetadataUtils

class AndroidContextAccess(private val context: Context) : IContextAccess {

    override fun getDrawable(id: Int, themed : Boolean) : Drawable {
        if(themed) {
            return context.resources.getDrawable(id, context.theme)
        }
        return context.getDrawable(id)
    }

    override fun getAppName(intent: Intent): String? {
        return AppMetadataUtils.getAppName(context, intent)
    }

    override fun getAppIcon(intent: Intent): Drawable {
        return AppMetadataUtils.getAppIcon(context, intent)
    }
}