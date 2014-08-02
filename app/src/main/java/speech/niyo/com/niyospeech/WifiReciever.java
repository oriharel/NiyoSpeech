package speech.niyo.com.niyospeech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiReciever extends BroadcastReceiver {
    public static final String LOG_TAG = WifiReciever.class.getSimpleName();
    public WifiReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //ignore if bluetooth is connected
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int state = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);

        if (state == BluetoothProfile.STATE_CONNECTED) {
            Log.d(LOG_TAG, "Bluetooth before wifi...");
            return;
        }

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        Log.d(LOG_TAG, "received wifi connection changed");
        Log.d(LOG_TAG, "with isInternetConnected(context): "+isInternetConnected(context));
        if (networkInfo != null) {
            Log.d(LOG_TAG, "networkInfo is connected? "+networkInfo.isConnected());
        }
        if (isInternetConnected(context)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("example_checkbox", false);
            editor.commit();
        }
    }

    public boolean isInternetConnected(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean ret = true;
        if (conMgr != null) {
            NetworkInfo i = conMgr.getActiveNetworkInfo();

            if (i != null) {
                if (!i.isConnected()) {
                    ret = false;
                }

                if (!i.isAvailable()) {
                    ret = false;
                }
            }

            if (i == null)
                ret = false;
        } else
            ret = false;
        return ret;
    }
}
