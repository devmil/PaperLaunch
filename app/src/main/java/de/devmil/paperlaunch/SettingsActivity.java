package de.devmil.paperlaunch;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toolbar;

import de.devmil.paperlaunch.view.fragments.SettingsFragment;

public class SettingsActivity extends Activity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);
        setActionBar(mToolbar);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.activity_settings_fragment_placeholder, new SettingsFragment()).commit();
    }

}
