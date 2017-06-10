/*
 * Copyright 2014 Devmil Solutions
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
package de.devmil.common.licensing

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

/**
 * Created by devmil on 18.04.14.
 */
class PackageInfo private constructor(val name: String, val vendor: String, val licenseId: String, val url: String, val copyright: String, val iconName: String, licenseAccess: ILicenseAccess) {
    val license: LicenseDefinition

    init {
        license = licenseAccess.getLicense(licenseId)
    }

    companion object {

        private val NAME_IDENTIFIER = "name"
        private val VENDOR_IDENTIFIER = "vendor"
        private val LICENSE_IDENTIFIER = "license"
        private val URL_IDENTIFIER = "url"
        private val COPYRIGHT_IDENTIFIER = "copyright"
        private val ICON_IDENTIFIER = "icon"

        fun readFromJSON(obj: JSONObject, licenseAccess: ILicenseAccess): PackageInfo? {
            try {
                val name = obj.getString(NAME_IDENTIFIER)
                val vendor = obj.getString(VENDOR_IDENTIFIER)
                val licenseId = obj.getString(LICENSE_IDENTIFIER)
                val url = obj.getString(URL_IDENTIFIER)
                val copyright = obj.getString(COPYRIGHT_IDENTIFIER)
                val iconName = obj.getString(ICON_IDENTIFIER)
                val result = PackageInfo(name, vendor, licenseId, url, copyright, iconName, licenseAccess)

                return result
            } catch (e: JSONException) {
                Log.w(PackageInfo::class.java.simpleName, "Error reading LicenseDefinition", e)
                return null
            }

        }
    }
}
