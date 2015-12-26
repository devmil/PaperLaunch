package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toolbar;

import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.utils.ViewUtils;
import de.devmil.paperlaunch.view.fragments.SettingsFragment;

public class SettingsActivity extends Activity implements SettingsFragment.IActivationParametersChangedListener {

    private Toolbar mToolbar;
    private LinearLayout mActivationIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);
        mActivationIndicator = (LinearLayout)findViewById(R.id.activity_settings_activation_indicator);

        setActionBar(mToolbar);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        SettingsFragment sf;
        getFragmentManager().beginTransaction().replace(R.id.activity_settings_fragment_placeholder, sf = new SettingsFragment()).commit();
        sf.setOnActivationParametersChangedListener(this);

        updateActivationIndicator();

        mActivationIndicator.bringToFront();
    }

    private void updateActivationIndicator() {
        UserSettings us = new UserSettings(this);

        int windowHeightPx = getWindow().getDecorView().getHeight();
        int offsetPositionPx = (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetPositionDip());
        int offsetHeightPx = (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetHeightDip());
        int width = (int)ViewUtils.getPxFromDip(this, us.getSensitivityDip());

        int activationHeight = windowHeightPx - offsetHeightPx;
        int marginTop = (windowHeightPx - activationHeight) / 2;
        int marginBottom = marginTop;

        marginTop -= offsetPositionPx;
        marginBottom += offsetPositionPx;

        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(mActivationIndicator.getLayoutParams());
        newParams.setMargins(0, marginTop, 0, marginBottom);
        newParams.width = width;

        newParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        newParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if(us.isOnRightSide()) {
            newParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            newParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        mActivationIndicator.setLayoutParams(newParams);
    }

    @Override
    public void onActivationParametersChanged() {
        updateActivationIndicator();
    }
}
