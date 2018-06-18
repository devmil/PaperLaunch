package de.devmil.paperlaunch.locale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toolbar
import de.devmil.paperlaunch.R

class EditSettingActivity : Activity() {

    private var btnOk: Button? = null
    private var rbEnable: RadioButton? = null
    private var rbDisable: RadioButton? = null
    private var toolbar: Toolbar? = null
    private var imgResult: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_setting)

        btnOk = findViewById(R.id.activity_edit_setting_btn_ok)
        rbEnable = findViewById(R.id.activity_edit_setting_rbEnable)
        rbDisable = findViewById(R.id.activity_edit_setting_rbDisable)
        toolbar = findViewById(R.id.activity_edit_settings_toolbar)
        imgResult = findViewById(R.id.activity_edit_setting_img_result)

        setActionBar(toolbar)

        var isEnabled = true


        if(intent.hasExtra(LocaleConstants.EXTRA_BUNDLE)) {
            val localeBundle = intent.getBundleExtra(LocaleConstants.EXTRA_BUNDLE)
            if(LocaleBundle.isValid(localeBundle)) {
                isEnabled = LocaleBundle.from(localeBundle).isEnabled
            }
        }

        rbEnable?.isChecked = isEnabled
        rbDisable?.isChecked = !isEnabled

        rbEnable?.setOnCheckedChangeListener { _, _ ->
            updateResultImage()
        }
        rbDisable?.setOnCheckedChangeListener { _, _ ->
            updateResultImage()
        }

        btnOk?.setOnClickListener {
            rbEnable?.let { itRbEnable ->
                val localeBundle = LocaleBundle(itRbEnable.isChecked)
                val resultIntent = Intent()
                resultIntent.putExtra(LocaleConstants.EXTRA_BUNDLE, localeBundle.toBundle())
                val stringResource =
                        if(itRbEnable.isChecked)
                            R.string.activity_edit_setting_description_enable
                        else
                            R.string.activity_edit_setting_description_disable
                resultIntent.putExtra(LocaleConstants.EXTRA_BLURB, getString(stringResource))
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        updateResultImage()
    }

    private fun updateResultImage() {
        val isEnabled = rbEnable?.isChecked ?: false
        imgResult?.setImageResource(if(isEnabled) R.mipmap.ic_play_arrow_black_24dp else R.mipmap.ic_pause_black_24dp)
    }
}
