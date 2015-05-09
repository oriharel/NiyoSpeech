package speech.niyo.com.niyospeech;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import speech.niyo.com.niyospeech.apps.AppsColumns;
import speech.niyo.com.niyospeech.speakers.DefaultNiyoSpeaker;
import speech.niyo.com.niyospeech.speakers.GmailSpekaer;
import speech.niyo.com.niyospeech.speakers.HangoutNiyoSpeaker;
import speech.niyo.com.niyospeech.speech.SpeechService;

/**
 * Created by oriharel on 7/11/14.
 */
public class NiyoNotifService extends NotificationListenerService {
    public static final String LOG_TAG = NiyoNotifService.class.getSimpleName();
//    private TextToSpeech _defaultTts;
//    private TextToSpeech _englishTts;

    private HashMap<String, NiyoSpeaker> _speakers;
    private List<String> _blackList;
//    private Pattern _p = Pattern.compile("\\p{InHebrew}");

    @Override
    public void onCreate() {
        super.onCreate();

        _speakers = new HashMap<String, NiyoSpeaker>();
        _speakers.put("com.google.android.talk", new HangoutNiyoSpeaker());

        //Gmail
        _speakers.put("com.google.android.gm", new GmailSpekaer());

        //Whatsup
        _speakers.put("com.whatsapp", new DefaultNiyoSpeaker());

        _blackList = new ArrayList<String>();
        _blackList.add("com.android.vending");
        _blackList.add("android");
        _blackList.add("net.hubalek.android.reborn.beta");
        _blackList.add("com.google.android.googlequicksearchbox");
        _blackList.add("com.android.providers.downloads");

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPref.registerOnSharedPreferenceChangeListener(this);
//        Set<String> set = sharedPref.getStringSet(AppListAdapter.APPS_TO_SPEECH, new HashSet<String>());
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean("shutup", false);
//        editor.commit();

//        Log.d(LOG_TAG, "set contains ("+set.size()+") "+printSet(set));

    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(LOG_TAG, "received new notification");
        Notification notif = sbn.getNotification();
        String pkg = sbn.getPackageName();
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(pkg, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        logNotif(notif, pkg);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isEnabled = sharedPref.getBoolean("general_switch", false);
        if (!isEnabled) return;

        String select = "((" + AppsColumns.APP_PKG + " NOTNULL) AND ("
                + AppsColumns.APP_PKG + " != '' ))";
        Cursor cursor = getContentResolver().query(NiyoSpeech.APPS_URI, NiyoSpeech.APPS_SUMMARY_PROJECTION,
                select, null, AppsColumns.APP_PKG + "COLLATE LOCALIZED ASC");
        List<String> selecteds = new ArrayList<String>();

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            String storedPkg = cursor.getString(0);
            selecteds.add(storedPkg);
            cursor.moveToNext();
        }

        if (!selecteds.contains(pkg)) return;

        NiyoSpeaker speaker = getSpeaker(pkg);

        if (speaker == null) {
            Log.d(LOG_TAG, "pkg "+pkg+" is black listed");
            return;
        }

        String appName = "";

        if (appInfo != null) {
            appName = getPackageManager().getApplicationLabel(appInfo).toString();
        }


        String textToSpeak = appName+", "+speaker.resolveText(notif);

        Intent speakIntent = new Intent(this, SpeechService.class);
        speakIntent.putExtra(SpeechService.SPEAKING_TEXT_EXTRA, textToSpeak);
        speakIntent.putExtra(SpeechService.SPEAK_RESOLVER_EXTRA, speaker);
        Log.d(LOG_TAG, "firing the speak service");
        startService(speakIntent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void logNotif(Notification notif, String pkg) {
        CharSequence text = notif.tickerText;
        Bundle bundle = notif.extras;
        Object contentText = bundle.get(Notification.EXTRA_TEXT);
        Log.d(LOG_TAG, "text received with "+text+" pkg is "+pkg);
        Log.d(LOG_TAG, "contentText received with "+contentText+" pkg is "+pkg);
    }

    private NiyoSpeaker getSpeaker(String pkgName) {
        NiyoSpeaker speaker = _speakers.get(pkgName);
        if (speaker != null) {
            return speaker;
        }
        else if (_blackList.contains(pkgName)) {
            return null;
        }
        else {
            return new DefaultNiyoSpeaker();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }

//    @Override
//    public void onInit(int status) {
//        Log.d(LOG_TAG, "text to speech initialized");
//    }


//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//
//        Log.d(LOG_TAG, "onSharedPreferenceChanged received with "+key);
//        if (key.equals("shutup")) {
//            Log.d(LOG_TAG, "onSharedPreferenceChanged being told to shut up");
//            if (sharedPreferences.getBoolean(key, false)) {
//                _chosenTts.stop();
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean("shutup", false);
//                editor.apply();
//            }
//        }
//        else {
//            Log.d(LOG_TAG, "called with not shutup");
//        }
//    }
}
