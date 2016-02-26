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
package de.devmil.paperlaunch.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.config.LauncherGravity;
import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.view.preferences.SeekBarPreference;

public class SettingsFragment extends PreferenceFragment {

    private UserSettings mUserSettings;
    private IActivationParametersChangedListener mActivationParametersChangedListener;

    public interface IActivationParametersChangedListener {
        void onActivationParametersChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();

        mUserSettings = new UserSettings(context);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(screen);

        addActivationSettings(context, screen);
        addAppearanceSettings(context, screen);
    }

    public void setOnActivationParametersChangedListener(IActivationParametersChangedListener listener) {
        mActivationParametersChangedListener = listener;
    }

    private void fireActivationParametersChanged() {
        if(mActivationParametersChangedListener != null) {
            mActivationParametersChangedListener.onActivationParametersChanged();
        }
    }

    private void addActivationSettings(Context context, PreferenceScreen screen) {
        PreferenceCategory activationCategory = new PreferenceCategory(context);

        screen.addPreference(activationCategory);

        activationCategory.setPersistent(false);
        activationCategory.setTitle(R.string.fragment_settings_category_activation_title);
        activationCategory.setIcon(R.mipmap.ic_wifi_tethering_black_36dp);

        SeekBarPreference sensitivityPreference = new SeekBarPreference(context, 5, 40);
        activationCategory.addPreference(sensitivityPreference);

        sensitivityPreference.setValue(mUserSettings.getSensitivityDip());
        sensitivityPreference.setTitle(R.string.fragment_settings_activation_sensitivity_title);
        sensitivityPreference.setPersistent(false);
        sensitivityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setSensitivityDip((Integer) newValue);
                mUserSettings.save(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float heightDpi = metrics.heightPixels / metrics.density;

        SeekBarPreference offsetHeightPreference = new SeekBarPreference(context, 0, (int)heightDpi);
        activationCategory.addPreference(offsetHeightPreference);

        offsetHeightPreference.setValue((int) heightDpi - mUserSettings.getActivationOffsetHeightDip());
        offsetHeightPreference.setTitle(R.string.fragment_settings_activation_offset_height_title);
        offsetHeightPreference.setPersistent(false);
        offsetHeightPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setActivationOffsetHeightDip((int) heightDpi - (Integer) newValue);
                mUserSettings.save(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });

        int offsetMin = -(int)(heightDpi / 2);
        int offsetMax = (int)(heightDpi / 2);

        SeekBarPreference offsetPositionPreference = new SeekBarPreference(context, offsetMin, offsetMax);
        activationCategory.addPreference(offsetPositionPreference);

        offsetPositionPreference.setValue(mUserSettings.getActivationOffsetPositionDip());
        offsetPositionPreference.setTitle(R.string.fragment_settings_activation_offset_position_title);
        offsetPositionPreference.setPersistent(false);
        offsetPositionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setActivationOffsetPositionDip((Integer) newValue);
                mUserSettings.save(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });

        CheckBoxPreference vibrateOnActivationPreference = new CheckBoxPreference(context);
        activationCategory.addPreference(vibrateOnActivationPreference);

