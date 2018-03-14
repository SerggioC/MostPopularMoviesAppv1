package com.sergiocruz.mostpopularmovies.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.sergiocruz.mostpopularmovies.Adapters.ReviewsAdapter;
import com.sergiocruz.mostpopularmovies.Adapters.VideosAdapter;
import com.sergiocruz.mostpopularmovies.Loaders.ReviewsLoader;
import com.sergiocruz.mostpopularmovies.Loaders.VideosLoader;
import com.sergiocruz.mostpopularmovies.MovieDataBase.MovieContract;
import com.sergiocruz.mostpopularmovies.MovieObject;
import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.ReviewObject;
import com.sergiocruz.mostpopularmovies.TheMovieDB;
import com.sergiocruz.mostpopularmovies.Utils.AndroidUtils;
import com.sergiocruz.mostpopularmovies.Utils.NetworkUtils;
import com.sergiocruz.mostpopularmovies.VideoObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.sergiocruz.mostpopularmovies.Activities.MainActivity.INTENT_EXTRA_IS_FAVORITE;
import static com.sergiocruz.mostpopularmovies.Activities.MainActivity.INTENT_MOVIE_EXTRA;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.BASE_IMAGE_URL;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.REVIEWS_PATH;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.VIDEOS_PATH;

public class DetailsActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks,
        VideosAdapter.VideoClickListener, ReviewsAdapter.ReviewClickListener {

    private static final int VIDEOS_LOADER_ID = 101;
    private static final int REVIEWS_LOADER_ID = 202;
    public static final String INTENT_REVIEW_EXTRA = "review_data_extra";
    public static final String FAVORITES_ACTIVITY_RESULT = "activity_result";
    private static final String LOADER_BUNDLE_MOVIE_ID = "movie_id_bundle";
    private static final String LOADER_BUNDLE_GOT_FAVORITE = "got_favorite_bundle";
    private static final String LOADER_BUNDLE_HAS_INTERNET = "has_internet_bundle";
    private static final String SAVED_INSTANCE_STATE_KEY = "saved_instance_bundle";
    public static final String YOUTUBE_URL_PREFIX = "http://www.youtube.com/watch?v=";
    public static final String YOUTUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg"; // /1.jpg /2.jpg /3.jpg /default.jpg

    private MovieObject mMovieDataFromIntent;
    private ArrayList<VideoObject> mVideosObjects;
    private ArrayList<ReviewObject> mReviewObjects;
    private Context mContext;
    private TextView titleTV;
    private ImageView posterImageView;
    private TextView dateTV;
    private TextView ratingTV;
    private TextView synopsisTV;
    private Toolbar toolbar;
    private FloatingActionButton favorite_star;
    private ImageView backdropImageView;
    private RecyclerView videosRecyclerView;
    private RecyclerView reviewsRecyclerView;
    private VideosAdapter videosAdapter;
    private ReviewsAdapter reviewsAdapter;
    private ProgressBar videos_loading_indicator;
    private ProgressBar reviews_loading_indicator;
    private CoordinatorLayout mainCoordinator;
    private TextView genresTextView;
    private AppCompatRatingBar ratingBar;
    private TextView votesTextView;
    private Integer outStateScrollPosition = null;

    private void bindViews() {
        mainCoordinator = findViewById(R.id.main_coordinator);
        toolbar = findViewById(R.id.toolbar);
        titleTV = findViewById(R.id.title_textView);
        posterImageView = findViewById(R.id.poster_imageView);
        dateTV = findViewById(R.id.date_textView);
        ratingTV = findViewById(R.id.rating_textView);
        synopsisTV = findViewById(R.id.synopsis_textView);
        favorite_star = findViewById(R.id.fab_star);
        backdropImageView = findViewById(R.id.backdrop_imageview);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        videos_loading_indicator = findViewById(R.id.videos_loading_indicator);
        reviews_loading_indicator = findViewById(R.id.reviews_loading_indicator);
        genresTextView = findViewById(R.id.genres_textview);
        ratingBar = findViewById(R.id.ratingBar);
        votesTextView = findViewById(R.id.votes_textview);

        favorite_star.setOnClickListener(view -> {

            //  Check if DB already has the movie_id favorite
            Uri queryUri = MovieContract.MovieTable.MOVIES_CONTENT_URI.buildUpon().appendPath(mMovieDataFromIntent.getId().toString()).build();
            Cursor queryCursor = getContentResolver().query(queryUri, null, null, null, null);
            if (queryCursor != null) queryCursor.close();

            if (queryCursor != null && (mMovieDataFromIntent.getFavorite() || queryCursor.getCount() > 0)) {
                Toast.makeText(mContext, R.string.movie_in_favorites, Toast.LENGTH_LONG).show();
                return;
            }

            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... nothing) {

                    getContentResolver().insert(MovieContract.MovieTable.MOVIES_CONTENT_URI, saveToContentValues(mMovieDataFromIntent));

                    int videos_inserted = 0;
                    ContentValues[] saveVideosToContentValues = saveVideosToContentValues(mVideosObjects, mMovieDataFromIntent.getId());
                    if (saveVideosToContentValues != null)
                        videos_inserted = getContentResolver().bulkInsert(MovieContract.VideosTable.VIDEOS_CONTENT_URI, saveVideosToContentValues);

                    int reviews_inserted = 0;
                    ContentValues[] saveReviewsToContentValues = saveReviewsToContentValues(mReviewObjects, mMovieDataFromIntent.getId());
                    if (saveReviewsToContentValues != null)
                        reviews_inserted = getContentResolver().bulkInsert(MovieContract.ReviewsTable.REVIEWS_CONTENT_URI, saveReviewsToContentValues);

                    setActivityResult();

                    Log.i("Sergio>", this + " doInBackground\nvideos_inserted= %s, reviews_inserted= %s" + videos_inserted + " , " + reviews_inserted);
                    return null;
                }

                @Override
                protected void onPostExecute(Void nothing) {
                    super.onPostExecute(nothing);
                    favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_on));
                    mMovieDataFromIntent.setFavorite(true);
                    Toast.makeText(mContext, R.string.saved_movie, Toast.LENGTH_LONG).show();
                }
            };
            asyncTask.execute();
        });

        // Delete favorite on Long click
        favorite_star.setOnLongClickListener(v -> {

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    int deletedRow = getContentResolver().delete(
                            MovieContract.MovieTable.MOVIES_CONTENT_URI.buildUpon()
                                    .appendPath(mMovieDataFromIntent.getId().toString())
                                    .build()
                            , null, null);
                    Boolean deletedPoster = AndroidUtils.deleteImageFile(mMovieDataFromIntent.getPosterFilePath());
                    Boolean deletedBackDrop = AndroidUtils.deleteImageFile(mMovieDataFromIntent.getBackdropFilePath());
                    mMovieDataFromIntent.setPosterFilePath(null);
                    mMovieDataFromIntent.setBackdropFilePath(null);

                    if (deletedRow == 1) setActivityResult();

                    Log.i("Sergio>", this + " doInBackground\n" +
                            "deletedRow row = " + deletedRow + "\n" +
                            "DeletedPoster = " + deletedPoster + "\n" +
                            "deletedBackdrop = " + deletedBackDrop);

                    return deletedRow == 1 && deletedPoster && deletedBackDrop;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);
                    favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_off));
                    mMovieDataFromIntent.setFavorite(false);
                    Toast.makeText(mContext, "Removed from favorites" + (success ? " successfully." : " with errors...\nCheck log."), Toast.LENGTH_SHORT).show();
                }
            }.execute();

            return true;
        });
    }

    private void setActivityResult() {
        Intent intent = new Intent();
        intent.putExtra(FAVORITES_ACTIVITY_RESULT, true);
        setResult(Activity.RESULT_OK, intent);
    }

    private ContentValues[] saveReviewsToContentValues(ArrayList<ReviewObject> mReviewObjects, Integer id) {
        if (mReviewObjects == null) return null;
        int size = mReviewObjects.size();
        ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            ReviewObject review = mReviewObjects.get(i);
            values[i] = new ContentValues(5);
            values[i].put(MovieContract.ReviewsTable.MOVIE_ID, id.toString());
            values[i].put(MovieContract.ReviewsTable.REVIEW_ID, review.getReviewId());
            values[i].put(MovieContract.ReviewsTable.AUTHOR, review.getAuthor());
            values[i].put(MovieContract.ReviewsTable.CONTENT, review.getContent());
            values[i].put(MovieContract.ReviewsTable.URL, review.getUrl());
        }
        return values;
    }

    private ContentValues[] saveVideosToContentValues(ArrayList<VideoObject> mVideosObjects, Integer id) {
        if (mVideosObjects == null) return null;
        int size = mVideosObjects.size();
        ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            VideoObject video = mVideosObjects.get(i);
            values[i] = new ContentValues(10);
            values[i].put(MovieContract.VideosTable.MOVIE_ID, id.toString());
            values[i].put(MovieContract.VideosTable.VIDEO_ID, video.getVideo_id());
            values[i].put(MovieContract.VideosTable.ISO_639_1, video.getIso_639_1());
            values[i].put(MovieContract.VideosTable.ISO_3166_1, video.getIso_3166_1());
            values[i].put(MovieContract.VideosTable.KEY, video.getKey());
            values[i].put(MovieContract.VideosTable.NAME, video.getName());
            values[i].put(MovieContract.VideosTable.SITE, video.getSite());
            values[i].put(MovieContract.VideosTable.SIZE, video.getSize());
            values[i].put(MovieContract.VideosTable.TYPE, video.getType());

            Bitmap bitmap = AndroidUtils.getBitmapFromURL(mContext, String.format(YOUTUBE_THUMBNAIL_URL, video.getKey()));
            Uri imageFileUri = AndroidUtils.saveBitmapToDevice(mContext, bitmap, video.getKey() + ".jpeg");
            String imageStringUri = imageFileUri != null ? imageFileUri.toString() : "";
            values[i].put(MovieContract.VideosTable.THUMBNAIL_FILE_URI, imageStringUri);
        }
        return values;
    }

    private ContentValues saveToContentValues(MovieObject movieObject) {
        ContentValues values = new ContentValues(17);
        values.put(MovieContract.MovieTable.VOTE_COUNT, movieObject.getVoteCount());
        values.put(MovieContract.MovieTable.MOVIE_ID, movieObject.getId());
        values.put(MovieContract.MovieTable.HAS_VIDEO, movieObject.getVideo());
        values.put(MovieContract.MovieTable.VOTE_AVERAGE, movieObject.getVoteAverage());
        values.put(MovieContract.MovieTable.TITLE, movieObject.getTitle());
        values.put(MovieContract.MovieTable.POPULARITY, movieObject.getPopularity());
        String relativePosterPath = movieObject.getPosterPath();
        values.put(MovieContract.MovieTable.POSTER_PATH, relativePosterPath);
        values.put(MovieContract.MovieTable.ORIGINAL_LANGUAGE, movieObject.getOriginalLanguage());
        values.put(MovieContract.MovieTable.ORIGINAL_TITLE, movieObject.getOriginalTitle());
        String jsonGenreIDs = new JSONArray(movieObject.getGenreIDs()).toString();
        values.put(MovieContract.MovieTable.GENRE_ID, jsonGenreIDs);
        String relativeBackdropPath = movieObject.getBackdropPath();
        values.put(MovieContract.MovieTable.BACKDROP_PATH, relativeBackdropPath);
        values.put(MovieContract.MovieTable.IS_ADULT, movieObject.getAdult());
        values.put(MovieContract.MovieTable.OVERVIEW, movieObject.getOverview());
        values.put(MovieContract.MovieTable.RELEASE_DATE, movieObject.getReleaseDate());
        values.put(MovieContract.MovieTable.IS_FAVORITE, 1);

        Uri posterFileUri = AndroidUtils.saveBitmapToDevice(mContext, posterImageView, relativePosterPath.replace("/", ""));
        String posterStringUri = posterFileUri != null ? posterFileUri.toString() : "";
        values.put(MovieContract.MovieTable.POSTER_FILE_PATH, posterStringUri);
        mMovieDataFromIntent.setPosterFilePath(posterStringUri);

        Uri backDropFileUri = AndroidUtils.saveBitmapToDevice(mContext, backdropImageView, relativeBackdropPath.replace("/", ""));
        String backdropStringUri = backDropFileUri != null ? backDropFileUri.toString() : "";
        values.put(MovieContract.MovieTable.BACKDROP_FILE_PATH, backdropStringUri);
        mMovieDataFromIntent.setBackdropFilePath(backdropStringUri);

        return values;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_details);
        bindViews();

        AndroidUtils.animateViewsOnPreDraw(mainCoordinator, new View[]{titleTV,
                posterImageView, dateTV, ratingBar,
                votesTextView, ratingTV, favorite_star,
                synopsisTV, genresTextView, backdropImageView});

        LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        videosAdapter = new VideosAdapter(mContext, this);
        videosAdapter.setHasStableIds(true);
        videosRecyclerView.setAdapter(videosAdapter);
        videosRecyclerView.setLayoutManager(videosLinearLayoutManager);

        LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        reviewsAdapter = new ReviewsAdapter(this, this);
        reviewsRecyclerView.setAdapter(reviewsAdapter);
        reviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);

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
        if (!gotFavorite)
            mMovieDataFromIntent.setFavorite(false);

        Boolean hasInternet = NetworkUtils.hasActiveNetworkConnection(mContext);
        if (!hasInternet) {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        initializeUIPopulating(mMovieDataFromIntent, gotFavorite, hasInternet);

        String receivedMovieId = mMovieDataFromIntent.getId().toString();

        Bundle bundle = new Bundle(3);
        bundle.putString(LOADER_BUNDLE_MOVIE_ID, receivedMovieId);
        bundle.putBoolean(LOADER_BUNDLE_GOT_FAVORITE, gotFavorite);
        bundle.putBoolean(LOADER_BUNDLE_HAS_INTERNET, hasInternet);

        getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, bundle, this);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, bundle, this);

        if (savedInstanceState != null) {
            outStateScrollPosition = savedInstanceState.getInt(SAVED_INSTANCE_STATE_KEY);
        }
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

        RequestOptions glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.noimage)
                .priority(Priority.HIGH);

