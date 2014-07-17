package speech.niyo.com.niyospeech;

import android.app.Notification;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oriharel on 7/12/14.
 */
public class NiyoSpeaker {

    public static final String LOG_TAG = NiyoSpeaker.class.getSimpleName();

    public void speak(String text, TextToSpeech tts, HashMap<String, String> params) {
        tts.speak(text.toString(), TextToSpeech.QUEUE_ADD, params);
    }

    public String resolveText(Notification notif) {

        CharSequence text = notif.tickerText;
        Bundle bundle = notif.extras;
        Object contentText = bundle.get(Notification.EXTRA_TEXT);
        Object title = bundle.get(Notification.EXTRA_TITLE);

        String result = null;

        if (contentText != null) {
            result = contentText.toString();
        }

        if (result == null) {
            Log.d(LOG_TAG, "contentText is null, returning text");
            result = text.toString();
        }
        else if (text != null && !result.equals(text)){
            Log.d(LOG_TAG, "contentText is NOT null and text is different");
            result = text+" "+contentText;
        }
        else {
            Log.d(LOG_TAG, "returning just the contextText");
        }

        if (result != null) {
            return title+" "+result;
        }

        Pattern p = Pattern.compile("\\p{InHebrew}");
        Matcher m = p.matcher(result);

        if (m.find()) {
            Log.d(LOG_TAG, "the text has hebrew letters");
        }

        return result;
    }
}
