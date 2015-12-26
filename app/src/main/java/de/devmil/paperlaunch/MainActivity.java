package de.devmil.paperlaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class MainActivity extends Activity {

    private Toolbar mToolbar;
    private EditFolderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherOverlayService.launch(this);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.activity_main_toolbar);
        mFragment = (EditFolderFragment)getFragmentManager().findFragmentById(R.id.activity_main_editfolder_fragment);

        setActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        MenuItem itemSettings = menu.add(R.string.title_activity_settings);
        itemSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
        });

        MenuItem itemAbout = menu.add(R.string.title_activity_about);
        itemAbout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent settingsIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(settingsIntent);
                return true;
            }
        });

        return result;
    }
}
