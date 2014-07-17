package speech.niyo.com.niyospeech.speakers;

import android.app.Notification;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;

import speech.niyo.com.niyospeech.NiyoSpeaker;

/**
 * Created by oriharel on 7/17/14.
 */
public class GmailSpekaer extends NiyoSpeaker {

    public static final String LOG_TAG = GmailSpekaer.class.getSimpleName();

    @Override
    public String resolveText(Notification notif) {
        String superText = super.resolveText(notif);

        if (superText == null) {
            return null;
        }
        if (superText.indexOf("http://capriza.zendesk.com/agent/#/tickets") < -1) {
            return resolveZendeskFeedback(superText);
        }
        else if (superText.indexOf("Assemb") < -1) {
            return resolveAssemblaMail(superText);
        }

        return superText;
    }

    private String resolveAssemblaMail(String superText) {
        String part1 = superText.substring(superText.indexOf("Ticket alert"), superText.indexOf("Created on:"));

        String part2 = superText.substring(superText.indexOf("Comment:"), superText.indexOf("Assembla |"));

        Log.d(LOG_TAG, "resolve assembla text is " + part1 + " " + part2);

        return part1+" "+part2;
    }

    private String resolveZendeskFeedback(String superText) {
        String part1 = superText.substring(superText.indexOf("Ticket #"), superText.indexOf("You have been assigned to this ticket"));

        String part2 = superText.substring(superText.indexOf("#rnd"), superText.indexOf("mobile logs:"));

        Log.d(LOG_TAG, "resolve zendesk text is " + part1 + " " + part2);

        return part1+" "+part2;
    }
}
