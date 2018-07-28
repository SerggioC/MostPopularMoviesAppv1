package com.sergiocruz.mostpopularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sergio on 03/03/2018.
 * https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
 * Create parcelable object http://www.parcelabler.com/
 */

public class VideoObject implements Parcelable {
    private String video_id;
    private String iso_639_1;
    private String iso_3166_1;
    private String key;
    private String name;
    private String site;
    private Integer size;
    private String type;
    private String thumbnailUri;

    public VideoObject(String video_id, String iso_639_1, String iso_3166_1, String key, String name, String site, Integer size, String type, String thumbnailUri) {
        this.video_id = video_id;
        this.iso_639_1 = iso_639_1;
        this.iso_3166_1 = iso_3166_1;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
        this.thumbnailUri = thumbnailUri;
    }

    protected VideoObject(Parcel in) {
        video_id = in.readString();
        iso_639_1 = in.readString();
        iso_3166_1 = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readByte() == 0x00 ? null : in.readInt();
        type = in.readString();
        thumbnailUri = in.readString();
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

    public String getType() { return type; }

    public String getThumbnailUri() { return thumbnailUri; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(video_id);
        dest.writeString(iso_639_1);
        dest.writeString(iso_3166_1);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        if (size == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(size);
        }
        dest.writeString(type);
        dest.writeString(thumbnailUri);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoObject> CREATOR = new Parcelable.Creator<VideoObject>() {
        @Override
        public VideoObject createFromParcel(Parcel in) {
            return new VideoObject(in);
        }

        @Override
        public VideoObject[] newArray(int size) {
            return new VideoObject[size];
        }
    };
}

