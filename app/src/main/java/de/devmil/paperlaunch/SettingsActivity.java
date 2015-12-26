package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.view.utils.ViewUtils;
import de.devmil.paperlaunch.view.fragments.SettingsFragment;

public class SettingsActivity extends Activity implements SettingsFragment.IActivationParametersChangedListener {

    private Toolbar mToolbar;
    private LinearLayout mActivationIndicatorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);

        setActionBar(mToolbar);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        SettingsFragment sf;
        getFragmentManager().beginTransaction().replace(R.id.activity_settings_fragment_placeholder, sf = new SettingsFragment()).commit();
        sf.setOnActivationParametersChangedListener(this);

        updateActivationIndicator();
    }

    private void updateActivationIndicator() {
        UserSettings us = new UserSettings(this);

        LauncherOverlayService.ActivationViewResult avr = LauncherOverlayService.addActivationViewToWindow(
                mActivationIndicatorContainer,
                this,
                (int)ViewUtils.getPxFromDip(this, us.getSensitivityDip()),
                (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetPositionDip()),
                (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetHeightDip()),
                us.isOnRightSide()
        );

        mActivationIndicatorContainer = avr.container;

        avr.activationView.setElevation(ViewUtils.getPxFromDip(this, 3));
        avr.activationView.setBackgroundColor(getResources().getColor(R.color.theme_accent));

        LauncherOverlayService.ensureActivationTappable(this);
    }

    @Override
    public void onActivationParametersChanged() {
        updateActivationIndicator();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LauncherOverlayService.removeTouchReceiver(this, mActivationIndicatorContainer);
        mActivationIndicatorContainer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActivationIndicator();
    }
}
