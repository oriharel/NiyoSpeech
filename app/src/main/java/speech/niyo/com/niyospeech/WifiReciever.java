package speech.niyo.com.niyospeech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiReciever extends BroadcastReceiver {
    public WifiReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        String msg = null;
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                msg = "wifi is disabled";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                msg = "wifi is enabled";
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("example_checkbox", false);
                editor.commit();
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                msg = "wifi is switching off";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                msg = "wifi is getting enabled";
                break;
            default:
                msg = "not working properly";
                break;
        }
        if (msg != null) {
            Log.d("************%%%%%%%%wifi state ", "WIFI" + msg);
        }
    }
}
