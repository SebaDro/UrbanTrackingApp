package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * Handles network connections
 * Created by Seba on 11.06.2016.
 */
public class NetworkReceiver extends BroadcastReceiver {
    private static final String PACKAGE_NAME = "drost_stein.fbg.hsbo.de.urbantrackingapp";
    private static final String BROADCAST_ACTION_NETWORK_STATUS = PACKAGE_NAME + ".BROADCAST_NETWORK_STATUS";
    private static final String EXTENDED_DATA_NETWORK_STATUS= PACKAGE_NAME + ".DATA_NETWORK_STATUS";

    private final int WIFI = ConnectivityManager.TYPE_WIFI;
    private final int MOBILE = ConnectivityManager.TYPE_MOBILE;

    private boolean isPrefferedConnected = false;
    private int prefferedConnectivity;
    private Context context;

    public NetworkReceiver(int prefferedConnectivity, Context context) {
        this.prefferedConnectivity = prefferedConnectivity;
        this.context = context;
    }

    public int getPrefferedConnectivity() {
        return prefferedConnectivity;
    }

    public void setPrefferedConnectivity(int prefferedConnectivity) {
        this.prefferedConnectivity = prefferedConnectivity;
    }

    /**
     * checks if there is an online connection dependent on the preferred connection type
     *
     * @return
     */
    public boolean hasPrefferedConnection() {
        switch (this.prefferedConnectivity) {
            case MOBILE:
                return checkIsOnline();
            case WIFI:
                return checkHasWIFI();
            default:
                return checkHasWIFI();
        }
    }

    //

    /**
     * checks if there is a WIFI connection
     *
     * @return true if there is a WIFI connection
     */
    private boolean checkHasWIFI() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    /**
     * checks if there is any connection
     *
     * @return true if there is a any connection
     */
    private boolean checkIsOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            if (prefferedConnectivity == MOBILE) {
                isPrefferedConnected = true;
                sendNetworkChanged(isPrefferedConnected);
            }
        } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            isPrefferedConnected = true;
            sendNetworkChanged(isPrefferedConnected);

        } else {
            if (isPrefferedConnected) {
                isPrefferedConnected = false;
                sendNetworkChanged(isPrefferedConnected);
            }

        }

    }
    public void sendNetworkChanged(Boolean networkAvailable) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION_NETWORK_STATUS)
                        // Puts the location into the Intent
                        .putExtra(EXTENDED_DATA_NETWORK_STATUS, networkAvailable);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
