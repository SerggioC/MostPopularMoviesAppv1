package com.sergiocruz.mostpopularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergio on 19/02/2018.
 * Creating Parcelable with
 * http://www.parcelabler.com/
 */

public class MovieObject implements Parcelable {
    private Integer voteCount;
    private Integer id;
    private Boolean video;
    private Float voteAverage;
    private String title;
    private Float popularity;
    private String posterPath;
    private String originalLanguage;
    private String originalTitle;
    private List<Integer> genreIDs;
    private String backdropPath;
    private Boolean adult;
    private String overview;
    private String releaseDate;
    private Boolean isFavorite;
    private String posterFilePath;
    private String backdropFilePath;

    public MovieObject(Integer voteCount, Integer id, Boolean video, Float voteAverage, String title, Float popularity, String posterPath, String originalLanguage, String originalTitle, List<Integer> genreIDs, String backdropPath, Boolean adult, String overview, String releaseDate, Boolean isFavorite, String posterFilePath, String backdropFilePath) {
        this.voteCount = voteCount;
        this.id = id;
        this.video = video;
        this.voteAverage = voteAverage;
        this.title = title;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.genreIDs = genreIDs;
        this.backdropPath = backdropPath;
        this.adult = adult;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.isFavorite = isFavorite;
        this.posterFilePath = posterFilePath;
        this.backdropFilePath = backdropFilePath;
    }

    public MovieObject(Parcel in) {
        voteCount = in.readByte() == 0x00 ? null : in.readInt();
        id = in.readByte() == 0x00 ? null : in.readInt();
        byte videoVal = in.readByte();
        video = videoVal == 0x02 ? null : videoVal != 0x00;
        voteAverage = in.readByte() == 0x00 ? null : in.readFloat();
        title = in.readString();
        popularity = in.readByte() == 0x00 ? null : in.readFloat();
        posterPath = in.readString();
        originalLanguage = in.readString();
        originalTitle = in.readString();
        if (in.readByte() == 0x01) {
            genreIDs = new ArrayList<>();
            in.readList(genreIDs, Integer.class.getClassLoader());
        } else {
            genreIDs = null;
        }
        backdropPath = in.readString();
        byte adultVal = in.readByte();
        adult = adultVal == 0x02 ? null : adultVal != 0x00;
        overview = in.readString();
        releaseDate = in.readString();
        byte isFavVal = in.readByte();
        isFavorite = isFavVal == 0x02 ? null : isFavVal != 0x00;
        posterFilePath = in.readString();
        backdropFilePath = in.readString();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (voteCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(voteCount);
        }
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        if (video == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (video ? 0x01 : 0x00));
        }
        if (voteAverage == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeFloat(voteAverage);
        }
        dest.writeString(title);
        if (popularity == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeFloat(popularity);
        }
        dest.writeString(posterPath);
        dest.writeString(originalLanguage);
        dest.writeString(originalTitle);
        if (genreIDs == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(genreIDs);
        }
        dest.writeString(backdropPath);
        if (adult == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (adult ? 0x01 : 0x00));
        }
        dest.writeString(overview);
        dest.writeString(releaseDate);

        if (isFavorite == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isFavorite ? 0x01 : 0x00));
        }
        dest.writeString(posterFilePath);
        dest.writeString(backdropFilePath);

    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getVideo() {
        return video;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public String getTitle() {
        return title;
    }

    public Float getPopularity() {
        return popularity;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public List<Integer> getGenreIDs() {
        return genreIDs;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Boolean getAdult() {
        return adult;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public String getPosterFilePath() {
        return posterFilePath;
    }

    public String getBackdropFilePath() {
        return backdropFilePath;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public void setPosterFilePath(String posterFilePath) {
        this.posterFilePath = posterFilePath;
    }

    public void setBackdropFilePath(String backdropFilePath) {
        this.backdropFilePath = backdropFilePath;
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieObject> CREATOR = new Parcelable.Creator<MovieObject>() {
        @Override
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

}