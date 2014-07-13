package speech.niyo.com.niyospeech.speakers;

import android.app.Notification;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

import speech.niyo.com.niyospeech.NiyoSpeaker;

/**
 * Created by oriharel on 7/12/14.
 */
public class DefaultNiyoSpeaker implements NiyoSpeaker {
    @Override
    public void speak(String text, TextToSpeech tts, HashMap<String, String> params) {
        tts.speak(text.toString(), TextToSpeech.QUEUE_ADD, params);
    }

    @Override
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
