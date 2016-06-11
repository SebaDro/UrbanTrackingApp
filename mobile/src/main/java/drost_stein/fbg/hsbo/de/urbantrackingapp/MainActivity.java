package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.map.CallbackListener;
import com.google.android.gms.location.DetectedActivity;

import org.joda.time.DateTime;

import java.util.ArrayList;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;


public class MainActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        StartFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    private Intent mLocationServiceIntent;
    private Location mLastLocation;
    private DetectedActivity mLikelyActivity;
    private Menu mMenu;
    private static boolean mGPSActive = false;
    private StartFragment mStartFragment;
    private SettingsFragment mSettingsFragment;

    private MapFragment mMapFragment;
    private MapView mMapView;
    private GeodatabaseFeatureServiceTable mFeatureServiceTable;
    private FeatureLayer mTrackPointFeatureLayer;

    private Track currentTrack;
    private ArrayList<Track> unSyncedTracks;

    private TrackPointSource trackPointsource;
    private NetworkManager mNetworkManager;

    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_LOCATION = PACKAGE_NAME + ".BROADCAST_LOCATION";
    private static final String BROADCAST_ACTION_ACTIVITIES = PACKAGE_NAME + ".BROADCAST_ACTIVITIES";
    private static final String EXTENDED_DATA_LOCATION = PACKAGE_NAME + ".DATA_LOCATION";
    private static final String EXTENDED_DATA_ACTIVITIES = PACKAGE_NAME + ".DATA_ACTIVITIES";

    public static final String PREFS_UPDATE_INTERVAL_KEY = "updateInterval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationServiceIntent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);

        LocationResponseReceiver locationResponseReceiver = new LocationResponseReceiver();
        IntentFilter intentFilter1 = new IntentFilter(BROADCAST_ACTION_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationResponseReceiver, intentFilter1);

        ActivitiesResponseReceiver activitiesResponseReceiver = new ActivitiesResponseReceiver();
        IntentFilter intentFilter2 = new IntentFilter(BROADCAST_ACTION_ACTIVITIES);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                activitiesResponseReceiver, intentFilter2);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            mStartFragment = new StartFragment();
            mSettingsFragment = new SettingsFragment();
            mMapFragment = new MapFragment();

            transaction.add(R.id.content_frame, mStartFragment, "start_fragment");
            transaction.add(R.id.content_frame, mSettingsFragment, "settings_fragment");
            transaction.add(R.id.content_frame, mMapFragment, "map_fragment");
            transaction.hide(mSettingsFragment);
            transaction.hide(mMapFragment);
            transaction.commit();
        }
        mNetworkManager=new NetworkManager(ConnectivityManager.TYPE_MOBILE,getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        if (mGPSActive == true) {
            mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_off_white_48dp));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                hideAllFragments();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.show(mSettingsFragment);
                transaction.commit();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id) {
            case R.id.nav_start:
                hideAllFragments();
                transaction.show(mStartFragment);
                break;
            case R.id.nav_settings:
                hideAllFragments();
                transaction.show(mSettingsFragment);
                break;
            case R.id.nav_map:
                hideAllFragments();
                transaction.show(mMapFragment);
            default:
        }

        transaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Starts the tracking if the permission was granted
     */
    public void startTracking() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            DateTime startTime=DateTime.now();
            currentTrack=new Track(1,1,startTime);
            mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_fixed_white_48dp));
            mGPSActive = true;
            mLocationServiceIntent.putExtra("type", "start");
            mLocationServiceIntent.putExtra("detectionRate", getUpdateIntervalFromPreferences());
            startService(mLocationServiceIntent);
        }
    }

    /**
     * Stops the tracking
     */
    public void stopTracking() {
        DateTime endTime=DateTime.now();
        currentTrack.setEndTime(endTime);
        mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_off_white_48dp));
        mGPSActive = false;
        mLocationServiceIntent.putExtra("type", "end");
        startService(mLocationServiceIntent);

        String trackInfo = currentTrack.getTrackPoints().size()+" points have been tracked.";
        Toast toast = Toast.makeText(mMapFragment.getContext(), trackInfo, Toast.LENGTH_LONG);
        toast.show();

    }

    /**
     * Adds the Feature-Service for trackpoints to the map
     */
    private void addTrackPointFeatureService() {
        String serviceURL = "http://services6.arcgis.com/RF3oqOe1dChQus9k/arcgis/rest/services/UrbanTrackPoints/FeatureServer";
        mFeatureServiceTable = new GeodatabaseFeatureServiceTable(serviceURL, 0);
        mFeatureServiceTable.initialize(
                new CallbackListener<GeodatabaseFeatureServiceTable.Status>() {

                    @Override
                    public void onError(Throwable e) {
                        String error = mFeatureServiceTable.getInitializationError();
                        Toast toast = Toast.makeText(mMapFragment.getContext(), error, Toast.LENGTH_LONG);
                        toast.show();
                    }

                    @Override
                    public void onCallback(GeodatabaseFeatureServiceTable.Status status) {
                        if (status == GeodatabaseFeatureServiceTable.Status.INITIALIZED) {
                            trackPointsource=new TrackPointSource(mFeatureServiceTable);
                            mTrackPointFeatureLayer = new FeatureLayer(mFeatureServiceTable);
                            mMapView.addLayer(mTrackPointFeatureLayer);
                        }
                    }
                });
    }

    /**
     * gets the update intervall for position requests
     * @return update intervall
     */
    public int getUpdateIntervalFromPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int updateInterval = sharedPref.getInt(PREFS_UPDATE_INTERVAL_KEY, 10000);
        return updateInterval;
    }

    public void hideAllFragments() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : fm.getFragments()) {
            if (fragment.isVisible()) {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onMapFragmentGetMapView(MapView mapView) {
        mMapView = mapView;
        addTrackPointFeatureService();
    }

    @Override
    public void onStartFragmentStartTracking() {
        startTracking();
    }

    @Override
    public void onStartFragmentStopTracking() {
        stopTracking();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startTracking();
                } else {
                    // permission denied, boo!
                }
                return;
            }
        }
    }

    /**
     * gets a string representation for the type of the detected activty
     *
     * @param detectedActivityType type of the detected activity
     * @return string representation for the detected activity
     */
    public String getDetectedActivity(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BYCICLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            default:
                return "UNKNOWN";
        }
    }

    private class LocationResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getExtras().get(EXTENDED_DATA_LOCATION);
            mLastLocation = location;
            String activity = "UNKNOWN";
            if (mLikelyActivity != null) {
                activity = getDetectedActivity(mLikelyActivity.getType());
            }
            if (mLastLocation != null) {
                DateTime time = new DateTime(location.getTime());
                TrackPoint point = new TrackPoint(location.getTime(), 2l, location.getLatitude(), location.getLongitude(),
                        location.getAltitude(), location.getBearing(), location.getAccuracy(), time, activity);
                currentTrack.addTrackPoint(point);
                mStartFragment.updatePointLocation(point);
            }
        }
    }


    private class ActivitiesResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(EXTENDED_DATA_ACTIVITIES);
            DetectedActivity likelyActivity = null;
            for (DetectedActivity activity : detectedActivities) {
                if (likelyActivity == null) {
                    likelyActivity = activity;
                } else {
                    if (likelyActivity.getConfidence() < activity.getConfidence()) {
                        likelyActivity = activity;
                    }
                }
            }
            mLikelyActivity = likelyActivity;
            if (likelyActivity != null)
                mStartFragment.updatePointActivities(likelyActivity.getType());
        }
    }

}
