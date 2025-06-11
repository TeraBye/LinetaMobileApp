package com.example.lineta.Entity;

import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class Notification {
    private String receiverUsername;
    private  String senderUsername;
    private String content;
    private String type;
    private String postId;
    private FirebaseTimestamp timestamp;
    private boolean read;
    private String profilePicURL;
}
