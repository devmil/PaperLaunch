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

    private var mCurrentValue = 0
    private var mMin = 0
    private var mMax = 1
    private var mIsRangeInitialized = false
    private var mValueSet = false

    init {
        construct()
        setRange(min, max)
    }

    private fun construct() {
        layoutResource = R.layout.preference_seekbar
    }

    override fun onBindView(view: View) {
        super.onBindView(view)

        if (!mIsRangeInitialized || !mValueSet) {
            return
        }

        val seekbar = view.findViewById(R.id.preference_seekbar_seek) as SeekBar
        val labelText = view.findViewById(R.id.preference_seekbar_label) as TextView

        seekbar.max = mMax - mMin
        seekbar.progress = mCurrentValue - mMin
        seekbar.setOnSeekBarChangeListener(this)
        seekbar.isEnabled = isEnabled

        if (titleRes > 0) {
            labelText.text = context.getString(titleRes)
        } else {
            labelText.text = title
        }
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any) {
        setValue(if (restorePersistedValue) getPersistedInt(mCurrentValue) else defaultValue as Int)
    }

    fun setRange(min: Int, max: Int) {
        mMin = min
        mMax = max
        mIsRangeInitialized = true
        if (!mValueSet) {
            mCurrentValue = mMin
        }
        notifyChanged()
    }

    fun setValue(value: Int) {
        if (mValueSet && mCurrentValue == value) {
            return
        }
        mCurrentValue = value
        persistInt(value)
        mValueSet = true
        notifyChanged()
        callChangeListener(value)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && mIsRangeInitialized) {
            setValue(seekBar.progress + mMin)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }
}