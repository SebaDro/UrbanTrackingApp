package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StartFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {

    private Intent mLocationServiceIntent;
    private Location mLastLocation;
    private Menu mMenu;
    private static boolean mGPSActive = false;

    private static final String BROADCAST_ACTION = "drost_stein.fbg.hsbo.de.urbantrackingapp.BROADCAST";
    private static final String EXTENDED_DATA_LOCATION = "drost_stein.fbg.hsbo.de.urbantrackingapp.DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationServiceIntent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);

        ResponseReceiver responseReceiver = new ResponseReceiver();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                responseReceiver, intentFilter);

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
            StartFragment startFragment = new StartFragment();
            SettingsFragment settingsFragment = new SettingsFragment();
            transaction.add(R.id.content_frame, startFragment, "start_fragment");
            transaction.add(R.id.content_frame, settingsFragment, "settings_fragment");
            transaction.hide(settingsFragment);
            transaction.commit();
        }
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

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag("settings_fragment");

        switch (id) {
            case R.id.action_settings:
                hideAllFragments();
                transaction.show(settingsFragment);
                break;
            case R.id.action_gps:
                if (mGPSActive == false) {
                    mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_off_white_48dp));
                    mGPSActive = true;
                    mLocationServiceIntent.putExtra("type", "start");
                    startService(mLocationServiceIntent);
                } else if (mGPSActive == true) {
                    mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_gps_fixed_white_48dp));
                    mGPSActive = false;
                    mLocationServiceIntent.putExtra("type", "end");
                    startService(mLocationServiceIntent);
                }
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

        StartFragment startFragment = (StartFragment) fm.findFragmentByTag("start_fragment");
        SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag("settings_fragment");

        switch (id) {
            case R.id.nav_start:
                hideAllFragments();
                transaction.show(startFragment);
                break;
            case R.id.nav_settings:
                hideAllFragments();
                transaction.show(settingsFragment);
                break;
            default:
        }

        transaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public void updateCoordinateText() {
        TextView coordinateText = (TextView) findViewById(R.id.settings_fragment_text);
        if (coordinateText != null) {
            coordinateText.setText(mLastLocation.getLongitude() + " " + mLastLocation.getLatitude());
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getExtras().get(EXTENDED_DATA_LOCATION);
            mLastLocation = location;
            updateCoordinateText();
        }
    }


}