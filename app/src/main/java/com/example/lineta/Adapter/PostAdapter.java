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
import com.example.lineta.Home.interaction.CommentBottomSheetFragment; // Thêm import dialog nếu chưa có

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> posts;
    private Context context;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
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

        holder.imgPost.setVisibility(View.GONE);
        holder.videoPost.setVisibility(View.GONE);
        holder.btnPlay.setVisibility(View.GONE);
        holder.avatar.setVisibility(View.GONE);

        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
            holder.avatar.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getProfilePicURL())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.avatar);
        }

        if (post.getPicture() != null && !post.getPicture().isEmpty() && !post.getPicture().equalsIgnoreCase("null")) {
            holder.imgPost.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPicture())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.imgPost);
        }

        String video = post.getVideo();
        if (video != null && !video.trim().isEmpty() && !video.equalsIgnoreCase("null")) {
            holder.videoPost.setVisibility(View.VISIBLE);
            holder.videoPost.setVideoPath(video);
            holder.videoPost.seekTo(1);
            holder.btnPlay.setVisibility(View.VISIBLE);

            holder.btnPlay.setOnClickListener(v -> {
                holder.btnPlay.setVisibility(View.GONE);
                holder.videoPost.start();
            });

            holder.videoPost.setOnClickListener(v -> {
                if (holder.videoPost.isPlaying()) {
                    holder.videoPost.pause();
                } else {
                    holder.videoPost.start();
                }
            });
        } else {
            holder.videoPost.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.videoPost.setVideoPath("");
        }
        holder.btnComment.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CommentBottomSheetFragment fragment = CommentBottomSheetFragment.newInstance(post.getPostId());
                fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "CommentBottomSheet");
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvCaption;
        ImageView imgPost, btnPlay, avatar, btnComment;
        VideoView videoPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            videoPost = itemView.findViewById(R.id.videoPost);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnComment = itemView.findViewById(R.id.btnComment); // Thêm ánh xạ nút comment
        }
    }
}
