package com.sergiocruz.mostpopularmovies.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
import android.util.Log;

import com.sergiocruz.mostpopularmovies.JSONParser;
import com.sergiocruz.mostpopularmovies.movieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.model.ReviewObject;
import com.sergiocruz.mostpopularmovies.utils.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Sergio on 06/03/2018.
 */


public class ReviewsLoader extends AsyncTaskLoader<ArrayList<ReviewObject>> {
    private WeakReference<Context> weakContext;
    private Uri queryUri;
    private Boolean gotFavorite;
    // Initialize a VideoObject, this will hold all the videos data
    private ArrayList<ReviewObject> mReviewObjects;

    public ReviewsLoader(Context context, Uri queryURI, Boolean gotFavorite) {
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
        if (mReviewObjects != null) {
            // Delivers any previously loaded data immediately
            deliverResult(mReviewObjects);

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
    public ArrayList<ReviewObject> loadInBackground() {

        // Query and load all task data in the background; sort by priority
        // [Hint] use a try/catch block to catch any errors in loading data
        // * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US

        ArrayList<ReviewObject> reviewsData;

        try {
            if (gotFavorite) { // get all favorites from database
                Cursor cursor = weakContext.get().getContentResolver()
                        .query(
                                queryUri,
                                null,
                                null,
                                null,
                                null);
                reviewsData = getArrayListFromCursor(cursor);

            } else { // get movie data from internet

                String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(queryUri);
                if (jsonDataFromAPI == null) return null;
                reviewsData = JSONParser.parseReviewsDataFromJSON(jsonDataFromAPI);
            }

        } catch (Exception e) {
            Log.e("Sergio>", "Failed to asynchronously load data.");
            e.printStackTrace();
            return null;
        }

        super.deliverResult(reviewsData);
        return reviewsData;
    }

    // deliverResult sends the result of the load, a Cursor, to the registered listener
    public void deliverResult(ArrayList<ReviewObject> data) {
        mReviewObjects = data;
        super.deliverResult(data);

    }

    private ArrayList<ReviewObject> getArrayListFromCursor(Cursor cursor) {
        int cursorCount = cursor.getCount();

        Log.i("Sergio>", this + " getArrayListFromCursor\n= columnCount" + cursor.getColumnCount());

        ArrayList<ReviewObject> arrayList = new ArrayList<>(cursorCount);

        for (int i = 0; i < cursorCount; i++) {
            while (cursor.moveToNext()) {
                arrayList.add(
                        new ReviewObject(
                                cursor.getString(MovieContract.ReviewsTable.REVIEW_ID_INDEX),
                                cursor.getString(MovieContract.ReviewsTable.AUTHOR_INDEX),
                                cursor.getString(MovieContract.ReviewsTable.CONTENT_INDEX),
                                cursor.getString(MovieContract.ReviewsTable.URL_INDEX)
                        ));
            }
        }
        return arrayList;
    }

}

