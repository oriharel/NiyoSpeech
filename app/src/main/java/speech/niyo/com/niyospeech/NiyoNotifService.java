package speech.niyo.com.niyospeech;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import speech.niyo.com.niyospeech.speakers.DefaultNiyoSpeaker;
import speech.niyo.com.niyospeech.speakers.HangoutNiyoSpeaker;

/**
 * Created by oriharel on 7/11/14.
 */
public class NiyoNotifService extends NotificationListenerService implements TextToSpeech.OnInitListener, AudioManager.OnAudioFocusChangeListener {
    public static final String LOG_TAG = NiyoNotifService.class.getSimpleName();
    private TextToSpeech _tts;

    private HashMap<String, NiyoSpeaker> _speakers;
    private List<String> _blackList;

    @Override
    public void onCreate() {
        super.onCreate();

        _tts = new TextToSpeech(this, this);
        _speakers = new HashMap<String, NiyoSpeaker>();
        _speakers.put("com.google.android.talk", new HangoutNiyoSpeaker());

        //Gmail
        _speakers.put("com.google.android.gm", new DefaultNiyoSpeaker());
        _speakers.put("com.whatsapp", new DefaultNiyoSpeaker());

        _blackList = new ArrayList<String>();
        _blackList.add("com.android.vending");
        _blackList.add("android");
        _blackList.add("net.hubalek.android.reborn.beta");
        _blackList.add("com.google.android.googlequicksearchbox");
        _blackList.add("com.android.providers.downloads");

        SharedPreferences prefs = this.getSharedPreferences(AppListAdapter.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        Set<String> set = prefs.getStringSet(AppListAdapter.APPS_TO_SPEECH, new HashSet<String>());

        Log.d(LOG_TAG, "set contains ("+set.size()+") "+printSet(set));

    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(LOG_TAG, "received new notification");
        Notification notif = sbn.getNotification();
        String pkg = sbn.getPackageName();
        CharSequence text = notif.tickerText;
        Bundle bundle = notif.extras;
        Object contentText = bundle.get(Notification.EXTRA_TEXT);
        Log.d(LOG_TAG, "text received with "+text+" pkg is "+pkg);
        Log.d(LOG_TAG, "contentText received with "+contentText+" pkg is "+pkg);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isEnabled = sharedPref.getBoolean("example_checkbox", false);


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

        selecteds.add("com.google.android.talk");
        selecteds.add("com.google.android.gm");

        if (!selecteds.contains(pkg)) return;

        NiyoSpeaker speaker = getSpeaker(pkg);

        if (speaker == null) {
            Log.d(LOG_TAG, "pkg "+pkg+" is black listed");
            return;
        }

        String textToSpeak = speaker.resolveText(notif);

        Log.d(LOG_TAG, "ok, going to speak "+pkg);

        if (textToSpeak != null) {
            final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            final UUID id = UUID.randomUUID();

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                final AudioManager.OnAudioFocusChangeListener listener = this;
                _tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        if (s.equals(id.toString())) {
                            am.abandonAudioFocus(listener);
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
                speaker.speak(textToSpeak.toString(), _tts, params);

            }
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
}
