package speech.niyo.com.niyospeech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

/**
 * Created by oriharel on 11/17/14.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, TextToSpeech.OnInitListener {

    private TextToSpeech _defaultTts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        PackageManager packageManager = getActivity().getPackageManager();
        _defaultTts = new TextToSpeech(getActivity(), this);
        String ttsPkg = _defaultTts.getDefaultEngine();

        try {
            ApplicationInfo engineInfo = packageManager.getApplicationInfo(ttsPkg, PackageManager.GET_META_DATA);
            CharSequence engineLabel = packageManager.getApplicationLabel(engineInfo);
            Preference ttsLabel = findPreference("tts_label");
            ttsLabel.setSummary(engineLabel);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("general_switch")) {
            CheckBoxPreference connectionPref = (CheckBoxPreference)findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setChecked(sharedPreferences.getBoolean(key, false));

//            Intent announceIntent = new Intent(getActivity(), AnnouncingService.class);
//            announceIntent.setAction(AnnouncingService.ACTION_ANNOUNCE);
//            getActivity().startService(announceIntent);
        }
    }

    @Override
    public void onInit(int i) {

    }
}
