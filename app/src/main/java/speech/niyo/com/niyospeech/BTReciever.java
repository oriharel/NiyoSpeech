package speech.niyo.com.niyospeech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class BTReciever extends BroadcastReceiver {
    public static final String LOG_TAG = BTReciever.class.getSimpleName();

    public BTReciever() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        Log.d(LOG_TAG, "received bluetooth event with " + device.getName());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPref.getBoolean("bt_detect", true)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("general_switch", true);
            editor.commit();
        }



    }


}
