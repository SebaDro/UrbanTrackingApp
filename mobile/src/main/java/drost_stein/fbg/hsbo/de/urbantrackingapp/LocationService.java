package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.joda.time.DateTime;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * Service that controls the tracking
 * Created by Matthias Stein.
 */
public class LocationService extends Service {

    private static MyLocationListener myLocationListener;
    private static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_TRACK_POINT = PACKAGE_NAME + ".BROADCAST_TRACK_POINT";
    private static final String EXTENDED_DATA_TRACK_POINT = PACKAGE_NAME + ".DATA_TRACK_POINT";
    private static final String BROADCAST_ACTION_ACTIVITY = PACKAGE_NAME + ".BROADCAST_ACTIVITY";
    private static final String EXTENDED_DATA_ACTIVITY = PACKAGE_NAME + ".DATA_ACTIVITY";
    private static final String BROADCAST_ACTION_TRACK = PACKAGE_NAME + ".BROADCAST_TRACK";
    private static final String EXTENDED_DATA_TRACK = PACKAGE_NAME + ".DATA_TRACK";


    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    private static final String LATITUDE_KEY = PACKAGE_NAME + ".latitude";
    private static final String LONGITUDE_KEY = PACKAGE_NAME + ".longitude";
    private static final String TIME_KEY = PACKAGE_NAME + ".time";
    private static final String ACTIVITY_KEY = PACKAGE_NAME + ".activity";
    private static final String SPEED_KEY = PACKAGE_NAME + ".speed";


    private Track mCurrentTrack;
    private TrackPoint mCurrentTrackPoint;
    private DetectedActivity mCurrentActivity;
    private String mUserID;
    private long trackPointCounter;

    private NotificationManager mNM;


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int updateInterval = intent.getIntExtra("updateInterval", 10000);
        mUserID = intent.getStringExtra("userId");

        myLocationListener = new MyLocationListener(this);

        myLocationListener.setUpdateInterval(updateInterval);

        ActivitiesResponseReceiver activitiesResponseReceiver = new ActivitiesResponseReceiver();
        IntentFilter activityIntentFilter = new IntentFilter(BROADCAST_ACTION_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                activitiesResponseReceiver, activityIntentFilter);

        trackPointCounter = 0;
        DateTime startTime = DateTime.now();
        mCurrentTrack = new Track(startTime.getMillis(), mUserID, startTime);
        myLocationListener.startLocationUpdates();

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onDestroy() {
        myLocationListener.stopLocationUpdates();
        mNM.cancelAll();
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

    public void requestLastTrackPoint() {
        if (mCurrentTrackPoint != null)
            sendTrackPoint(mCurrentTrackPoint);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.location_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.urban_tracker_app_logo)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(R.string.location_service_started, notification);
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
                        //.addApi(Wearable.API)
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
                point = new TrackPoint(trackPointCounter, mCurrentTrack.getId(), mUserID, location.getLatitude(), location.getLongitude(),
                        location.getAltitude(), location.getBearing(), location.getSpeed(), location.getAccuracy(), time, activity);
                mCurrentTrackPoint = point;
                mCurrentTrack.addTrackPoint(point);
            }
            mLocationService.sendTrackPoint(point);
            trackPointCounter++;

            Intent intent = new Intent(getApplicationContext(), GeocodingIntentService.class);
            intent.putExtra(LOCATION_DATA_EXTRA, mLastLocation);
            startService(intent);

            /*PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/trackPoint");
            putDataMapReq.getDataMap().putDouble(LATITUDE_KEY, point.getLatitude());
            putDataMapReq.getDataMap().putDouble(LONGITUDE_KEY, point.getLongitude());
            putDataMapReq.getDataMap().putString(TIME_KEY, point.getTime().toString());
            putDataMapReq.getDataMap().putString(ACTIVITY_KEY, point.getTypeOfMovement());
            putDataMapReq.getDataMap().putDouble(SPEED_KEY, point.getSpeed());
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);*/
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
            DetectedActivity detectedActivity = intent.getParcelableExtra(EXTENDED_DATA_ACTIVITY);
            mCurrentActivity = detectedActivity;
        }
    }
}
