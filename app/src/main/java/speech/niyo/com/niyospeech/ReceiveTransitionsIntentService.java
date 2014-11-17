package speech.niyo.com.niyospeech;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    // Persistent storage for geofences

    public ReceiveTransitionsIntentService() {

        super("ReceiveTransitionsIntentService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (
                    (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                            ||
                            (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
                    ) {
                List<Geofence> triggerList =
                        LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();


                if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    editor.putBoolean("general_switch", false);
                }
                else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
//                    editor.putBoolean("general_switch", true);
                }

                editor.commit();
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
            }
            else {
                Log.e("ReceiveTransitionsIntentService",
                        "Geofence transition error: " +
                                transitionType);
            }
            // An invalid transition was reported
        }
    }

}
