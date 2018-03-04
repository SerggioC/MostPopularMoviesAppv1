package com.sergiocruz.mostpopularmovies;

/**
 * Created by Sergio on 03/03/2018.
 * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
 *
 */

public class VideosObject {
    String video_id;
    String iso_639_1;
    String iso_3166_1;
    String key;
    String name;
    String site;
    Integer size;
    String type;

    public VideosObject(String video_id, String iso_639_1, String iso_3166_1, String key, String name, String site, Integer size, String type) {
        this.video_id = video_id;
        this.iso_639_1 = iso_639_1;
        this.iso_3166_1 = iso_3166_1;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }

    public String getVideo_id() {
        return video_id;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public Integer getSize() {
        return size;
    }

    public String getType() {
        return type;
    }
}

// Sample data
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