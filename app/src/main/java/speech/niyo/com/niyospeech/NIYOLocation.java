package speech.niyo.com.niyospeech;

/**
 * Created by oriharel on 11/22/14.
 */
public class NIYOLocation {

    private String _latitude;
    private String _longitude;
    private String _text;

    public String getLatitude() {
        return _latitude;
    }

    public void setLatitude(String _latitude) {
        this._latitude = _latitude;
    }

    public String getLongitude() {
        return _longitude;
    }

    public void setLongitude(String _longitude) {
        this._longitude = _longitude;
    }

    public String getText() {
        return _text;
    }

    public void setText(String _text) {
        this._text = _text;
    }

    @Override
    public String toString() {
        return getText();
    }
}
