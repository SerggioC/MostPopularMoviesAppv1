package com.sergiocruz.mostpopularmovies.loaders;

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
import com.sergiocruz.mostpopularmovies.movieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.model.MovieObject;
import com.sergiocruz.mostpopularmovies.utils.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Sergio on 06/03/2018.
 * <p>
 * Load favorite Movies from Database or different Movie sections from Internet
 */

public class MoviesLoader extends AsyncTaskLoader<ArrayList<MovieObject>> {
    private WeakReference<Context> weakContext;
    private Uri queryUri;
    private Boolean getFavorites;

    // Initialize a ArrayList, this will hold all the data
    private ArrayList<MovieObject> mMovieData;

    public MoviesLoader(@NonNull Context context, Uri uri, Boolean getFavorites) {
        super(context);
        this.weakContext = new WeakReference<>(context);
        this.queryUri = uri;
        this.getFavorites = getFavorites;
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
        if (mMovieData != null) {
            // Delivers any previously loaded data immediately
            deliverResult(mMovieData);

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
    public ArrayList<MovieObject> loadInBackground() {

        // Query and load all movie data in the background;
        // Use a try/catch block to catch any errors in loading data
        ArrayList<MovieObject> data;

        try {
            if (getFavorites) { // get all favorites from database
                Cursor cursor = weakContext.get()
                        .getContentResolver()
                        .query(
                                queryUri,
                                null,
                                null,
                                null,
                                null);
                data = getArrayListFromCursor(cursor);

            } else { // get movie data from internet

                String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(queryUri);
                if (jsonDataFromAPI == null) return null;

                data = JSONParser.parseMovieDataFromJSON(jsonDataFromAPI);
            }

        } catch (Exception e) {
            Log.e("Sergio>", "Failed to asynchronously load data.");
            e.printStackTrace();
            return null;
        }

        super.deliverResult(data);
        return data;
    }

    // deliverResult sends the result of the load, ArrayList, to the registered listener
    public void deliverResult(ArrayList<MovieObject> data) {
        mMovieData = data;
        super.deliverResult(data);
    }

    private ArrayList<MovieObject> getArrayListFromCursor(Cursor cursor) {
        int cursorCount = cursor.getCount();

        Log.i("Sergio>", this + " getArrayListFromCursor\n= columnCount" + cursor.getColumnCount());

        ArrayList<MovieObject> arrayList = new ArrayList<>(cursorCount);

        for (int i = 0; i < cursorCount; i++) {
            while (cursor.moveToNext()) {
                arrayList.add(
                        new MovieObject(
                                cursor.getInt(MovieContract.MovieTable.VOTE_COUNT_INDEX),
                                cursor.getInt(MovieContract.MovieTable.MOVIE_ID_INDEX),
                                cursor.getInt(MovieContract.MovieTable.HAS_VIDEO_INDEX) == 1,
                                cursor.getFloat(MovieContract.MovieTable.VOTE_AVERAGE_INDEX),
                                cursor.getString(MovieContract.MovieTable.TITLE_INDEX),
                                cursor.getFloat(MovieContract.MovieTable.POPULARITY_INDEX),
                                cursor.getString(MovieContract.MovieTable.POSTER_PATH_INDEX),
                                cursor.getString(MovieContract.MovieTable.ORIGINAL_LANGUAGE_INDEX),
                                cursor.getString(MovieContract.MovieTable.ORIGINAL_TITLE_INDEX),
                                JSONParser.getIntArrayFromJSON(cursor.getString(MovieContract.MovieTable.GENRE_ID_INDEX)),
                                cursor.getString(MovieContract.MovieTable.BACKDROP_PATH_INDEX),
                                cursor.getInt(MovieContract.MovieTable.IS_ADULT_INDEX) == 1,
                                cursor.getString(MovieContract.MovieTable.OVERVIEW_INDEX),
                                cursor.getString(MovieContract.MovieTable.RELEASE_DATE_INDEX),
                                cursor.getInt(MovieContract.MovieTable.IS_FAVORITE_INDEX) == 1,
                                cursor.getString(MovieContract.MovieTable.POSTER_FILE_PATH_INDEX),
                                cursor.getString(MovieContract.MovieTable.BACKDROP_FILE_PATH_INDEX)
                        )); //add the movie
            }
        }


        return arrayList;
    }


}

