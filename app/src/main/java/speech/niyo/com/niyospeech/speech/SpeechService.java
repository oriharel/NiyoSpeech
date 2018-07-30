package speech.niyo.com.niyospeech.speech;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import speech.niyo.com.niyospeech.NiyoSpeaker;
import speech.niyo.com.niyospeech.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SpeechService extends Service implements AudioManager.OnAudioFocusChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOG_TAG = SpeechService.class.getSimpleName();
    public static final String SPEAKING_TEXT_EXTRA = "speaking_text_extra";
    public static final String SPEAK_RESOLVER_EXTRA = "speak_resolver_extra";

    private TextToSpeech _tts;
    private Pattern _p = Pattern.compile("\\p{InHebrew}");


    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(LOG_TAG, "onCreate is called");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String textToSpeak = intent.getStringExtra(SPEAKING_TEXT_EXTRA);
        final NiyoSpeaker speaker = (NiyoSpeaker)intent.getSerializableExtra(SPEAK_RESOLVER_EXTRA);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("shutup", false);
        editor.apply();

        Log.d(LOG_TAG, "onStartCommand started with "+textToSpeak);

        TextToSpeech.OnInitListener listener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                final UUID id = UUID.randomUUID();
                Log.d(LOG_TAG, "text to speech initialized with status: "+status+" id is: "+id);

                final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

                final AudioManager.OnAudioFocusChangeListener audioListener = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {

                    }
                };


                int result = am.requestAudioFocus(audioListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);


                if (status == TextToSpeech.SUCCESS && result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                    _tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.d(LOG_TAG, "UtteranceProgress started");
                        }

                        @Override
                        public void onDone(String s) {
                            Log.d(LOG_TAG, "UtteranceProgress done");
                            if (s.equals(id.toString())) {
                                am.abandonAudioFocus(audioListener);
                            }
                            removeSpeakingNotification();
                        }

                        @Override
                        public void onError(String s) {
                            if (s.equals(id.toString())) {
                                am.abandonAudioFocus(audioListener);
                            }
                            Log.e(LOG_TAG, "Error in UtteranceProgress: "+s);
                            stopSelf();
                        }
                    });

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id.toString());
                    speaker.speak(textToSpeak, _tts, params);
                    showSpeakingNotification();

                }
            }
        };


        if (!isHebrewInText(textToSpeak)) {

            _tts = new TextToSpeech(this, listener, "com.google.android.tts");
        }
        else {
            _tts =  new TextToSpeech(this, listener);
        }

        return START_STICKY;
    }

    private boolean isHebrewInText(String textToSpeak) {

        Matcher m = _p.matcher(textToSpeak);

        if (m.find()) {
            Log.d(LOG_TAG, "****************the text has hebrew letters");
            return true;
        }
        else {
            Log.d(LOG_TAG, "*****************text has no hebrew");
            return false;
        }
    }

    private void removeSpeakingNotification() {
        Log.d(LOG_TAG, "removeSpeakingNotification and stopping self");
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        stopSelf();
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy called");
        removeSpeakingNotification();
        _tts.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d(LOG_TAG, "onSharedPreferenceChanged received with "+key);
        if (key.equals("shutup")) {
            Log.d(LOG_TAG, "onSharedPreferenceChanged being told to shut up");
            if (sharedPreferences.getBoolean(key, false)) {
                Log.d(LOG_TAG, "shutting up!");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("shutup", false);
                editor.apply();
                stopSelf();
            }
            else {
                Log.d(LOG_TAG, "Not Shutting up!");
            }
        }
        else {
            Log.d(LOG_TAG, "called with not shutup");
        }
    }
}
