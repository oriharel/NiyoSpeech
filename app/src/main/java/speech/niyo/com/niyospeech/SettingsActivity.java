package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        SettingsFragment firstFragment = new SettingsFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    }


//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        setupSimplePreferencesScreen();
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPref.registerOnSharedPreferenceChangeListener(this);

//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
//    private void setupSimplePreferencesScreen() {
//        if (!isSimplePreferences(this)) {
//            return;
//        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
//        addPreferencesFromResource(R.xml.pref_general);
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_geo) {
//            Intent intent = new Intent(this, GeoSpeechFragment.class);
//            startActivity(intent);
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /** {@inheritDoc} */
//    @Override
//    public boolean onIsMultiPane() {
//        return isXLargeTablet(this) && !isSimplePreferences(this);
//    }

//    private static boolean isXLargeTablet(Context context) {
//        return (context.getResources().getConfiguration().screenLayout
//        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
//    }

//    private static boolean isSimplePreferences(Context context) {
//        return ALWAYS_SIMPLE_PREFS
//                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
//                || !isXLargeTablet(context);
//    }

    /** {@inheritDoc} */
//    @Override
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public void onBuildHeaders(List<Header> target) {
//        if (!isSimplePreferences(this)) {
//            loadHeadersFromResource(R.xml.pref_headers, target);
//        }
//    }


}
