package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import com.sergiocruz.mostpopularmovies.Adapters.VideosAdapter;
import com.sergiocruz.mostpopularmovies.Loaders.ReviewsLoader;
import com.sergiocruz.mostpopularmovies.Loaders.VideosLoader;
import com.sergiocruz.mostpopularmovies.MovieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.Utils.AndroidUtils;

import java.util.ArrayList;

import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_EXTRA_IS_FAVORITE;
import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_MOVIE_EXTRA;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.BASE_IMAGE_URL;

public class DetailsActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks,
        AppBarLayout.OnOffsetChangedListener, VideosAdapter.VideoClickListener {

    private static final int VIDEOS_LOADER_ID = 101;
    private static final int REVIEWS_LOADER_ID = 202;
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
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private Context mContext;
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
    private RecyclerView videosRecyclerView;
    private RecyclerView reviewsRecyclerView;

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
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_details_coordinator);
        bindViews();

        videosRecyclerView.setAdapter(new VideosAdapter(mContext, this));


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
            String posterImageURI = movieData.getPosterFilePath();
            String backDropImageURI = movieData.getBackdropFilePath();
            Glide.with(mContext).load(posterImageURI).into(posterImageView);
            Glide.with(mContext).load(backDropImageURI).into(backdropImageView);

        } else {
            favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_off));
            if (hasInternet) {
                String[] imageSizes = mContext.getResources().getStringArray(R.array.image_sizes);
                String imageSize = imageSizes[0]; // "w92" thumbnail
                String posterImageURI = new StringBuilder(BASE_IMAGE_URL).append(imageSize).append(movieData.getPosterPath()).toString();
                String backDropImageURI = new StringBuilder(BASE_IMAGE_URL).append(imageSize).append(movieData.getBackdropPath()).toString();
                Glide.with(mContext).load(posterImageURI).into(posterImageView);
                Glide.with(mContext).load(backDropImageURI).into(backdropImageView);
            } else {
                Glide.with(mContext).load(R.drawable.noimage).into(posterImageView);
                Glide.with(mContext).load(R.drawable.noimage).into(backdropImageView);
            }
        }

        titleTV.setText(movieData.getTitle());
        dateTV.setText(movieData.getReleaseDate().split("-")[0]);
        ratingTV.setText(movieData.getVoteAverage() + "/10");
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
        String movieId = args.getString(LOADER_BUNDLE_MOVIE_ID);
        Uri queryURI = MovieContract.MovieTable.MOVIES_CONTENT_URI.buildUpon().appendPath(movieId).build();
        Boolean gotFavorite = args.getBoolean(LOADER_BUNDLE_GOT_FAVORITE);

        switch (id) {
            case VIDEOS_LOADER_ID:
                return new VideosLoader(mContext, queryURI, gotFavorite);
            case REVIEWS_LOADER_ID:
                return new ReviewsLoader(mContext, queryURI, gotFavorite);
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

    @Override
    public void onVideoClicked(MovieObject movie, Boolean isFavorite) {

    }
}
