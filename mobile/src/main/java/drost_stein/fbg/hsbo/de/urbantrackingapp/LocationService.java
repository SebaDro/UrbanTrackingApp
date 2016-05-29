package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends IntentService {

    private static MyLocationListener myLocationListener;
    private static final String BROADCAST_ACTION = "drost_stein.fbg.hsbo.de.urbantrackingapp.BROADCAST";
    private static final String EXTENDED_DATA_LOCATION = "drost_stein.fbg.hsbo.de.urbantrackingapp.DATA";

    public LocationService() {
        super("Location Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String type = intent.getStringExtra("type");
        if (myLocationListener == null) {
            myLocationListener = new MyLocationListener(this);
        }

        switch (type) {
            case "start":
                myLocationListener.startLocationUpdates();
                break;
            case "end":
                myLocationListener.stopLocationUpdates();
            default:
        }
    }

    public void sendLocation(Location location) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the location into the Intent
                        .putExtra(EXTENDED_DATA_LOCATION, location);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private class MyLocationListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
        private GoogleApiClient mGoogleApiClient;
        private Location mLastLocation;
        private LocationRequest mLocationRequest;
        private LocationService mLocationService;

        private static final long UPDATE_INTERVAL_MS = 5 * 1000;
        private static final long FASTEST_INTERVAL_MS = 5 * 1000;

        public MyLocationListener(LocationService locationService) {

            mLocationService = locationService;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL_MS)
                    .setFastestInterval(FASTEST_INTERVAL_MS);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            mLocationService.sendLocation(location);
        }

        public void startLocationUpdates() {
            mGoogleApiClient.connect();
        }

        public void stopLocationUpdates() {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
            mGoogleApiClient.disconnect();
        }
    }
}
