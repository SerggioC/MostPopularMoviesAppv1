package com.sergiocruz.mostpopularmovies.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.model.MovieObject;
import com.sergiocruz.mostpopularmovies.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.sergiocruz.mostpopularmovies.TheMovieDB.BASE_IMAGE_URL;

/**
 * Created by Sergio on 17/02/2018.
 * Adapter for the gridlayout in the main activity
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    final private PosterClickListener mPosterClickListener;
    private Context mContext;
    private ArrayList<MovieObject> mMovieData;
    private String imageSize;
    private Boolean isFavorite;
    private int previousPosition = -1;

    public MovieAdapter(Context context, PosterClickListener mPosterClickListener) {
        this.mContext = context;
        this.mPosterClickListener = mPosterClickListener;
        this.imageSize = AndroidUtils.getOptimalImageWidth(context, (int) context.getResources().getDimension(R.dimen.grid_image_width) / 2);
    }

    /**
     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item_layout, parent, false);
        return new MovieViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        setItemViewAnimation(holder.itemView, position);

        String posterPath;
        if (isFavorite) {
            posterPath = mMovieData.get(position).getPosterFilePath();
        } else {
            posterPath = new StringBuilder(BASE_IMAGE_URL)
                    .append(imageSize)
                    .append(mMovieData.get(position).getPosterPath())
                    .toString();
        }

        Glide.with(mContext)
                .load(posterPath)
                .transition(withCrossFade())
                .apply(new RequestOptions().error(R.drawable.noimage))
                .into(holder.posterImageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != -1) {
                ViewCompat.setTransitionName(holder.itemView, "transition" + mMovieData.get(adapterPosition).getId());
            }
        }

    }

    private void setItemViewAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position >= previousPosition) {
            Animation bottomAnimation = AnimationUtils.loadAnimation(mContext, R.anim.item_animation_slide_from_bottom);
            viewToAnimate.startAnimation(bottomAnimation);
        } else if (position < previousPosition) {
            Animation topAnimation = AnimationUtils.loadAnimation(mContext, R.anim.item_animation_slide_from_top);
            viewToAnimate.startAnimation(topAnimation);
        }
        previousPosition = position;
    }

    /**
     * Swaps the cursor used by the MovieAdapter for its movie data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the movie data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param movieData the new List<MovieObject> to use as MovieAdapter data source
     */
    public void swapMovieData(ArrayList<MovieObject> movieData, Boolean isFavorite) {
        this.mMovieData = movieData;
        this.isFavorite = isFavorite;
        notifyDataSetChanged();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mMovieData == null ? 0 : mMovieData.size();
    }

    public interface PosterClickListener {
        void onPosterClicked(MovieObject movie, Integer position, Boolean isFavorite, View itemView);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.movie_poster);
            posterImageView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                mPosterClickListener.onPosterClicked(mMovieData.get(clickedPosition), clickedPosition, isFavorite, itemView);
            });

        }
    }

}
