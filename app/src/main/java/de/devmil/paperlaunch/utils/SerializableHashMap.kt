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
package de.devmil.paperlaunch.utils

import java.io.Serializable
import java.util.HashMap

class SerializableHashMap<K, V> : Serializable {

    private var keyArray: Array<Any?>? = null
    private var valuesArray: Array<Any?>? = null

    constructor() {

    }

    constructor(map: Map<*, *>) {
        populateFromhashMap(map)
    }

    fun populateFromhashMap(map: Map<*, *>?) {
        val size = map?.size ?: 0
        keyArray = arrayOfNulls<Any>(size)
        valuesArray = arrayOfNulls<Any>(size)
        if (map != null) {
            var idx = 0
            for (k in map.keys) {
                keyArray!![idx] = k
                valuesArray!![idx] = map[k]
                idx++
            }
        }
    }

    val hashMap: Map<K, V>?
        get() {
            if (keyArray == null || valuesArray == null)
                return null
            val result = HashMap<K, V>(keyArray!!.size)
            fillHashMap(result)
            return result
        }

    fun fillHashMap(map: HashMap<K, V>) {
        if (keyArray == null || valuesArray == null)
            return
        @Suppress("UNCHECKED_CAST")
        for (i in keyArray!!.indices)
            map.put(keyArray!![i] as K, valuesArray!![i] as V)
    }

    companion object {

        /**

         */
        private const val serialVersionUID = 1L
    }
}
