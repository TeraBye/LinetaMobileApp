package com.example.lineta.Entity;

public class ReplyComment {
    private String username;
    private String commentId;
    private String content;
    private String fullName;
    private String profilePicURL;

    // Constructor mặc định
    public ReplyComment() {
    }

    // Constructor đầy đủ
    public ReplyComment(String username, String commentId, String content, String fullName, String profilePicURL) {
        this.username = username;
        this.commentId = commentId;
        this.content = content;
        this.fullName = fullName;
        this.profilePicURL = profilePicURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }
}
