package de.devmil.paperlaunch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class EditFolderActivity extends FragmentActivity {

    private static final String ARG_FOLDERID = "folderId";

    public static Intent createLaunchIntent(Context context, long folderId) {
        Intent result = new Intent(context, EditFolderActivity.class);
        result.putExtra(ARG_FOLDERID, folderId);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_folder);

        long folderId = -1;
        if(getIntent().hasExtra(ARG_FOLDERID)) {
            folderId = getIntent().getLongExtra(ARG_FOLDERID, -1);
        }

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(R.id.activity_edit_folder_folder_fragment, EditFolderFragment.newInstance(folderId));
        trans.commit();
    }

}
