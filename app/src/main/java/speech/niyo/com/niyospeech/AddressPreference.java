package speech.niyo.com.niyospeech;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Location;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by oriharel on 11/21/14.
 */
public class AddressPreference extends EditTextPreference {
    public static final String LOG_TAG = AddressPreference.class.getSimpleName();

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDwamDrY7mp6CKS8SvWZJaerxe73i6mMqs";
    private NIYOLocation _selectedLocation;
    private String mKey;

    public AddressPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.Preference, defStyle, 0);
        for (int i = a.getIndexCount(); i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == com.android.internal.R.styleable.Preference_key) {
                mKey = a.getString(attr);
            }
        }
    }

    public AddressPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddressPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);


        final EditText editText = (EditText)view.findViewById(android.R.id.edit);
        ViewGroup vg = (ViewGroup)editText.getParent();
        AutoCompleteTextView ac = new AutoCompleteTextView(getContext());
        ArrayAdapter<NIYOLocation> adapter = new PlacesAutoCompleteAdapter(getContext(), R.layout.address_list_item);
        ac.setAdapter(adapter);
        ac.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NIYOLocation location = (NIYOLocation) adapterView.getItemAtPosition(position);
                editText.setText(location.getText());
                _selectedLocation = location;
            }
        });
        ac.setText(editText.getText());
        vg.addView(ac, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editText.setVisibility(View.GONE);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPref.edit().putString(mKey+"_latitude", _selectedLocation.getLatitude()).apply();
        sharedPref.edit().putString(mKey+"_longitude", _selectedLocation.getLongitude()).apply();
    }

    private ArrayList<NIYOLocation> autocomplete(String input) {
        ArrayList<NIYOLocation> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:il");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<NIYOLocation>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                NIYOLocation currLoc = new NIYOLocation();
                currLoc.setText(predsJsonArray.getJSONObject(i).getString("description"));
                resultList.add(currLoc);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<NIYOLocation> implements Filterable {
        private ArrayList<NIYOLocation> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public NIYOLocation getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }
}
