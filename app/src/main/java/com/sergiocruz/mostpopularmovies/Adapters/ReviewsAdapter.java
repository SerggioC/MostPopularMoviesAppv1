package com.sergiocruz.mostpopularmovies.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.ReviewsObject;

import java.util.ArrayList;

/**
 * Created by Sergio on 06/03/2018. 
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {
    final private ReviewClickListener mReviewsClickListener;
    private Context mContext;
    private ArrayList<ReviewsObject> reviewObjects;

    public ReviewsAdapter(Context context, ReviewClickListener mReviewClickListener) {
        this.mContext = context;
        this.mReviewsClickListener = mReviewClickListener;
    }

    @Override
    public ReviewsAdapter.ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_item_layout, parent, false);
        return new ReviewsAdapter.ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapter.ReviewsViewHolder holder, int position) {

        holder.reviewTextView.setText(reviewObjects.get(position).getContent());

    }

    public void swapReviewData(ArrayList<ReviewsObject> reviewData) {
        this.reviewObjects = reviewData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reviewObjects == null ? 0 : reviewObjects.size();
    }

    public interface ReviewClickListener {
        void onReviewClicked(ReviewsObject reviewObject);
    }

    public class ReviewsViewHolder extends RecyclerView.ViewHolder {
        TextView reviewTextView;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            reviewTextView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                mReviewsClickListener.onReviewClicked(reviewObjects.get(clickedPosition));
            });
        }
    }
}
