package com.example.lineta.Entity.Conversation;

public class Timestamp  {
    private Long seconds;
    private Integer nanos;

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

    public Integer getNanos() {
        return nanos;
    }

    public void setNanos(Integer nanos) {
        this.nanos = nanos;
    }

    // Chuyển thành milliseconds
    public Long toMilliseconds() {
        if (seconds != null) {
            return seconds * 1000 + (nanos != null ? nanos / 1000000 : 0);
        }
        return null;
    }

    // Tính thời gian tương đối (similar to Day.js)
    public String toRelativeTime() {
        if (seconds == null) return "Unknown";

        long timestampMillis = toMilliseconds();
        long nowMillis = System.currentTimeMillis();
        long diffMillis = nowMillis - timestampMillis;

        if (diffMillis < 0) return "Future"; // Trường hợp timestamp lớn hơn hiện tại

        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;
        long diffWeeks = diffDays / 7;

        if (diffSeconds < 60) {
            return diffSeconds <= 1 ? "few seconds ago" : diffSeconds + " seconds ago";
        } else if (diffMinutes < 60) {
            return diffMinutes == 1 ? "1 minute ago" : diffMinutes + " minutes ago";
        } else if (diffHours < 24) {
            return diffHours == 1 ? "1 hour ago" : diffHours + " hours ago";
        } else if (diffDays < 7) {
            return diffDays == 1 ? "1 day ago" : diffDays + " days ago";
        } else {
            return diffWeeks == 1 ? "1 week ago" : diffWeeks + " weeks ago";
        }
    }
}