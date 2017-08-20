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

class LicenseDefinition private constructor(val id: String, val name: String, @Suppress("UNUSED_PARAMETER") url: String, val content: String) {
    companion object {

        private val ID_IDENTIFIER = "id"
        private val URL_IDENTIFIER = "url"
        private val NAME_IDENTIFIER = "name"
        private val CONTENT_IDENTIFIER = "content"

        fun readFromJSON(obj: JSONObject): LicenseDefinition? {
            return try {
                val id = obj.getString(ID_IDENTIFIER)
                val name = obj.getString(NAME_IDENTIFIER)
                val url = obj.getString(URL_IDENTIFIER)
                val content = obj.getString(CONTENT_IDENTIFIER)
                val result = LicenseDefinition(id, name, url, content)

                result
            } catch (e: JSONException) {

                Log.w(LicenseDefinition::class.java.simpleName, "Error reading LicenseDefinition", e)
                null
            }

        }
    }
}
