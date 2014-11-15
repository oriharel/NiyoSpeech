package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NiyoInteraction} interface
 * to handle interaction events.
 * Use the {@link WelcomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment implements TextToSpeech.OnInitListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextToSpeech _defaultTts;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private NiyoInteraction mListener;
    public static final String LOG_TAG = WelcomeFragment.class.getSimpleName();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WelcomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        _defaultTts = new TextToSpeech(getActivity(), this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView started");
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.welcome, container, false);

        String ttsPkg = _defaultTts.getDefaultEngine();

        PackageManager packageManager = getActivity().getPackageManager();
        try {
            ApplicationInfo engineInfo = packageManager.getApplicationInfo(ttsPkg, PackageManager.GET_META_DATA);
            CharSequence engineLabel = packageManager.getApplicationLabel(engineInfo);
            Drawable appIcon = packageManager.getApplicationIcon(engineInfo);

            ImageView ttsImage = (ImageView)layout.findViewById(R.id.tts_icon);
            TextView ttsLabel = (TextView)layout.findViewById(R.id.tts_name);

            ttsImage.setImageDrawable(appIcon);
            ttsLabel.setText(engineLabel);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPref.getBoolean("example_checkbox", false)){
//            findViewById(R.id)
        }

        TextView welcome = (TextView)layout.findViewById(R.id.welcome);
        Typeface roboto_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
//        welcome.setTypeface(roboto_font);


        return layout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri.toString());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NiyoInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onInit(int i) {

    }

}
