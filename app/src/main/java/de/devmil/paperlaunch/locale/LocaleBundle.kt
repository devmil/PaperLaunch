package de.devmil.paperlaunch.locale

import android.os.Bundle

class LocaleBundle(var isEnabled: Boolean) {

    fun toBundle(): Bundle {
        val result = Bundle()
        result.putBoolean(EXTRA_IS_ENABLED, isEnabled)
        return result
    }

    companion object {
        private const val EXTRA_IS_ENABLED: String = "de.devmil.paperlaunch.locale.extra.IS_ENABLED"

        fun isValid(bundle: Bundle?): Boolean {
            return bundle != null
                && bundle.containsKey(EXTRA_IS_ENABLED)
        }

        fun from(bundle: Bundle?): LocaleBundle {
            var isEnabled = true
            if(bundle != null && bundle.containsKey(EXTRA_IS_ENABLED)) {
                isEnabled = bundle.getBoolean(EXTRA_IS_ENABLED)
            }
            return LocaleBundle(isEnabled)
        }
    }
}