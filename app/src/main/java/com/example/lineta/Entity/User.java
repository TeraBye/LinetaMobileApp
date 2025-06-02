package com.example.lineta.Entity;

import com.google.firebase.Timestamp;

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
public class User {
    String uid;
    String username;
    String email;
    String fullName;
    String profilePicURL;
    String bio;
    Long postNum;
    Long followerNum;
    Long followingNum;
    Timestamp createdAt;

}
