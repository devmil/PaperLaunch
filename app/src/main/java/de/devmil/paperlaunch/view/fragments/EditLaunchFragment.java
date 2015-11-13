package de.devmil.paperlaunch.view.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.storage.LaunchDTO;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditLaunchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditLaunchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditLaunchFragment extends Fragment {
    private static final String ARG_LAUNCH = "paramLaunch";

    private Launch mLaunch;

    private OnFragmentInteractionListener mListener;
    private TextView mTxtName;
    private ImageView mImgIcon;
    private ImageView mImgApp;
    private TextView mTxtLaunch;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param launchDTO the Launch to edit.
     * @return A new instance of fragment EditLaunchFragment.
     */
    public static EditLaunchFragment newInstance(LaunchDTO launchDTO) {
        EditLaunchFragment fragment = new EditLaunchFragment();
        fragment.setArguments(createArgumentsBundle(launchDTO));
        return fragment;
    }

    public static Bundle createArgumentsBundle(LaunchDTO launchDTO) {
        Bundle args = new Bundle();
        args.putString(ARG_LAUNCH, launchDTO.serialize());

        return args;
    }

    public EditLaunchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            String serializedLaunch = getArguments().getString(ARG_LAUNCH);
            if(serializedLaunch != null) {
                LaunchDTO dto = LaunchDTO.deserialize(serializedLaunch);
                setLaunch(dto);
            }
        }
    }

    public void setLaunch(LaunchDTO launchDTO) {
        mLaunch = new Launch(launchDTO);
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_launch, container, false);

        mTxtName = (TextView)view.findViewById(R.id.fragment_edit_launch_txtName);
        mImgIcon = (ImageView)view.findViewById(R.id.fragment_edit_launch_imgIcon);
        mImgApp = (ImageView)view.findViewById(R.id.fragment_edit_launch_imgApp);
        mTxtLaunch = (TextView)view.findViewById(R.id.fragment_edit_launch_txtLaunch);

        updateView();

        return view;
    }

    private void updateView() {
        if(mLaunch == null) {
            return;
        }
        if(mTxtLaunch == null) {
            return;
        }
        mTxtName.setText(mLaunch.getName(getActivity()));
        mImgIcon.setImageDrawable(mLaunch.getIcon(getActivity()));
        mImgApp.setImageDrawable(mLaunch.getAppIcon(getActivity()));
        mTxtLaunch.setText(mLaunch.getLaunchIntent().getComponent().getClassName());
    }

    private void notifyChanged() {
        if (mListener != null) {
            mListener.onLaunchChanged(mLaunch.getDto());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onLaunchChanged(LaunchDTO launch);
    }

    //TODO: save and load instance state
}
