package com.sergiocruz.mostpopularmovies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergio on 18/02/2018.
 */

class JSONParser {

    // JSON key names
    private static final String RESULTS = "results";
    private static final String VOTE_COUNT = "vote_count";
    private static final String ID = "id";
    private static final String VIDEO = "video";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String TITLE = "title";
    private static final String POPULARITY = "popularity";
    private static final String ORIGINAL_LANGUAGE = "original_language";
    private static final String POSTER_PATH = "poster_path";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String GENRE_IDS = "genre_ids";
    private static final String BACKDROP_PATH = "backdrop_path";
    private static final String ADULT = "adult";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String NOT_AVAILABLE_FALLBACK = "(N/A)";
    private static final String NO_LANGUAGE_FALLBACK = "en-US";

    static ArrayList<MovieObject> parseMovieDataFromJSON(String jsonDataFromAPI) {
        ArrayList<MovieObject> movieObjects = new ArrayList<>();
        try {
            JSONObject jsonData = new JSONObject(jsonDataFromAPI);
            JSONArray resultsArray = jsonData.optJSONArray(RESULTS);
            if (resultsArray == null) return null;

            int length = resultsArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject movie = (JSONObject) resultsArray.get(i);
                int voteCount = movie.optInt(VOTE_COUNT, 0);
                Integer id = movie.optInt(ID, 0);
                Boolean video = movie.optBoolean(VIDEO, false);
                float voteAverage = ((float) movie.optLong(VOTE_AVERAGE, 0));
                String title = movie.optString(TITLE, NOT_AVAILABLE_FALLBACK);
                float popularity = ((float) movie.optLong(POPULARITY, 0));
                String posterPath = movie.optString(POSTER_PATH);
                String originalLanguage = movie.optString(ORIGINAL_LANGUAGE, NO_LANGUAGE_FALLBACK);
                String originalTitle = movie.optString(ORIGINAL_TITLE, NOT_AVAILABLE_FALLBACK);

                JSONArray jsonGenreIds;
                List<Integer> genreIds = null;
                if (movie.has(GENRE_IDS)) {
                    jsonGenreIds = movie.optJSONArray(GENRE_IDS);
                    int genreSize = jsonGenreIds.length();
                    genreIds = new ArrayList<>(genreSize);
                    for (int j = 0; j < genreSize; j++) {
                        genreIds.add((Integer) jsonGenreIds.get(j));
                    }
                }

                String backdropPath = movie.optString(BACKDROP_PATH);
                Boolean adult = movie.optBoolean(ADULT, false);
                String overview = movie.optString(OVERVIEW, NOT_AVAILABLE_FALLBACK);
                String releaseDate = movie.optString(RELEASE_DATE, NOT_AVAILABLE_FALLBACK);
                movieObjects.add(new MovieObject(voteCount, id, video, voteAverage, title,
                        popularity, posterPath, originalLanguage, originalTitle, genreIds,
                        backdropPath, adult, overview, releaseDate));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieObjects;
    }

    static ArrayList<VideosObject> parseVideosDataFromJSON(String jsonDataFromAPI) {
        ArrayList<VideosObject> videosObjects = new ArrayList<>();


        return videosObjects;
    }


}


/*
Sample data
    {
        "page": 1,
        "total_results": 8115,
        "total_pages": 406,
        "results": [
        {
        "vote_count": 1051,
        "id": 19404,
        "video": false,
        "vote_average": 9.1,
        "title": "Dilwale Dulhania Le Jayenge",
        "popularity": 16.553605,
        "poster_path": "/uC6TTUhPpQCmgldGyYveKRAu8JN.jpg",
        "original_language": "hi",
        "original_title": "Dilwale Dulhania Le Jayenge",
        "genre_ids": [
        35,
        18,
        10749
        ],
        "backdrop_path": "/nl79FQ8xWZkhL3rDr1v2RFFR6J0.jpg",
        "adult": false,
        "overview": "Raj is a rich, carefree, happy-go-lucky second generation NRI. Simran is the daughter of Chaudhary Baldev Singh, who in spite of being an NRI is very strict about adherence to Indian values. Simran has left for India to be married to her childhood fiancé. Raj leaves for India with a mission at his hands, to claim his lady love under the noses of her whole family. Thus begins a saga.",
        "release_date": "1995-10-20"
        },
        {
        "vote_count": 81,
        "id": 20532,
        "video": false,
        "vote_average": 8.7,
        "title": "Sansho the Bailiff",
        "popularity": 10.957644,
        "poster_path": "/deBjt3LT3UQHRXiNX1fu28lAtK6.jpg",
        "original_language": "ja",
        "original_title": "山椒大夫",
        "genre_ids": [
        18
        ],
        "backdrop_path": "/keaFMNUr1OpdHzPWJ0qeDP8hrO8.jpg",
        "adult": false,
        "overview": "In medieval Japan a compassionate governor is sent into exile. His wife and children try to join him, but are separated, and the children grow up amid suffering and oppression.",
        "release_date": "1954-03-31"
        },*/
