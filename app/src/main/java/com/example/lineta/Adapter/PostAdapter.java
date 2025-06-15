package com.example.lineta.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Like;
import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.User;
import com.example.lineta.Home.interaction.CommentBottomSheetFragment;
import com.example.lineta.Home.profile.AccountFragment;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.PostService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> posts;
    private final Context context;
    private final User currentUser;

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

        // Reset icon like về outline trước khi gọi API
//        holder.iconLike.setImageResource(R.drawable.ic_heart_outline);

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

        // Kiểm tra quyền hiển thị menu (chỉ hiện khi user đăng bài chính là user hiện tại)
        if (post.getUsername().equals(currentUser.getUsername())) {
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.btnMore.setOnClickListener(v -> {
                showOptionsMenu(holder, post, position);
            });
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }

        // CLick avatar
//        holder.avatar.setOnClickListener(v -> {
//            Log.d("PostAdapter", "UserId of post " + post.getUserId());
//            openProfile(post.getUserId()); // hoặc post.getUsername() tuỳ bạn
//        });
//
//        holder.tvUsername.setOnClickListener(v -> {
//            openProfile(post.getUserId()); // hoặc post.getUsername() tuỳ bạn
//        });



    }

    private void openProfile(String userId) {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            AccountFragment accountFragment = AccountFragment.newInstance(userId);
            transaction.replace(R.id.frame_layout, accountFragment);  // frame_layout là ID container chính của bạn
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(context, "Cannot open profile", Toast.LENGTH_SHORT).show();
        }
    }


    private void showOptionsMenu(ViewHolder holder, Post post, int position) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(context, holder.btnMore);
        popupMenu.getMenuInflater().inflate(R.menu.post_option_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Xử lý edit
                Log.d("PostAdapter", "Edit post: " + post.getPostId());
                showEditDialog(post, position);
                // TODO: mở màn hình edit bài viết
                return true;
            } else if (itemId == R.id.menu_delete) {
                // Xử lý xóa
                deletePost(post, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

//    private void showEditDialog(Post post, int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View dialogView = inflater.inflate(R.layout.dialog_edit_post, null);
//        builder.setView(dialogView);
//
//        // Ánh xạ view
//        ShapeableImageView avatar = dialogView.findViewById(R.id.edit_avatar);
//        TextView username = dialogView.findViewById(R.id.edit_username);
//        ImageView imgPost = dialogView.findViewById(R.id.edit_imgPost);
//        PlayerView videoPost = dialogView.findViewById(R.id.edit_videoPost);
//        EditText contentEdit = dialogView.findViewById(R.id.edit_content);
//
//        // Load avatar
//        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
//            Glide.with(context)
//                    .load(post.getProfilePicURL())
//                    .placeholder(R.drawable.main_logo)
//                    .into(avatar);
//        } else {
//            avatar.setImageResource(R.drawable.professor);
//        }
//
//        username.setText(post.getFullName());
//        contentEdit.setText(post.getContent());
//
//        // Load ảnh nếu có
//        if (post.getPicture() != null && !post.getPicture().equalsIgnoreCase("null")) {
//            imgPost.setVisibility(View.VISIBLE);
//            Glide.with(context).load(post.getPicture()).into(imgPost);
//        } else {
//            imgPost.setVisibility(View.GONE);
//        }

    private void showEditDialog(Post post, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_post, null);

        // Ánh xạ các view
        ShapeableImageView avatar = view.findViewById(R.id.edit_avatar);
        TextView username = view.findViewById(R.id.edit_username);
        TextView time = view.findViewById(R.id.edit_time);
        EditText content = view.findViewById(R.id.edit_content);
        ImageView imgPost = view.findViewById(R.id.edit_imgPost);
        PlayerView videoPost = view.findViewById(R.id.edit_videoPost);

        // Set dữ liệu ban đầu
        username.setText(post.getFullName());
        // Giả sử bạn có thuộc tính thời gian nếu chưa có thì bỏ qua dòng dưới
        time.setText(post.getDate());

        content.setText(post.getContent());

        // Load avatar
        if (post.getProfilePicURL() != null && !post.getProfilePicURL().isEmpty()) {
            Glide.with(context)
                    .load(post.getProfilePicURL())
                    .placeholder(R.drawable.main_logo)
                    .into(avatar);
        } else {
            avatar.setImageResource(R.drawable.main_logo);
        }

        // Load ảnh hoặc video
        if (post.getPicture() != null && !post.getPicture().equalsIgnoreCase("null") && !post.getPicture().isEmpty()) {
            imgPost.setVisibility(View.VISIBLE);
            videoPost.setVisibility(View.GONE);
            Glide.with(context)
                    .load(post.getPicture())
                    .placeholder(R.drawable.main_logo)
                    .into(imgPost);
        } else if (post.getVideo() != null && !post.getVideo().equalsIgnoreCase("null") && !post.getVideo().isEmpty()) {
            imgPost.setVisibility(View.GONE);
            videoPost.setVisibility(View.VISIBLE);
            // Khởi tạo player đúng version ExoPlayer
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            videoPost.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(post.getVideo());
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(false);

// Release khi dialog đóng
            builder.setOnDismissListener(dialog -> {
                player.release();
        });
        } else {
            imgPost.setVisibility(View.GONE);
            videoPost.setVisibility(View.GONE);
        }

        builder.setView(view)
                .setTitle("Edit Your Post")
                .setPositiveButton("Submit", (dialog, which) -> {
                    String newContent = content.getText().toString().trim();
                    if (!newContent.isEmpty()) {
                        updatePostContent(post, newContent, position);
                    } else {
                        Toast.makeText(context, "Content can not be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    // Load video nếu có
//        ExoPlayer editPlayer = null;
//        if (post.getVideo() != null && !post.getVideo().equalsIgnoreCase("null")) {
//            videoPost.setVisibility(View.VISIBLE);
//            editPlayer = new ExoPlayer.Builder(context).build();
//            videoPost.setPlayer(editPlayer);
//            MediaItem mediaItem = MediaItem.fromUri(post.getVideo());
//            editPlayer.setMediaItem(mediaItem);
//            editPlayer.prepare();
//            editPlayer.setPlayWhenReady(false);
//        } else {
//            videoPost.setVisibility(View.GONE);
//        }

//        // Set button
//        builder.setPositiveButton("Lưu", (dialog, which) -> {
//            String newContent = contentEdit.getText().toString().trim();
//            if (!newContent.isEmpty()) {
//                updatePostContent(post, newContent, position);
//            }
////            if (editPlayer != null) {
////                editPlayer.release();
////            }
//        });
//
//        builder.setNegativeButton("Huỷ", (dialog, which) -> {
////            if (editPlayer != null) {
////                editPlayer.release();
////            }
//            dialog.dismiss();
//        });
//
//        builder.setOnDismissListener(dialog -> {
////            if (editPlayer != null) {
////                editPlayer.release();
////            }
//        });
//
//        builder.show();
//    }


    private void updatePostContent(Post post, String newContent, int position) {
        PostService postService = ApiClient.getRetrofit().create(PostService.class);

        // Chuẩn bị body theo yêu cầu backend
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", newContent);

        Call<ApiResponse<Void>> call = postService.updatePostContent(post.getPostId(), requestBody);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    post.setContent(newContent); // update local data
                    notifyItemChanged(position); // refresh UI
                    Log.d("PostAdapter", "Post updated successfully");
                } else {
                    Log.e("PostAdapter", "Failed to update: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("PostAdapter", "Update failed: " + t.getMessage());
            }
        });
    }

    private void deletePost(Post post, int position) {
    new AlertDialog.Builder(context)
            .setTitle("Confirm deletion of post")
            .setMessage("Are you sure you want to delete the post?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Gọi API xoá sau khi người dùng xác nhận
                PostService postService = ApiClient.getRetrofit().create(PostService.class);
                Call<ApiResponse<Void>> call = postService.deletePost(post.getPostId());

                call.enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Log.d("PostAdapter", "Post deleted: " + post.getPostId());
                            posts.remove(position);
                            notifyItemRemoved(position);
                        } else {
                            Log.e("PostAdapter", "Failed to delete: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("PostAdapter", "Delete failed: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
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
        ImageView btnMore;


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
            btnMore = itemView.findViewById(R.id.btnMore);

            player = null;
        }
    }


}
