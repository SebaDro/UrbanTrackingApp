package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Seba on 21.06.2016.
 */
public class GeocodingIntentService extends IntentService {
    private static final String TAG = "GeocodingIntentService";

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String BROADCAST_ACTION_ADDRESS=PACKAGE_NAME + ".BROADCAST_ADDRESS";
    private static final String EXTENDED_DATA_ADDRESS= PACKAGE_NAME + ".DATA_ADDRESS";

    protected ResultReceiver mReceiver;


    public GeocodingIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            //errorMessage = getString(R.string.service_not_available);
            errorMessage = "service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            //errorMessage = getString(R.string.invalid_lat_long_used);
            errorMessage = "invalid Lat/Long";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                //errorMessage = getString(R.string.no_address_found);
                errorMessage = "no adress found";
                Log.e(TAG, errorMessage);
                deliverResultToReceiver(FAILURE_RESULT,null);
            }
            //deliverResultToReceiver(FAILURE_RESULT, address);
        } else {
            Address address = addresses.get(0);


            Log.i(TAG, "adress found");
            deliverResultToReceiver(SUCCESS_RESULT,address);
        }

    }
    private void deliverResultToReceiver(int errorcode,Address address) {
        Intent i = new Intent(BROADCAST_ACTION_ADDRESS);
        i.putExtra(EXTENDED_DATA_ADDRESS, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
