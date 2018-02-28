package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

import static android.widget.GridLayout.VERTICAL;
import static com.sergiocruz.mostpopularmovies.Utils.AndroidUtils.getPxFromDp;
import static com.sergiocruz.mostpopularmovies.Utils.AndroidUtils.getWindowSizeXY;

public class MainActivity extends AppCompatActivity implements MovieAdapter.PosterClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<MovieObject>> {

    public static final String API_KEY_PARAM = "api_key";
    public static final String LANGUAGE_PARAM = "language";
    public static final String PAGE_PARAM = "page";
    public static final String LOADER_BUNDLE = "movie_loader_bundle";
    public static final int LOADER_ID = 1;
    public static final String INTENT_MOVIE_EXTRA = "intent_movie_extra";
    public static String themoviedb_BASE_API_URL_V3;
    public static String themoviedb_MOVIES_PATH;
    public static String themoviedb_POPULAR_MOVIES_PATH;
    public static String themoviedb_TOP_RATED_MOVIES_PATH;
    public static String movieSection;
    public int selectedRadioId;
    PopupWindow popupWindow;
    private MovieAdapter movieAdapter;
    private ProgressBar loading_indicator;
    private RecyclerView gridRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridRecyclerView = findViewById(R.id.recyclergridview);
        gridRecyclerView.setHasFixedSize(true);
        int spanCount = Math.round(getWindowSizeXY(this).x / getResources().getDimension(R.dimen.grid_image_width));
        GridLayoutManager manager = new GridLayoutManager(this, spanCount, VERTICAL, false);
        gridRecyclerView.setLayoutManager(manager);
        movieAdapter = new MovieAdapter(this, this);
        gridRecyclerView.setAdapter(movieAdapter);

        loading_indicator = findViewById(R.id.loading_indicator);
        showLoadingView();

        themoviedb_BASE_API_URL_V3 = getString(R.string.base_api_url_v3); // API V3
        themoviedb_MOVIES_PATH = getString(R.string.movie_apth);
        themoviedb_POPULAR_MOVIES_PATH = getString(R.string.popular_path);
        themoviedb_TOP_RATED_MOVIES_PATH = getString(R.string.top_rated_path);

        // To make the App open in the last selected section
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        movieSection = sharedPrefs.getString(getString(R.string.movie_section_key), themoviedb_POPULAR_MOVIES_PATH);
        selectedRadioId = sharedPrefs.getInt(getString(R.string.radio_selected_key), R.id.radio_popular);

