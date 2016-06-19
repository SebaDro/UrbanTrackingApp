package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Handles network connections
 * Created by Seba on 11.06.2016.
 */
public class NetworkReceiver extends BroadcastReceiver {
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
                Toast.makeText(context, R.string.mobile_connection, Toast.LENGTH_SHORT).show();
                isPrefferedConnected = true;
            }
        } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            isPrefferedConnected = true;
            Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();

        } else {
            if (isPrefferedConnected) {
                isPrefferedConnected = false;
                Toast.makeText(context, R.string.lost_internet_connection, Toast.LENGTH_SHORT).show();
            }

        }

    }
}
