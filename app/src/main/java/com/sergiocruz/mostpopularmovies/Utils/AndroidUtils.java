package com.sergiocruz.mostpopularmovies.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.sergiocruz.mostpopularmovies.R;

/**
 * Created by Sergio on 25/02/2018.
 */

public class AndroidUtils {

    //    int width = size.x; returns window width
    //    int height = size.y; returns window height
    public static Point getWindowSizeXY(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            height = context.getResources().getDimensionPixelSize(resourceId);
        return height;
    }

    public static final int getPxFromDp(int dp) {
        return (int) (dp * (getDisplayDensity() / 160f));
    }

    public static final int getDpFromPx(int pixels) {
        return (int) (pixels * 160f) / getDisplayDensity();
    }

    public static final int getDisplayDensity() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.densityDpi;
    }

    public static final int getDensityFactor() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (metrics.densityDpi / 160f);
    }

    public static String getOptimalImageWidth(Context context, int imageWidth) {
        String width = "original";
        String[] imageSizes = context.getResources().getStringArray(R.array.image_sizes);
        int[] imageWidthsPx = context.getResources().getIntArray(R.array.image_width_px);

//        int imageWidth = getPxFromDp(imageWidthDp);

        int arrLenght_1 = imageWidthsPx.length - 1;
        for (int i = 0; i < arrLenght_1; i++) {
            if (imageWidth >= imageWidthsPx[i] && imageWidth < imageWidthsPx[i + 1])
                width = imageSizes[i];
        }
        if (imageWidth >= imageWidthsPx[arrLenght_1])
            width = imageSizes[arrLenght_1];

        return width;
    }

    public static void animateViewsOnPreDraw(View[] viewsToAnimate) {
        Interpolator interpolator = new DecelerateInterpolator();
        for (int i = 0; i < viewsToAnimate.length; ++i) {
            View view = viewsToAnimate[i];
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            view.setAlpha(0);
            view.setTranslationY(100);
            view.animate()
                    .setInterpolator(interpolator)
                    .alpha(1)
                    .translationY(0)
                    .setStartDelay(50 * (i + 1))
                    .start();
        }
    }

}
