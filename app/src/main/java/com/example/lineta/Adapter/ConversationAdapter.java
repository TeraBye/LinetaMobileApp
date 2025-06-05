package com.example.lineta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Conversation;
import com.example.lineta.Entity.Timestamp;
import com.example.lineta.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private String userId;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String userId, Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnItemClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        notifyDataSetChanged();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView name;
        public TextView lastMessage;
        public TextView time;
        public TextView unreadCount;

        public ConversationViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            lastMessage = view.findViewById(R.id.lastMessage);
            time = view.findViewById(R.id.time);
            unreadCount = view.findViewById(R.id.unreadCount);
        }
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.name.setText(conversation.getName(userId));
        String avatarUrl = conversation.getAvatarUrl(userId);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.default_avatar);
        }

        holder.lastMessage.setText(conversation.getLastMessage());
        Long lastUpdate = conversation.getLastUpdate();
        if (lastUpdate != null) {
            long diffMillis = System.currentTimeMillis() - lastUpdate;
            long diffSeconds = diffMillis / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;
            long diffWeeks = diffDays / 7;

            if (diffSeconds < 60) {
                holder.time.setText(diffSeconds <= 1 ? "few seconds ago" : diffSeconds + " seconds ago");
            } else if (diffMinutes < 60) {
                holder.time.setText(diffMinutes == 1 ? "1 minute ago" : diffMinutes + " minutes ago");
            } else if (diffHours < 24) {
                holder.time.setText(diffHours == 1 ? "1 hour ago" : diffHours + " hours ago");
            } else if (diffDays < 7) {
                holder.time.setText(diffDays == 1 ? "1 day ago" : diffDays + " days ago");
            } else {
                holder.time.setText(diffWeeks == 1 ? "1 week ago" : diffWeeks + " weeks ago");
            }
        } else {
            holder.time.setText("Unknown");
        }

        long unreadCount = conversation.getUnreadCountForUser(userId);
        if (unreadCount > 0) {
            holder.unreadCount.setText(String.valueOf(unreadCount));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(userId, conversation));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }
}