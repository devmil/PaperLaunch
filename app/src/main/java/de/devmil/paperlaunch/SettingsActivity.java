package de.devmil.paperlaunch;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.storage.ITransactionAction;
import de.devmil.paperlaunch.storage.ITransactionContext;
import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class SettingsActivity extends FragmentActivity {

    private Toolbar mToolbar;
    private Button mButtonTest;
    private Button mButtonReset;
    private EditFolderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherOverlayService.launch(this);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);
        mButtonTest = (Button)findViewById(R.id.activity_settings_buttontest);
        mButtonReset = (Button)findViewById(R.id.activity_settings_buttonreset);
        mFragment = (EditFolderFragment)getSupportFragmentManager().findFragmentById(R.id.activity_settings_editfolder_fragment);

        setActionBar(mToolbar);

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
        EntriesDataSource.getInstance().accessData(this, new ITransactionAction() {
            @Override
            public void execute(ITransactionContext transactionContext) {
                transactionContext.clear();
                createLaunch(transactionContext, "com.agilebits.onepassword", "com.agilebits.onepassword.activity.LoginActivity", 1);
                createLaunch(transactionContext, "org.kman.AquaMail", "org.kman.AquaMail.ui.AccountListActivity", 2);
                createLaunch(transactionContext, "com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.ONMSplashActivity", 3);
                createLaunch(transactionContext, "com.spotify.music", "com.spotify.music.MainActivity", 5);

                List<ComponentName> folderLaunchComponents = new ArrayList<>();
                folderLaunchComponents.add(new ComponentName("mobi.koni.appstofiretv", "mobi.koni.appstofiretv.MainActivity"));
                folderLaunchComponents.add(new ComponentName("org.dmfs.tasks", "org.dmfs.tasks.TaskListActivity"));

                createFolder(transactionContext, "Test Folder", folderLaunchComponents, 4);
            }
        });
    }

    private Launch createLaunch(ITransactionContext transactionContext, String packageName, String className, int orderIndex) {
        return createLaunch(transactionContext, -1, packageName, className, orderIndex);
    }


    private Launch createLaunch(ITransactionContext transactionContext, long parentFolderId, String packageName, String className, int orderIndex) {
        Launch l = transactionContext.createLaunch(parentFolderId, orderIndex);

        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(packageName, className));

        l.getDto().setLaunchIntent(launchIntent);

        transactionContext.updateLaunchData(l);

        return l;
    }

    private Folder createFolder(ITransactionContext transactionContext, String folderName, List<ComponentName> launchComponentNames, int orderIndex)
    {
        Folder f = transactionContext.createFolder(-1, orderIndex);
        f.getDto().setName(folderName);

        transactionContext.updateFolderData(f);

        int launchOrderIndex = 1;

        for(ComponentName cn : launchComponentNames) {
            createLaunch(transactionContext, f.getId(), cn.getPackageName(), cn.getClassName(), launchOrderIndex);
            launchOrderIndex++;
        }

        return transactionContext.loadFolder(f.getId());
    }


}
