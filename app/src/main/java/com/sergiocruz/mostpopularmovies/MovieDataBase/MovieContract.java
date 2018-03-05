package com.sergiocruz.mostpopularmovies.MovieDataBase;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sergiocruz.mostpopularmovies.BuildConfig;

/**
 * Created by Sergio on 23/02/2018.
 */

public class MovieContract {
    public static final String AUTHORITY = "com.sergiocruz.mostpopularmovies";

    // The base content URI = "content://" + <authority> , ContentResolver.SCHEME_CONTENT => "content"
    // "content://com.sergiocruz.mostpopularmovies/"
    public static final Uri BASE_CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.APPLICATION_ID)
            .build();
    
    
    // Define the possible paths for accessing data in this contract
    public static final String PATH_MOVIES = "movies";
    public final static class MovieTable implements BaseColumns {
        public static final String MOVIES_TABLE_NAME = "movies_table";
        
        // Movies content URI = base content URI + path
        // "content://com.sergiocruz.mostpopularmovies/movies"
        public static final Uri MOVIES_CONTENT_URI = 
                BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        // Since MovieTable implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to columns below
        public static final String VOTE_COUNT = "vote_count";
        public static final String MOVIE_ID = "movie_id";
        public static final String HAS_VIDEO = "has_video";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String TITLE = "title";
        public static final String POPULARITY = "popularity";
        public static final String POSTER_PATH = "poster_path";
        public static final String ORIGINAL_LANGUAGE = "original_language";
        public static final String ORIGINAL_TITLE = "original_title";
        public static final String GENRE_ID = "genre_id";
        public static final String BACKDROP_PATH = "backdrop_path";
        public static final String IS_ADULT = "is_adult";
        public static final String OVERVIEW = "overview";
        public static final String RELEASE_DATE = "release_date";
        public static final String IS_FAVORITE = "is_favorite";
        public static final String POSTER_FILE_PATH = "poster_file_path";
        public static final String BACKDROP_FILE_PATH = "backdrop_file_path";

    }

    // Define the possible paths for accessing data in this contract
    public static final String PATH_VIDEOS = "videos";
    public final static class VideosTable implements BaseColumns {
        public static final String VIDEOS_TABLE_NAME = "videos_table";

        // Movies content URI = base content URI + path
        // "content://com.sergiocruz.mostpopularmovies/videos"
        public static final Uri VIDEOS_CONTENT_URI =
                BASE_CONTENT_URI
                        .buildUpon()
                        .appendPath(PATH_VIDEOS)
                        .build();

        // Since MovieTable implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String MOVIE_ID = "movie_id";
        public static final String VIDEO_URL = "video_url";


    }

    // Define the possible paths for accessing data in this contract
    public static final String PATH_REVIEWS = "reviews";
    public final static class ReviewsTable implements BaseColumns {
        public static final String REVIEWS_TABLE_NAME = "reviews_table";

        // Movies content URI = base content URI + path
        // "content://com.sergiocruz.mostpopularmovies/reviews"
        public static final Uri REVIEWS_CONTENT_URI =
                BASE_CONTENT_URI
                        .buildUpon()
                        .appendPath(PATH_REVIEWS)
                        .build();

        // Since MovieTable implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String MOVIE_ID = "movie_id";
        public static final String MOVIE_REVIEW = "movie_review";


    }



}
