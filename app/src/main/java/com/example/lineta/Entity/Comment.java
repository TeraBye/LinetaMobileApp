package com.example.lineta.Entity;

public class Comment {
    private String commentId;
    private String username;
    private String content;
    private String date;
    private String postID;
    private String fullName;
    private String profilePicURL;

    // Getters và setters

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPostID() { return postID; }
    public void setPostID(String postID) { this.postID = postID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfilePicURL() { return profilePicURL; }
    public void setProfilePicURL(String profilePicURL) { this.profilePicURL = profilePicURL; }
}
