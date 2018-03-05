package com.sergiocruz.mostpopularmovies;

/**
 * Created by Sergio on 03/03/2018.
 * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
 *
 */

public class VideoObject {
    String video_id;
    String iso_639_1;
    String iso_3166_1;
    String key;
    String name;
    String site;
    Integer size;
    String type;

    public VideoObject(String video_id, String iso_639_1, String iso_3166_1, String key, String name, String site, Integer size, String type) {
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

