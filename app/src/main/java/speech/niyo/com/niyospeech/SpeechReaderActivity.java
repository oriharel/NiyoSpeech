package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import speech.niyo.com.niyospeech.speakers.DefaultNiyoSpeaker;
import speech.niyo.com.niyospeech.speech.SpeechService;


public class SpeechReaderActivity extends Activity {

    public static final String LOG_TAG = SpeechReaderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_reader);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }

        TextView tv = (TextView)findViewById(R.id.reader_text);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

        Context _context;
        public DownloadWebPageTask(Context context) {
            _context = context;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                try {
                    Log.d(LOG_TAG, "received result " + result);
                    JSONObject resultJson = new JSONObject(result);
                    String spechText = resultJson.getString("content");
                    speak(spechText);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error parsing json from "+result);
                }

            }


        }
    }

    private void speak(String textToSpeak) {
        Intent speakIntent = new Intent(this, SpeechService.class);
        speakIntent.putExtra(SpeechService.SPEAKING_TEXT_EXTRA, Html.fromHtml(textToSpeak).toString());
        speakIntent.putExtra(SpeechService.SPEAK_RESOLVER_EXTRA, new DefaultNiyoSpeaker());
        Log.d(LOG_TAG, "firing the speak service with " + textToSpeak);
        startService(speakIntent);

        TextView tv = (TextView)findViewById(R.id.reader_text);
        tv.setText(Html.fromHtml(textToSpeak));
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            List<String> result = new ArrayList<String>();

            Pattern pattern = Pattern.compile(
                    "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
                            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
                            "|mil|biz|info|mobi|name|aero|jobs|museum" +
                            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
                            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
                            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
                            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
                            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

            Matcher matcher = pattern.matcher(sharedText);
            while (matcher.find()) {
                result.add(matcher.group());
            }

            if (result.size() > 0) {
                DownloadWebPageTask task = new DownloadWebPageTask(this);

                String parserUrl = "https://readability.com/api/content/v1/parser?url="+result.get(0)+"&token=a619feb9b5c4e1c2ed1e0c167f02bbf12da72efa";
                Log.d(LOG_TAG, "fetching webpage at " + parserUrl);
                task.execute(parserUrl);
            }
            else {
                speak(sharedText);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speech_reader, menu);
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
}
