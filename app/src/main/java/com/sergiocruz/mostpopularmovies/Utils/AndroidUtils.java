package com.sergiocruz.mostpopularmovies.Utils;

import android.content.Context;
import android.graphics.Point;
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

}
