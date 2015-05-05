package speech.niyo.com.niyospeech.speech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ShutUpReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = ShutUpReceiver.class.getSimpleName();
    public ShutUpReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "ShutUp received!!");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("shutup", true);
        editor.apply();

    }
}
