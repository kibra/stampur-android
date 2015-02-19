package com.stampur.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkManager {

    private static String LOGTAG = "STAMPUR";

    private static Context context;

    public static void init(Context context) {
        NetworkManager.context = context;
    }

    /**
     * Get Network Availability
     *
     * @return true, if a Network is available
     *         false, otherwise.
     */
    public static boolean isNetworkAvailable() {
        try {
            if (context == null) {
                Log.e(LOGTAG, "Invalid Context!");
                return false;
            }

            ConnectivityManager connectionManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo[] networks = connectionManager.getAllNetworkInfo();
            for (NetworkInfo networkInfo : networks) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error while finding Network Availability: " + e.getMessage());
        }

        return false;
    }
}