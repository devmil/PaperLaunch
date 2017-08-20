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

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LicenseManager(private val context: Context, private val licenseInfoFileId: Int) {
    val licenseInfo by lazy { loadLicenseInfo() }

    private fun loadLicenseInfo(): LicenseInfo? {
        val stream = context.resources.openRawResource(licenseInfoFileId)

        val sr = InputStreamReader(stream)
        val br = BufferedReader(sr)

        val licenseInfo = StringBuilder()
        try {
            var line = br.readLine()
            while (line != null) {
                licenseInfo.append(line)
                licenseInfo.append("\n")
                line = br.readLine()
            }
        } catch (e: IOException) {
            return null
        }

        return try {
            LicenseInfo.readFromJSON(JSONObject(licenseInfo.toString()))
        } catch(e: JSONException) {
            null
        }
    }

}
