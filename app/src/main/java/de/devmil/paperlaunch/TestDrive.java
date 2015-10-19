package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.model.LaunchEntry;
import de.devmil.paperlaunch.view.LauncherView;


public class TestDrive extends Activity {

    private LauncherView mLauncherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_drive);

        mLauncherView = (LauncherView)findViewById(R.id.tstLauncherView);

        LaunchConfig cfg = new LaunchConfig();

        List<LaunchEntry> entries = new ArrayList<>();
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "com.agilebits.onepassword"));
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "au.com.shiftyjelly.pocketcasts"));
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "org.kman.AquaMail"));
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "com.microsoft.office.onenote"));
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "pl.solidexplorer"));
        entries.add(LaunchEntry.create(this, cfg.getDesignConfig(), "com.spotify.music"));

        cfg.setEntries(entries);

        mLauncherView.doInitialize(cfg);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLauncherView.start();
            }
        }, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_drive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
