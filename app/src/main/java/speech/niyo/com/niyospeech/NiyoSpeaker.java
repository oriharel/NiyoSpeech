package speech.niyo.com.niyospeech;

import android.app.Notification;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

/**
 * Created by oriharel on 7/12/14.
 */
public interface NiyoSpeaker {

    public void speak(String text, TextToSpeech tts, HashMap<String, String> params);
    public String resolveText(Notification notif);
}
