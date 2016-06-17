package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Service that tracks the activity.
 * Created by Matthias Stein.
 */
public class ActivitiesIntentService extends IntentService {

    private static final String TAG = "ActivitiesIntentService";

    private static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_ACTIVITY = PACKAGE_NAME + ".BROADCAST_ACTIVITY";
    private static final String EXTENDED_DATA_ACTIVITY = PACKAGE_NAME + ".DATA_ACTIVITY";


    public ActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent i = new Intent(BROADCAST_ACTION_ACTIVITY);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        i.putExtra(EXTENDED_DATA_ACTIVITY, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}