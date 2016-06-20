package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.MapView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SeekBar mUpdateIntervalSeekBar;
    private Button mResetButton;
    private Button mUploadButton;
    private View mView;
    private int seekBarMax = 360000;
    private int seekBarMin = 5000;
    private static final String PREFS_UPDATE_INTERVAL_KEY = "updateInterval";
    private static final String PREFS_USER_ID_KEY = "userId";
    private static final String PREFS_NAME = "urbanTrackingPrefs";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        mUpdateIntervalSeekBar = (SeekBar) mView.findViewById(R.id.seekBar);

        mUpdateIntervalSeekBar.setMax(seekBarMax - seekBarMin);

        mUpdateIntervalSeekBar.setProgress(seekBarMax - getUpdateIntervalFromPreferences());

        mUpdateIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int updateInterval = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateInterval = seekBarMax - progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences sharedPref = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(PREFS_UPDATE_INTERVAL_KEY, updateInterval);
                editor.commit();
                Toast.makeText(getActivity(), getString(R.string.update_interval) + ": " + updateInterval / 1000 + " " + getString(R.string.seconds),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mUploadButton=(Button) mView.findViewById(R.id.uploadButton);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsFragmentUploadTracks();
            }
        });

        return mView;
    }

    public void setUnsyncedTracksCount(int count){
        TextView countTracks=(TextView)mView.findViewById(R.id.count_unsynced_tracks);
        countTracks.setText(String.valueOf(count));
    }

    public int getUpdateIntervalFromPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int updateInterval = sharedPref.getInt(PREFS_UPDATE_INTERVAL_KEY, 10000);
        return updateInterval;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onSettingsFragmentUploadTracks();
    }
}
