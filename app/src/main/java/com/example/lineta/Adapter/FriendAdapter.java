package com.example.lineta.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.R;
import com.example.lineta.dto.response.UserFollowResponse;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static int TYPE_USER = 1;
    static int TYPE_LOADING = 2;
    List<UserFollowResponse> users; // dùng lại UserFollowResponse vì friend tương tự
    Context context;
    OnItemClickListener itemClickListener;
    boolean isLoading = false;

    @Override
    public int getItemViewType(int position) {
        return users.get(position) == null ? TYPE_LOADING : TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_follow_list, parent, false);
            return new FriendViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FriendViewHolder) {
            UserFollowResponse user = users.get(position);
            FriendViewHolder userHolder = (FriendViewHolder) holder;
            userHolder.tvUsername.setText(user.getUsername());
            userHolder.tvFullName.setText(user.getFullName());
            Glide.with(context)
                    .load(user.getProfilePicURL())
                    .placeholder(R.drawable.default_avatar)
                    .into(userHolder.profilePic);

            userHolder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(user.getUid());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profilePic;
        TextView tvUsername, tvFullName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String userId);
    }

    public FriendAdapter(List<UserFollowResponse> users, Context context) {
        this.users = users;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setUsers(List<UserFollowResponse> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void addUsers(List<UserFollowResponse> newUsers) {
        int startPosition = users.size();
        users.addAll(newUsers);
        notifyItemRangeInserted(startPosition, newUsers.size());
    }

    public void clearUsers() {
        users.clear();
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        if (isLoading != loading) {
            isLoading = loading;
            if (loading) {
                users.add(null); // Thêm item loading
                notifyItemInserted(users.size() - 1);
            } else if (!users.isEmpty() && users.get(users.size() - 1) == null) {
                users.remove(users.size() - 1); // Xóa item loading
                notifyItemRemoved(users.size());
            }
        }
    }
}
