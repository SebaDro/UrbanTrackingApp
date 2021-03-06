package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import drost_stein.fbg.hsbo.de.urbantrackingapp.databinding.FragmentStartBinding;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;
import drost_stein.fbg.hsbo.de.urbantrackingapp.viewmodel.StartFragmentViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StartFragment extends Fragment {

    private StartFragmentViewModel startFragmentVM;

    private OnFragmentInteractionListener mListener;
    private TrackPoint mCurrentTrackPoint;
    private Switch mSwitchTracking;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentStartBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false);
        View view = binding.getRoot();
        startFragmentVM = new StartFragmentViewModel();
        mCurrentTrackPoint = startFragmentVM.getTrackPoint();
        binding.setStartVM(startFragmentVM);

        mSwitchTracking = (Switch) view.findViewById(R.id.switch_Tracking);
        mSwitchTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handleTrackingSwitchChecked();
                } else {
                    handleTrackingSwitchUnChecked();
                }
            }
        });
        if (mListener != null) {
            mListener.onStartFragmentStarted();
        }
        return view;
    }

    public void enableSwitch() {
        mSwitchTracking.setOnCheckedChangeListener(null);
        mSwitchTracking.setChecked(true);
        mSwitchTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handleTrackingSwitchChecked();
                } else {
                    handleTrackingSwitchUnChecked();
                }
            }
        });
    }


    /**
     * sets the text of the tracking switch and notifies the MainActivity for unchecking the switch
     * to stop the tracking
     */
    private void handleTrackingSwitchUnChecked() {
        mSwitchTracking.setText(R.string.switchStartTracking);
        if (mListener != null) {
            mListener.onStartFragmentStopTracking();
        }
    }

    /**
     * sets the text of the tracking switch and notifies the MainActivity for checking the switch
     * to start the tracking
     */
    private void handleTrackingSwitchChecked() {
        mSwitchTracking.setText(R.string.switchStopTracking);
        if (mListener != null) {
            mListener.onStartFragmentStartTracking();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateTrackPoint(TrackPoint point) {
        handleDetectedActivity(point.getTypeOfMovement());
        startFragmentVM.setTrackPoint(point);
    }

    public void updateAddress(Address address){
        String addressString = "";
        if (address == null) {
            addressString = getString(R.string.unknown_address);
        } else {
            addressString = address.getAddressLine(0)+", " +address.getAddressLine(1);
        }


        if (getView()!=null){
            TextView addressView = (TextView) getView().findViewById(R.id.address);
            addressView.setText(addressString);
        }

    }

    /**
     * sets the image and text on the screen for the type of the detected activty
     *
     * @param typeOfMovement type of the detected activity
     */
    public void handleDetectedActivity(String typeOfMovement) {
        if (isAdded()) {
            Resources resources = this.getResources();
            ImageView image = (ImageView) getView().findViewById(R.id.activityImage);
            TextView text = (TextView) getView().findViewById(R.id.activityText);
            switch (typeOfMovement) {
                case "IN_VEHICLE":
                    image.setImageResource(R.drawable.ic_directions_car);
                    text.setText(resources.getString(R.string.in_vehicle));
                    break;
                case "ON_BICYCLE":
                    image.setImageResource(R.drawable.ic_directions_bike);
                    text.setText(resources.getString(R.string.on_bicycle));
                    break;
                case "ON_FOOT":
                    image.setImageResource(R.drawable.ic_directions_walk);
                    text.setText(resources.getString(R.string.on_foot));
                    break;
                case "RUNNING":
                    image.setImageResource(R.drawable.ic_directions_run);
                    text.setText(resources.getString(R.string.running));
                    break;
                case "WALKING":
                    image.setImageResource(R.drawable.ic_directions_walk);
                    text.setText(resources.getString(R.string.walking));
                    break;
                case "STILL":
                    image.setImageResource(R.drawable.ic_person);
                    text.setText(resources.getString(R.string.still));
                    break;
                case "TILTING":
                    image.setImageResource(R.drawable.ic_screen_rotation);
                    text.setText(resources.getString(R.string.tilting));
                    break;
                case "UNKNOWN":
                    image.setImageResource(R.drawable.ic_do_not_disturb);
                    text.setText(resources.getString(R.string.unknown));
                    break;
                default:
                    image.setImageResource(R.drawable.ic_do_not_disturb);
                    text.setText(resources.getString(R.string.unknown));
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onStartFragmentStarted();

        void onStartFragmentStartTracking();

        void onStartFragmentStopTracking();
    }
}
