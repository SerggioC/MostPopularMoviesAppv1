package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sergiocruz.mostpopularmovies.Utils.CoordinatorBehaviors;

import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_MOVIE_EXTRA;

public class DetailsActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        //setContentView(R.layout.activity_details);
        setContentView(R.layout.activity_details_coordinator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Very long title is very very long like in the things that go from here to the moon");
        collapsingToolbar.setCollapsedTitleGravity(Gravity.LEFT);
        collapsingToolbar.setExpandedTitleGravity(Gravity.LEFT);

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

        if (!NetworkUtils.hasActiveNetworkConnection(context)) {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.i("Sergio>", this + " onCreate\nmoviedata= " + data);


        CoordinatorBehaviors behavior = new CoordinatorBehaviors();
        behavior.layoutDependsOn(findViewById(R.id.coordinator), findViewById(R.id.cardviewtest), toolbar);


        //populateUI(data);

    }

    private void populateUI(MovieObject movieData) {
        String baseImageUrl = context.getString(R.string.base_image_url);
        String[] imageSizes = context.getResources().getStringArray(R.array.image_sizes);
        String imageSize = imageSizes[0]; // "w92" thumbnail
        String imgURL = new StringBuilder(baseImageUrl).append(imageSize).append(movieData.getPoster_path()).toString();

        TextView titleTV = findViewById(R.id.title_textView);
        ImageView imagePosterTV = findViewById(R.id.poster_imageView);
        TextView dateTV = findViewById(R.id.date_textView);
        TextView ratingTV = findViewById(R.id.rating_textView);
        TextView synopsisTV = findViewById(R.id.synopsis_textView);

        titleTV.setText(movieData.getTitle());
        Glide.with(context).load(imgURL).into(imagePosterTV);
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

}