package de.devmil.paperlaunch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class SettingsActivity extends FragmentActivity {

    private Button mButtonTest;
    private Button mButtonReset;
    private EditFolderFragment mFragment;
    private EntriesDataSource mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherOverlayService.launch(this);
        setContentView(R.layout.activity_settings);

        mDataSource = new EntriesDataSource(this);

        mButtonTest = (Button)findViewById(R.id.activity_settings_buttontest);
        mButtonReset = (Button)findViewById(R.id.activity_settings_buttonreset);
        mFragment = (EditFolderFragment)getSupportFragmentManager().findFragmentById(R.id.activity_settings_editfolder_fragment);

        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(SettingsActivity.this, TestDrive.class);
                startActivity(launchIntent);
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                mFragment.invalidate();
            }
        });
    }

    private void resetData() {
        mDataSource.executeWithOpenDataSource(new EntriesDataSource.IAction() {
            @Override
            public void execute() {
                mDataSource.clear();

                createLaunch("com.agilebits.onepassword", "com.agilebits.onepassword.activity.LoginActivity", 1);
                createLaunch("org.kman.AquaMail", "org.kman.AquaMail.ui.AccountListActivity", 2);
                createLaunch("com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.ONMSplashActivity", 3);
                createLaunch("com.spotify.music", "com.spotify.music.MainActivity", 5);

                List<ComponentName> folderLaunchComponents = new ArrayList<>();
                folderLaunchComponents.add(new ComponentName("mobi.koni.appstofiretv", "mobi.koni.appstofiretv.MainActivity"));
                folderLaunchComponents.add(new ComponentName("org.dmfs.tasks", "org.dmfs.tasks.TaskListActivity"));

                createFolder("Test Folder", folderLaunchComponents, 4);
            }
        });
    }

    private Launch createLaunch(String packageName, String className, int orderIndex) {
        return createLaunch(-1, packageName, className, orderIndex);
    }


    private Launch createLaunch(long parentFolderId, String packageName, String className, int orderIndex) {
        Launch l = mDataSource.createLaunch(parentFolderId, orderIndex);

        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(packageName, className));

        l.getDto().setLaunchIntent(launchIntent);

        mDataSource.updateLaunchData(l);

        return l;
    }

    private Folder createFolder(String folderName, List<ComponentName> launchComponentNames, int orderIndex)
    {
        Folder f = mDataSource.createFolder(-1, orderIndex);
        f.getDto().setName(folderName);

        mDataSource.updateFolderData(f);

        int launchOrderIndex = 1;

        for(ComponentName cn : launchComponentNames) {
            createLaunch(f.getId(), cn.getPackageName(), cn.getClassName(), launchOrderIndex);
            launchOrderIndex++;
        }

        return mDataSource.loadFolder(f.getId());
    }


}
