package speech.niyo.com.niyospeech;

import android.content.Context;
import android.widget.ArrayAdapter;

import speech.niyo.com.niyospeech.dummy.LangContent;

/**
 * Created by oriharel on 12/5/14.
 */
public class LanguagesAdapter extends ArrayAdapter<LangContent.LangItem> {
    public LanguagesAdapter(Context context, int resource) {
        super(context, resource);
    }
}
