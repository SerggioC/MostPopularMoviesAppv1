package com.sergiocruz.mostpopularmovies;

/**
 * Created by Sergio on 03/03/2018.
 */

public class ReviewsObject {
    String reviewId;
    String author;
    String content;
    String url;

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
}

