package speech.niyo.com.niyospeech;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import speech.niyo.com.niyospeech.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link speech.niyo.com.niyospeech.NiyoInteraction}
 * interface.
 */
public class ItemFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String LOG_TAG = ItemFragment.class.getSimpleName();
    private static final boolean INCLUDE_SYSTEM_APPS = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private NiyoInteraction mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private AppListAdapter mAdapter;

    LoaderManager.LoaderCallbacks<Cursor> mLoader;
    int mLoaderId;
    private List<App> mApps;

    // TODO: Rename and change types of parameters
    public static ItemFragment newInstance(String param1, String param2) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mApps = loadInstalledApps(INCLUDE_SYSTEM_APPS);

        mAdapter = new AppListAdapter(getActivity());
        mAdapter.setListItems(mApps);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        initLoader();

        getLoaderManager().initLoader(0, null, mLoader);
        mListView.setSelection(0);

        new LoadIconsTask().execute(mApps.toArray(new App[]{}));

        return view;
    }

    private void initLoader() {

        mLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri baseUri = NiyoSpeech.APPS_URI;

                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                String select = "((" + AppsColumns.APP_PKG + " NOTNULL) AND ("
                        + AppsColumns.APP_PKG + " != '' ))";
                Loader<Cursor> result = new CursorLoader(getActivity(), baseUri,
                        NiyoSpeech.APPS_SUMMARY_PROJECTION, select, null,
                        AppsColumns.APP_PKG + " COLLATE LOCALIZED ASC");

                mLoaderId = result.getId();
                return result;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

                Log.d(LOG_TAG, "finished loading feeds from local storage");

                if (cursor.getCount() <= 0) {
                    Log.d(LOG_TAG, "got 0 feeds from local storage");
                }
                else {
                    setSelectedApps(cursor);
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {

            }
        };

    }

    private List<App> loadInstalledApps(boolean includeSysApps) {
        List<App> apps = new ArrayList<App>();


        // the package manager contains the information about all installed apps
        PackageManager packageManager = getActivity().getPackageManager();

        List<PackageInfo> packs = packageManager.getInstalledPackages(0); //PackageManager.GET_META_DATA

        for (PackageInfo p : packs) {
            ApplicationInfo a = p.applicationInfo;
            // skip system apps if they shall not be included
            if ((!includeSysApps) && ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                continue;
            }
            App app = new App();
            app.setTitle(p.applicationInfo.loadLabel(packageManager).toString());
            app.setPackageName(p.packageName);
            app.setVersionName(p.versionName);
            app.setVersionCode(p.versionCode);
            CharSequence description = p.applicationInfo.loadDescription(packageManager);
            app.setDescription(description != null ? description.toString() : "");
            apps.add(app);
        }

        App tempApp = new App();
        tempApp.setPackageName("com.google.android.gm");


        if (!apps.contains(tempApp)) {
            App gmailApp = new App();
            gmailApp.setTitle("Gmail");
            gmailApp.setPackageName("com.google.android.gm");
            gmailApp.setIcon(getResources().getDrawable(R.drawable.gmail_icon));
            apps.add(gmailApp);
        }

        tempApp.setPackageName("com.google.android.talk");

        if (!apps.contains(tempApp)) {
            App hangouts = new App();
            hangouts.setTitle("Hangouts");
            hangouts.setPackageName("com.google.android.talk");
            hangouts.setIcon(getResources().getDrawable(R.drawable.hangouts_icon));
            apps.add(hangouts);
        }
        App[] toArray = new App[apps.size()];
        apps.toArray(toArray);
        Arrays.sort(toArray, new Comparator<Object>() {
            @Override
            public int compare(Object o, Object o2) {

                if (o instanceof App && o2 instanceof App) {
                    return ((App) o).getTitle().compareTo(((App) o2).getTitle());
                } else {
                    return 0;
                }

            }
        });
        return Arrays.asList(toArray);
    }

    private void setSelectedApps(Cursor cursor) {
        List<String> selecteds = new ArrayList<String>();

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            String pkg = cursor.getString(0);
            selecteds.add(pkg);
            cursor.moveToNext();
        }

        for (App app : mApps) {
            if (selecteds.contains(app.getPackageName())) {
                app.setIsSelected(true);
            }
        }

        mAdapter.notifyDataSetChanged();

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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
        Log.d(LOG_TAG, "clicked!!!");
//        ((CheckedTextView)adapterView.findViewById(android.R.id.text1)).setChecked(true);
        Log.d(LOG_TAG, "position: "+adapterView.getItemAtPosition(i));
        mApps.get(i).setIsSelected(!mApps.get(i).getIsSelected());
        mAdapter.notifyDataSetChanged();



        if (mApps.get(i).getIsSelected()) {
            ContentValues values = new ContentValues();
            values.put(AppsColumns.APP_PKG, mApps.get(i).getPackageName());
            Uri insertResult = getActivity().getContentResolver().insert(NiyoSpeech.APPS_URI, values);
        }
        else {
            String select = "((" + AppsColumns.APP_PKG + " NOTNULL) AND ("
                    + AppsColumns.APP_PKG + " = '"+mApps.get(i).getPackageName()+"' ))";
            int delResult = getActivity().getContentResolver().delete(NiyoSpeech.APPS_URI, select, null);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * An asynchronous task to load the icons of the installed applications.
     */
    private class LoadIconsTask extends AsyncTask<App, Void, Void> {
        @Override
        protected Void doInBackground(App... apps) {

            Map<String, Drawable> icons = new HashMap<String, Drawable>();
            PackageManager manager = getActivity().getApplicationContext().getPackageManager();

            for (App app : apps) {
                String pkgName = app.getPackageName();
                Drawable ico = null;
                try {
                    Intent i = manager.getLaunchIntentForPackage(pkgName);
                    if (i != null) {
                        ico = manager.getActivityIcon(i);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("ERROR", "Unable to find icon for package '" + pkgName + "': " + e.getMessage());
                }
                icons.put(app.getPackageName(), ico);
            }
            mAdapter.setIcons(icons);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
        }
    }


}
