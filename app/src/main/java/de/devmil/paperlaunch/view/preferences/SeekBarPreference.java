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
package de.devmil.paperlaunch.view.preferences;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import de.devmil.paperlaunch.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private int mCurrentValue = 0;
    private int mMin = 0;
    private int mMax = 1;
    private boolean mIsRangeInitialized = false;
    private boolean mValueSet = false;

    public SeekBarPreference(Context context, int min, int max) {
        super(context);
        construct();
        setRange(min, max);
    }

    private void construct() {
        setLayoutResource(R.layout.preference_seekbar);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if(!mIsRangeInitialized
                || !mValueSet) {
            return;
        }

        SeekBar seekbar = (SeekBar)view.findViewById(R.id.preference_seekbar_seek);
        TextView labelText = (TextView)view.findViewById(R.id.preference_seekbar_label);

        seekbar.setMax(mMax - mMin);
        seekbar.setProgress(mCurrentValue - mMin);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setEnabled(isEnabled());

        if(getTitleRes() > 0) {
            labelText.setText(getContext().getString(getTitleRes()));
        } else {
           labelText.setText(getTitle());
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(mCurrentValue) : (Integer) defaultValue);
    }

    public void setRange(int min, int max) {
        mMin = min;
        mMax = max;
        mIsRangeInitialized = true;
        if(!mValueSet) {
            mCurrentValue = mMin;
        }
        notifyChanged();
    }

    public void setValue(int value) {
        if(mValueSet && mCurrentValue == value) {
            return;
        }
        mCurrentValue = value;
        persistInt(value);
        mValueSet = true;
        notifyChanged();
        callChangeListener(value);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser
                && mIsRangeInitialized) {
            setValue(seekBar.getProgress() + mMin);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}