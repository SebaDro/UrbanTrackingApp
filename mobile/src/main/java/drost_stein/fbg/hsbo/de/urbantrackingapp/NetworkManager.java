package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Handles network connections
 * Created by Seba on 11.06.2016.
 */
public class NetworkManager {
    private final int WIFI = ConnectivityManager.TYPE_WIFI;
    private final int MOBILE = ConnectivityManager.TYPE_MOBILE;

    private int connectivityType;
    private Context context;

    public NetworkManager(int connectivityType, Context context) {
        this.connectivityType = connectivityType;
        this.context = context;
    }

    public int getConnectivityType() {
        return connectivityType;
    }

    public void setConnectivityType(int connectivityType) {
        this.connectivityType = connectivityType;
    }

    /**
     * checks if there is an online connection dependent on the preferred connection type
     *
     * @return
     */
    public boolean isOnline() {
        switch (this.connectivityType) {
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
}
