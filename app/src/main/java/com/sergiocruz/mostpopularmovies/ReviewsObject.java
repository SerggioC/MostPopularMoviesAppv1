package com.sergiocruz.mostpopularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sergio on 03/03/2018.
 * Parcelable to be able to send in intent Extra to details activity
 */

public class ReviewsObject implements Parcelable{
    private String reviewId;
    private String author;
    private String content;
    private String url;

    public ReviewsObject(String reviewId, String author, String content, String url) {
        this.reviewId = reviewId;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    protected ReviewsObject(Parcel in) {
        reviewId = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reviewId);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ReviewsObject> CREATOR = new Parcelable.Creator<ReviewsObject>() {
        @Override
        public ReviewsObject createFromParcel(Parcel in) {
            return new ReviewsObject(in);
        }

        @Override
        public ReviewsObject[] newArray(int size) {
            return new ReviewsObject[size];
        }
    };
}

