package speech.niyo.com.niyospeech.speakers;

import android.app.Notification;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

import speech.niyo.com.niyospeech.NiyoSpeaker;

/**
 * Created by oriharel on 7/17/14.
 */
public class GmailSpekaer extends NiyoSpeaker {

    @Override
    public String resolveText(Notification notif) {
        String superText = super.resolveText(notif);

        if (superText.indexOf("http://capriza.zendesk.com/agent/#/tickets") < -1) {
            return resolveZendeskFeedback(superText);
        }

        return superText;
    }

    private String resolveZendeskFeedback(String superText) {
        String part1 = superText.substring(superText.indexOf("Ticket #"), superText.indexOf("You have been assigned to this ticket"));

        String part2 = superText.substring(superText.indexOf("#rnd"), superText.indexOf("mobile logs:"));

        return part1+part2;
    }
}
