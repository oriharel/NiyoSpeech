package speech.niyo.com.niyospeech;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import speech.niyo.com.niyospeech.speakers.DefaultNiyoSpeaker;
import speech.niyo.com.niyospeech.speakers.GmailSpekaer;
import speech.niyo.com.niyospeech.speakers.HangoutNiyoSpeaker;

/**
 * Created by oriharel on 7/11/14.
 */
public class NiyoNotifService extends NotificationListenerService implements TextToSpeech.OnInitListener,
        AudioManager.OnAudioFocusChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String LOG_TAG = NiyoNotifService.class.getSimpleName();
    private TextToSpeech _defaultTts;
    private TextToSpeech _englishTts;
    private TextToSpeech _chosenTts;

    private HashMap<String, NiyoSpeaker> _speakers;
    private List<String> _blackList;
    private Pattern _p = Pattern.compile("\\p{InHebrew}");

    @Override
    public void onCreate() {
        super.onCreate();

        _defaultTts = new TextToSpeech(this, this);
        _englishTts = new TextToSpeech(this, this, "com.google.android.tts");
        List<TextToSpeech.EngineInfo> engines = _defaultTts.getEngines();

        for (TextToSpeech.EngineInfo engine: engines) {
            Log.d(LOG_TAG, "engine name is "+engine.name+" label is "+engine.label);
        }
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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        Set<String> set = sharedPref.getStringSet(AppListAdapter.APPS_TO_SPEECH, new HashSet<String>());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("shutup", false);
        editor.commit();

        Log.d(LOG_TAG, "set contains ("+set.size()+") "+printSet(set));

    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(LOG_TAG, "received new notification");
        Notification notif = sbn.getNotification();
        String pkg = sbn.getPackageName();

        logNotif(notif, pkg);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isEnabled = sharedPref.getBoolean("example_checkbox", false);

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

//        selecteds.add("com.google.android.talk");
//        selecteds.add("com.google.android.gm");

        if (!selecteds.contains(pkg)) return;

        NiyoSpeaker speaker = getSpeaker(pkg);

        if (speaker == null) {
            Log.d(LOG_TAG, "pkg "+pkg+" is black listed");
            return;
        }

        String textToSpeak = speaker.resolveText(notif);

        _chosenTts = _defaultTts;

        if (!isHebrewInText(textToSpeak)) {
            _chosenTts = _englishTts;
        }

        Log.d(LOG_TAG, "ok, going to speak "+textToSpeak+" for pkg "+pkg+" with tts: "+_chosenTts.toString());

        if (!isEnabled) return;

        if (textToSpeak != null) {
            final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            final UUID id = UUID.randomUUID();

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                final AudioManager.OnAudioFocusChangeListener listener = this;
                _chosenTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        if (s.equals(id.toString())) {
                            am.abandonAudioFocus(listener);
                            removeSpeakingNotification();
                        }
                    }

                    @Override
                    public void onError(String s) {
                        if (s.equals(id.toString())) {
                            am.abandonAudioFocus(listener);
                        }
                    }
                });

                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id.toString());
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null) {
                    int state = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, Integer.toString(AudioManager.STREAM_RING));
                    }
                }
                speaker.speak(textToSpeak, _chosenTts, params);
                showSpeakingNotification();

            }
        }

    }

    public void shutUp() {

    }

    private void showSpeakingNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.speak)
                .setContentTitle("NIYO is speaking!");

        Intent shutUpintent = new Intent(this, ShutUpReceiver.class);

        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, shutUpintent, 0);

        mBuilder.addAction(R.drawable.ic_action_cancel, "Shut Up!", contentIntent);
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void removeSpeakingNotification() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void logNotif(Notification notif, String pkg) {
        CharSequence text = notif.tickerText;
        Bundle bundle = notif.extras;
        Object contentText = bundle.get(Notification.EXTRA_TEXT);
        Log.d(LOG_TAG, "text received with "+text+" pkg is "+pkg);
        Log.d(LOG_TAG, "contentText received with "+contentText+" pkg is "+pkg);
    }

    @Override
    public void onDestroy() {
        _defaultTts.shutdown();
        _englishTts.shutdown();
    }

    private boolean isHebrewInText(String textToSpeak) {

        Matcher m = _p.matcher(textToSpeak);

        if (m.find()) {
            Log.d(LOG_TAG, "****************the text has hebrew letters");
            return true;
        }
        else {
            Log.d(LOG_TAG, "*****************test has no hebrew");
            return false;
        }
    }

    private String printSet(Set<String> set) {
        StringBuffer buffer = new StringBuffer();

        for (String item : set) {
            buffer.append(item);
            buffer.append(", ");
        }

        return buffer.toString();
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

    @Override
    public void onInit(int status) {
        Log.d(LOG_TAG, "text to speech initialized");
    }

    @Override
    public void onAudioFocusChange(int i) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d(LOG_TAG, "onSharedPreferenceChanged received with "+key);
        if (key.equals("shutup")) {
            Log.d(LOG_TAG, "onSharedPreferenceChanged being told to shut up");
            if (sharedPreferences.getBoolean(key, false)) {
                _chosenTts.stop();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("shutup", false);
                editor.apply();
            }
        }
        else {
            Log.d(LOG_TAG, "called with not shutup");
        }
    }
}
