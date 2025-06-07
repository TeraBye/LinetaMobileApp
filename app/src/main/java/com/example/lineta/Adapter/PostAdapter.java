package com.example.lineta.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Post;
import com.example.lineta.Home.interaction.CommentBottomSheetFragment;
import com.example.lineta.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> posts;
    private final Context context;

    // Giới hạn số dòng caption khi chưa mở rộng
    private static final int MAX_LINES_COLLAPSED = 3;

    // Lưu trạng thái mở rộng caption cho từng item
    private final SparseBooleanArray expandedPositions = new SparseBooleanArray();

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.tvUsername.setText(post.getFullName());
        holder.tvCaption.setText(post.getContent());

        // Kiểm tra trạng thái mở rộng của item hiện tại
        boolean isExpanded = expandedPositions.get(position, false);

        if (isExpanded) {
            holder.tvCaption.setMaxLines(Integer.MAX_VALUE);
            holder.tvCaption.setEllipsize(null);
        } else {
            holder.tvCaption.setMaxLines(MAX_LINES_COLLAPSED);
            holder.tvCaption.setEllipsize(TextUtils.TruncateAt.END);
        }

        // Gán click listener cho caption
        holder.tvCaption.setOnClickListener(v -> {
            boolean currentlyExpanded = expandedPositions.get(position, false);
            if (currentlyExpanded) {
                // Thu gọn
                holder.tvCaption.setMaxLines(MAX_LINES_COLLAPSED);
                holder.tvCaption.setEllipsize(TextUtils.TruncateAt.END);
                expandedPositions.put(position, false);
            } else {
                // Mở rộng
                holder.tvCaption.setMaxLines(Integer.MAX_VALUE);
                holder.tvCaption.setEllipsize(null);
                expandedPositions.put(position, true);
            }
        });

        // Ẩn trước
        holder.imgPost.setVisibility(View.GONE);
        holder.playerView.setVisibility(View.GONE);

        // Avatar
        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
            holder.avatar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getProfilePicURL())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.avatar);
        } else {
            holder.avatar.setVisibility(View.GONE);
        }

        // Ảnh bài viết
        if (post.getPicture() != null && !post.getPicture().equalsIgnoreCase("null")) {
            holder.imgPost.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getPicture())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.imgPost);
        }

        // Video bài viết
        String video = post.getVideo();
        if (video != null && !video.trim().isEmpty() && !video.equalsIgnoreCase("null")) {
            holder.imgPost.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.VISIBLE);

            if (holder.player == null) {
                holder.player = new ExoPlayer.Builder(context).build();
                holder.playerView.setPlayer(holder.player);
            } else {
                holder.player.stop();
                holder.player.clearMediaItems();
            }

            MediaItem mediaItem = MediaItem.fromUri(video);
            holder.player.setMediaItem(mediaItem);
            holder.player.prepare();
            holder.player.setPlayWhenReady(false);
        } else {
            holder.playerView.setVisibility(View.GONE);
            if (holder.player != null) {
                holder.player.release();
                holder.player = null;
            }
        }

        // Xử lý nút comment
        holder.btnComment.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CommentBottomSheetFragment fragment = CommentBottomSheetFragment.newInstance(post.getPostId());
                fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "CommentBottomSheet");
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView tvUsername, tvCaption, btnComment;
        ImageView imgPost;
        PlayerView playerView;
        ExoPlayer player;

        // Biến trạng thái caption đã mở rộng hay chưa
        boolean isCaptionExpanded = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            playerView = itemView.findViewById(R.id.videoPost);
            btnComment = itemView.findViewById(R.id.btnComment);
            player = null;
        }
    }
}
