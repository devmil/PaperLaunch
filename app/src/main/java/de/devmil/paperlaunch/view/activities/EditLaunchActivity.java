package de.devmil.paperlaunch.view.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.storage.LaunchDTO;
import de.devmil.paperlaunch.view.fragments.EditLaunchFragment;

public class EditLaunchActivity extends Activity implements EditLaunchFragment.OnFragmentInteractionListener {

    private static final String EXTRA_LAUNCH = "de.devmil.paperlaunch.view.activities.EditLaunchActivity.EXTRA_LAUNCH";

    public static Intent createLaunchIntent(Context context, LaunchDTO launchToEdit) {
        Intent result = new Intent();
        result.setClass(context, EditLaunchActivity.class);
        result.putExtra(EXTRA_LAUNCH, launchToEdit.serialize());

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_launch);

        String serializedLaunch = getIntent().getStringExtra(EXTRA_LAUNCH);
        LaunchDTO launchDTO = LaunchDTO.deserialize(serializedLaunch);

        EditLaunchFragment fragment = (EditLaunchFragment)getFragmentManager().findFragmentById(R.id.activity_edit_launch_fragment);
        fragment.setLaunch(launchDTO);
    }

    @Override
    public void onLaunchChanged(LaunchDTO launch) {

    }
}
