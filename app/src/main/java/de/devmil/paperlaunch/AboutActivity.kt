/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.paperlaunch

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.devmil.common.licensing.LicenseManager
import de.devmil.common.licensing.PackageInfo

class AboutActivity : Activity() {

    private var mToolbar: Toolbar? = null
    private var mLicenseManager: LicenseManager? = null
    private var mLicenseList: ListView? = null
    private var mVersionText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        mToolbar = findViewById(R.id.activity_about_toolbar) as Toolbar
        mLicenseList = findViewById(R.id.activity_about_info_listView) as ListView
        mVersionText = findViewById(R.id.feature_image_versiontext) as TextView

        setActionBar(mToolbar)
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        mLicenseManager = LicenseManager(this, R.raw.licenseinfo)

        val adapter = LicenseEntryAdapter(this, mLicenseManager!!.licenseInfo.packages.toTypedArray())

        mLicenseList!!.adapter = adapter

        mVersionText!!.text = versionText
    }

    private val versionText: String
        get() = String.format("%s %s (%d)", getString(R.string.app_name), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

    internal inner class LicenseEntryAdapter(context: Context, items: Array<PackageInfo>) : ArrayAdapter<PackageInfo>(context, 0, items) {

        internal inner class ViewHolder {
            var image: ImageView? = null
            var name: TextView? = null
            var copyright: TextView? = null
            var url: TextView? = null
            var licenseButton: Button? = null
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var localConvertView = convertView
            if (convertView == null) {
                localConvertView = LayoutInflater.from(context).inflate(R.layout.activity_about_info_entry, parent, false)
                val vh = ViewHolder()
                vh.image = localConvertView!!.findViewById(R.id.activity_license_info_entry_image) as ImageView
                vh.name = localConvertView.findViewById(R.id.activity_license_info_entry_name) as TextView
                vh.copyright = localConvertView.findViewById(R.id.activity_license_info_entry_copyright) as TextView
                vh.url = localConvertView.findViewById(R.id.activity_license_info_entry_url) as TextView
                vh.licenseButton = localConvertView.findViewById(R.id.activity_license_info_entry_licensebutton) as Button
                localConvertView.tag = vh
            }

            val holder = convertView!!.tag as ViewHolder

            val item = getItem(position)

            val drawableId = context.resources.getIdentifier(item!!.iconName, "drawable", "de.devmil.paperlaunch")

            holder.image!!.setImageResource(drawableId)
            holder.name!!.text = item.name
            holder.copyright!!.text = item.copyright
            holder.url!!.text = Html.fromHtml("<a href=\"" + item.url + "\">" + item.url + "</a>")
            holder.url!!.autoLinkMask = Linkify.WEB_URLS
            holder.url!!.tag = item
            holder.url!!.setOnClickListener { v ->
                try {
                    val pi = v.tag as PackageInfo
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(pi.url)

                    startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing package url", e)
                }
            }
            holder.licenseButton!!.text = item.license.name
            holder.licenseButton!!.tag = item
            holder.licenseButton!!.setOnClickListener { v ->
                val pi = v.tag as PackageInfo
                val scrollView = ScrollView(context)
                val tvMessage = TextView(context)
                tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 6f)
                tvMessage.typeface = Typeface.MONOSPACE
                tvMessage.text = pi.license.content
                scrollView.addView(tvMessage)
                val builder = AlertDialog.Builder(context)
                val dlg = builder.setTitle(pi.license.name)
                        .setView(scrollView)
                        .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                dlg.show()
            }

            return convertView
        }
    }

    companion object {

        private val TAG = AboutActivity::class.java.simpleName
    }
}
