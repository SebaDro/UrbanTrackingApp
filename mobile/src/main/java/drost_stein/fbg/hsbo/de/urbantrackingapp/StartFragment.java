package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

import org.joda.time.DateTime;

import drost_stein.fbg.hsbo.de.urbantrackingapp.databinding.FragmentStartBinding;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;
import drost_stein.fbg.hsbo.de.urbantrackingapp.viewmodel.StartFragmentViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private StartFragmentViewModel startFragmentVM;

    private OnFragmentInteractionListener mListener;
    private TrackPoint mCurrentTrackPoint;
    private String mCurrentActivity;
    private Switch mSwitch;

    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentStartBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false);
        View view = binding.getRoot();
        startFragmentVM = new StartFragmentViewModel();
        mCurrentTrackPoint = startFragmentVM.getTrackPoint();
        binding.setStartVM(startFragmentVM);

        mSwitch = (Switch) view.findViewById(R.id.switch_Tracking);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handleTrackingSwitchChecked();
                } else {
                    handleTrackingSwitchUnChecked();
                }
            }
        });
        return view;
    }


    private void handleTrackingSwitchUnChecked() {
        mSwitch.setText(R.string.switchStartTracking);
        ((MainActivity) getActivity()).stopTracking();
    }

    private void handleTrackingSwitchChecked() {
        mSwitch.setText(R.string.switchStopTracking);
        ((MainActivity) getActivity()).startTracking();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void updatePointLocation(Location location) {
        DateTime time = new DateTime(location.getTime());
        TrackPoint point = new TrackPoint(location.getTime(), 2l, location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getBearing(), location.getAccuracy(), time, mCurrentActivity);
        startFragmentVM.setTrackPoint(point);
    }

    public void updatePointActivities(int activity) {
        this.mCurrentActivity = getDetectedActivity(activity);
    }

    /**
     * sets the image in the fragment and returns a string representation for the type of the detected activty
     *
     * @param detectedActivityType type of the detected activity
     * @return string representation for the detected activity
     */
    public String getDetectedActivity(int detectedActivityType) {
        Resources resources = this.getResources();
        ImageView image = (ImageView) getView().findViewById(R.id.activityImage);
        TextView text = (TextView) getView().findViewById(R.id.activityText);
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                image.setImageResource(R.drawable.ic_directions_car);
                text.setText(resources.getString(R.string.in_vehicle));
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                image.setImageResource(R.drawable.ic_directions_bike);
                text.setText(resources.getString(R.string.on_bicycle));
                return "ON_BYCICLE";
            case DetectedActivity.ON_FOOT:
                image.setImageResource(R.drawable.ic_directions_walk);
                text.setText(resources.getString(R.string.on_foot));
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                image.setImageResource(R.drawable.ic_directions_run);
                text.setText(resources.getString(R.string.running));
                return "RUNNING";
            case DetectedActivity.WALKING:
                image.setImageResource(R.drawable.ic_directions_walk);
                text.setText(resources.getString(R.string.walking));
                return "WALKING";
            case DetectedActivity.STILL:
                image.setImageResource(R.drawable.ic_person);
                text.setText(resources.getString(R.string.still));
                return "STILL";
            case DetectedActivity.TILTING:
                image.setImageResource(R.drawable.ic_screen_rotation);
                text.setText(resources.getString(R.string.tilting));
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                image.setImageResource(R.drawable.ic_do_not_disturb);
                text.setText(resources.getString(R.string.unknown));
                return "UNKNOWN";
            default:
                image.setImageResource(R.drawable.ic_do_not_disturb);
                text.setText(resources.getString(R.string.unknown));
                return "UNKNOWN";
        }
    }
}
