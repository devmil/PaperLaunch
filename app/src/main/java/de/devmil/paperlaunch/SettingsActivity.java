package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;

public class SettingsActivity extends Activity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);

        setActionBar(mToolbar);
    }

}
