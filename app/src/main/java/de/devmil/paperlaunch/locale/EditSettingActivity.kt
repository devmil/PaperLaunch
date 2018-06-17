package de.devmil.paperlaunch.locale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toolbar
import de.devmil.paperlaunch.R

class EditSettingActivity : Activity() {

    private var btnOk: Button? = null
    private var rbEnable: RadioButton? = null
    private var rbDisable: RadioButton? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_setting)

        btnOk = findViewById(R.id.activity_edit_setting_btn_ok)
        rbEnable = findViewById(R.id.activity_edit_setting_rbEnable)
        rbDisable = findViewById(R.id.activity_edit_setting_rbDisable)
        toolbar = findViewById(R.id.activity_edit_settings_toolbar)

        setActionBar(toolbar)

        var isEnabled = true


        if(intent.hasExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")) {
            val localeBundle = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")
            if(LocaleBundle.isValid(localeBundle)) {
                isEnabled = LocaleBundle.from(localeBundle).isEnabled
            }
        }

        rbEnable?.isChecked = isEnabled
        rbDisable?.isChecked = !isEnabled

        btnOk?.setOnClickListener {
            rbEnable?.let { itRbEnable ->
                val localeBundle = LocaleBundle(itRbEnable.isChecked)
                val resultIntent = Intent()
                resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", localeBundle.toBundle())
                resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", if(itRbEnable.isChecked) "Activate" else "Pause")
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }

}
