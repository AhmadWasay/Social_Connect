package com.example.socialconnect;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String textContent;
    private String imageUrl;
    private Timestamp timestamp;
    private List<String> likes;

    public Post() {
    }

    public Post(String postId, String userId, String textContent, String imageUrl, Timestamp timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.textContent = textContent;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getTextContent() { return textContent; }
    public String getImageUrl() { return imageUrl; }
    public Timestamp getTimestamp() { return timestamp; }

    public List<String> getLikes() {
        if (likes == null){
            return new ArrayList<>();
        }
        return likes;
    }
}
