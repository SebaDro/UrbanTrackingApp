package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Service that controls the tracking
 * Created by Matthias Stein.
 */
public class LocationService extends IntentService {

    private static MyLocationListener myLocationListener;
    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_LOCATION = PACKAGE_NAME + ".BROADCAST_LOCATION";
    private static final String EXTENDED_DATA_LOCATION = PACKAGE_NAME + ".DATA_LOCATION";

    public LocationService() {
        super("Location Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String type = intent.getStringExtra("type");
        int detectionRate = intent.getIntExtra("detectionRate", 10000);

        if (myLocationListener == null) {
            myLocationListener = new MyLocationListener(this);
        }

        myLocationListener.setDetectionRate(detectionRate);

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
                new Intent(BROADCAST_ACTION_LOCATION)
                        // Puts the location into the Intent
                        .putExtra(EXTENDED_DATA_LOCATION, location);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private class MyLocationListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {
        private GoogleApiClient mGoogleApiClient;
        private Location mLastLocation;
        private LocationRequest mLocationRequest;
        private LocationService mLocationService;

        private long UPDATE_INTERVAL_MS = 10 * 1000;

        public MyLocationListener(LocationService locationService) {

            mLocationService = locationService;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .addApi(ActivityRecognition.API)
                        .build();
            }
        }

        public void setDetectionRate(int detectionRate) {
            UPDATE_INTERVAL_MS = detectionRate;
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
                    .setFastestInterval(UPDATE_INTERVAL_MS);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            ActivityRecognition.ActivityRecognitionApi
                    .requestActivityUpdates(mGoogleApiClient, UPDATE_INTERVAL_MS, getActivityDetectionPendingIntent()).setResultCallback(this);
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

        public void onResult(Status status) {
            if (status.isSuccess()) {
                //Log.e(TAG, "Successfully added activity detection.");
            } else {
                //Log.e(TAG, "Error: " + status.getStatusMessage());
            }
        }

        public void startLocationUpdates() {
            mGoogleApiClient.connect();
        }

        public void stopLocationUpdates() {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendingIntent());
            }
            mGoogleApiClient.disconnect();
        }

        private PendingIntent getActivityDetectionPendingIntent() {
            Intent intent = new Intent(getBaseContext(), ActivitiesIntentService.class);
            return PendingIntent.getService(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}
