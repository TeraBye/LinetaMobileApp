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
public class Post {
    private String postId;
    private String fullName;  // Tên tác giả
    private String content;   // Nội dung bài viết
    private String picture;   // URL ảnh (có thể là null)
    private String video;
    private String profilePicURL;
    private int numberOfLike;
    private String username;
    private String date;


}
