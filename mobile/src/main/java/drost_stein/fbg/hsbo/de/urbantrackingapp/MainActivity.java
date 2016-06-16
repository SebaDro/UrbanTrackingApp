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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.JodaTimeAndroid;

import java.lang.reflect.Type;
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

    private Menu mMenu;
    private static boolean mGPSActive = false;
    private StartFragment mStartFragment;
    private SettingsFragment mSettingsFragment;

    private MapFragment mMapFragment;
    private MapView mMapView;
    private GeodatabaseFeatureServiceTable mFeatureServiceTable;
    private FeatureLayer mTrackPointFeatureLayer;


    private ArrayList<Track> unSyncedTracks;

    private TrackPointSource trackPointsource;
    private NetworkManager mNetworkManager;
    private GsonBuilder gsonBuilder;

    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_LOCATION = PACKAGE_NAME + ".BROADCAST_LOCATION";

    private static final String EXTENDED_DATA_LOCATION = PACKAGE_NAME + ".DATA_LOCATION";
    private static final String BROADCAST_ACTION_TRACK=PACKAGE_NAME + ".BROADCAST_TRACK";
    private static final String EXTENDED_DATA_TRACK=PACKAGE_NAME + ".DATA_TRACK";

    public static final String PREFS_UPDATE_INTERVAL_KEY = "updateInterval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationServiceIntent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);

        TrackPointResponseReceiver trackPointResponseReceiver = new TrackPointResponseReceiver();
        IntentFilter trackPointIntentFilter = new IntentFilter(BROADCAST_ACTION_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                trackPointResponseReceiver, trackPointIntentFilter);

        TrackResponseReceiver trackResponseReceiver=new TrackResponseReceiver();
        IntentFilter trackIntentFilter = new IntentFilter(BROADCAST_ACTION_TRACK);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                trackResponseReceiver, trackIntentFilter);


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
        mNetworkManager = new NetworkManager(ConnectivityManager.TYPE_MOBILE, getApplicationContext());
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Track.class, new TrackSerializer());
        gsonBuilder.registerTypeAdapter(Track.class, new TrackDeserializer());

        JodaTimeAndroid.init(this);
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
            mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_fixed_white_48dp));
            mGPSActive = true;
            mLocationServiceIntent.putExtra("type", "start");
            mLocationServiceIntent.putExtra("updateInterval", getUpdateIntervalFromPreferences());
            startService(mLocationServiceIntent);
        }
    }

    /**
     * Stops the tracking
     */
    public void stopTracking() {
        mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_off_white_48dp));
        mGPSActive = false;
        mLocationServiceIntent.putExtra("type", "end");
        startService(mLocationServiceIntent);
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
                            trackPointsource = new TrackPointSource(mFeatureServiceTable);
                            mTrackPointFeatureLayer = new FeatureLayer(mFeatureServiceTable);
                            mMapView.addLayer(mTrackPointFeatureLayer);
                        }
                    }
                });
    }

    /**
     * gets the update interval for position requests
     *
     * @return update interval
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

    @Override
    protected void onStop() {
        super.onStop();
        Gson gson = gsonBuilder.create();
        String jsonTracks = gson.toJson(unSyncedTracks);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preference_file_key_tracks), jsonTracks);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        String jsonTracks = sharedPref.getString(getString(R.string.preference_file_key_tracks), null);
        if (jsonTracks != null) {
            Gson gson = gsonBuilder.create();
            Type collectionType = new TypeToken<ArrayList<Track>>() {
            }.getType();
            unSyncedTracks = gson.fromJson(jsonTracks, collectionType);

            String info = unSyncedTracks.size() + " tracks have been restored.";
            Toast toast = Toast.makeText(mMapFragment.getContext(), info, Toast.LENGTH_LONG);
            toast.show();
        }
    }


    private class TrackPointResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackPoint point = (TrackPoint) intent.getExtras().get(EXTENDED_DATA_LOCATION);

            mStartFragment.updateTrackPoint(point);
        }
    }

    private class TrackResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Track track = (Track) intent.getExtras().get(EXTENDED_DATA_TRACK);
            if (mNetworkManager.isOnline()) {
                //TODO uploading tracks
            } else {
                if (unSyncedTracks == null) {
                    unSyncedTracks = new ArrayList<Track>();
                }
                unSyncedTracks.add(track);
            }
            String trackInfo = track.getTrackPoints().size() + " points have been tracked.";
            Toast toast = Toast.makeText(mMapFragment.getContext(), trackInfo, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