        Bundle bundle = new Bundle(1);
        bundle.putString(LOADER_BUNDLE, movieSection);
        bundle.putString("buldle2", "nada");
        getSupportLoaderManager().initLoader(LOADER_ID, bundle, this).startLoading();

    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
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
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }

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
            case R.id.overflow_menu: {
                View menuItemView = findViewById(R.id.overflow_menu);

                if (popupWindow == null) {
                    onOverFlowMenuClick(menuItemView);
                } else if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    onOverFlowMenuClick(menuItemView);
                }

                break;
            }
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

    private void onOverFlowMenuClick(View menuItemView) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupLayout = layoutInflater.inflate(R.layout.custom_menu_item_layout, null);

        popupWindow = new PopupWindow(
                popupLayout,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(8);
        }

        popupWindow.setAnimationStyle(2);
        popupWindow.showAsDropDown(menuItemView, -getPxFromDp(64), 0);

        RadioGroup radioGroup = popupLayout.findViewById(R.id.radioGroup);
        setRadioSelection(radioGroup, selectedRadioId, false);

        RadioButton radioPopular = popupLayout.findViewById(R.id.radio_popular);
        View.OnClickListener radioPopularListener = v -> {
            loadMostPopular();
            setRadioSelection(radioGroup, R.id.radio_popular, true);
        };
        radioPopular.setOnClickListener(radioPopularListener);
        popupLayout.findViewById(R.id.menu_textView_popular).setOnClickListener(radioPopularListener);

        RadioButton radioButtonTopRated = popupLayout.findViewById(R.id.radio_top_rated);
        View.OnClickListener radio_top_rated = v -> {
            loadHighestRated();
            setRadioSelection(radioGroup, R.id.radio_top_rated, true);
        };
        radioButtonTopRated.setOnClickListener(radio_top_rated);
        popupLayout.findViewById(R.id.menu_textView_top_rated).setOnClickListener(radio_top_rated);

        RadioButton radioButtonFavourite = popupLayout.findViewById(R.id.radio_favourite);
        View.OnClickListener radio_favourite = v -> {
            loadFavouriteMovies();
            setRadioSelection(radioGroup, R.id.radio_favourite, true);
        };
        radioButtonFavourite.setOnClickListener(radio_favourite);
        popupLayout.findViewById(R.id.menu_textView_favourite).setOnClickListener(radio_favourite);

    }

    private void setRadioSelection(RadioGroup radioGroup, int radioId, boolean dismiss) {
        radioGroup.clearCheck();
        radioGroup.check(radioId);
        selectedRadioId = radioId;
        if (dismiss) popupWindow.dismiss();
    }

    private void loadFavouriteMovies() {
        selectedRadioId = R.id.radio_favourite;
    }


    private void loadMostPopular() {
        restartLoader(themoviedb_POPULAR_MOVIES_PATH);
        saveMovieSectionPreference(themoviedb_POPULAR_MOVIES_PATH, R.id.radio_popular);
    }

    private void loadHighestRated() {
        restartLoader(themoviedb_TOP_RATED_MOVIES_PATH);
        saveMovieSectionPreference(themoviedb_TOP_RATED_MOVIES_PATH, R.id.radio_top_rated);
    }

    private void saveMovieSectionPreference(String section, int radioId) {
        selectedRadioId = radioId;
        movieSection = section;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString(this.getString(R.string.movie_section_key), section).apply();
        sharedPrefs.edit().putInt(this.getString(R.string.radio_selected_key), radioId).apply();
    }

    private void restartLoader(String section) {
        showLoadingView();
        Bundle bundle = new Bundle(1);
        bundle.putString(LOADER_BUNDLE, section);
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
        gridRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onPosterClicked(MovieObject movie) {
        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        detailsIntent.putExtra(INTENT_MOVIE_EXTRA, movie);
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
        // forceLoad overridden in onStartLoading
        //movieloader.forceLoad();
        return movieloader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MovieObject>> loader, ArrayList<MovieObject> data) {
        movieAdapter.swapMovieData(data);
        showDataView();
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


    // Source for saving API KEY in Native code
    // https://medium.com/@abhi007tyagi/storing-api-keys-using-android-ndk-6abb0adcadad
    // getNativeAPIKeyV3()

    private void showLoadingView() {
        gridRecyclerView.setVisibility(View.GONE);
        loading_indicator.setVisibility(View.VISIBLE);
    }

    // R.drawable.menu_indicator
    // https://stackoverflow.com/questions/26775840/custom-actionbar-overflow-menu
    public void onOverFlowMenuClickListPopupWindow(View anchor) {
        // This a sample data to fill our ListView
        ArrayList<MenuOptions> menuOptions = new ArrayList<>(3);
        menuOptions.add(new MenuOptions(true, R.drawable.menu_indicator, getString(R.string.action_most_popular)));
        menuOptions.add(new MenuOptions(false, R.drawable.menu_indicator, getString(R.string.action_highest_rated)));
        menuOptions.add(new MenuOptions(false, R.drawable.ic_favourite, getString(R.string.action_saved_movies)));

        ListPopupWindowAdapter mListPopUpAdapter = new ListPopupWindowAdapter(this, menuOptions);

        //Initialise our ListPopupWindow instance
        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        // Configure ListPopupWindow properties
        listPopupWindow.setAdapter(mListPopUpAdapter);
        // Set the view below/above which ListPopupWindow dropdowns
        listPopupWindow.setAnchorView(anchor);
        // Setting this enables window to be dismissed by click outside ListPopupWindow
        listPopupWindow.setModal(true);
        // Sets the width of the ListPopupWindow
        listPopupWindow.setContentWidth(getPxFromDp(300));

        // Sets the Height of the ListPopupWindow
        listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);

        listPopupWindow.setVerticalOffset(0);
        listPopupWindow.setHorizontalOffset(getPxFromDp(50));

        // Set up a click listener for the ListView items
        listPopupWindow.setOnItemClickListener((adapterView, view, position, l) -> {
            // Dismiss the LisPopupWindow when a list item is clicked
            listPopupWindow.dismiss();
            Toast.makeText(MainActivity.this, "Clicked ListPopUp item " +
                    ((MenuOptions) adapterView.getItemAtPosition(position)).getMenuText(), Toast.LENGTH_LONG).show();
        });
        listPopupWindow.show();
    }


    private static class MovieLoader extends AsyncTaskLoader<ArrayList<MovieObject>> {
        String movieSectionPath; // requested movie section, popular or top rated
        private ArrayList<MovieObject> movieObjectArrayList = null;

        MovieLoader(@NonNull Context context, String movieSectionPath) {
            super(context);
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
         * <p>
         * <Sample urls>
         * https://api.themoviedb.org/3/movie/popular?api_key=<<api_key>>&language=en-US&page=1
         * https://api.themoviedb.org/3/movie/top_rated?api_key=<<api_key>>&language=en-US&page=1
         */
        @Nullable
        @Override
        public ArrayList<MovieObject> loadInBackground() {
            ArrayList<MovieObject> movieObjects;

            Uri uri = Uri.parse(themoviedb_BASE_API_URL_V3).buildUpon()
                    .appendPath(themoviedb_MOVIES_PATH)
                    .appendPath(movieSectionPath)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY_V3)
                    .appendQueryParameter(LANGUAGE_PARAM, "en-US")
                    .appendQueryParameter(PAGE_PARAM, "1")
                    .build();

            Log.i("Sergio>", this + " loadInBackground\nmovie query uri= " + uri);

            String jsonDataFromAPI = NetworkUtils.getJSONDataFromAPI(uri);
            if (jsonDataFromAPI == null) return null;

            movieObjects = JSONParser.parseDataFromJSON(jsonDataFromAPI);

            Log.i("Sergio>", this + " loadInBackground\nmovieObjects= " + movieObjects);

            return movieObjects;
        }

        /**
         * Subclasses must implement this to take care of loading their data,
         * as per {@link #startLoading()}.  This is not called by clients directly,
         * but as a result of a call to {@link #startLoading()}.
         */
        @Override
        protected void onStartLoading() {
            if (!NetworkUtils.hasActiveNetworkConnection(this.getContext())) {
                Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                return;
            }
            if (movieObjectArrayList != null) {
                // Delivers any previously loaded data immediately
                deliverResult(movieObjectArrayList);
            } else {
                // Force a new load
                forceLoad();
            }
        }

        /**
         * Sends the result of the load to the registered listener. Should only be called by subclasses.
         * <p>
         * Must be called from the process's main thread.
         *
         * @param data the result of the load
         */
        @Override
        public void deliverResult(@Nullable ArrayList<MovieObject> data) {
            movieObjectArrayList = data;
            super.deliverResult(data);
        }
    }

    public class MenuOptions {
        private Boolean hasIndicator;
        private int menuIcon;
        private String menuText;

        public MenuOptions(Boolean hasIndicator, int menuIcon, String menuText) {
            this.hasIndicator = hasIndicator;
            this.menuIcon = menuIcon;
            this.menuText = menuText;
        }

        public Boolean getHasIndicator() {
            return hasIndicator;
        }

        public int getMenuIcon() {
            return menuIcon;
        }

        public String getMenuText() {
            return menuText;
        }

    }


}
