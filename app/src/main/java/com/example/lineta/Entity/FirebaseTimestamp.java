package com.example.lineta.Entity;

import java.sql.Timestamp;

public class FirebaseTimestamp {
    private long seconds;
    private int nanos;

    public Timestamp toTimestamp() {
        return new Timestamp(seconds * 1000L + nanos / 1_000_000L);
    }

    // getters và setters
    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public int getNanos() {
        return nanos;
    }

    public void setNanos(int nanos) {
        this.nanos = nanos;
    }
}
