package com.sergiocruz.mostpopularmovies.adapters;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class BindingAdapters {

    @BindingAdapter("drawableLeft")
    public static void setDrawableLeft(TextView textView, Drawable drawableU) {
        //Drawable drawable = AppCompatResources.getDrawable(textView.getContext(), resourceId);
        Drawable[] drawables = textView.getCompoundDrawables();
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableU,
                drawables[1], drawables[2], drawables[3]);
    }
}
