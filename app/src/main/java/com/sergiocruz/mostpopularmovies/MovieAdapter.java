package com.sergiocruz.mostpopularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergio on 17/02/2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    final private PosterClickListener mPosterClickListener;
    Context mContext;
    private ArrayList<MovieObject> mMovieData;
    String BASE_IMAGE_URL;
    String imageSize;

    public MovieAdapter(Context context, PosterClickListener mPosterClickListener, ArrayList<MovieObject> movieData) {
        this.mContext = context;
        this.mPosterClickListener = mPosterClickListener;
        this.mMovieData = movieData;
        this.BASE_IMAGE_URL = context.getString(R.string.base_image_url);
        String[] imageSizes = context.getResources().getStringArray(R.array.image_sizes);
        this.imageSize = imageSizes[2];
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
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        String posterPath = new StringBuilder(BASE_IMAGE_URL)
                .append(imageSize)
                .append(mMovieData.get(position).getPoster_path())
                .toString();

        Glide.with(mContext)
                .load(posterPath)
                .into(holder.posterImageView)
//                .getSize(new SizeReadyCallback() {
//                    /**
//                     * A callback called on the main thread.
//                     *
//                     * @param width  The width in pixels of the target, or {@link Target#SIZE_ORIGINAL} to indicate
//                     *               that we want the resource at its original width.
//                     * @param height The height in pixels of the target, or {@link Target#SIZE_ORIGINAL} to indicate
//                     */
//                    @Override
//                    public void onSizeReady(int width, int height) {
//                        holder.posterImageView.setMinimumWidth(width);
//                        holder.posterImageView.setMinimumHeight(height);
//                    }
//                })
                //.onLoadFailed(ContextCompat.getDrawable(mContext, R.drawable.noimage))
        ;

        Log.i("Sergio>", this + " onBindViewHolder\nposterPath= " + posterPath);
    }

    /**
     * Swaps the cursor used by the MovieAdapter for its movie data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the movie data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param movieData the new List<MovieObject> to use as MovieAdapter data source
     */
    public void swapMovieData(ArrayList<MovieObject> movieData) {
        this.mMovieData = movieData;
        notifyDataSetChanged();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mMovieData.size();
    }

    interface PosterClickListener {
        void onPosterClicked(int position);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.movie_poster);
            posterImageView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                mPosterClickListener.onPosterClicked(clickedPosition);
            });
        }
    }

}
