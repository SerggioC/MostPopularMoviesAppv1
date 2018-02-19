package com.sergiocruz.mostpopularmovies;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.sergiocruz.mostpopularmovies.MainActivity.INTENT_MOVIE_EXTRA;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                actionBar.setElevation(4);
//            }
        }

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

        Log.i("Sergio>", this + " onCreate\nmoviedata= " + data);

        populateUI(data);

    }

    private void populateUI(MovieObject movieData) {
        Context context = getApplicationContext();
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
        Toast.makeText(this, "Error, no movie info.", Toast.LENGTH_SHORT).show();
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