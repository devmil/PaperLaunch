package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.view.LauncherView;


public class TestDrive extends Activity {

    private LauncherView mLauncherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_drive);

        mLauncherView = (LauncherView)findViewById(R.id.tstLauncherView);

        LaunchConfig cfg = new LaunchConfig();

        List<Launch> entries = new ArrayList<>();
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.agilebits.onepassword", "com.agilebits.onepassword.activity.LoginActivity", 1));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "org.kman.AquaMail", "org.kman.AquaMail.ui.AccountListActivity", 2));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.ONMSplashActivity", 3));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.spotify.music", "com.spotify.music.MainActivity", 4));

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
