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
package de.devmil.paperlaunch.view.preferences

import android.content.Context
import android.preference.Preference
import android.view.View
import android.widget.SeekBar
import android.widget.TextView

import de.devmil.paperlaunch.R

class SeekBarPreference(context: Context, min: Int, max: Int) : Preference(context), SeekBar.OnSeekBarChangeListener {

    private var currentValue = 0
    private var min = 0
    private var max = 1
    private var isRangeInitialized = false
    private var valueSet = false
    private var touchActive = false

    init {
        construct()
        setRange(min, max)
    }

    private fun construct() {
        layoutResource = R.layout.preference_seekbar
    }

    @Deprecated("Deprecated in Java")
    override fun onBindView(view: View) {
        super.onBindView(view)

        if (!isRangeInitialized || !valueSet) {
            return
        }

        val seekbar = view.findViewById<SeekBar>(R.id.preference_seekbar_seek)
        val labelText = view.findViewById<TextView>(R.id.preference_seekbar_label)

        val seekbarMax: Int = max - min
        val progress = currentValue - min

        if(seekbar.max != seekbarMax) {
            seekbar.max = seekbarMax
        }
        seekbar.setOnSeekBarChangeListener(this)

        if(seekbar.isEnabled != isEnabled) {
            seekbar.isEnabled = isEnabled
        }
        if(seekbar.progress != progress) {
            seekbar.progress = progress
        }

        if (titleRes > 0) {
            labelText.text = context.getString(titleRes)
        } else {
            labelText.text = title
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any) {
        setValue(if (restorePersistedValue) getPersistedInt(currentValue) else defaultValue as Int)
    }

    fun setRange(min: Int, max: Int) {
        this.min = min
        this.max = max
        isRangeInitialized = true
        if (!valueSet) {
            currentValue = this.min
        }
        notifyChanged()
    }

    fun setValue(value: Int) {
        if (valueSet && currentValue == value) {
            return
        }
        currentValue = value
        persistInt(value)
        valueSet = true
        if(!touchActive) {
            notifyChanged()
        }
        callChangeListener(value)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && isRangeInitialized) {
            setValue(seekBar.progress + min)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        touchActive = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        touchActive = false
    }
}