package speech.niyo.com.niyospeech.apps;

import android.app.Activity;
import android.app.ListActivity;
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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import speech.niyo.com.niyospeech.NiyoSpeech;
import speech.niyo.com.niyospeech.R;
import speech.niyo.com.niyospeech.SettingsActivity;

public class MainActivity extends ListActivity implements AdapterView.OnItemClickListener {

    private ListView mAppsList;
    private List<App> mApps;
    private AppListAdapter mAdapter;
    LoaderManager.LoaderCallbacks<Cursor> mLoader;
    int mLoaderId;

    private static final boolean INCLUDE_SYSTEM_APPS = false;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppsList = getListView();
        mAppsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mAppsList.setItemsCanFocus(false);
        mAppsList.setOnItemClickListener(this);

        mApps = loadInstalledApps(INCLUDE_SYSTEM_APPS);

        mAdapter = new AppListAdapter(this);
        mAdapter.setListItems(mApps);


        setListAdapter(mAdapter);

        initLoader();

        getLoaderManager().initLoader(0, null, mLoader);
        mAppsList.setSelection(0);

        new LoadIconsTask().execute(mApps.toArray(new App[]{}));

    }

    private void initLoader() {

        final Activity context = this;
        mLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri baseUri = NiyoSpeech.APPS_URI;

                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                String select = "((" + AppsColumns.APP_PKG + " NOTNULL) AND ("
                        + AppsColumns.APP_PKG + " != '' ))";
                Loader<Cursor> result = new CursorLoader(context, baseUri,
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

        ((AppListAdapter)getListAdapter()).notifyDataSetChanged();

    }

    /**
     * Uses the package manager to query for all currently installed apps which are put into beans and returned
     * in form of a list.
     *
     * @param includeSysApps whether or not to include system applications
     * @return a list containing an {@code App} bean for each installed application
     */
    private List<App> loadInstalledApps(boolean includeSysApps) {
        List<App> apps = new ArrayList<App>();


        // the package manager contains the information about all installed apps
        PackageManager packageManager = getPackageManager();

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
                    return ((App)o).getTitle().compareTo(((App)o2).getTitle());
                }
                else {
                    return 0;
                }

            }
        });
        return Arrays.asList(toArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
//        if (id == R.id.action_done) {
//            insertSelectedApps();
//        }
        return super.onOptionsItemSelected(item);
    }

    private void insertSelectedApps() {
        SparseBooleanArray wtf = getListView().getCheckedItemPositions();

//        long[] selecteds = getListView().getCheckedItemIds();

        Log.d(LOG_TAG, "count is "+wtf.size());
        for (int i = 0; i < getListAdapter().getCount(); i++) {
            if (wtf.get(i)) {
                App app = (App)getListAdapter().getItem(i);
                Log.d(LOG_TAG, "pkg "+app.getPackageName()+" is selected");

                ContentValues values = new ContentValues();
                values.put(AppsColumns.APP_PKG, app.getPackageName());

                Uri insertResult = getContentResolver().insert(NiyoSpeech.APPS_URI, values);
                Log.d(LOG_TAG, "insertion result: "+insertResult);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(LOG_TAG, "clicked!!!");
//        ((CheckedTextView)adapterView.findViewById(android.R.id.text1)).setChecked(true);
        Log.d(LOG_TAG, "position: "+adapterView.getItemAtPosition(i));
        mApps.get(i).setIsSelected(!mApps.get(i).getIsSelected());
        mAdapter.notifyDataSetChanged();



        if (mApps.get(i).getIsSelected()) {
            ContentValues values = new ContentValues();
            values.put(AppsColumns.APP_PKG, mApps.get(i).getPackageName());
            Uri insertResult = getContentResolver().insert(NiyoSpeech.APPS_URI, values);
        }
        else {
            String select = "((" + AppsColumns.APP_PKG + " NOTNULL) AND ("
                    + AppsColumns.APP_PKG + " = '"+mApps.get(i).getPackageName()+"' ))";
            int delResult = getContentResolver().delete(NiyoSpeech.APPS_URI, select, null);
        }


    }

    /**
     * An asynchronous task to load the icons of the installed applications.
     */
    private class LoadIconsTask extends AsyncTask<App, Void, Void> {
        @Override
        protected Void doInBackground(App... apps) {

            Map<String, Drawable> icons = new HashMap<String, Drawable>();
            PackageManager manager = getApplicationContext().getPackageManager();

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
