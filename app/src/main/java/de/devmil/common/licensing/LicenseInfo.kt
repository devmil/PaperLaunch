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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.Dictionary
import java.util.Hashtable

class LicenseInfo private constructor() : ILicenseAccess {

    private val licenses: Dictionary<String, LicenseDefinition>
    private val packagesMutableList: MutableList<PackageInfo>

    init {
        licenses = Hashtable<String, LicenseDefinition>()
        packagesMutableList = ArrayList()
    }

    val packages: List<PackageInfo>
        get() = packagesMutableList


    override fun getLicense(identifier: String): LicenseDefinition {
        return licenses.get(identifier)
    }

    companion object {

        private val TAG = LicenseInfo::class.java.simpleName

        private val LICENSE_ARRAY_IDENTIFIER = "license"
        private val PACKAGE_ARRAY_IDENTIFIER = "package"

        fun readFromJSON(obj: JSONObject): LicenseInfo? {
            val result = LicenseInfo()
            val licenses: JSONArray?
            try {
                licenses = obj.getJSONArray(LICENSE_ARRAY_IDENTIFIER)
            } catch (e: JSONException) {
                Log.w(TAG, "Error reading LicenseInfo", e)
                return null
            }

            for (i in 0 until licenses!!.length()) {
                try {
                    val licenseObj = licenses.getJSONObject(i)
                    val ld = LicenseDefinition.readFromJSON(licenseObj)
                    if (ld != null)
                        result.licenses.put(ld.id, ld)
                } catch (e: JSONException) {
                    Log.w(TAG, "Error reading LicenseInfo", e)
                }

            }
            val packages: JSONArray?
            try {
                packages = obj.getJSONArray(PACKAGE_ARRAY_IDENTIFIER)
            } catch (e: JSONException) {
                Log.w(TAG, "Error reading LicenseInfo", e)
                return null
            }

            for (i in 0 until packages!!.length()) {
                try {
                    val packageObj = packages.getJSONObject(i)
                    val pi = PackageInfo.readFromJSON(packageObj, result)
                    if (pi != null)
                        result.packagesMutableList.add(pi)
                } catch (e: JSONException) {
                    Log.w(TAG, "Error reading LicenseInfo", e)
                }

            }
            return result
        }
    }
}
