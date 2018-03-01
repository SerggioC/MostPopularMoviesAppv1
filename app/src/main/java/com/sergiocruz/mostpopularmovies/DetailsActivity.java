package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sergiocruz.mostpopularmovies.Utils.AndroidUtils;

import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_MOVIE_EXTRA;

public class DetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private float INITIAL_IMAGE_POSITION_X;
    private float INITIAL_IMAGE_POSITION_Y;
    private float INITIAL_IMAGE_WIDTH;
    private float INITIAL_IMAGE_HEIGHT;
    private float FINAL_IMAGE_POSITION_X;
    private float FINAL_IMAGE_POSITION_Y;
    private float FINAL_IMAGE_WIDTH_PERCENTAGE = 0.5f;
    private float FINAL_IMAGE_HEIGHT_PERCENTAGE= 0.5F;
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private Context mContext;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private TextView titleTV;
    private ImageView posterImageView;
    private TextView dateTV;
    private TextView ratingTV;
    private TextView synopsisTV;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;

    public static void startAlphaAnimation(View view, long duration, int visibility) {
        AlphaAnimation alphaAnimation =
                (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        //setContentView(R.layout.activity_details);
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

        MovieObject data = intent.getParcelableExtra(INTENT_MOVIE_EXTRA);
        if (data == null) {
            closeNoData();
            return;
        }

        if (!NetworkUtils.hasActiveNetworkConnection(mContext)) {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.i("Sergio>", this + " onCreate\nmoviedata= " + data);


        //populateUI(data);

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

    private void initializeImageProperties() {
        INITIAL_IMAGE_POSITION_X = posterImageView.getX();
        INITIAL_IMAGE_POSITION_Y = posterImageView.getY();
        INITIAL_IMAGE_WIDTH = posterImageView.getMeasuredWidth();
        INITIAL_IMAGE_HEIGHT = posterImageView.getMeasuredHeight();
        FINAL_IMAGE_POSITION_X = AndroidUtils.getPxFromDp(200);
        FINAL_IMAGE_POSITION_Y = AndroidUtils.getStatusBarHeight(mContext) + FINAL_IMAGE_POSITION_X;
    }

    private void populateUI(MovieObject movieData) {
        String baseImageUrl = mContext.getString(R.string.base_image_url);
        String[] imageSizes = mContext.getResources().getStringArray(R.array.image_sizes);
        String imageSize = imageSizes[0]; // "w92" thumbnail
        String imgURL = new StringBuilder(baseImageUrl).append(imageSize).append(movieData.getPoster_path()).toString();

        titleTV.setText(movieData.getTitle());
        Glide.with(mContext).load(imgURL).into(posterImageView);
        dateTV.setText(movieData.getRelease_date().split("-")[0]);
        ratingTV.setText(movieData.getVote_average() + "/10");
        synopsisTV.setText(movieData.getOverview());
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

    private void bindViews() {
        coordinatorLayout = findViewById(R.id.main_coordinator);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.main_appbar);
        titleTV = findViewById(R.id.title_textView);
        posterImageView = findViewById(R.id.poster_imageView);
        dateTV = findViewById(R.id.date_textView);
        ratingTV = findViewById(R.id.rating_textView);
        synopsisTV = findViewById(R.id.synopsis_textView);
    }




}
