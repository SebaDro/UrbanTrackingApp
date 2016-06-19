package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WearMainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private static final String TAG = "WearableActivity";
    private static final String HANDLE_TRACKING_PATH = "/handle_tracking";

    private static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String LATITUDE_KEY = PACKAGE_NAME + ".latitude";
    private static final String LONGITUDE_KEY = PACKAGE_NAME + ".longitude";
    private static final String TIME_KEY = PACKAGE_NAME + ".time";
    private static final String ACTIVITY_KEY = PACKAGE_NAME + ".activity";
    private static final String SPEED_KEY = PACKAGE_NAME + ".speed";

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private GoogleApiClient mGoogleApiClient;
    private Node mNode;
    private BoxInsetLayout mContainerView;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Intent i = new Intent();
        i.setAction("drost_stein.fbg.hsbo.de.urbantrackingapp.SHOW_NOTIFICATION");
        i.putExtra(MyPostNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);*/

        setContentView(R.layout.activity_wear_main);

        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new GridViewPagerAdapter());
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    public void showOpenOnPhoneConfirmationActivity(String s) {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, s);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Adjusts the clock view and the container background color in case of ambiend mode.
     */
    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.background));
            mClockView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/trackPoint") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    TextView coordinateTextView = (TextView) findViewById(R.id.coordinates);
                    TextView timeTextView = (TextView) findViewById(R.id.time);
                    TextView activityTextView = (TextView) findViewById(R.id.activity);
                    TextView speedTextView = (TextView) findViewById(R.id.speed);
                    coordinateTextView.setText(String.valueOf(dataMap.getDouble(LATITUDE_KEY)) + "," + String.valueOf(dataMap.getDouble(LONGITUDE_KEY)));
                    timeTextView.setText(dataMap.getString(TIME_KEY));
                    activityTextView.setText(dataMap.getString(ACTIVITY_KEY));
                    speedTextView.setText(String.valueOf(dataMap.getDouble(SPEED_KEY)));
                }
            }
        }
    }

    /*
     * GridViewAdapeter Class
     */
    public class GridViewPagerAdapter extends GridPagerAdapter {
        @Override
        public int getColumnCount(int arg0) {
            return 2;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int row, int col) {
            if (col == 0) {
                final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid_view_page_1, container, false);
                container.addView(view);
                return view;
            } else if (col == 1) {
                final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid_view_page_2, container, false);
                final CircledImageView trackingButton = (CircledImageView) view.findViewById(R.id.trackingButton);

                trackingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                container.addView(view);
                return view;
            } else {
                return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int row, int col, Object view) {
            container.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
