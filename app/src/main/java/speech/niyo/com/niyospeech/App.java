package speech.niyo.com.niyospeech;

import android.graphics.drawable.Drawable;

/**
 * Created by oriharel on 7/13/14.
 */
public class App {
    private String _title;
    private String _packageName;
    private String _versionName;
    private int _versionCode;
    private String _description;
    private Boolean isSelected = false;
    private Drawable _icon;

    public String getTitle() {
        return _title;
    }

    public String getPackageName() {
        return _packageName;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public void setPackageName(String packageName) {
        this._packageName = packageName;
    }

    public void setVersionName(String versionName) {
        this._versionName = versionName;
    }

    public String get_versionName() {
        return _versionName;
    }

    public void set_versionName(String _versionName) {
        this._versionName = _versionName;
    }

    public void setVersionCode(int versionCode) {
        this._versionCode = versionCode;
    }

    public int get_versionCode() {
        return _versionCode;
    }

    public void set_versionCode(int _versionCode) {
        this._versionCode = _versionCode;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Boolean getIsSelected() {
        return this.isSelected;
    }

    @Override
    public String toString() {
        return this._title;
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof App) {
            return this._packageName.equals(((App)other).getPackageName());
        }
        else {
            return this._packageName.equals(other.toString());
        }

    }

    public Drawable getIcon() {
        return this._icon;
    }

    public void setIcon(Drawable icon) {
        this._icon = icon;
    }
}
