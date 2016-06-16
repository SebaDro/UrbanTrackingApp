package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;

import java.util.ArrayList;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * Service that controls the tracking
 * Created by Matthias Stein.
 */
public class LocationService extends Service {

    private static MyLocationListener myLocationListener;
    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_TRACK_POINT = PACKAGE_NAME + ".BROADCAST_TRACK_POINT";
    private static final String EXTENDED_DATA_TRACK_POINT = PACKAGE_NAME + ".DATA_TRACK_POINT";
    private static final String BROADCAST_ACTION_ACTIVITY = PACKAGE_NAME + ".BROADCAST_ACTIVITY";
    private static final String EXTENDED_DATA_ACTIVITY = PACKAGE_NAME + ".DATA_ACTIVITY";
    private static final String BROADCAST_ACTION_TRACK = PACKAGE_NAME + ".BROADCAST_TRACK";
    private static final String EXTENDED_DATA_TRACK = PACKAGE_NAME + ".DATA_TRACK";

    private Track mCurrentTrack;
    private DetectedActivity mCurrentActivity;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int updateInterval = intent.getIntExtra("updateInterval", 10000);

        if (myLocationListener == null) {
            myLocationListener = new MyLocationListener(this);
        }

        myLocationListener.setUpdateInterval(updateInterval);

        ActivitiesResponseReceiver activitiesResponseReceiver = new ActivitiesResponseReceiver();
        IntentFilter activityIntentFilter = new IntentFilter(BROADCAST_ACTION_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                activitiesResponseReceiver, activityIntentFilter);
        DateTime startTime = DateTime.now();
        mCurrentTrack = new Track(1, 1, startTime);
        myLocationListener.startLocationUpdates();

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        myLocationListener.stopLocationUpdates();
        DateTime endTime = DateTime.now();
        mCurrentTrack.setEndTime(endTime);
        sendTrack(mCurrentTrack);
    }

    public void sendTrackPoint(TrackPoint point) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION_TRACK_POINT)
                        // Puts the location into the Intent
                        .putExtra(EXTENDED_DATA_TRACK_POINT, point);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void sendTrack(Track track) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION_TRACK)
                        // Puts the location into the Intent
                        .putExtra(EXTENDED_DATA_TRACK, track);
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

        public void setUpdateInterval(int updateInterval) {
            UPDATE_INTERVAL_MS = updateInterval;
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
            String activity = "UNKNOWN";
            TrackPoint point = null;
            if (mCurrentActivity != null) {
                activity = getDetectedActivity(mCurrentActivity.getType());
            }
            if (mLastLocation != null) {
                DateTime time = new DateTime(location.getTime());
                point = new TrackPoint(location.getTime(), 2l, location.getLatitude(), location.getLongitude(),
                        location.getAltitude(), location.getBearing(), location.getAccuracy(), time, activity);
                mCurrentTrack.addTrackPoint(point);
            }
            mLocationService.sendTrackPoint(point);
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
    }

    private class ActivitiesResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(EXTENDED_DATA_ACTIVITY);
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
            mCurrentActivity = likelyActivity;
        }
    }
}
