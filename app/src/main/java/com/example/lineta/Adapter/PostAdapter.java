package com.example.lineta.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> posts;
    private Context context;  // Thêm Context vào adapter

    public PostAdapter(List<Post> posts) {
        this.context = context;  // Nhận context từ Activity
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.tvUsername.setText(post.getFullName());
        holder.tvCaption.setText(post.getContent());

        // Ẩn ảnh và video trước
        holder.imgPost.setVisibility(View.GONE);
        holder.videoPost.setVisibility(View.GONE);
        holder.btnPlay.setVisibility(View.GONE);
        holder.avatar.setVisibility(View.GONE);// Ẩn nút play

        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
            holder.avatar.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getProfilePicURL())
                    .placeholder(R.drawable.main_logo) // Thay bằng hình placeholder khi chưa có ảnh
                    .into(holder.avatar);
        }

        // Nếu có ảnh
        if (post.getPicture() != null && !post.getPicture().isEmpty()) {
            holder.imgPost.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPicture())
                    .placeholder(R.drawable.main_logo) // Thay bằng hình placeholder khi chưa có ảnh
                    .into(holder.imgPost);
        }

        // Nếu có video
        if (post.getVideo() != null && !post.getVideo().isEmpty()) {
            holder.videoPost.setVisibility(View.VISIBLE);
            holder.videoPost.setVideoPath(post.getVideo());
            holder.videoPost.seekTo(1); // Để video hiển thị thumbnail và không chạy

            // Hiển thị nút Play
            holder.btnPlay.setVisibility(View.VISIBLE);

            // Khi nhấn vào nút Play, video sẽ phát
            holder.btnPlay.setOnClickListener(v -> {
                // Lập tức ẩn nút play và bắt đầu phát video
                holder.btnPlay.setVisibility(View.GONE);
                holder.videoPost.start();
            });
            holder.videoPost.setOnClickListener(v -> {
                if (holder.videoPost.isPlaying()) {
                    // Nếu video đang phát, dừng video
                    holder.videoPost.pause();
                } else {
                    // Nếu video không phát, bắt đầu phát video
                    holder.videoPost.start();
                }
            });



        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvCaption;
        ImageView imgPost, btnPlay, avatar;  // Thêm btnPlay vào
        VideoView videoPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            videoPost = itemView.findViewById(R.id.videoPost);
            btnPlay = itemView.findViewById(R.id.btnPlay);  // Khởi tạo btnPlay
        }
    }
}

