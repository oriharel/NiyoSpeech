package speech.niyo.com.niyospeech.speakers;

import android.app.Notification;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

import speech.niyo.com.niyospeech.NiyoSpeaker;

/**
 * Created by oriharel on 7/12/14.
 */
public class HangoutNiyoSpeaker implements NiyoSpeaker {
    @Override
    public void speak(String text, TextToSpeech tts, HashMap<String, String> params) {
        tts.speak(text.toString(), TextToSpeech.QUEUE_ADD, params);
    }

    @Override
    public String resolveText(Notification notif) {

        CharSequence text = notif.tickerText;

        return text.toString();
    }
}
