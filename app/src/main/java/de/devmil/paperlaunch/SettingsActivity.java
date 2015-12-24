package de.devmil.paperlaunch;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toolbar;

import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class SettingsActivity extends FragmentActivity {

    private Toolbar mToolbar;
    private EditFolderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherOverlayService.launch(this);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);
        mFragment = (EditFolderFragment)getSupportFragmentManager().findFragmentById(R.id.activity_settings_editfolder_fragment);

        setActionBar(mToolbar);
    }
}
