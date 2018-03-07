package de.devmil.paperlaunch.view.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toolbar
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.model.Launch

class UrlSelector : Activity() {
    private var editName : EditText? = null
    private var editUrl : EditText? = null
    private var btnOK : Button? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_urlselectorview)

        toolbar = findViewById(R.id.activity_urlselectorview_toolbar)

        setActionBar(toolbar)

        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        editName = findViewById(R.id.activity_urlselectorview_edit_name)
        editUrl = findViewById(R.id.activity_urlselectorview_edit_url)
        btnOK = findViewById(R.id.activity_urlselectorview_btn_OK)

        editUrl?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(edit: Editable?) {
                editName?.let {
                    if(it.text.toString().isBlank() && !edit.toString().isBlank()) {
                        it.hint = edit.toString()
                    } else {
                        //restore original hint in case the URL is empty (or the name is filled)
                        it.hint = getString(R.string.activity_urlselectorview_edit_hint_name)
                    }
                }
            }
        })

        btnOK?.setOnClickListener {
            val urlTxt = editUrl?.text.toString()
            urlTxt.let {
                val resultIntent = Intent(Intent.ACTION_VIEW)
                resultIntent.data = Uri.parse(it)
                editName?.let {
                    if(!it.toString().isBlank()) {
                        resultIntent.putExtra(Launch.EXTRA_URL_NAME, it.text.toString())
                    }
                }
                setResultIntent(resultIntent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setResultIntent(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}