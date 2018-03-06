package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
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

import com.sergiocruz.mostpopularmovies.Adapters.MovieAdapter;
import com.sergiocruz.mostpopularmovies.Loaders.MoviesLoader;
import com.sergiocruz.mostpopularmovies.MovieDataBase.MovieContract;

import java.util.ArrayList;

import static android.widget.GridLayout.VERTICAL;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.POPULAR_MOVIES_PATH;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.TOP_RATED_MOVIES_PATH;
import static com.sergiocruz.mostpopularmovies.Utils.AndroidUtils.getPxFromDp;
import static com.sergiocruz.mostpopularmovies.Utils.AndroidUtils.getWindowSizeXY;

public class MainActivity extends AppCompatActivity implements MovieAdapter.PosterClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<MovieObject>> {

    public static final String LOADER_BUNDLE = "movie_loader_bundle";
    public static final int LOADER_ID_INTERNET = 11;
    public static final int LOADER_ID_DATABASE = 22;
    public static final String INTENT_MOVIE_EXTRA = "intent_movie_extra";
    public static final String INTENT_EXTRA_IS_FAVORITE = "intent_extra_is_favorite";
    public static final String FAVORITE_MOVIES = "favorite";

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

        // To make the App open in the last selected section
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        movieSection = sharedPrefs.getString(getString(R.string.movie_section_pref_key), POPULAR_MOVIES_PATH);
        selectedRadioId = sharedPrefs.getInt(getString(R.string.radio_selected_key), R.id.radio_popular);

        Bundle bundle = new Bundle(1);
        bundle.putString(LOADER_BUNDLE, movieSection);
        if (movieSection == FAVORITE_MOVIES) {
            getSupportLoaderManager().initLoader(LOADER_ID_DATABASE, bundle, this).startLoading();
        } else {
            getSupportLoaderManager().initLoader(LOADER_ID_INTERNET, bundle, this).startLoading();
        }

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
        restartLoader(FAVORITE_MOVIES);
        saveMovieSectionPreference(FAVORITE_MOVIES, R.id.radio_favourite);
    }

    private void loadMostPopular() {
        restartLoader(POPULAR_MOVIES_PATH);
        saveMovieSectionPreference(POPULAR_MOVIES_PATH, R.id.radio_popular);
    }

    private void loadHighestRated() {
        restartLoader(TOP_RATED_MOVIES_PATH);
        saveMovieSectionPreference(TOP_RATED_MOVIES_PATH, R.id.radio_top_rated);
    }

    private void saveMovieSectionPreference(String section, int radioId) {
        selectedRadioId = radioId;
        movieSection = section;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString(this.getString(R.string.movie_section_pref_key), section).apply();
        sharedPrefs.edit().putInt(this.getString(R.string.radio_selected_key), radioId).apply();
    }

    private void restartLoader(String section) {
        showLoadingView();

        String stringURI;
        int loaderID;
        if (section == FAVORITE_MOVIES) {
            stringURI = MovieContract.MovieTable.MOVIES_CONTENT_URI.toString();
            loaderID = LOADER_ID_DATABASE;
        } else {
            stringURI = TheMovieDB.prepareAPIStringURI(section, null);
            loaderID = LOADER_ID_INTERNET;
        }

        Bundle bundleURI = new Bundle(1);
        bundleURI.putString(LOADER_BUNDLE, stringURI);
        getSupportLoaderManager().restartLoader(loaderID, bundleURI, this);

        gridRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onPosterClicked(MovieObject movie, Boolean isFavorite) {
        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
        detailsIntent.putExtra(INTENT_MOVIE_EXTRA, movie);
        detailsIntent.putExtra(INTENT_EXTRA_IS_FAVORITE, isFavorite);
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
        Uri queryUri = Uri.parse(args.getString(LOADER_BUNDLE));

        MoviesLoader movieloader = new MoviesLoader(getApplicationContext(), queryUri, id == LOADER_ID_DATABASE ? true : false);

        // forceLoad overridden in onStartLoading
        //movieloader.forceLoad();
        return movieloader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MovieObject>> loader, ArrayList<MovieObject> data) {
        Boolean isFavorite = loader.getId() == LOADER_ID_DATABASE ? true : false;
        movieAdapter.swapMovieData(data, isFavorite);
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
        Boolean isFavorite = loader.getId() == LOADER_ID_DATABASE ? true : false;
        movieAdapter.swapMovieData(null, isFavorite);
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
