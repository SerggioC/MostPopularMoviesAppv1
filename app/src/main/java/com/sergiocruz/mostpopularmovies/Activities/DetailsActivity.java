package com.sergiocruz.mostpopularmovies.Activities;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.sergiocruz.mostpopularmovies.ReviewsObject;
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

    public static final String INTENT_REVIEW_EXTRA = "review_data_extra";
    private static final int VIDEOS_LOADER_ID = 101;
    private static final int REVIEWS_LOADER_ID = 202;
    private static final String LOADER_BUNDLE_MOVIE_ID = "movie_id_bundle";
    private static final String LOADER_BUNDLE_GOT_FAVORITE = "got_favorite_bundle";
    private static final String LOADER_BUNDLE_HAS_INTERNET = "has_internet_bundle";
    static MovieObject mMovieDataFromIntent;
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

        favorite_star.setOnClickListener(v -> {

            AsyncTask<Void, Void, Uri> asyncTask = new AsyncTask<Void, Void, Uri>() {

                @Override
                protected Uri doInBackground(Void... voids) {
                    return getContentResolver().insert(MovieContract.MovieTable.MOVIES_CONTENT_URI, saveToContentValues(mMovieDataFromIntent));
                }

                @Override
                protected void onPostExecute(Uri uri) {
                    super.onPostExecute(uri);
                    Toast.makeText(mContext, "Movie data saved to Favorites", Toast.LENGTH_LONG).show();
                }
            };
            asyncTask.execute();
        });


        favorite_star.setOnLongClickListener(v -> {
            Toast.makeText(mContext, "Remove from favorite", Toast.LENGTH_SHORT).show();
            return true;
        });
    }



    private ContentValues saveToContentValues(MovieObject movieObject) {
        ContentValues values = new ContentValues();
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
        values.put(MovieContract.MovieTable.POSTER_FILE_PATH, posterFileUri != null ? posterFileUri.toString() : "");

        Uri backDropFileUri = AndroidUtils.saveBitmapToDevice(mContext, backdropImageView, relativeBackdropPath.replace("/", ""));
        values.put(MovieContract.MovieTable.BACKDROP_FILE_PATH, backDropFileUri != null ? backDropFileUri.toString() : "");

        return values;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_details_coordinator);
        bindViews();

        AndroidUtils.animateViewsOnPreDraw(mainCoordinator, new View[]{titleTV,
                posterImageView, dateTV, ratingBar,
                votesTextView, ratingTV, favorite_star,
                synopsisTV, genresTextView, backdropImageView});

        LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        videosAdapter = new VideosAdapter(mContext, this);
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
                String posterWidth = AndroidUtils.getOptimalImageWidth(mContext, posterWidthDp);

                int backDropWidthDp = AndroidUtils.getWindowSizeXY(mContext).x; // match window width
                String backDropWidth = AndroidUtils.getOptimalImageWidth(mContext, backDropWidthDp);

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
    @Override
    public Loader onCreateLoader(int loaderID, Bundle args) {
        String movieId = args.getString(LOADER_BUNDLE_MOVIE_ID);
        Boolean gotFavorite = args.getBoolean(LOADER_BUNDLE_GOT_FAVORITE);

        Uri queryURI;
        switch (loaderID) {
            case VIDEOS_LOADER_ID:
                videos_loading_indicator.setVisibility(View.VISIBLE);
                if (gotFavorite) {
                    queryURI = MovieContract.VideosTable.VIDEOS_CONTENT_URI.buildUpon().appendPath(movieId).build();
                } else {
                    queryURI = TheMovieDB.prepareAPIUri(VIDEOS_PATH, movieId);
                }
                return new VideosLoader(mContext, queryURI, gotFavorite);
            case REVIEWS_LOADER_ID:
                if (gotFavorite) {
                    queryURI = MovieContract.ReviewsTable.REVIEWS_CONTENT_URI.buildUpon().appendPath(movieId).build();
                } else {
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
                populateReviews((ArrayList<ReviewsObject>) data);
                break;
        }
    }

    private void populateVideos(ArrayList<VideoObject> videoObjects) {
        videosAdapter.swapVideoData(videoObjects);
        videos_loading_indicator.setVisibility(View.GONE);
    }

    private void populateReviews(ArrayList<ReviewsObject> reviewsObjects) {
        reviewsAdapter.swapReviewData(reviewsObjects);
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
    public void onLoaderReset(Loader loader) {
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
                .append("Hey Check out this cool movie").append("\n")
                .append(mMovieDataFromIntent.getTitle()).append("\n")
                .append(mMovieDataFromIntent.getReleaseDate()).append("\n")
                .append(mMovieDataFromIntent.getOverview()).append("\n")
                .append("Sent from: " + getString(R.string.app_name))
                .toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);

        Uri fileUri = AndroidUtils.saveBitmapToDevice(mContext, posterImageView, "shareimage.jpg");

        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        sendIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));

    }


    @Override
    public void onVideoClicked(VideoObject videoObject) {
        Toast.makeText(mContext, "Clicked video " + videoObject.getName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReviewClicked(ReviewsObject reviewObject, View itemView) {

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
}
