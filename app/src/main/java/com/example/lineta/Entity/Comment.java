package com.example.lineta.Entity;

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
public class Comment {
    private String commentId;
    private String username;
    private String content;
    private String date;
    private String postID;
    private String fullName;
    private String profilePicURL;
    private int numberOfLike;

}
