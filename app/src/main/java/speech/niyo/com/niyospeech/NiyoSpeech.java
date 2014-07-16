package speech.niyo.com.niyospeech;

import android.net.Uri;

/**
 * Created by oriharel on 7/15/14.
 */
public class NiyoSpeech {
    public static String AUTHORITY = "com.niyo.speech.provider";
    public static String SCHEME = "content://";
    public static final String FEEDS = "/apps";
    public static final Uri APPS_URI =  Uri.parse(SCHEME + AUTHORITY + FEEDS);
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.speech.app";

    public static final String[] APPS_SUMMARY_PROJECTION = new String[] {
            AppsColumns.APP_PKG
    };

}
