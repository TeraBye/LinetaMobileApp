package com.example.lineta.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.ReplyComment;
import com.example.lineta.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lineta.dto.response.ApiResponse;
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

    // Dùng Map lưu trạng thái hiển thị reply theo vị trí hoặc commentId
    private final HashMap<String, Boolean> replyVisibilityMap = new HashMap<>();
    // Lưu reply đã load để không gọi lại API nhiều lần
    private final HashMap<String, List<ReplyComment>> loadedRepliesMap = new HashMap<>();

    public CommentAdapter(Context context) {
        this.context = context;
        this.commentList = new ArrayList<>();
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

        Glide.with(context)
                .load(comment.getProfilePicURL())
                .placeholder(R.drawable.main_logo)
                .into(holder.imgAvatar);

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
                }
            }
        });
    }

    private void loadReplyComments(String commentId, ViewHolder holder, int position) {
        CommentApi apiService = ApiClient.getRetrofit().create(CommentApi.class);
        apiService.getReplyComments(commentId, 1, 5).enqueue(new Callback<ApiResponse<List<ReplyComment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReplyComment>>> call, Response<ApiResponse<List<ReplyComment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReplyComment> replyList = response.body().getResult();  // ✅ Lấy từ ApiResponse
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvContent, tvLoadReply;
        ImageView imgAvatar;
        RecyclerView recyclerReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            recyclerReply = itemView.findViewById(R.id.recyclerReply);
            tvLoadReply = itemView.findViewById(R.id.tvLoadReply);
        }
    }
}


