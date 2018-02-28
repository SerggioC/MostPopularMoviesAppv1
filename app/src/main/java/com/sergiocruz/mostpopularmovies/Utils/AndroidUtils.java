package com.sergiocruz.mostpopularmovies.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

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

    public static final int getPxFromDp(int pixels) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (pixels * (metrics.densityDpi / 160f));
    }
}
