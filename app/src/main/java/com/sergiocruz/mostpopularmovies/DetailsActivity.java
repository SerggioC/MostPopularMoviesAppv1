package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sergiocruz.mostpopularmovies.MovieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.Utils.AndroidUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.sergiocruz.mostpopularmovies.MainActivity.API_KEY_PARAM;
import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_EXTRA_IS_FAVORITE;
import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_MOVIE_EXTRA;
import static com.sergiocruz.mostpopularmovies.MainActivity.LANGUAGE_PARAM;
import static com.sergiocruz.mostpopularmovies.MainActivity.PAGE_PARAM;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.BASE_API_URL_V3;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.BASE_IMAGE_URL;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.MOVIES_PATH;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.VIDEOS_PATH;

public class DetailsActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks,
        AppBarLayout.OnOffsetChangedListener {

    private static final int FAVORITES_LOADER_ID = 101;
    private static final int VIDEOS_LOADER_ID = 202;
    private static final int REVIEWS_LOADER_ID = 303;
    private static final String LOADER_BUNDLE_MOVIE_ID = "movie_id_bundle";
    private static final String LOADER_BUNDLE_GOT_FAVORITE = "got_favorite_bundle";
    private static final String LOADER_BUNDLE_HAS_INTERNET = "has_internet_bundle";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private float INITIAL_IMAGE_POSITION_X;
    private float INITIAL_IMAGE_POSITION_Y;
    private float INITIAL_IMAGE_WIDTH;
    private float INITIAL_IMAGE_HEIGHT;
    private float FINAL_IMAGE_POSITION_X;
    private float FINAL_IMAGE_POSITION_Y;
    private float FINAL_IMAGE_WIDTH_PERCENTAGE = 0.5f;
    private float FINAL_IMAGE_HEIGHT_PERCENTAGE = 0.5F;

    private Context mContext;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private TextView titleTV;
    private ImageView posterImageView;
    private TextView dateTV;
    private TextView ratingTV;
    private TextView synopsisTV;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private FloatingActionButton favorite_star;
    private ImageView backdropImageView;
    static MovieObject mMovieDataFromIntent;

    public static void startAlphaAnimation(View view, long duration, int visibility) {
        AlphaAnimation alphaAnimation =
                (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);

    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.main_appbar);
        titleTV = findViewById(R.id.title_textView);
        posterImageView = findViewById(R.id.poster_imageView);
        dateTV = findViewById(R.id.date_textView);
        ratingTV = findViewById(R.id.rating_textView);
        synopsisTV = findViewById(R.id.synopsis_textView);
        favorite_star = findViewById(R.id.fab_star);
        backdropImageView = findViewById(R.id.backdrop_imageview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_details_coordinator);
        bindViews();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);

        appBarLayout.addOnOffsetChangedListener(this);
        initializeImageProperties();

        // Intent that started this activity
        Intent intent = getIntent();
        if (intent == null) {
            closeNoData();
            return;
        }

        mMovieDataFromIntent = intent.getParcelableExtra(INTENT_MOVIE_EXTRA);
        Boolean gotFavorite = intent.getBooleanExtra(INTENT_EXTRA_IS_FAVORITE, false);
        if (mMovieDataFromIntent == null && !gotFavorite) {
            closeNoData();
            return;
        }

        Boolean hasInternet = NetworkUtils.hasActiveNetworkConnection(mContext);
        if (!hasInternet) {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        initializeUIPopulating(mMovieDataFromIntent, gotFavorite, hasInternet);

        String receivedMovieId = mMovieDataFromIntent.getId().toString();

        Bundle bundle = new Bundle(2);
        bundle.putString(LOADER_BUNDLE_MOVIE_ID, receivedMovieId);
        bundle.putBoolean(LOADER_BUNDLE_GOT_FAVORITE, gotFavorite);
        bundle.putBoolean(LOADER_BUNDLE_HAS_INTERNET, hasInternet);

        if (gotFavorite)
            getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, bundle, this);

        getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, bundle, this);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, bundle, this);

        Log.i("Sergio>", this + " onCreate\nmoviedata= " + mMovieDataFromIntent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initializeUIPopulating(MovieObject movieData, Boolean gotFavorite, Boolean hasInternet) {

        if (gotFavorite) {
            favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_on));
        } else {
            favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_off));
            if (hasInternet) {
                String[] imageSizes = mContext.getResources().getStringArray(R.array.image_sizes);
                String imageSize = imageSizes[0]; // "w92" thumbnail
                String posterImageURI = new StringBuilder(BASE_IMAGE_URL).append(imageSize).append(movieData.getPoster_path()).toString();
                String backDropImageURI = new StringBuilder(BASE_IMAGE_URL).append(imageSize).append(movieData.getBackdrop_path()).toString();
                Glide.with(mContext).load(posterImageURI).into(posterImageView);
                Glide.with(mContext).load(backDropImageURI).into(backdropImageView);
            }
        }

        titleTV.setText(movieData.getTitle());
        dateTV.setText(movieData.getRelease_date().split("-")[0]);
        ratingTV.setText(movieData.getVote_average() + "/10");
        synopsisTV.setText(movieData.getOverview());

    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri queryURI = MovieContract.MovieTable.MOVIES_CONTENT_URI;
        String movieId = args.getString(LOADER_BUNDLE_MOVIE_ID);
        Boolean gotFavorite = args.getBoolean(LOADER_BUNDLE_GOT_FAVORITE);

        switch (id) {
            case FAVORITES_LOADER_ID:
                return new FavoritesMovieLoader(mContext, queryURI, movieId, gotFavorite);
            case VIDEOS_LOADER_ID:
                return new VideosLoader(mContext, queryURI, movieId, gotFavorite);
            case REVIEWS_LOADER_ID:
                return new ReviewsLoader(mContext, queryURI, movieId, gotFavorite);
            default:
                return null;
        }
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case FAVORITES_LOADER_ID:
                populateUIFromDataBase((ArrayList<MovieObject>) data);
                break;
            case VIDEOS_LOADER_ID:
                populateVideos((ArrayList<VideoObject>) data);
                break;
            case REVIEWS_LOADER_ID:
                populateReviews((ArrayList<ReviewsObject>) data);
                break;
        }
    }

    private void populateReviews(ArrayList<ReviewsObject> reviewsObjects) {

    }

    private void populateVideos(ArrayList<VideoObject> videoObjects) {

    }

    private void populateUIFromDataBase(ArrayList<MovieObject> data) {

    }



    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void closeNoData() {
        finish();
        Toast.makeText(this, R.string.no_movie_data, Toast.LENGTH_SHORT).show();
    }

    // Load data from Database and get a Cursor
    public static class FavoritesMovieLoader extends AsyncTaskLoader<ArrayList<MovieObject>> {
        WeakReference weakContext;
        Uri queryUri;
        String movieId;
        Boolean gotFavorite;

        public FavoritesMovieLoader(@NonNull Context context, Uri uri, String movieId, Boolean gotFavorite) {
            super(context);
            this.weakContext = new WeakReference(context);
            this.queryUri = uri;
            this.movieId = movieId;
            this.gotFavorite = gotFavorite;
        }

        // Initialize a Cursor, this will hold all the data
        ArrayList<MovieObject> mMovieData = new ArrayList<>();
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

            // Query and load all task data in the background; sort by priority
            // [Hint] use a try/catch block to catch any errors in loading data
            mMovieData.add(mMovieDataFromIntent);

            ArrayList<MovieObject> movies = null;

            try {
                if (gotFavorite) {

                } else {
                    Cursor cursor = ((Context) weakContext.get()).getContentResolver().query(
                            queryUri,
                            null,
                            "_id=?",
                            new String[]{movieId},
                            null);
                    movies = getArrayListFromCursor(cursor);
                }


            } catch (Exception e) {
                Log.e("Sergio>", "Failed to asynchronously load data.");
                e.printStackTrace();
                return movies;
            }

            super.deliverResult(movies);
            return movies;
        }

        private ArrayList<MovieObject> getArrayListFromCursor(Cursor cursor) {
            int cursorCount = cursor.getCount();
            ArrayList<MovieObject> arrayList = new ArrayList<>(cursorCount);

//            MovieContract.MovieTable.ALL_MOVIES_COLUMNS;


            for (int i = 0; i < cursorCount; i++) {
                while (cursor.moveToNext()) {
                    arrayList.add(
                            new MovieObject(
                                    cursor.getInt(MovieContract.MovieTable._ID_INDEX), cursor.getInt(1), cursor.getInt(2) == 0 ? true : false,
                                    cursor.getFloat(3), cursor.getString(4), cursor.getFloat(5),
                                    cursor.getString(6), cursor.getString(7), cursor.getString(8),
                                    cursor.getString(9), cursor.getString(10), cursor.getString(11),
                                    cursor.getString(12), cursor.getString(13), cursor.getString(14),
                                    cursor.getString(15), cursor.getString(16)
                            )); //add the movie
                }
            }


            return arrayList;
        }

        // deliverResult sends the result of the load, a Cursor, to the registered listener
        public void deliverResult(ArrayList<MovieObject> data) {
            mMovieData = data;
            super.deliverResult(data);

        }


    }

    public static class VideosLoader extends AsyncTaskLoader<ArrayList<VideoObject>> {
        WeakReference weakContext;
        Uri queryUri;
        String movieId;
        Boolean gotFavorite;

        public VideosLoader(@NonNull Context context, Uri queryURI, String movieId, Boolean gotFavorite) {
            super(context);
            this.weakContext = new WeakReference(context);
            this.queryUri = queryURI;
            this.movieId = movieId;
            this.gotFavorite = gotFavorite;
        }

        // Initialize a VideoObject, this will hold all the videos data
        ArrayList<VideoObject> mVideoData = null;

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

            // Query and load all task data in the background; sort by priority
            // [Hint] use a try/catch block to catch any errors in loading data
            // * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US

            ArrayList<VideoObject> videosData;
            try {

                Uri uri = Uri.parse(BASE_API_URL_V3).buildUpon()
                        .appendPath(MOVIES_PATH)
                        .appendPath(movieId)
                        .appendPath(VIDEOS_PATH)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY_V3)
                        .appendQueryParameter(LANGUAGE_PARAM, "en-US")
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();

                Log.i("Sergio>", this + " loadInBackground\nvideos query uri= " + uri);

                String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(uri);
                if (jsonDataFromAPI == null) return null;

                videosData = JSONParser.parseVideosDataFromJSON(jsonDataFromAPI);

            } catch (Exception e) {
                Log.e("Sergio>", "Failed to asynchronously load data.");
                e.printStackTrace();
                return null;
            }

            super.deliverResult(videosData);
            return videosData;
        }

        // deliverResult sends the result of the load, a Cursor, to the registered listener
        public void deliverResult(ArrayList<VideoObject> data) {
            mVideoData = data;
            super.deliverResult(data);

        }

    }


    private class ReviewsLoader extends AsyncTaskLoader<ArrayList<ReviewsObject>>  {
        WeakReference weakContext;
        Uri queryUri;
        String movieId;
        Boolean gotFavorite;

        public ReviewsLoader(Context context, Uri queryURI, String movieId, Boolean gotFavorite) {
            super(context);
            this.weakContext = new WeakReference(context);
            this.queryUri = queryURI;
            this.movieId = movieId;
            this.gotFavorite = gotFavorite;
        }

        // Initialize a VideoObject, this will hold all the videos data
        ArrayList<ReviewsObject> mReviewsObjects = null;

        /**
         * Subclasses must implement this to take care of loading their data,
         * as per {@link #startLoading()}.  This is not called by clients directly,
         * but as a result of a call to {@link #startLoading()}.
         * This will always be called from the process's main thread.
         * onStartLoading() is called when a loader first starts loading data
         */
        @Override
        protected void onStartLoading() {
            if (mReviewsObjects != null) {
                // Delivers any previously loaded data immediately
                deliverResult(mReviewsObjects);

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
        public ArrayList<ReviewsObject> loadInBackground() {

            // Query and load all task data in the background; sort by priority
            // [Hint] use a try/catch block to catch any errors in loading data
            // * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US

            ArrayList<ReviewsObject> reviewsData;
            try {

                Uri uri = Uri.parse(BASE_API_URL_V3).buildUpon()
                        .appendPath(MOVIES_PATH)
                        .appendPath(movieId)
                        .appendPath(VIDEOS_PATH)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY_V3)
                        .appendQueryParameter(LANGUAGE_PARAM, "en-US")
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();

                Log.i("Sergio>", this + " loadInBackground\nvideos query uri= " + uri);

                String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(uri);
                if (jsonDataFromAPI == null) return null;

                reviewsData = JSONParser.parseReviewsDataFromJSON(jsonDataFromAPI);

            } catch (Exception e) {
                Log.e("Sergio>", "Failed to asynchronously load data.");
                e.printStackTrace();
                return null;
            }

            super.deliverResult(reviewsData);
            return reviewsData;
        }

        // deliverResult sends the result of the load, a Cursor, to the registered listener
        public void deliverResult(ArrayList<ReviewsObject> data) {
            mReviewsObjects = data;
            super.deliverResult(data);

        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeImageProperties() {
        INITIAL_IMAGE_POSITION_X = posterImageView.getX();
        INITIAL_IMAGE_POSITION_Y = posterImageView.getY();
        INITIAL_IMAGE_WIDTH = posterImageView.getMeasuredWidth();
        INITIAL_IMAGE_HEIGHT = posterImageView.getMeasuredHeight();
        FINAL_IMAGE_POSITION_X = AndroidUtils.getPxFromDp(200);
        FINAL_IMAGE_POSITION_Y = AndroidUtils.getStatusBarHeight(mContext) + FINAL_IMAGE_POSITION_X;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
//        int maxScroll = appBarLayout.getTotalScrollRange();
//        float percentage = Math.abs(offset) / maxScroll;
//        Log.i("Sergio>", this + " onOffsetChanged\npercentage= " + percentage + "\n" +
//                "offset= " + offset + "\n" +
//                "maxScroll= " + maxScroll);
//
//        posterImageView.setTranslationX(FINAL_IMAGE_POSITION_X * percentage);
//        posterImageView.setTranslationY(FINAL_IMAGE_POSITION_Y * percentage);
//
//        int imageWidth = posterImageView.getMeasuredWidth();
//        int imageHeight = posterImageView.getMeasuredHeight();
//
//
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) posterImageView.getLayoutParams();
//        if (imageWidth <= INITIAL_IMAGE_WIDTH * FINAL_IMAGE_WIDTH_PERCENTAGE) {
//            params.width = (int) (INITIAL_IMAGE_WIDTH / percentage);
//            params.height = (int) (INITIAL_IMAGE_HEIGHT / percentage);
//        } else {
//            params.width = (int) (INITIAL_IMAGE_WIDTH * percentage);
//            params.height = (int) (INITIAL_IMAGE_HEIGHT * percentage);
//        }
//        posterImageView.setLayoutParams(params);

//        handleToolbarTitleVisibility(percentage);
//        handleAlphaOnTitle(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(titleTV, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(titleTV, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(titleTV, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(titleTV, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

}
