package com.example.socialconnect;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String userId;
    private String commentText;
    private Timestamp timestamp;

    public Comment() {}

    public Comment(String commentId, String userId, String commentText, Timestamp timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public String getCommentText() { return commentText; }
    public Timestamp getTimestamp() { return timestamp; }
}
