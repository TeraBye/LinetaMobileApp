package com.example.lineta.Entity;

public class Message {
    private String text;
    private String time;
    private boolean isSent;

    public Message(String text, String time, boolean isSent) {
        this.text = text;
        this.time = time;
        this.isSent = isSent;
    }

    public String getText() { return text; }
    public String getTime() { return time; }
    public boolean isSent() { return isSent; }
}