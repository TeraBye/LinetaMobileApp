package com.example.lineta.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.CommentLike;
import com.example.lineta.Entity.Like;
import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.ReplyComment;
import com.example.lineta.Entity.User;
import com.example.lineta.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lineta.config.WebSocketManager;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.CommentApi;
import com.example.lineta.service.client.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> commentList;
    private Context context;

    private List<ReplyComment> replyList= new ArrayList<>();

    private final HashMap<String, Boolean> likedStates = new HashMap<>();

    private final User currentUser;

    // Dùng Map lưu trạng thái hiển thị reply theo vị trí hoặc commentId
    private final HashMap<String, Boolean> replyVisibilityMap = new HashMap<>();
    // Lưu reply đã load để không gọi lại API nhiều lần
    private final HashMap<String, List<ReplyComment>> loadedRepliesMap = new HashMap<>();

    public CommentAdapter(Context context, User currentUser) {
        this.context = context;
        this.commentList = new ArrayList<>();
        this.currentUser = currentUser;
    }

    public void setComments(List<Comment> comments) {
        this.commentList.clear();
        this.commentList.addAll(comments);
        replyVisibilityMap.clear();
        loadedRepliesMap.clear();
        notifyDataSetChanged();
    }

    public void addComments(List<Comment> comments) {
        int start = this.commentList.size();
        this.commentList.addAll(comments);
        notifyItemRangeInserted(start, comments.size());
    }

    public void addCommentAtTop(Comment comment) {
        this.commentList.add(0, comment);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        String commentId = comment.getCommentId();

        holder.tvFullName.setText(comment.getFullName());
        holder.tvContent.setText(comment.getContent());
        holder.tvCommentDate.setText(comment.getDate());

        Glide.with(context)
                .load(comment.getProfilePicURL())
                .placeholder(R.drawable.main_logo)
                .into(holder.imgAvatar);

        checkLikeStatus(currentUser.getUsername(), commentId, holder.btnLikeComment);

        holder.btnLikeComment.setOnClickListener(v -> {
            boolean currentlyLiked = likedStates.getOrDefault(commentId, false);
            boolean newState = !currentlyLiked;

            likedStates.put(commentId, newState);

            CommentLike likeCmtRequest = CommentLike.builder()
                    .username(currentUser.getUsername())
                    .commentId(commentId)
                    .fullName(currentUser.getFullName())
                    .profilePicURL(currentUser.getProfilePicURL())
                    .tempContent(comment.getContent())
                    .build();

            ApiService likeApi = ApiClient.getRetrofit().create(ApiService.class);
            Call<ApiResponse<Void>> call = likeApi.toggleLikeCmt(likeCmtRequest);

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
            holder.btnLikeComment.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        });


        // Thiết lập trạng thái hiển thị reply
        boolean isReplyVisible = replyVisibilityMap.getOrDefault(commentId, false);
        holder.recyclerReply.setVisibility(isReplyVisible ? View.VISIBLE : View.GONE);
        holder.tvLoadReply.setText(isReplyVisible ? "hide" : "show replies");

        // Nếu reply đã load thì set adapter luôn
        if (loadedRepliesMap.containsKey(commentId)) {
            ReplyCommentAdapter replyAdapter = new ReplyCommentAdapter(loadedRepliesMap.get(commentId), context);
            holder.recyclerReply.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerReply.setAdapter(replyAdapter);
        } else {
            holder.recyclerReply.setAdapter(null);
        }

        holder.tvLoadReply.setOnClickListener(v -> {
            boolean currentlyVisible = replyVisibilityMap.getOrDefault(commentId, false);
            if (currentlyVisible) {
                // Ẩn reply
                replyVisibilityMap.put(commentId, false);
                notifyItemChanged(position);
            } else {
                // Hiện reply
                replyVisibilityMap.put(commentId, true);

                if (loadedRepliesMap.containsKey(commentId)) {
                    // Nếu đã load rồi, chỉ hiển thị
                    notifyItemChanged(position);

                } else {
                    // Gọi API để load reply
                    loadReplyComments(commentId, holder, position);
                    setupWebSocket(commentId, holder, position);

                }
            }
        });

        holder.tvReply.setOnClickListener(v -> {
            if (holder.layoutReplyInput.getVisibility() == View.VISIBLE) {
                // Đang hiển thị -> ẩn đi
                holder.layoutReplyInput.setVisibility(View.GONE);
            } else {
                // Đang ẩn -> hiện lên và focus
                holder.layoutReplyInput.setVisibility(View.VISIBLE);
                holder.tvReplyingTo.setText("Đang phản hồi " + holder.tvFullName.getText().toString() + "...");
                holder.inputCommentRep.requestFocus();

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(holder.inputCommentRep, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        holder.btnSendRep.setOnClickListener(v -> {
            String replyText = holder.inputCommentRep.getText().toString().trim();

            if (!replyText.isEmpty()) {
                ReplyComment replyCommentRequest = ReplyComment.builder()
                        .commentId(commentId)
                        .content(replyText)
                        .username(currentUser.getUsername())
                        .fullName(currentUser.getFullName())
                        .profilePicURL(currentUser.getProfilePicURL())
                        .build();
                sendCommentRepToApi(replyCommentRequest);
                holder.inputCommentRep.setText("");

                // Sau khi gửi -> ẩn input + xóa nội dung
                holder.layoutReplyInput.setVisibility(View.GONE);
                holder.inputCommentRep.setText("");

                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(holder.inputCommentRep.getWindowToken(), 0);
                }
            }
        });





    }

    private void sendCommentRepToApi(ReplyComment comment) {
        ApiService api = ApiClient.getRetrofit().create(ApiService.class);
        api.createCommentRep(comment).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("COMMENT", "Gửi bình luận thành công");
                } else {
                    Log.e("COMMENT", "Gửi thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("COMMENT", "Lỗi: " + t.getMessage());
            }
        });
    }

    private void loadReplyComments(String commentId, ViewHolder holder, int position) {
        CommentApi apiService = ApiClient.getRetrofit().create(CommentApi.class);
        apiService.getReplyComments(commentId, 1, 5).enqueue(new Callback<ApiResponse<List<ReplyComment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReplyComment>>> call, Response<ApiResponse<List<ReplyComment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    replyList = response.body().getResult();  // ✅ Lấy từ ApiResponse
                    loadedRepliesMap.put(commentId, replyList);

                    ReplyCommentAdapter replyAdapter = new ReplyCommentAdapter(replyList, context);
                    holder.recyclerReply.setLayoutManager(new LinearLayoutManager(context));
                    holder.recyclerReply.setAdapter(replyAdapter);

                    notifyItemChanged(position);
                } else {
                    Log.e("API_ERROR", "Phản hồi không hợp lệ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ReplyComment>>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi khi gọi API Retrofit: " + t.getMessage());
            }
        });
    }



    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private void checkLikeStatus(String username, String commentId, ImageView btnLikeComment) {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.checkIfLikedCmt(username, commentId).enqueue(new Callback<ApiResponse<Map<String, Boolean>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Boolean>>> call, Response<ApiResponse<Map<String, Boolean>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean liked = response.body().getResult().get("liked");
                    boolean isLiked = liked != null && liked;

                    likedStates.put(commentId, isLiked);
                    btnLikeComment.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
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

    private WebSocketManager commentRepSocketManager;

    private void setupWebSocket(String commentId, ViewHolder holder, int position) {
        if (commentRepSocketManager != null) {
            return; // Đã kết nối rồi
        }

        String url = "ws://localhost:9000/ws/websocket";
        String topic = "/topic/reply/" + commentId;

        commentRepSocketManager = new WebSocketManager(url);
        commentRepSocketManager.setListener(jsonObject -> {
            try {
                ReplyComment reply = ReplyComment.builder()
                        .commentId(jsonObject.getString("commentId"))
                        .username(jsonObject.getString("username"))
                        .content(jsonObject.getString("content"))
                        .profilePicURL(jsonObject.getString("profilePicURL"))
                        .fullName(jsonObject.getString("fullName"))
                        .build();
                // Thêm vào danh sách
                List<ReplyComment> replies = loadedRepliesMap.getOrDefault(commentId, new ArrayList<>());
                replies.add(reply);
                loadedRepliesMap.put(commentId, replies);
                new Handler(Looper.getMainLooper()).post(() -> {
                    // cập nhật giao diện ở đây
                    notifyItemChanged(position); // ví dụ
                });

                // Cập nhật RecyclerView
                ((ViewHolder) holder).recyclerReply.post(() -> {
                    ReplyCommentAdapter adapter = new ReplyCommentAdapter(replies, context);
                    holder.recyclerReply.setLayoutManager(new LinearLayoutManager(context));
                    holder.recyclerReply.setAdapter(adapter);
                });

            } catch (JSONException e) {
                Log.e("WebSocket", "Error parsing reply comment: " + e.getMessage());
            }
        });

        commentRepSocketManager.connect(() -> {
            commentRepSocketManager.subscribe(topic);
        });
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvContent, tvLoadReply, tvCommentDate, tvReply, tvReplyingTo;
        ImageView imgAvatar, btnLikeComment;
        RecyclerView recyclerReply;

        EditText inputCommentRep;
        ImageView btnSend, btnSendRep;

        LinearLayout layoutReplyInput;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            recyclerReply = itemView.findViewById(R.id.recyclerReply);
            tvLoadReply = itemView.findViewById(R.id.tvLoadReply);

            btnSend = itemView.findViewById(R.id.btnSend);
            btnLikeComment = itemView.findViewById(R.id.btnLikeComment);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvReply = itemView.findViewById(R.id.tvReply);

            inputCommentRep = itemView.findViewById(R.id.inputCommentRep);
            btnSendRep = itemView.findViewById(R.id.btnSendRep);
            layoutReplyInput =  itemView.findViewById(R.id.layoutReplyInput);
            tvReplyingTo = itemView.findViewById(R.id.tvReplyingTo);
        }
    }
}


