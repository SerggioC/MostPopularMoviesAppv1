package com.sergiocruz.mostpopularmovies.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.ReviewsObject;
import com.sergiocruz.mostpopularmovies.Utils.AndroidUtils;

import static com.sergiocruz.mostpopularmovies.Activities.DetailsActivity.INTENT_REVIEW_EXTRA;

/**
 * Created by Sergio on 10/03/2018.
 */

public class ReviewDetailsActivity extends AppCompatActivity {

    private LinearLayout main_linear_layout;
    private TextView reviewAuthorTextView;
    private TextView reviewDetailTextView;
    private TextView reviewURLTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_details_activity);
        bindViews();

        AndroidUtils.animateViewsOnPreDraw(main_linear_layout, new View[]{
                reviewAuthorTextView, reviewDetailTextView, reviewURLTextView,});

        // Intent that started this activity
        Intent intent = getIntent();
        if (intent == null) {
            closeNoData();
            return;
        }

        ReviewsObject reviewDataFromIntent = intent.getParcelableExtra(INTENT_REVIEW_EXTRA);
        if (reviewDataFromIntent == null) {
            closeNoData();
            return;
        }

        populateView(reviewDataFromIntent);

    }

    private void populateView(ReviewsObject reviewData) {
        reviewAuthorTextView.append(" " + reviewData.getAuthor());
        reviewDetailTextView.setText(reviewData.getContent());

        String url = reviewData.getUrl();
        reviewURLTextView.setText(Html.fromHtml(url));
        reviewURLTextView.setLinksClickable(true);
        reviewURLTextView.setAutoLinkMask(Linkify.ALL);

    }


    private void closeNoData() {
        finish();
        Toast.makeText(this, R.string.no_review_data, Toast.LENGTH_SHORT).show();
    }

    private void bindViews() {
        main_linear_layout = findViewById(R.id.main_linear_layout);
        reviewAuthorTextView = findViewById(R.id.reviewAuthorTextView);
        reviewDetailTextView = findViewById(R.id.reviewDetailTextView);
        reviewURLTextView = findViewById(R.id.reviewURLTextView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onBackPressed();
        return true;
    }

}
