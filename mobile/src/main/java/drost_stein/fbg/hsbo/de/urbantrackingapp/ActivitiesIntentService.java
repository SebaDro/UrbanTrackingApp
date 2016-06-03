package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class ActivitiesIntentService extends IntentService {

    private static final String TAG = "ActivitiesIntentService";

    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_ACTIVITIES = PACKAGE_NAME + ".BROADCAST_ACTIVITIES";
    private static final String EXTENDED_DATA_ACTIVITIES = PACKAGE_NAME + ".DATA_ACTIVITIES";


    public ActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent i = new Intent(BROADCAST_ACTION_ACTIVITIES);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        i.putExtra(EXTENDED_DATA_ACTIVITIES, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}