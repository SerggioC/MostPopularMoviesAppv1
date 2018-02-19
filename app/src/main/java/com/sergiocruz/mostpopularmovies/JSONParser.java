package com.sergiocruz.mostpopularmovies;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Sergio on 18/02/2018.
 */

public class JSONParser {

    public static ArrayList<MovieObject> parseDataFromJSON(String jsonDataFromAPI, String baseImageUrl, String imageSize) {
        ArrayList<MovieObject> movieObjects = new ArrayList<>();
        try {
            JSONObject jsonData = new JSONObject(jsonDataFromAPI);
            JSONArray resultsArray = jsonData.optJSONArray("results");
            if (resultsArray == null) return null;
            int length = resultsArray.length();

            for (int i = 0; i < length; i++) {
                JSONObject movie = (JSONObject) resultsArray.get(i);
                int voteCount = movie.optInt("vote_count", 0);
                Integer id = movie.optInt("id", 0);
                Boolean video = movie.optBoolean("video", false);
                float voteAverage = ((float) movie.optLong("vote_average", 0));
                String title = movie.optString("title", "");
                float popularity = ((float) movie.optLong("popularity", 0));
                String posterPath = getURL_Path(baseImageUrl, imageSize, movie.optString("poster_path", ""));
                String originalLanguage = movie.optString("original_language", "");
                String originalTitle = movie.optString("original_title", "");
                JSONArray jsonGenreIds = movie.optJSONArray("genre_ids");
                int[] genreIds;
                if (jsonGenreIds == null) genreIds = new int[0];

                int genreSize = jsonGenreIds.length();
                genreIds = new int[genreSize];
                for (int j = 0; j < genreSize; j++) {
                    genreIds[j] = (int) jsonGenreIds.get(j);
                }

                String backdropPath = getURL_Path(baseImageUrl, imageSize, movie.optString("backdrop_path", ""));
                Boolean adult = movie.optBoolean("adult", false);
                String overview = movie.optString("overview", "");
                String releaseDate = movie.optString("release_date", "");
                movieObjects.add(new MovieObject(voteCount, id, video, voteAverage, title,
                        popularity, posterPath, originalLanguage, originalTitle, genreIds,
                        backdropPath, adult, overview, releaseDate));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieObjects;
    }

    private static String getURL_Path(String baseImageUrl, String imageSize, String img_signature) {
        return Uri.parse(baseImageUrl).buildUpon().appendPath(imageSize).appendEncodedPath(img_signature).build().toString();
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
