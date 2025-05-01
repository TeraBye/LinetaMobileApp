package com.example.lineta.Entity;

public class Post {
    private String fullName;  // Tên tác giả
    private String content;   // Nội dung bài viết
    private String picture;   // URL ảnh (có thể là null)
    private String video;
    private String profilePicURL;// URL video (có thể là null)

    public Post(String fullName, String content, String picture, String video, String profilePicURL) {
        this.fullName = fullName;
        this.content = content;
        this.picture = picture;
        this.video = video;
        this.profilePicURL = profilePicURL;
    }

    // Getter và Setter
    public String getFullName() {
        return fullName;
    }

    public String getContent() {
        return content;
    }

    public String getPicture() {
        return picture;
    }

    public String getVideo() {
        return video;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setProfilePicURL(String picture) {
        this.profilePicURL = picture;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }
}
