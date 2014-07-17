package speech.niyo.com.niyospeech;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

        Object contentText = getContentTextKiKat(notif);

        if (contentText == null) {
            contentText = getContentTextOther(notif);
        }


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

        Pattern p = Pattern.compile("\\p{InHebrew}");
        Matcher m = p.matcher(result);

        if (m.find()) {
            Log.d(LOG_TAG, "the text has hebrew letters");
        }

        return result;
    }

    private Object getContentTextOther(Notification notification) {
        RemoteViews views = notification.contentView;
        Class secretClass = views.getClass();

        Map<Integer, String> text = new HashMap<Integer, String>();

        try {


            Field outerField = secretClass.getDeclaredField("mActions");
            outerField.setAccessible(true);
            ArrayList<Object> actions = (ArrayList<Object>) outerField.get(views);

            for (Object action : actions) {
                Field innerFields[] = action.getClass().getDeclaredFields();
                Field innerFieldsSuper[] = action.getClass().getSuperclass().getDeclaredFields();

                Object value = null;
                Integer type = null;
                Integer viewId = null;
                for (Field field : innerFields) {
                    field.setAccessible(true);
                    if (field.getName().equals("value")) {
                        value = field.get(action);
                    } else if (field.getName().equals("type")) {
                        type = field.getInt(action);
                    }
                }
                for (Field field : innerFieldsSuper) {
                    field.setAccessible(true);
                    if (field.getName().equals("viewId")) {
                        viewId = field.getInt(action);
                    }
                }

                if (value != null && type != null && viewId != null && (type == 9 || type == 10)) {
                    text.put(viewId, value.toString());
                }
            }

            System.out.println("title is: " + text.get(16908310));
            System.out.println("info is: " + text.get(16909082));
            System.out.println("text is: " + text.get(16908358));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text.get(16908358);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Object getContentTextKiKat(Notification notif) {
        Bundle bundle = notif.extras;
        return bundle.get(Notification.EXTRA_TEXT);
    }
}
