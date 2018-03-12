package com.sergiocruz.mostpopularmovies.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
import android.util.Log;

import com.sergiocruz.mostpopularmovies.JSONParser;
import com.sergiocruz.mostpopularmovies.MovieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.Utils.NetworkUtils;
import com.sergiocruz.mostpopularmovies.VideoObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Sergio on 06/03/2018.
 */

public class VideosLoader extends AsyncTaskLoader<ArrayList<VideoObject>> {
    private WeakReference<Context> weakContext;
    private Uri queryUri;
    private Boolean gotFavorite;
    // Initialize a VideoObject, this will hold all the videos data
    private ArrayList<VideoObject> mVideoData;

    public VideosLoader(@NonNull Context context, Uri queryURI, Boolean gotFavorite) {
        super(context);
        this.weakContext = new WeakReference<>(context);
        this.queryUri = queryURI;
        this.gotFavorite = gotFavorite;
    }

    /**
     * Subclasses must implement this to take care of loading their data,
     * as per {@link #startLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #startLoading()}.
     * This will always be called from the process's main thread.
     * onStartLoading() is called when a loader first starts loading data
     */
    @Override
    protected void onStartLoading() {
        if (mVideoData != null) {
            // Delivers any previously loaded data immediately
            deliverResult(mVideoData);

        } else {
            // Force a new load
            forceLoad();
        }
    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     * Implementations should not deliver the result directly, but should return them
     * from this method, which will eventually end up calling {@link #deliverResult} on
     * the UI thread.  If implementations need to process the results on the UI thread
     * they may override {@link #deliverResult} and do so there.
     * <p>
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * <p>
     * When the load is canceled, this method may either return normally or throw
     * {@link OperationCanceledException}.  In either case, the {@link Loader} will
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Nullable
    @Override
    public ArrayList<VideoObject> loadInBackground() {

        // Query and load all data in the background;
        // Use a try/catch block to catch any errors in loading data

        ArrayList<VideoObject> videosData;

        try {
            if (gotFavorite) { // get all favorites from database
                Cursor cursor = (weakContext.get()).getContentResolver()
                        .query(
                                queryUri,
                                null,
                                null,
                                null,
                                null);
                videosData = getArrayListFromCursor(cursor);

            } else { // get movie data from internet

                String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(queryUri);
                if (jsonDataFromAPI == null) return null;
                videosData = JSONParser.parseVideosDataFromJSON(jsonDataFromAPI);
            }

        } catch (Exception e) {
            Log.e("Sergio>", "Failed to asynchronously load data.");
            e.printStackTrace();
            return null;
        }

        super.deliverResult(videosData);
        return videosData;
    }

    // deliverResult sends the result of the load to the registered listener
    public void deliverResult(ArrayList<VideoObject> data) {
        mVideoData = data;
        super.deliverResult(data);

    }

    private ArrayList<VideoObject> getArrayListFromCursor(Cursor cursor) {
        int cursorCount = cursor.getCount();

        Log.i("Sergio>", this + " getArrayListFromCursor Videos\n= columnCount" + cursor.getColumnCount());

        ArrayList<VideoObject> arrayList = new ArrayList<>(cursorCount);

        for (int i = 0; i < cursorCount; i++) {
            while (cursor.moveToNext()) {
                arrayList.add(
                        new VideoObject(
                                cursor.getString(MovieContract.VideosTable.VIDEO_ID_INDEX),
                                cursor.getString(MovieContract.VideosTable.ISO_639_1_INDEX),
                                cursor.getString(MovieContract.VideosTable.ISO_3166_1_INDEX),
                                cursor.getString(MovieContract.VideosTable.KEY_INDEX),
                                cursor.getString(MovieContract.VideosTable.NAME_INDEX),
                                cursor.getString(MovieContract.VideosTable.SITE_INDEX),
                                cursor.getInt(MovieContract.VideosTable.SIZE_INDEX),
                                cursor.getString(MovieContract.VideosTable.TYPE_INDEX)
                        ));
            }
        }

        return arrayList;
    }


}

