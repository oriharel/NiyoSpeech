package speech.niyo.com.niyospeech.cutsomviews;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import speech.niyo.com.niyospeech.R;

/**
 * Created by oriharel on 11/15/14.
 */
public class NiyoTextView extends TextView {

    private static Map<String, Typeface> mTypefaces;

    public NiyoTextView(final Context context) {
        this(context, null);
    }

    public NiyoTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NiyoTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        if (mTypefaces == null){
            mTypefaces = new HashMap<String, Typeface>();
        }

        if (this.isInEditMode()) {
            return;
        }

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NiyoTextView);
        if (array != null) {
            final String typefaceAssethPath = array.getString(R.styleable.NiyoTextView_customTypeface);

            if (typefaceAssethPath != null) {
                Typeface typeface = null;

                if (mTypefaces.containsKey(typefaceAssethPath)) {
                    typeface = mTypefaces.get(typefaceAssethPath);
                }
                else {
                    AssetManager assets = context.getAssets();
                    typeface = Typeface.createFromAsset(assets, typefaceAssethPath);
                    mTypefaces.put(typefaceAssethPath, typeface);
                }

                setTypeface(typeface);
            }
            array.recycle();
        }

    }
}
