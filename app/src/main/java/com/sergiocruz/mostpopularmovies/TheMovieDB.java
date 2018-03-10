package com.sergiocruz.mostpopularmovies;

import android.net.Uri;

import java.util.LinkedHashMap;

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
    public static final String VIDEOS_PATH = "videos";
    public static final String REVIEWS_PATH = "reviews";
    public static final String POPULAR_MOVIES_PATH = "popular";
    public static final String TOP_RATED_MOVIES_PATH = "top_rated";
    public static final String UPCOMING_MOVIES_PATH = "upcoming";
    public static final String LATEST_MOVIES_PATH = "latest";
    public static final String NOW_PLAYING_PATH = "now_playing";
    public static LinkedHashMap<Integer, String> genres = populateGenres();

    public static LinkedHashMap<Integer, String> populateGenres() {
        genres = (LinkedHashMap<Integer, String>) new LinkedHashMap(19);
        genres.put(28, "Action");
        genres.put(12, "Adventure");
        genres.put(16, "Animation");
        genres.put(35, "Comedy");
        genres.put(80, "Crime");
        genres.put(99, "Documentary");
        genres.put(18, "Drama");
        genres.put(10751, "Family");
        genres.put(14, "Fantasy");
        genres.put(36, "History");
        genres.put(27, "Horror");
        genres.put(10402, "Music");
        genres.put(9648, "Mystery");
        genres.put(10749, "Romance");
        genres.put(878, "Science Fiction");
        genres.put(10770, "TV Movie");
        genres.put(53, "Thriller");
        genres.put(10752, "War");
        genres.put(37, "Western");
        return genres;
    }

    // example api urls
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
