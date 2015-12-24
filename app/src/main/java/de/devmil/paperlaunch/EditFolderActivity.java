package de.devmil.paperlaunch;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toolbar;

import de.devmil.paperlaunch.view.fragments.EditFolderFragment;

public class EditFolderActivity extends Activity {

    private static final String ARG_FOLDERID = "folderId";

    private Toolbar mToolbar;

    public static Intent createLaunchIntent(Context context, long folderId) {
        Intent result = new Intent(context, EditFolderActivity.class);
        result.putExtra(ARG_FOLDERID, folderId);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_folder);

        mToolbar = (Toolbar)findViewById(R.id.activity_edit_folder_toolbar);

        setActionBar(mToolbar);

        long folderId = -1;
        if(getIntent().hasExtra(ARG_FOLDERID)) {
            folderId = getIntent().getLongExtra(ARG_FOLDERID, -1);
        }

        EditFolderFragment fragment = null;

        if(savedInstanceState  == null) {
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.add(R.id.activity_edit_folder_folder_fragment, fragment = EditFolderFragment.newInstance(folderId));
            trans.commit();
        } else {
            fragment = (EditFolderFragment)getFragmentManager().findFragmentById(R.id.activity_edit_folder_folder_fragment);
        }

        if(fragment != null) {
            fragment.setListener(new EditFolderFragment.IEditFolderFragmentListener() {
                @Override
                public void onFolderNameChanged(String newName) {
                    setTitle(newName);
                }
            });
        }
    }

}
