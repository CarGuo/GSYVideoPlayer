
package com.shuyu.gsyvideoplayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.net.ConnectivityManagerCompat;

/**
 * Module that monitors and provides information about the connectivity state of the device.
 */

public class NetInfoModule {

    public interface NetChangeListener {
        void changed(String state);
    }

    private static final String CONNECTION_TYPE_NONE = "NONE";
    private static final String CONNECTION_TYPE_UNKNOWN = "UNKNOWN";
    private static final String MISSING_PERMISSION_MESSAGE =
            "To use NetInfo on Android, add the following to your AndroidManifest.xml:\n" +
                    "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />";

    private static final String ERROR_MISSING_PERMISSION = "E_MISSING_PERMISSION";

    private final ConnectivityManager mConnectivityManager;
    private final ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private NetChangeListener mNetChangeListener;

    private String mConnectivity = "";
    private Context mContext;

    private boolean mNoNetworkPermission = false;


    public NetInfoModule(Context context, NetChangeListener netChangeListener) {
        mContext = context;
        mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();
        mNetChangeListener = netChangeListener;
    }

    public void onHostResume() {
        registerReceiver();
    }

    public void onHostPause() {
        unregisterReceiver();
    }

    public void onHostDestroy() {
    }

    public String getCurrentConnectivity() {
        if (mNoNetworkPermission) {
            return ERROR_MISSING_PERMISSION;
        }

        return mConnectivity;
    }

    public boolean isConnectionMetered() {
        if (mNoNetworkPermission) {
            return false;
        }
        return ConnectivityManagerCompat.isActiveNetworkMetered(mConnectivityManager);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mConnectivityBroadcastReceiver, filter);
        mConnectivityBroadcastReceiver.setRegistered(true);
    }

    private void unregisterReceiver() {
        if (mConnectivityBroadcastReceiver.isRegistered()) {
            mContext.unregisterReceiver(mConnectivityBroadcastReceiver);
            mConnectivityBroadcastReceiver.setRegistered(false);
        }
    }

    private void updateAndSendConnectionType() {
        String currentConnectivity = getCurrentConnectionType();
        // It is possible to get multiple broadcasts for the same connectivity change, so we only
        // update and send an event when the connectivity has indeed changed.
        if (!currentConnectivity.equalsIgnoreCase(mConnectivity)) {
            mConnectivity = currentConnectivity;
            sendConnectivityChangedEvent();
        }
    }

    public String getCurrentConnectionType() {
        try {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                return CONNECTION_TYPE_NONE;
            } else if (ConnectivityManager.isNetworkTypeValid(networkInfo.getType())) {
                return networkInfo.getTypeName().toUpperCase();
            } else {
                return CONNECTION_TYPE_UNKNOWN;
            }
        } catch (SecurityException e) {
            mNoNetworkPermission = true;
            return CONNECTION_TYPE_UNKNOWN;
        }
    }


    private void sendConnectivityChangedEvent() {
        if (mNetChangeListener != null) {
            mNetChangeListener.changed(mConnectivity);
        }
    }

    /**
     * Class that receives intents whenever the connection type changes.
     * NB: It is possible on some devices to receive certain connection type changes multiple times.
     */
    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        //TODO: Remove registered check when source of crash is found. t9846865
        private boolean isRegistered = false;

        public void setRegistered(boolean registered) {
            isRegistered = registered;
        }

        public boolean isRegistered() {
            return isRegistered;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateAndSendConnectionType();
            }
        }
    }
}
