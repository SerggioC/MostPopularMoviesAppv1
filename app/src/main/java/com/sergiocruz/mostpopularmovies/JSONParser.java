package com.sergiocruz.mostpopularmovies;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Sergio on 18/02/2018.
 * JSON Parsing
 */

public class JSONParser {

    // JSON key names Movies
    static final class MoviesKeys {
        static final String RESULTS = "results";
        static final String VOTE_COUNT = "vote_count";
        static final String MOVIE_ID = "id";
        static final String VIDEO = "video";
        static final String VOTE_AVERAGE = "vote_average";
        static final String TITLE = "title";
        static final String POPULARITY = "popularity";
        static final String ORIGINAL_LANGUAGE = "original_language";
        static final String POSTER_PATH = "poster_path";
        static final String ORIGINAL_TITLE = "original_title";
        static final String GENRE_IDS = "genre_ids";
        static final String BACKDROP_PATH = "backdrop_path";
        static final String ADULT = "adult";
        static final String OVERVIEW = "overview";
        static final String RELEASE_DATE = "release_date";
        static final String NOT_AVAILABLE_FALLBACK = "(N/A)";
        static final String NO_LANGUAGE_FALLBACK = "en-US";
    }
    
    public static ArrayList<MovieObject> parseMovieDataFromJSON(String jsonDataFromAPI) {
        ArrayList<MovieObject> movieObjects;
        try {
            JSONObject jsonData = new JSONObject(jsonDataFromAPI);
            JSONArray resultsArray = jsonData.optJSONArray(MoviesKeys.RESULTS);
            if (resultsArray == null) return null;

            int length = resultsArray.length();
            movieObjects = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                JSONObject movie = (JSONObject) resultsArray.get(i);
                int voteCount = movie.optInt(MoviesKeys.VOTE_COUNT, 0);
                Integer id = movie.optInt(MoviesKeys.MOVIE_ID, 0);
                Boolean video = movie.optBoolean(MoviesKeys.VIDEO, false);
                float voteAverage = ((float) movie.optLong(MoviesKeys.VOTE_AVERAGE, 0));
                String title = movie.optString(MoviesKeys.TITLE, MoviesKeys.NOT_AVAILABLE_FALLBACK);
                float popularity = ((float) movie.optLong(MoviesKeys.POPULARITY, 0));
                String posterPath = movie.optString(MoviesKeys.POSTER_PATH);
                String originalLanguage = movie.optString(MoviesKeys.ORIGINAL_LANGUAGE, MoviesKeys.NO_LANGUAGE_FALLBACK);
                String originalTitle = movie.optString(MoviesKeys.ORIGINAL_TITLE, MoviesKeys.NOT_AVAILABLE_FALLBACK);

                JSONArray jsonGenreIds;
                List<Integer> genreIds = null;
                if (movie.has(MoviesKeys.GENRE_IDS)) {
                    jsonGenreIds = movie.optJSONArray(MoviesKeys.GENRE_IDS);
                    int genreSize = jsonGenreIds.length();
                    genreIds = new ArrayList<>(genreSize);
                    for (int j = 0; j < genreSize; j++) {
                        genreIds.add(jsonGenreIds.optInt(j));
                    }
                }

                String backdropPath = movie.optString(MoviesKeys.BACKDROP_PATH);
                Boolean adult = movie.optBoolean(MoviesKeys.ADULT, false);
                String overview = movie.optString(MoviesKeys.OVERVIEW, MoviesKeys.NOT_AVAILABLE_FALLBACK);
                String releaseDate = movie.optString(MoviesKeys.RELEASE_DATE, MoviesKeys.NOT_AVAILABLE_FALLBACK);
                movieObjects.add(new MovieObject(voteCount, id, video, voteAverage, title,
                        popularity, posterPath, originalLanguage, originalTitle, genreIds,
                        backdropPath, adult, overview, releaseDate, null, null, null));
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return movieObjects;
    }

    static final class VideoKeys {
        static final String RESULTS = "results";
        static final String VIDEO_ID = "id";
        static final String ISO_639_1 = "iso_639_1";
        static final String ISO_3166_1 = "iso_3166_1";
        static final String KEY = "key";
        static final String NAME = "name";
        static final String SITE = "site";
        static final String SIZE = "size";
        static final String TYPE = "type";
    }

    public static ArrayList<VideoObject> parseVideosDataFromJSON(String jsonDataFromAPI) {
        ArrayList<VideoObject> videoObjects;
        try {
            JSONObject jsonData = new JSONObject(jsonDataFromAPI);
            JSONArray resultsArray = jsonData.optJSONArray(VideoKeys.RESULTS);
            if (resultsArray == null) return null;

            int length = resultsArray.length();
            videoObjects = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                JSONObject video = (JSONObject) resultsArray.get(i);

                String videoID = video.optString(VideoKeys.VIDEO_ID);
                String iso6391 = video.optString(VideoKeys.ISO_639_1);
                String iso31661 = video.optString(VideoKeys.ISO_3166_1);
                String key = video.optString(VideoKeys.KEY);
                String name = video.optString(VideoKeys.NAME);
                String site = video.optString(VideoKeys.SITE);
                Integer size = video.optInt(VideoKeys.SIZE);
                String type = video.optString(VideoKeys.TYPE);

                videoObjects.add(new VideoObject(videoID, iso6391, iso31661, key, name, site, size, type, null));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return videoObjects;
    }

    static final class ReviewsKeys {
        static final String RESULTS = "results";
        static final String REVIEW_ID = "id";
        static final String AUTHOR = "author";
        static final String CONTENT = "content";
        static final String URL = "url";
    }

    public static ArrayList<ReviewObject> parseReviewsDataFromJSON(String JSONDataFromAPI) {
        ArrayList<ReviewObject> reviewObjects;

        try {
            JSONObject jsonData = new JSONObject(JSONDataFromAPI);
            JSONArray resultsArray = jsonData.optJSONArray(ReviewsKeys.RESULTS);
            if (resultsArray == null) return null;

            int length = resultsArray.length();
            reviewObjects = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                JSONObject review = (JSONObject) resultsArray.get(i);

                String reviewID = review.optString(ReviewsKeys.REVIEW_ID);
                String author = review.optString(ReviewsKeys.AUTHOR);
                String content = review.optString(ReviewsKeys.CONTENT);
                String url = review.optString(ReviewsKeys.URL);

                reviewObjects.add(new ReviewObject(reviewID, author, content, url));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return reviewObjects;
    }

    public static List<Integer> getIntArrayFromJSON(String stringJSONData) {
        List<Integer> genreIDs;
        if (TextUtils.isEmpty(stringJSONData)) {
            return null;
        } else {
            try {
                JSONArray jsonArray = new JSONArray(stringJSONData);
                int arSize = jsonArray.length();
                genreIDs = new ArrayList<>(arSize);
                for (int i = 0; i < arSize; i++) {
                    genreIDs.add(jsonArray.optInt(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }
        return genreIDs;
    }

}

//{
//        "id": 284054,
//        "page": 1,
//        "results": [
//        {
//          "id": "5a844e0fc3a36862e100c22f",
//          "author": "Gimly",
//          "content": "The reviews for _Black Panther_ are all essentially saying the same thing: This is a great movie because it's so different from anything we've seen before, not just in the MCU but in the superhero genre overall, the villain is fantastic and _Black Panther_ is just a new and totally innovative film. Allow me to disagree (almost) entirely. _Black Panther_ is a great film, not because it breaks the mould, but because Marvel Studios has a successful pattern and _Black Panther_ adheres to it completely. Obviously the importance of a lead who is not just black but actually African is not to be understated, we have the least white cast of any superhero movie to date and they absolutely killed it, all very important socio-political stuff, to be sure. But the bones of _Black Panther_, the plot, the script, the events, the turns, all of that, it's a carbon copy of what's worked 17 times before. It works again, don't get me wrong, _Black Panther_ truly was an experience, both important and fantastic, but to say it re-invented the wheel here, is, to me, an outright lie.\r\n\r\n_Final rating:★★★½ - I really liked it. Would strongly recommend you give it your time._",
//          "url": "https://www.themoviedb.org/review/5a844e0fc3a36862e100c22f"
//        },
//        {
//          "id": "5a87b8550e0a26535a047632",
//          "author": "hankster3000",
//          "content": "Overrated and overhyped. Definitely avoid. Watch on redbox if you must.",
//          "url": "https://www.themoviedb.org/review/5a87b8550e0a26535a047632"
//        },
//        {
//          "id": "5a8894ba0e0a2653db05d34f",
//          "author": "Movie Queen41",
//          "content": "This is definitely one of Marvel's best because the story is compelling and the characters are well developed. Black Panther and his friends and family must defend themselves and their homeland of Wakanda from the clutches of Klau (Andy Serkis) and Killmonger (Michael B. Jordan). Not only is the lead actor great as T'Challa/Black Panther, but the supporting cast does a fine job as well, especially the female actors. T'Challa is surrounded by smart, strong women. Marvel seems to break its villain curse with Klau and especially Killmonger. Killmonger is very well developed and even sympathetic at times, despite his villainy. He sort of reminded me of Tom Hiddleston's Loki. The action scenes and special effects are outstanding. Highly recommended if you enjoy comic book movies.",
//        "url": "https://www.themoviedb.org/review/5a8894ba0e0a2653db05d34f"
//        },
//        {
//          "id": "5a8dfdcf0e0a263e6e006d75",
//          "author": "Crenor",
//          "content": "Really good movie. Good story, lots of fun. This is what DC is missing.",
//          "url": "https://www.themoviedb.org/review/5a8dfdcf0e0a263e6e006d75"
//        },
//        {
//          "id": "5a93b14b0e0a2616c602de5e",
//          "author": "Per Gunnar Jonsson",
//          "content": "It was with some hesitation that I went to see this movie. I was afraid that it would be burdened with too much political and social preaching. Especially knowing how much the movie is praised amongst certain factions in our society today. Sure enough there were indeed some of that nonsense but luckily it was much less than I feared.\r\n\r\nI actually found the movie fairly entertaining. It is far from great, certainly very far from the insane hype surrounding it, but pretty okay. It is more or less a standard issue super hero movie on a big budget. Of course this means a fairly mediocre underlying story beefed up with lots of action and special effects.\r\n\r\nThe story itself is a traditional revenge story with some black power stuff. Nothing to write home about but it works as a vehicle for the action and special effects which is what makes this movie. There are the usual nonsense and plot holes in it of course. Like Shuri claiming that Vibranium (silly name but never mind) is instable at high speeds yet they have no problem making flying ships and other fast moving stuff with it.\r\n\r\nThe characters are a wee bit disappointing as far as the main characters are concerned. I felt that the main protagonist has little in terms of charisma. Killmonger, the main bad guy, was downright disappointing. To me he looked like the stereotype of a dumb thug and mostly he behaved like one. Bloody hell could they not have found an actor that looked less…well…stupid!\r\n\r\nPersonally the character I liked best was actually Klaue which was pretty cool, insane but cool. The female warriors, which were both kick-ass and smart, and Shuri is also fairly high up on my approval list.\r\n\r\nSpecial effects! This is of course where this movie shines. Overall I think the special effects were good. The design of the various gadgets, ships etc were very nice. The rampaging rhinos in the final fight really made me laugh. I should have seen that coming. I definitely liked all the sonic effects. Obviously this movie would have been more or less null if the special effects would not have been a success.\r\n\r\nOn the whole Black Panther is a quite enjoyable special effects and action movie. Nothing more, nothing less. Some social preaching nonsense but less than I feared, hum ho story, lots of action and lots of special effects.",
//          "url": "https://www.themoviedb.org/review/5a93b14b0e0a2616c602de5e"
//        }
//        ],
//          "total_pages": 1,
//          "total_results": 5
//        }



// Videos Sample data
//{
//        "id": 284054,
//        "results": [
//        {
//            "id": "593b558fc3a3680f7b0046d0",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "dxWvtMOGAhw",
//            "name": "Official Teaser Trailer",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Trailer"
//        },
//        {
//            "id": "5a96a57c0e0a26544a008cf9",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "xjDjIWPwcPU",
//            "name": "Official Trailer",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Trailer"
//        },
//        {
//            "id": "5a9730300e0a26016500595d",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "HnnRyhuX_jM",
//            "name": "Warriors of Wakanda",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Featurette"
//        },
//        {
//            "id": "5a9730510e0a260153005b65",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "jJm2WFTscDM",
//            "name": "Wakanda Revealed",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Featurette"
//        },
//        {
//            "id": "5a973072c3a368613d005542",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "y9fBzUtrKwY",
//            "name": "Good to Be King",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Featurette"
//        },
//        {
//            "id": "5a97309b92514128a70052b3",
//            "iso_639_1": "en",
//            "iso_3166_1": "US",
//            "key": "yH72D12BkzY",
//            "name": "From Page to Screen",
//            "site": "YouTube",
//            "size": 1080,
//            "type": "Featurette"
//        }
//        ]
//        }


/*
Sample data Movies
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
