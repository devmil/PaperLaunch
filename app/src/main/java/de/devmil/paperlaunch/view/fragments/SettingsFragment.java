package de.devmil.paperlaunch.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.view.preferences.SeekBarPreference;

public class SettingsFragment extends PreferenceFragment {

    private PreferenceScreen mScreen;
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

        mScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(mScreen);

        addActivationSettings(context, mScreen);
        addAppearanceSettings(context, mScreen);
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
        activationCategory.setTitle("Activation");
        activationCategory.setIcon(R.mipmap.ic_wifi_tethering_black_36dp);

        SeekBarPreference sensitivityPreference = new SeekBarPreference(context, 5, 40);
        activationCategory.addPreference(sensitivityPreference);

        sensitivityPreference.setValue(mUserSettings.getSensitivityDip());
        sensitivityPreference.setTitle("Sensitivity");
        sensitivityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setSensitivityDip((Integer) newValue);
                mUserSettings.save(getActivity());
                LauncherOverlayService.notifyConfigChanged(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float heightDpi = metrics.heightPixels / metrics.density;

        SeekBarPreference offsetHeightPreference = new SeekBarPreference(context, 0, (int)heightDpi);
        activationCategory.addPreference(offsetHeightPreference);

        offsetHeightPreference.setValue((int)heightDpi - mUserSettings.getActivationOffsetHeightDip());
        offsetHeightPreference.setTitle("Height");
        offsetHeightPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setActivationOffsetHeightDip((int)heightDpi - (Integer) newValue);
                mUserSettings.save(getActivity());
                LauncherOverlayService.notifyConfigChanged(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });

        int offsetMin = -(int)(heightDpi / 2);
        int offsetMax = (int)(heightDpi / 2);

        SeekBarPreference offsetPositionPreference = new SeekBarPreference(context, offsetMin, offsetMax);
        activationCategory.addPreference(offsetPositionPreference);

        offsetPositionPreference.setValue(mUserSettings.getActivationOffsetPositionDip());
        offsetPositionPreference.setTitle("Position");
        offsetPositionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mUserSettings.load(getActivity());
                mUserSettings.setActivationOffsetPositionDip((Integer) newValue);
                mUserSettings.save(getActivity());
                LauncherOverlayService.notifyConfigChanged(getActivity());
                fireActivationParametersChanged();
                return true;
            }
        });
    }

    private void addAppearanceSettings(Context context, PreferenceScreen screen) {
        PreferenceCategory apperanceCategory = new PreferenceCategory(context);
        screen.addPreference(apperanceCategory);

        apperanceCategory.setPersistent(false);
        apperanceCategory.setTitle("Appearance");

        CheckBoxPreference showBackgroundPreference = new CheckBoxPreference(context);
        apperanceCategory.addPreference(showBackgroundPreference);

        showBackgroundPreference.setPersistent(false);
        showBackgroundPreference.setTitle("Show background");
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
    }
}
