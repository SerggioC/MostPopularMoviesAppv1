package com.sergiocruz.mostpopularmovies.utils;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.activities.DetailsActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by Sergio on 25/02/2018.
 * Utilities
 */

public final class AndroidUtils {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Avoid instantiation
    private AndroidUtils() {
    }

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

    public static int getPxFromDp(int dp) {
        return (int) (dp * (getDisplayDensity() / 160f));
    }

    public static int getDpFromPx(int pixels) {
        return (int) (pixels * 160f) / getDisplayDensity();
    }

    public static int getDisplayDensity() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.densityDpi;
    }

    public static int getDensityFactor() {
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

    /**
     * pass new View[]{view1, view2, view3, view...}
     */
    public static void animateViewsOnPreDraw(View parent, @Nullable DetailsActivity.OnPreDrawCompleteListener onPreDrawCompleteListener, View[] viewsToAnimate) {
        ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Interpolator interpolator = new DecelerateInterpolator();
                int length = viewsToAnimate.length;
                ViewPropertyAnimator animator = null;
                for (int i = 0; i < length; i++) {
                    View view = viewsToAnimate[i];
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    view.setAlpha(0);
                    view.setScaleX(0.8f);
                    view.setScaleY(0.8f);
                    view.setTranslationY(100);
                    animator = view.animate()
                            .setInterpolator(interpolator)
                            .alpha(1)
                            .scaleX(1)
                            .scaleY(1)
                            .translationY(0)
                            .setStartDelay(50 * (i + 1))
                            .setDuration(100);
                    animator.start();
                }

                animator.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onPreDrawCompleteListener != null)
                            onPreDrawCompleteListener.preDrawComplete();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

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

    public static void showSlimToast(Context context, String toastText, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        TextView layout = (TextView) inflater.inflate(R.layout.toast_layout, null);
        layout.setText(toastText);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
        layout.setAnimation(new RotateAnimation(90, 0));
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
        imageView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        return getUri(context, bitmap, fileName);
    }

    @Nullable
    private static Uri getUri(Context context, Bitmap bitmap, String fileName) {
        Uri fileUri;
        try {
            File picturesDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            File mostPMFolder = new File(picturesDirectory, context.getString(R.string.app_name));
            mostPMFolder.mkdirs(); // Make folder if it doesn't exist
            File imageFile = new File(mostPMFolder, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            //FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // write the bitmap to a ByteArrayOutputStream compressed as a JPEG with 100% quality

            fileOutputStream.write(stream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();

            fileUri = Uri.parse(imageFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fileUri;
    }

    public static Boolean deleteImageFile(String filePath) {
        if (filePath == null) return false;
        File file = new File(filePath).getAbsoluteFile();
        return file.exists() && file.delete();
    }


    public static Uri saveBitmapToDevice(Context mContext, Bitmap bitmap, String fileName) {
        return getUri(mContext, bitmap, fileName);
    }
}
