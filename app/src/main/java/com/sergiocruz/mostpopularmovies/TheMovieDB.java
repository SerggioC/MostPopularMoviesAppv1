package com.sergiocruz.mostpopularmovies;

import android.net.Uri;

/**
 * Created by Sergio on 04/03/2018.
 * TheMovieDB.org API constants
 */

public final class TheMovieDB {
    public static final String BASE_API_URL_V3 = "https://api.themoviedb.org/3/";
    public static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    public static final String API_KEY_PARAM = "api_key";
    public static final String LANGUAGE_PARAM = "language";
    public static final String PAGE_PARAM = "page";
    public static final String MOVIE_PATH = "movie";
    public static final String POPULAR_MOVIES_PATH = "popular";
    public static final String TOP_RATED_MOVIES_PATH = "top_rated";
    public static final String VIDEOS_PATH = "videos";
    public static final String REVIEWS_PATH = "reviews";
    public static final String NOW_PLAYING_PATH = "now_playing";
    public static final String UPCOMING_PATH = "upcoming";
    public static final String LATEST_PATH = "latest";

    // https://api.themoviedb.org/3/movie/{movie_id}/reviews?api_key=<<api_key>>&language=en-US&page=1
    // https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
    // https://api.themoviedb.org/3/movie/popular?api_key=<<api_key>>&language=en-US&page=1
    // https://api.themoviedb.org/3/movie/top_rated?api_key=<<api_key>>&language=en-US&page=1
    // https://api.themoviedb.org/3/movie/now_playing?api_key=<<api_key>>&language=en-US&page=1
    // https://api.themoviedb.org/3/movie/upcoming?api_key=<<api_key>>&language=en-US&page=1
    // https://api.themoviedb.org/3/movie/latest?api_key=<<api_key>>&language=en-US

    public static Uri prepareAPIUri(String section, String movieID) {
        Uri uri;

        if (movieID != null) {
            uri = Uri.parse(BASE_API_URL_V3).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(movieID)
                    .appendPath(section)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY_V3)
                    .appendQueryParameter(LANGUAGE_PARAM, "en-US") // TODO save/get language preference
                    .appendQueryParameter(PAGE_PARAM, "1")
                    .build();
        } else {
            uri = Uri.parse(BASE_API_URL_V3).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(section)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY_V3)
                    .appendQueryParameter(LANGUAGE_PARAM, "en-US") // TODO save/get language preference
                    .appendQueryParameter(PAGE_PARAM, "1")
                    .build();
        }

        return uri;
    }
}
