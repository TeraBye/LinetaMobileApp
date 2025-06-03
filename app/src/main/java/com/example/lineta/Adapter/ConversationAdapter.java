package com.example.lineta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Entity.Conversation;
import com.example.lineta.R;

import java.util.List;

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
        holder.name.setText(conversation.getName(userId)); // Hiển thị tên người dùng khác
        holder.lastMessage.setText(conversation.getLastMessage());
        holder.time.setText(conversation.getLastUpdate());

        int unreadCount = conversation.getUnreadCountForUser(userId);
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