package com.example.lineta.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Like;
import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.User;
import com.example.lineta.Home.interaction.CommentBottomSheetFragment;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> posts;
    private final Context context;
    @Setter
    private  User currentUser;

    private static final int MAX_LINES_COLLAPSED = 5;

    private final HashMap<String, Boolean> expandedStates = new HashMap<>();
    private final HashMap<String, Boolean> likedStates = new HashMap<>();

    public PostAdapter(Context context, List<Post> posts, User currentUser) {
        this.context = context;
        this.posts = posts;
        this.currentUser = currentUser;
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
        String postId = post.getPostId();

        holder.tvUsername.setText(post.getFullName());
        holder.tvCaption.setText(post.getContent());

        boolean isExpanded = expandedStates.getOrDefault(postId, false);
        holder.tvCaption.setMaxLines(isExpanded ? Integer.MAX_VALUE : MAX_LINES_COLLAPSED);
        holder.tvCaption.setEllipsize(isExpanded ? null : TextUtils.TruncateAt.END);

        holder.tvCaption.setOnClickListener(v -> {
            expandedStates.put(postId, !isExpanded);
            notifyItemChanged(position);
        });

        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
            holder.avatar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getProfilePicURL())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.avatar);
        } else {
            holder.avatar.setVisibility(View.GONE);
        }

        if (post.getPicture() != null && !post.getPicture().equalsIgnoreCase("null")) {
            holder.imgPost.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getPicture())
                    .placeholder(R.drawable.main_logo)
                    .into(holder.imgPost);
        } else {
            holder.imgPost.setVisibility(View.GONE);
        }

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
                holder.playerView.setPlayer(null);
                holder.player = null;
            }
        }

        holder.btnComment.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CommentBottomSheetFragment fragment = CommentBottomSheetFragment.newInstance(postId);
                fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "CommentBottomSheet");
            }
        });


        // Gọi API check trạng thái like
        checkLikeStatus(currentUser.getUsername(), postId, holder.iconLike);

        // Chuyển đổi icon khi nhấn, không gọi API
        holder.iconLike.setOnClickListener(v -> {
            boolean currentlyLiked = likedStates.getOrDefault(postId, false);
            boolean newState = !currentlyLiked;

            likedStates.put(postId, newState);

            Like likeRequest = Like.builder()
                    .username(currentUser.getUsername())
                    .postID(postId)
                    .fullName(currentUser.getFullName())
                    .profilePicURL(currentUser.getProfilePicURL())
                    .tempContent(post.getContent())
                    .build();

            ApiService likeApi = ApiClient.getRetrofit().create(ApiService.class);
            Call<ApiResponse<Void>> call = likeApi.toggleLike(likeRequest);

            call.enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        Log.d("LIKE", "Success: " + message);
                    } else {
                        Log.e("LIKE", "Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Log.e("LIKE", "Failed: " + t.getMessage());
                }
            });
            holder.iconLike.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            holder.player.release();
            holder.playerView.setPlayer(null);
            holder.player = null;
        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void checkLikeStatus(String username, String postId, ImageView iconLike) {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.checkIfLiked(username, postId).enqueue(new Callback<ApiResponse<Map<String, Boolean>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Boolean>>> call, Response<ApiResponse<Map<String, Boolean>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean liked = response.body().getResult().get("liked");
                    boolean isLiked = liked != null && liked;

                    likedStates.put(postId, isLiked);
                    iconLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                } else {
                    Log.e("checkLike", "Invalid response");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Boolean>>> call, Throwable t) {
                Log.e("checkLike", "API failed: " + t.getMessage());
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView tvUsername, tvCaption, btnComment, tvLike;
        ImageView imgPost, iconLike;
        PlayerView playerView;
        ExoPlayer player;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            playerView = itemView.findViewById(R.id.videoPost);
            btnComment = itemView.findViewById(R.id.btnComment);
            iconLike = itemView.findViewById(R.id.iconLike);
            tvLike = itemView.findViewById(R.id.tvLike);
            player = null;
        }
    }


}
