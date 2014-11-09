package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class WelcomeActivity extends Activity implements TextToSpeech.OnInitListener{

    private TextToSpeech _defaultTts;
    public static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        _defaultTts = new TextToSpeech(this, this);
        String ttsPkg = _defaultTts.getDefaultEngine();

        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo engineInfo = packageManager.getApplicationInfo(ttsPkg, PackageManager.GET_META_DATA);
            CharSequence engineLabel = packageManager.getApplicationLabel(engineInfo);
            Drawable appIcon = packageManager.getApplicationIcon(engineInfo);

            ImageView ttsImage = (ImageView)findViewById(R.id.tts_icon);
            TextView ttsLabel = (TextView)findViewById(R.id.tts_name);

            ttsImage.setImageDrawable(appIcon);
            ttsLabel.setText(engineLabel);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("example_checkbox", false)){
//            findViewById(R.id)
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int i) {

    }
}