        vibrateOnActivationPreference.setChecked(mUserSettings.isVibrateOnActivation());
        vibrateOnActivationPreference.setTitle(R.string.fragment_settings_activation_vibrate_on_activation_title);
        vibrateOnActivationPreference.setSummaryOn(R.string.fragment_settings_activation_vibrate_on_activation_summary_on);
        vibrateOnActivationPreference.setSummaryOff(R.string.fragment_settings_activation_vibrate_on_activation_summary_off);
        vibrateOnActivationPreference.setPersistent(false);
        vibrateOnActivationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setVibrateOnActivation((Boolean) newValue);
                mUserSettings.save(getActivity());
                LauncherOverlayService.notifyConfigChanged(getActivity());
                return true;
            }
        });
    }

    private void addAppearanceSettings(final Context context, PreferenceScreen screen) {
        PreferenceCategory apperanceCategory = new PreferenceCategory(context);
        screen.addPreference(apperanceCategory);

        apperanceCategory.setPersistent(false);
        apperanceCategory.setTitle(R.string.fragment_settings_category_appearance_title);

        CheckBoxPreference showBackgroundPreference = new CheckBoxPreference(context);
        apperanceCategory.addPreference(showBackgroundPreference);

        showBackgroundPreference.setPersistent(false);
        showBackgroundPreference.setTitle(R.string.fragment_settings_appearance_background_title);
        showBackgroundPreference.setSummaryOn(R.string.fragment_settings_appearance_background_summary_on);
        showBackgroundPreference.setSummaryOff(R.string.fragment_settings_appearance_background_summary_off);
        showBackgroundPreference.setChecked(mUserSettings.isShowBackground());
        showBackgroundPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setShowBackground((Boolean) newValue);
                mUserSettings.save(getActivity());
                LauncherOverlayService.notifyConfigChanged(getActivity());
                return true;
            }
        });

        final ListPreference sidePreference = new ListPreference(context);
        apperanceCategory.addPreference(sidePreference);

        class SideEntry {
            SideEntry(String title, boolean value) {
                this.title = title;
                this.value = value;
            }
            String title;
            boolean value;
        }

        SideEntry[] sideEntryArray = new SideEntry[]
                {
                        new SideEntry(
                                context.getString(R.string.fragment_settings_appearance_side_optionleft),
                                false
                        ),
                        new SideEntry(
                                context.getString(R.string.fragment_settings_appearance_side_optionright),
                                true
                        )
                };

        List<CharSequence> sideEntryTitles = new ArrayList<>();
        List<CharSequence> sideEntryValues = new ArrayList<>();
        for(SideEntry se : sideEntryArray) {
            sideEntryTitles.add(se.title);
            sideEntryValues.add(Boolean.toString(se.value));
        }

        sidePreference.setPersistent(false);
        sidePreference.setTitle(R.string.fragment_settings_appearance_side_title);
        sidePreference.setEntries(sideEntryTitles.toArray(new CharSequence[sideEntryTitles.size()]));
        sidePreference.setEntryValues(sideEntryValues.toArray(new CharSequence[sideEntryValues.size()]));
        sidePreference.setValue(Boolean.toString(mUserSettings.isOnRightSide()));
        sidePreference.setSummary(context.getString(getSideSummary(mUserSettings.isOnRightSide())));
        sidePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newBooleanValue = Boolean.parseBoolean((String) newValue);
                mUserSettings.load(getActivity());
                mUserSettings.setIsOnRightSide(newBooleanValue);
                mUserSettings.save(getActivity());
                sidePreference.setSummary(context.getString(getSideSummary(mUserSettings.isOnRightSide())));
                fireActivationParametersChanged();
                return true;
            }
        });

        final ListPreference gravityPreference = new ListPreference(context);
        apperanceCategory.addPreference(gravityPreference);

        class GravityEntry {
            GravityEntry(String title, LauncherGravity value) {
                this.title = title;
                this.value = value;
            }
            String title;
            LauncherGravity value;
        }

        GravityEntry[] gravityEntryArray = new GravityEntry[]
                {
                        new GravityEntry(
                                context.getString(R.string.fragment_settings_appearance_gravity_optiontop),
                                LauncherGravity.Top
                        ),
                        new GravityEntry(
                                context.getString(R.string.fragment_settings_appearance_gravity_optioncenter),
                                LauncherGravity.Center
                        ),
                        new GravityEntry(
                                context.getString(R.string.fragment_settings_appearance_gravity_optionbottom),
                                LauncherGravity.Bottom
                        )
                };

        List<CharSequence> gravityEntryTitles = new ArrayList<>();
        List<CharSequence> gravityEntryValues = new ArrayList<>();
        for(GravityEntry ge : gravityEntryArray) {
            gravityEntryTitles.add(ge.title);
            gravityEntryValues.add(Integer.toString(ge.value.getValue()));
        }

        gravityPreference.setPersistent(false);
        gravityPreference.setTitle(R.string.fragment_settings_appearance_gravity_title);
        gravityPreference.setEntries(gravityEntryTitles.toArray(new CharSequence[gravityEntryTitles.size()]));
        gravityPreference.setEntryValues(gravityEntryValues.toArray(new CharSequence[gravityEntryValues.size()]));
        gravityPreference.setValue(Integer.toString(mUserSettings.getLauncherGravity().getValue()));
        gravityPreference.setSummary(context.getString(getGravitySummary(mUserSettings.getLauncherGravity())));
        gravityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int newIntValue = Integer.parseInt((String) newValue);
                LauncherGravity gravityValue = LauncherGravity.fromValue(newIntValue);
                mUserSettings.load(getActivity());
                mUserSettings.setLauncherGravity(gravityValue);
                mUserSettings.save(getActivity());
                gravityPreference.setSummary(context.getString(getGravitySummary(mUserSettings.getLauncherGravity())));
                fireActivationParametersChanged();
                return true;
            }
        });
    }

    private int getSideSummary(boolean isOnRightSide) {
        return isOnRightSide ?
                R.string.fragment_settings_appearance_side_optionright_summary
                : R.string.fragment_settings_appearance_side_optionleft_summary;
    }

    private int getGravitySummary(LauncherGravity gravity) {
        switch(gravity) {
            case Top:
                return R.string.fragment_settings_appearance_gravity_optiontop_summary;
            case Center:
                return R.string.fragment_settings_appearance_gravity_optioncenter_summary;
            case Bottom:
                return R.string.fragment_settings_appearance_gravity_optionbottom_summary;
        }
        return -1;
    }
}
