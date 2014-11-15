package speech.niyo.com.niyospeech;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by oriharel on 11/15/14.
 */
public class NiyoApplication extends Application {
    private static Typeface robotoTypeface;
    public static final String PATH_TYPEFACE_ROBOTO = "fonts/Roboto-Light.ttf";

    public Typeface getRobotoTypeface() {
        if (robotoTypeface == null) {
            robotoTypeface = Typeface.createFromAsset(getAssets(), PATH_TYPEFACE_ROBOTO);
        }
        return robotoTypeface;
    }
}
