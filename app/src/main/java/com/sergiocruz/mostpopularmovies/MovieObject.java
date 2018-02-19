package com.sergiocruz.mostpopularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sergio on 17/02/2018.
 */

class MovieObject implements Parcelable {
    public static final Creator<MovieObject> CREATOR = new Creator<MovieObject>() {
        @Override
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };
    private Integer vote_count;
    private Integer id;
    private Boolean video;
    private Float vote_average;
    private String title;
    private Float popularity;
    private String poster_path;
    private String original_language;
    private String original_title;
    private int[] genre_ids;
    private String backdrop_path;
    private Boolean adult;
    private String overview;
    private String release_date;

    public MovieObject(Integer vote_count, Integer id, Boolean video, Float vote_average, String title, Float popularity, String poster_path, String original_language, String original_title, int[] genre_ids, String backdrop_path, Boolean adult, String overview, String release_date) {
        this.vote_count = vote_count;
        this.id = id;
        this.video = video;
        this.vote_average = vote_average;
        this.title = title;
        this.popularity = popularity;
        this.poster_path = poster_path;
        this.original_language = original_language;
        this.original_title = original_title;
        this.genre_ids = genre_ids;
        this.backdrop_path = backdrop_path;
        this.adult = adult;
        this.overview = overview;
        this.release_date = release_date;
    }

    protected MovieObject(Parcel in) {
        this.vote_count = in.readInt();
        this.id = in.readInt();
        this.video = in.readByte() == 1;
        this.vote_average = in.readFloat();
        this.title = in.readString();
        this.popularity = in.readFloat();
        this.poster_path = in.readString();
        this.original_language = in.readString();
        this.original_title = in.readString();
        in.readIntArray(genre_ids);
        this.backdrop_path = in.readString();
        this.adult = in.readByte() == 1;
        this.overview = in.readString();
        this.release_date = in.readString();
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.vote_count);
        dest.writeInt(this.id);
        dest.writeByte((byte) (this.video ? 1 : 0));
        dest.writeFloat(this.vote_average);
        dest.writeString(this.title);
        dest.writeFloat(this.popularity);
        dest.writeString(this.poster_path);
        dest.writeString(this.original_language);
        dest.writeString(this.original_title);
        dest.writeIntArray(this.genre_ids);
        dest.writeString(this.backdrop_path);
        dest.writeByte((byte) (this.adult ? 1 : 0));
        dest.writeString(this.overview);
        dest.writeString(this.release_date);

    }

    public Integer getVote_count() {
        return vote_count;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getVideo() {
        return video;
    }

    public Float getVote_average() {
        return vote_average;
    }

    public String getTitle() {
        return title;
    }

    public Float getPopularity() {
        return popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public int[] getGenre_ids() {
        return genre_ids;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public Boolean getAdult() {
        return adult;
    }

    public String getOverview() {
        return overview;
    }

    public String getRelease_date() {
        return release_date;
    }
}

// example movie data:
//                 "vote_count":5727,
//                 "id":211672,
//                 "video":false,
//                 "vote_average":6.4,
//                 "title":"Minions",
//                 "popularity":599.701427,
//                 "poster_path":"\/q0R4crx2SehcEEQEkYObktdeFy.jpg",
//                 "original_language":"en",
//                 "original_title":"Minions",
//                 "genre_ids":[
//                 10751,
//                 16,
//                 12,
//                 35
//                 ],
//                 "backdrop_path":"\/qLmdjn2fv0FV2Mh4NBzMArdA0Uu.jpg",
//                 "adult":false,
//                 "overview":"Minions Stuart, Kevin and Bob are recruited by Scarlet Overkill, a super-villain who, alongside her inventor husband Herb, hatches a plot to take over the world.",
//                 "release_date":"2015-06-17