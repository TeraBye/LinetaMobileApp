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
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSearchResponse;
import com.example.lineta.service.SearchService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    List<UserSearchResponse> userSearchResponses;
    List<UserSearchResponse> searchHistory;
    Context context;
    boolean isHistoryMode = false;
    OnHistoryItemClickListener historyItemClickListener;
    OnItemClickListener itemClickListener;
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String query);
    }
    public interface OnItemClickListener {
        void onItemClick(String userId);
    }
    String searcherId;

    public UserSearchAdapter(List<UserSearchResponse> userSearchResponses, Context context, String searcherId) {
        this.userSearchResponses = userSearchResponses;
        this.context = context;
        this.searchHistory = new ArrayList<>();
        this.searcherId = searcherId;
    }

    public UserSearchAdapter() {
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.historyItemClickListener = listener;
    }

    public void setUserSearchResponses(List<UserSearchResponse> userSearchResponses) {
        this.userSearchResponses = userSearchResponses;
        this.isHistoryMode = false;
        notifyDataSetChanged();
    }

    public void setSearchHistory(List<UserSearchResponse> searchHistory) {
        this.searchHistory = searchHistory;
        this.isHistoryMode = true;
        notifyDataSetChanged();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isHistoryMode) {
            UserSearchResponse historyItem = searchHistory.get(position);
            holder.tvUsername.setText(historyItem.getUsername());
            holder.tvFullname.setText(historyItem.getFullName());
            holder.tvFullname.setVisibility(View.VISIBLE);
            holder.profilePic.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(historyItem.getProfilePicURL())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.profilePic);

            // Hiển thị nút xóa cho lịch sử
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                deleteHistoryItem(historyItem.getUid(), position);
            });

            // Nhấn vào item lịch sử để tìm kiếm lại
            holder.itemView.setOnClickListener(v -> {
                if (historyItemClickListener != null) {
                    historyItemClickListener.onHistoryItemClick(historyItem.getUsername());
                }
            });
        } else {
            UserSearchResponse userSearchResponse = userSearchResponses.get(position);
            holder.tvUsername.setText(userSearchResponse.getUsername());
            holder.tvFullname.setText(userSearchResponse.getFullName());
            holder.tvFullname.setVisibility(View.VISIBLE);
            holder.profilePic.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            Glide.with(context)
                    .load(userSearchResponse.getProfilePicURL())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.profilePic);

            // Thêm lịch sử khi nhấn vào kết quả tìm kiếm
            holder.itemView.setOnClickListener(v -> {
                addSearchHistory(userSearchResponse.getUid());
            });

            if (itemClickListener != null) {
                itemClickListener.onItemClick(userSearchResponse.getUid());
            }
        }
    }

    private void addSearchHistory(String targetUserId) {
        SearchService searchService = ApiClient.getRetrofit().create(SearchService.class);
        Call<ApiResponse<Void>> call = searchService.addSearchHistory(searcherId, targetUserId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Xử lý thành công nếu cần
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void deleteHistoryItem(String targetUserId, int position) {
        SearchService searchService = ApiClient.getRetrofit().create(SearchService.class);
        Call<ApiResponse<Void>> call = searchService.deleteSearchHistory(searcherId, targetUserId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    searchHistory.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, searchHistory.size());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (isHistoryMode) {
            return searchHistory != null ? searchHistory.size() : 0;
        } else {
            return userSearchResponses != null ? userSearchResponses.size() : 0;
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profilePic;
        TextView tvUsername, tvFullname;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullname = itemView.findViewById(R.id.tvFullName);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
