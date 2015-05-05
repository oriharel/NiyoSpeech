package speech.niyo.com.niyospeech.apps;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import java.util.List;
import java.util.Map;

import speech.niyo.com.niyospeech.R;

/**
 * Created by oriharel on 7/13/14.
 */
public class AppListAdapter extends BaseAdapter{

    private final LayoutInflater mInflater;
    public static final String LOG_TAG = AppListAdapter.class.getSimpleName();
    public static final String PREFERENCES_FILE_NAME = "settings_data";
    public static final String APPS_TO_SPEECH = "apps_to_speech";
    private List<App> mApps;
    /** a map which maps the package name of an app to its icon drawable */
    private Map<String, Drawable> mIcons;
//    private List<String> mSelecteds;
    private Drawable mStdImg;
    private final Activity _activity;

    public AppListAdapter(Activity context) {
        // cache the LayoutInflater to avoid asking for a new one each time
        mInflater = LayoutInflater.from(context);

        // set the default icon until the actual icon is loaded for an app
        mStdImg = context.getResources().getDrawable(R.drawable.ic_launcher);
        _activity = context;

    }
    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        AppViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.pkg_list_item2, null);

            // creates a ViewHolder and stores a reference to the children view we want to bind data to
            holder = new AppViewHolder();
//            holder.mTitle = (TextView) convertView.findViewById(R.id.apptitle);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.appicon);
            holder.mCheckbox = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            // reuse/overwrite the view passed assuming(!) that it is castable!
            holder = (AppViewHolder) convertView.getTag();
        }

        App app = mApps.get(position);

        holder.setTitle(app.getTitle());
//        if (mSelecteds != null) {
        holder.mCheckbox.setChecked(app.getIsSelected());
//        }

        if (app.getIcon() != null) {
            holder.setIcon(app.getIcon());
        }
        if (mIcons == null || mIcons.get(app.getPackageName()) == null) {
            holder.setIcon(mStdImg);
        } else {
            holder.setIcon(mIcons.get(app.getPackageName()));
        }

        return convertView;
    }

    /**
     * Sets the list of apps to be displayed.
     *
     * @param list the list of apps to be displayed
     */
    public void setListItems(List<App> list) {
        mApps = list;
    }

    /**
     * Sets the map containing the icons for each displayed app.
     *
     * @param icons the map which maps the app's package name to its icon
     */
    public void setIcons(Map<String, Drawable> icons) {
        this.mIcons = icons;
    }

//    public void setSelecteds(List<String> selecteds) {
//        this.mSelecteds = selecteds;
//    }

    /**
     * A view holder which is used to re/use views inside a list.
     */
    public class AppViewHolder {

//        private TextView mTitle;
        private ImageView mIcon;
        private CheckedTextView mCheckbox;

        /**
         * Sets the text to be shown as the app's title
         *
         * @param title the text to be shown inside the list row
         */
        public void setTitle(String title) {
            mCheckbox.setText(title);
        }

        /**
         * Sets the icon to be shown next to the app's title
         *
         * @param img the icon drawable to be displayed
         */
        public void setIcon(Drawable img) {
            if (img != null) {
                mIcon.setImageDrawable(img);
            }
        }
    }
}
