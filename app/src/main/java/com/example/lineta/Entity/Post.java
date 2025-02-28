package com.example.lineta.Entity;

public class Post {
    private String username;
    private String caption;
    private int image;

    public Post(String username, String caption, int image) {
        this.username = username;
        this.caption = caption;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public String getCaption() {
        return caption;
    }

    public int getImage() {
        return image;
    }
}

