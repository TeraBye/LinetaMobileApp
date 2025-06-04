package com.example.lineta.Adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Message;
import com.example.lineta.Entity.Timestamp;
import com.example.lineta.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages;
    private String currentUserId;
    private String contactName;
    private String contactAvatarUrl;

    public ChatAdapter(List<Message> messages, String currentUserId, String contactName, String contactAvatarUrl) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.contactName = contactName;
        this.contactAvatarUrl = contactAvatarUrl;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView senderName;
        public TextView messageContent;
        public TextView timestamp;
        public LinearLayout messageContainer;
        public LinearLayout messageContentContainer;

        public ChatViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.avatar);
            senderName = view.findViewById(R.id.senderName);
            messageContent = view.findViewById(R.id.messageContent);
            timestamp = view.findViewById(R.id.timestamp);
            messageContainer = view.findViewById(R.id.message_container);
            messageContentContainer = view.findViewById(R.id.message_content_container);
        }
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        String senderId = message.getSenderId();

        boolean isCurrentUser = currentUserId.equals(senderId);

        if (!isCurrentUser) {
            holder.senderName.setText(contactName != null ? contactName : "Unknown");
            if (contactAvatarUrl != null && !contactAvatarUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(contactAvatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.default_avatar);
            }
            holder.avatar.setVisibility(View.VISIBLE);
            holder.senderName.setVisibility(View.VISIBLE);
            holder.messageContainer.setGravity(Gravity.START);
            holder.messageContentContainer.setBackgroundResource(R.drawable.message_background_light_gray);
        } else {
            holder.avatar.setVisibility(View.GONE);
            holder.senderName.setVisibility(View.GONE);
            holder.messageContainer.setGravity(Gravity.END);
            holder.messageContentContainer.setBackgroundResource(R.drawable.message_background_light_blue);
        }

        holder.messageContent.setText(message.getContext());

        Timestamp timestamp = message.getTimestamp();
        if (timestamp != null) {
            holder.timestamp.setText(timestamp.toRelativeTime());
        } else {
            holder.timestamp.setText("Unknown");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}