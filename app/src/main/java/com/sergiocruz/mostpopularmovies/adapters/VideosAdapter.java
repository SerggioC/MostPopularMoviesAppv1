package com.sergiocruz.mostpopularmovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sergiocruz.mostpopularmovies.R;
import com.sergiocruz.mostpopularmovies.model.VideoObject;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.sergiocruz.mostpopularmovies.activities.DetailsActivity.YOUTUBE_THUMBNAIL_URL;


/**
 * Created by Sergio on 06/03/2018.
 *
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosViewHolder> {
    final private VideoClickListener mVideoClickListener;
    private Context mContext;
    private ArrayList<VideoObject> videoObjects;

    public VideosAdapter(Context context, VideoClickListener mVideoClickListener) {
        this.mContext = context;
        this.mVideoClickListener = mVideoClickListener;
    }

    @NonNull
    @Override
    public VideosAdapter.VideosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item_layout, parent, false);
        return new VideosAdapter.VideosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosAdapter.VideosViewHolder holder, int position) {

        holder.videoTextView.setText(videoObjects.get(position).getName());
        Glide.with(mContext)
                .load(String.format(YOUTUBE_THUMBNAIL_URL, videoObjects.get(position).getKey()))
                .transition(withCrossFade())
                .apply(new RequestOptions().error(R.drawable.noimage))
                .into(holder.youtubeThumbnailIV);

        Glide.with(mContext)
                .load(R.drawable.video_play_icon)
                .into(holder.videoIcon);

    }

    public void swapVideoData(ArrayList<VideoObject> videoData) {
        this.videoObjects = videoData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return videoObjects == null ? 0 : videoObjects.size();
    }

    public interface VideoClickListener {
        void onVideoClicked(VideoObject videoObject);
    }

    public class VideosViewHolder extends RecyclerView.ViewHolder {
        TextView videoTextView;
        ImageView youtubeThumbnailIV;
        ImageView videoIcon;

        private VideosViewHolder(View itemView) {
            super(itemView);
            videoTextView = itemView.findViewById(R.id.videosTextView);
            youtubeThumbnailIV = itemView.findViewById(R.id.youtube_thumbnail);
            videoIcon = itemView.findViewById(R.id.video_icon);

            itemView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                mVideoClickListener.onVideoClicked(videoObjects.get(clickedPosition));
            });
        }
    }
}
