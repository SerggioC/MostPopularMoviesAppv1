package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;

import static android.widget.GridLayout.VERTICAL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.PosterClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<MovieObject>> {

    public static final String API_KEY_PARAM = "api_key";
    public static final String LANGUAGE_PARAM = "language";
    public static final String PAGE_PARAM = "page";
    public static final String LOADER_BUNDLE = "movie_loader_bundle";
    public static final int LOADER_ID = 1;
    public static final String INTENT_MOVIE_EXTRA = "intent_movie_extra";
    public static final int GRID_SPAN_COUNT = 2;
    public static String themoviedb_BASE_API_URL_V3;
    public static String themoviedb_MOVIES_PATH;
    public static String themoviedb_POPULAR_MOVIES_PATH;
    public static String themoviedb_TOP_RATED_MOVIES_PATH;
    public static String movieSection;
    public static String BASE_IMAGE_URL;
    public static String imageSize;
    // "https://api.themoviedb.org/3/movie/popular?api_key=<<api_key>>&language=en-US&page=1"
    // https://api.themoviedb.org/3/movie/top_rated?api_key=<<api_key>>&language=en-US&page=1

    // used for C++ JNI method calls
    static {
        System.loadLibrary("keys");
    }

    private ArrayList<MovieObject> movieObjectArrayList = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private ProgressBar loading_indicator;
    private RecyclerView gridRecyclerView;

    public static native String getNativeAPIKeyV3();

    public static native String getNativeAPIKeyV4();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridRecyclerView = findViewById(R.id.recyclergridview);
        gridRecyclerView.setHasFixedSize(true);
        GridLayoutManager manager = new GridLayoutManager(getApplicationContext(), GRID_SPAN_COUNT, VERTICAL, false);
        gridRecyclerView.setLayoutManager(manager);
        movieAdapter = new MovieAdapter(this, this, movieObjectArrayList);
        gridRecyclerView.setAdapter(movieAdapter);


        loading_indicator = findViewById(R.id.loading_indicator);
        showLoadingView();

        themoviedb_BASE_API_URL_V3 = getString(R.string.base_api_url_v3); // API V3
        themoviedb_MOVIES_PATH = getString(R.string.movie_apth);
        themoviedb_POPULAR_MOVIES_PATH = getString(R.string.popular_path);
        themoviedb_TOP_RATED_MOVIES_PATH = getString(R.string.top_rated_path);

        BASE_IMAGE_URL = getString(R.string.base_image_url);
        String[] IMAGE_SIZES = getResources().getStringArray(R.array.image_sizes);
        imageSize = IMAGE_SIZES[2];


        // To make the App open for last selected section
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        movieSection = sharedPrefs.getString(getString(R.string.movie_section_key), themoviedb_POPULAR_MOVIES_PATH);

        Bundle bundle = new Bundle(1);
        bundle.putString(LOADER_BUNDLE, movieSection);
        getSupportLoaderManager().initLoader(LOADER_ID, bundle, this).startLoading();


    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuID = item.getItemId();
        switch (menuID) {
            case R.id.menu_refresh: {
                restartLoader(movieSection);
                break;
            }
            case R.id.menu_most_popular: {
                loadMostPopular();
                break;
            }
            case R.id.menu_highest_rated: {
                loadHighestRated();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadMostPopular() {
        restartLoader(themoviedb_POPULAR_MOVIES_PATH);
        saveMovieSectionPreference(themoviedb_POPULAR_MOVIES_PATH);
    }

    private void loadHighestRated() {
        restartLoader(themoviedb_TOP_RATED_MOVIES_PATH);
        saveMovieSectionPreference(themoviedb_TOP_RATED_MOVIES_PATH);
    }

    private void saveMovieSectionPreference(String section) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString(this.getString(R.string.movie_section_key), section).apply();
    }

    private void restartLoader(String section) {
        showLoadingView();
        Bundle bundle = new Bundle(1);
        bundle.putString(LOADER_BUNDLE, section);
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
        gridRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onPosterClicked(int position) {
        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        detailsIntent.putExtra(INTENT_MOVIE_EXTRA, movieObjectArrayList.get(position));

        startActivity(detailsIntent);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<ArrayList<MovieObject>> onCreateLoader(int id, Bundle args) {
        String section = args.getString(LOADER_BUNDLE);
        MovieLoader movieloader = new MovieLoader(getApplicationContext(), section);
        movieloader.forceLoad();
        return movieloader;
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<MovieObject>> loader, ArrayList<MovieObject> data) {
        movieAdapter.swapMovieData(data);
        //if (data.size() != 0) showDataView();
        showDataView();
        movieObjectArrayList = data;
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<ArrayList<MovieObject>> loader) {
        movieAdapter.swapMovieData(null);

    }


    private void showDataView() {
        gridRecyclerView.setVisibility(View.VISIBLE);
        loading_indicator.setVisibility(View.GONE);
    }

    private void showLoadingView() {
        gridRecyclerView.setVisibility(View.GONE);
        loading_indicator.setVisibility(View.VISIBLE);
    }


    // Source for saving API KEY in Native code
    // https://medium.com/@abhi007tyagi/storing-api-keys-using-android-ndk-6abb0adcadad
    // getNativeAPIKeyV3()

    private static class MovieLoader extends AsyncTaskLoader<ArrayList<MovieObject>> {
        Context context;
        String movieSectionPath; // requested movie section, popular or top rated

        MovieLoader(@NonNull Context context, String movieSectionPath) {
            super(context);
            this.context = context;
            this.movieSectionPath = movieSectionPath;
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
            if (!NetworkUtils.hasActiveNetworkConnection(context)) {
                return null;
            }

            // background call to API
            ArrayList<MovieObject> movieObjects;

            Uri uri = Uri.parse(themoviedb_BASE_API_URL_V3).buildUpon()
                    .appendPath(themoviedb_MOVIES_PATH)
                    .appendPath(movieSectionPath)
                    .appendQueryParameter(API_KEY_PARAM, getNativeAPIKeyV3())
                    .appendQueryParameter(LANGUAGE_PARAM, "en-US")
                    .appendQueryParameter(PAGE_PARAM, "1")
                    .build();
            Log.i("Sergio>", this + " loadInBackground\nmovie query uri= " + uri);

            // "https://api.themoviedb.org/3/movie/popular?api_key=<<api_key>>&language=en-US&page=1"


            String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(uri);
            if (jsonDataFromAPI == null) return null;


            movieObjects = JSONParser.parseDataFromJSON(jsonDataFromAPI, BASE_IMAGE_URL, imageSize);

            Log.i("Sergio>", this + " loadInBackground\nmovieObjects= " + movieObjects);

            return movieObjects;
        }


    }
}
