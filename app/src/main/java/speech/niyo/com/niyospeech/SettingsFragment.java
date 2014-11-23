package speech.niyo.com.niyospeech;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;

import java.util.List;

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

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

        WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        CharSequence[] networksNames = getNetworskNames(networks);
        CharSequence[] networksValues = getNetworksValues(networks);
        ListPreference wifiHome = (ListPreference)findPreference("wifi_home");
        ListPreference wifiWork = (ListPreference)findPreference("wifi_work");
        wifiHome.setEntries(networksNames);
        wifiWork.setEntries(networksNames);
        wifiHome.setEntryValues(networksValues);
        wifiWork.setEntryValues(networksValues);
        wifiHome.setSummary(sharedPref.getString("wifi_home", getActivity().getResources().getString(R.string.add_home_wifi)));
        wifiWork.setSummary(sharedPref.getString("wifi_work", getActivity().getResources().getString(R.string.add_work_wifi)));

        Boolean isOn = sharedPref.getBoolean("general_switch", false);
        Preference genPref = findPreference("general_switch");
        if (isOn){
            genPref.setSummary(getActivity().getResources().getString(R.string.switch_on));
        }
        else {
            genPref.setSummary(getActivity().getResources().getString(R.string.switch_off));
        }

        String homeAddress = sharedPref.getString("geo_home", getActivity().getResources().getString(R.string.add_home_geo));
        Preference geoHome = findPreference("geo_home");
        geoHome.setSummary(homeAddress);

        String workAddress = sharedPref.getString("geo_work", getActivity().getResources().getString(R.string.add_work_geo));
        Preference geoWork = findPreference("geo_work");
        geoWork.setSummary(workAddress);

    }

    private CharSequence[] getNetworksValues(List<WifiConfiguration> networks) {
        int listSize = networks != null ? networks.size() : 0;
        CharSequence[] result = new CharSequence[listSize];
        for (int i = 0; i < listSize; i++) {
            result[i] = networks.get(i).SSID;
        }
        return result;
    }

    private CharSequence[] getNetworskNames(List<WifiConfiguration> networks) {
        int listSize = networks != null ? networks.size() : 0;
        CharSequence[] result = new CharSequence[listSize];
        for (int i = 0; i < listSize; i++) {
            result[i] = networks.get(i).SSID;
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("general_switch")) {
            SwitchPreference generalSwitch = (SwitchPreference)findPreference(key);
            // Set summary to be the user-description for the selected value

            Boolean value = sharedPreferences.getBoolean(key, false);
            if (value) {
                generalSwitch.setSummary(getActivity().getResources().getString(R.string.switch_on));
            }
            else {
                generalSwitch.setSummary(getActivity().getResources().getString(R.string.switch_off));
            }
            generalSwitch.setChecked(value);
            showShutingdownNotification(value);

        }
        else if (key.equals("wifi_home")) {
            Preference wifiHome = findPreference("wifi_home");
            wifiHome.setSummary(sharedPreferences.getString("wifi_home", getActivity().getResources().getString(R.string.add_home_wifi)));
        }
        else if (key.equals("wifi_work")) {
            Preference wifiHome = findPreference("wifi_work");
            wifiHome.setSummary(sharedPreferences.getString("wifi_work", getActivity().getResources().getString(R.string.add_home_wifi)));
        }
        else if (key.equals("geo_home")) {
            Preference wifiHome = findPreference("geo_home");
            wifiHome.setSummary(sharedPreferences.getString("geo_home", getActivity().getResources().getString(R.string.add_home_geo)));
        }
        else if (key.equals("geo_work")) {
            Preference wifiHome = findPreference("geo_work");
            wifiHome.setSummary(sharedPreferences.getString("geo_work", getActivity().getResources().getString(R.string.add_work_geo)));
        }
    }

    private void showShutingdownNotification(Boolean isOn) {
        String message;
        if (isOn) {
            message = "NIYO Speech is On!";
        }
        else {
            message = "NIYO Speech is off!";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.speak)
                        .setContentTitle(message);


        NotificationManager mNotificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _defaultTts.shutdown();
    }

    @Override
    public void onInit(int i) {

    }
}
