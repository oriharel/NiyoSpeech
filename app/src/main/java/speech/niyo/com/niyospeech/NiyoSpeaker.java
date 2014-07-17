package speech.niyo.com.niyospeech;

import android.app.Notification;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

/**
 * Created by oriharel on 7/12/14.
 */
public abstract class NiyoSpeaker {

    public void speak(String text, TextToSpeech tts, HashMap<String, String> params) {
        tts.speak(text.toString(), TextToSpeech.QUEUE_ADD, params);
    }

    public String resolveText(Notification notif) {

        CharSequence text = notif.tickerText;
        Bundle bundle = notif.extras;
        Object contentText = bundle.get(Notification.EXTRA_TEXT);

        String result = contentText.toString();

        if (result == null) {
            result = text.toString();
        }
        else if (text != null && !result.equals(text)){
            result = text+" "+contentText;
        }

        return result;
    }
}
