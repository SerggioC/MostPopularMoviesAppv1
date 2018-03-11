package com.sergiocruz.mostpopularmovies.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sergiocruz.mostpopularmovies.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

/**
 * Created by Sergio on 25/02/2018.
 */

public class AndroidUtils {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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

    public static void animateViewsOnPreDraw(View parent, View[] viewsToAnimate) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            return;
        ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
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
                parent.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        parent.getViewTreeObserver().addOnPreDrawListener(listener);

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // if we don't have permission prompt the user to give permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public static Bitmap getBitmapFromURL(Context context, String url) {
        Bitmap bitmap;
        try {
            bitmap = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .submit()
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Nullable
    public static Uri saveBitmapToDevice(Context context, ImageView imageView, String fileName) {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        Uri fileUri;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, fileName);
            OutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream); // saving the Bitmap to a file compressed as a JPEG with 100% quality
            fileOutputStream.flush();
            fileOutputStream.close();
            fileUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fileUri;
    }
}
