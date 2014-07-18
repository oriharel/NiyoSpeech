package speech.niyo.com.niyospeech;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AnnouncingService extends IntentService {
    public static final String LOG_TAG = AnnouncingService.class.getSimpleName();
    private static TextToSpeech _defaultTts;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_ANNOUNCE = "speech.niyo.com.niyospeech.action.ANNOUNCE";
    private static final String ACTION_BAZ = "speech.niyo.com.niyospeech.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "speech.niyo.com.niyospeech.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "speech.niyo.com.niyospeech.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionAnnounce(Context context, String param1, String param2) {
        _defaultTts = new TextToSpeech(context, getInitListener(), "com.google.android.tts");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AnnouncingService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public AnnouncingService() {
        super("AnnouncingService");
    }

    public static TextToSpeech.OnInitListener getInitListener() {
        return new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                _defaultTts.speak("NeeYo speech is on", TextToSpeech.QUEUE_ADD, null);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _defaultTts.shutdown();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ANNOUNCE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionAnnounce(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionAnnounce(String param1, String param2) {
        startActionAnnounce(this, param1, param2);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