//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
//        iv.setLayoutParams(layoutParams);

//        http://image.tmdb.org/t/p/w92/cGOPbv9wA5gEejkUN892JrveARt.jpg 92/138
//        http://image.tmdb.org/t/p/w342/vsjBeMPZtyB7yNsYY56XYxifaQZ.jpg 342/192

        if (gotFavorite) {
            favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_on));
            String posterImageURI = movieData.getPosterFilePath();
            String backDropImageURI = movieData.getBackdropFilePath();
            Glide.with(mContext).load(posterImageURI).apply(glideOptions).transition(withCrossFade()).into(posterImageView);
            Glide.with(mContext).load(backDropImageURI).apply(glideOptions).transition(withCrossFade()).into(backdropImageView);

        } else {
            favorite_star.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.btn_star_big_off));
            if (hasInternet) {

                int posterWidthDp = (int) mContext.getResources().getDimension(R.dimen.details_image_width);
                String posterWidth = AndroidUtils.getOptimalImageWidth(mContext, posterWidthDp / 2);

                int backDropWidthDp = AndroidUtils.getWindowSizeXY(mContext).x; // match window width
                String backDropWidth = AndroidUtils.getOptimalImageWidth(mContext, backDropWidthDp / 2);

                String posterImageURI = new StringBuilder(BASE_IMAGE_URL).append(posterWidth).append(movieData.getPosterPath()).toString();
                String backDropImageURI = new StringBuilder(BASE_IMAGE_URL).append(backDropWidth).append(movieData.getBackdropPath()).toString();
                Glide.with(mContext).load(posterImageURI).apply(glideOptions).transition(withCrossFade()).into(posterImageView);
                Glide.with(mContext).load(backDropImageURI).apply(glideOptions).transition(withCrossFade()).into(backdropImageView);
            } else {
                Glide.with(mContext).load(R.drawable.noimage).transition(withCrossFade()).into(posterImageView);
                Glide.with(mContext).load(R.drawable.noimage).transition(withCrossFade()).into(backdropImageView);
            }
        }

        titleTV.setText(movieData.getTitle());
        dateTV.setText(movieData.getReleaseDate().split("-")[0]);

        ObjectAnimator anim = ObjectAnimator.ofFloat(ratingBar, "rating", 0, movieData.getVoteAverage() / 2);
        anim.setDuration(1500);
        anim.start();

        SpannableStringBuilder ssb = new SpannableStringBuilder(movieData.getVoteAverage().toString());
        ssb.setSpan(new RelativeSizeSpan(2), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append("/10 ");
        ratingTV.setText(ssb);

        votesTextView.setText(movieData.getVoteCount() + " " + mContext.getString(R.string.votes));
        synopsisTV.setText(movieData.getOverview());

        List<Integer> genreList = movieData.getGenreIDs();
        StringBuilder sb = new StringBuilder();
        int size = genreList.size();
        for (int i = 0; i < size; i++) {
            String genreText = TheMovieDB.genres.get(genreList.get(i));
            sb.append(genreText);
            if (i < size - 1)
                sb.append(", ");
        }
        genresTextView.setText(sb);

    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param loaderID The ID whose loader is to be created.
     * @param args     Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader onCreateLoader(int loaderID, Bundle args) {
        String movieId = args.getString(LOADER_BUNDLE_MOVIE_ID);
        Boolean gotFavorite = args.getBoolean(LOADER_BUNDLE_GOT_FAVORITE);
        Boolean hasInternet = args.getBoolean(LOADER_BUNDLE_HAS_INTERNET);

        Uri queryURI = null;
        switch (loaderID) {
            case VIDEOS_LOADER_ID:
                videos_loading_indicator.setVisibility(View.VISIBLE);
                if (gotFavorite) {
                    queryURI = MovieContract.VideosTable.VIDEOS_CONTENT_URI.buildUpon().appendPath(movieId).build();
                } else {
                    if (hasInternet)
                        queryURI = TheMovieDB.prepareAPIUri(VIDEOS_PATH, movieId);
                }
                return new VideosLoader(mContext, queryURI, gotFavorite);
            case REVIEWS_LOADER_ID:
                if (gotFavorite) {
                    queryURI = MovieContract.ReviewsTable.REVIEWS_CONTENT_URI.buildUpon().appendPath(movieId).build();
                } else {
                    if (hasInternet)
                        queryURI = TheMovieDB.prepareAPIUri(REVIEWS_PATH, movieId);
                }
                reviews_loading_indicator.setVisibility(View.VISIBLE);
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
                populateReviews((ArrayList<ReviewObject>) data);
                break;
        }
        if (outStateScrollPosition != null) {
            mainCoordinator.setScrollY(outStateScrollPosition);
            outStateScrollPosition = null;
        }
    }

    private void populateVideos(ArrayList<VideoObject> videoObjects) {
        this.mVideosObjects = videoObjects;
        videosAdapter.swapVideoData(videoObjects);
        videos_loading_indicator.setVisibility(View.GONE);
    }

    private void populateReviews(ArrayList<ReviewObject> reviewObjects) {
        this.mReviewObjects = reviewObjects;
        reviewsAdapter.swapReviewData(reviewObjects);
        reviews_loading_indicator.setVisibility(View.GONE);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        switch (loader.getId()) {
            case VIDEOS_LOADER_ID:
                populateVideos(null);
                break;
            case REVIEWS_LOADER_ID:
                populateReviews(null);
                break;
        }
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
            case R.id.menu_action_share:
                shareMovieData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareMovieData() {

        String message = new StringBuilder()
                .append(getString(R.string.hey_check_this)).append("\n")
                .append(mMovieDataFromIntent.getTitle()).append("\n")
                .append(mMovieDataFromIntent.getReleaseDate()).append("\n")
                .append(mMovieDataFromIntent.getOverview()).append("\n")
                .append(getString(R.string.sent_from) + getString(R.string.app_name)).append(getString(R.string.android_app))
                .toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);

        Uri fileUri = AndroidUtils.saveBitmapToDevice(mContext, posterImageView, "share.jpg");

        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        sendIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));

    }


    @Override
    public void onVideoClicked(VideoObject videoObject) {

        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoObject.getKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URL_PREFIX + videoObject.getKey()));
        try {
            mContext.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            mContext.startActivity(webIntent);
        }
        Toast.makeText(mContext, "Clicked video " + videoObject.getName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReviewClicked(ReviewObject reviewObject, View itemView) {
        Intent detailsIntent = new Intent(DetailsActivity.this, ReviewDetailsActivity.class);
        detailsIntent.putExtra(INTENT_REVIEW_EXTRA, reviewObject);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions activityOptions = ActivityOptions
                    .makeScaleUpAnimation(itemView, 0, 0, itemView.getWidth(), itemView.getHeight());
            startActivity(detailsIntent, activityOptions.toBundle());
        } else {
            startActivity(detailsIntent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int position = mainCoordinator.getScrollY();
        outState.putInt(SAVED_INSTANCE_STATE_KEY, position);
    }

}
