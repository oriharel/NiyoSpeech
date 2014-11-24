package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

import speech.niyo.com.niyospeech.speakers.SimpleGeofence;


public class GeoSpeechManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener{

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    Activity _activity;
    public static final String LOG_TAG = GeoSpeechManager.class.getSimpleName();

    @Override
    public void onConnected(Bundle bundle) {
        // Get the PendingIntent for the request
        mGeofenceRequestIntent =
                getTransitionPendingIntent();

        switch (mRequestType) {
            case ADD :

                if (mGeofenceList.size() > 0) {
                    // Send a request to add the current geofences
                    Log.d(LOG_TAG, "adding "+mGeofenceList.size()+" fences");
                    mLocationClient.addGeofences(
                            mGeofenceList, mGeofenceRequestIntent, this);
                }

            case REMOVE:
                mLocationClient.removeGeofences(mGeofenceRequestIntent, this);
        }
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;

    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        mLocationClient.disconnect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
        /*
         * If the error has a resolution, start a Google Play services
         * activity to resolve it.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        _activity,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    _activity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        _activity.getFragmentManager(),
                        "Geofence Detection");
            }
        }
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {

    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */


    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(_activity,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                _activity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
     * Internal geofence objects for geofence 1 and 2
     */

    // Handle to geofence 1 radius in the UI
    private String mRadiusHome = "200";

    // Internal List of Geofence objects
    List<Geofence> mGeofenceList;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;

    // Holds the location client
    private LocationClient mLocationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, REMOVE}
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    public GeoSpeechManager(Activity activity) {

        _activity = activity;

        // Start with the request flag set to false
        mInProgress = false;


        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(activity);

        // Instantiate the current List of geofences
        mGeofenceList = new ArrayList<Geofence>();

    }

    public void startTracking() {
        createGeofences();
        mRequestType = REQUEST_TYPE.ADD;
        addOrRemoveGeofences();
    }

    public void stopTracking() {
        mRequestType = REQUEST_TYPE.REMOVE;
        addOrRemoveGeofences();
    }

    public void addOrRemoveGeofences() {
        Log.d(LOG_TAG, "addOrRemoveGeofences");
        // Start a request to add geofences
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(_activity, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    /**
     * Get the geofence parameters for each geofence from the UI
     * and add them to a List.
     */
    public void createGeofences() {
        Log.d(LOG_TAG, "createGeofences started");
        /*
         * Create an internal object to store the data. Set its
         * ID to "1". This is a "flattened" object that contains
         * a set of strings
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_activity);
        String defHome = _activity.getResources().getString(R.string.add_home_geo);
        String defWork = _activity.getResources().getString(R.string.add_work_geo);
        String latHome = sharedPref.getString("geo_home_latitude", defHome);
        String lonHome = sharedPref.getString("geo_home_longitude", defHome);
        String lonWork = sharedPref.getString("geo_work_longitude", defWork);
        String latWork = sharedPref.getString("geo_work_latitude", defWork);
        Log.d(LOG_TAG, "latHome is: "+latHome+" lonHome: "+lonHome);
        Log.d(LOG_TAG, "latWork is: "+latWork+" lonWork: "+lonWork);
        if (!latHome.equals(defHome) && !lonHome.equals(defHome)) {
            SimpleGeofence uiGeofenceHome = new SimpleGeofence(
                    "1",
                    Double.valueOf(latHome),
                    Double.valueOf(lonHome),
                    Float.valueOf(mRadiusHome),
                    GEOFENCE_EXPIRATION_TIME,
                    // This geofence records only entry transitions
                    Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT);
            // Store this flat version
            mGeofenceStorage.setGeofence("1", uiGeofenceHome);
            mGeofenceList.add(uiGeofenceHome.toGeofence());
        }
        else {
            Log.d(LOG_TAG, "can't find latHome");
        }

        if (!latWork.equals(defWork) && !lonWork.equals(defWork)) {
            // Create another internal object. Set its ID to "2"
            SimpleGeofence uiGeofenceWork = new SimpleGeofence(
                    "2",
                    Double.valueOf(latWork),
                    Double.valueOf(lonWork),
                    Float.valueOf(mRadiusHome),
                    GEOFENCE_EXPIRATION_TIME,
                    // This geofence records both entry and exit transitions
                    Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT);
            // Store this flat version
            mGeofenceStorage.setGeofence("2", uiGeofenceWork);

            mGeofenceList.add(uiGeofenceWork.toGeofence());
        }
        else {
            Log.d(LOG_TAG, "can't find latWork");
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(_activity);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Activity Recognition",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    _activity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        _activity.getFragmentManager(),
                        "Activity Recognition");
            }
            return false;
        }
    }
}
