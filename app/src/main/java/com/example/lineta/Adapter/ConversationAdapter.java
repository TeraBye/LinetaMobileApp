package com.example.lineta.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Entity.Conversation.UserInfo;
import com.example.lineta.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Map<String, Object>> conversations;
    private OnConversationClickListener clickListener;
    private Context context;
    private String userId; // Thêm userId để lọc contact

    public ConversationAdapter(List<Map<String, Object>> conversations, OnConversationClickListener clickListener, String userId) {
        this.conversations = conversations;
        this.clickListener = clickListener;
        this.userId = userId; // Nhận userId từ constructor
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView lastMessage;
        public TextView unreadCount;
        public TextView lastUpdate; // Thêm TextView cho lastUpdate
        public ImageView contactAvatar; // Thêm ImageView cho avatar

        public ConversationViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.name);
            lastMessage = view.findViewById(R.id.lastMessage);
            unreadCount = view.findViewById(R.id.unreadCount);
            lastUpdate = view.findViewById(R.id.last_update_text); // ID khớp với layout
            contactAvatar = view.findViewById(R.id.contact_avatar); // ID khớp với layout
        }
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Map<String, Object> conversation = conversations.get(position);
        String lastMessage = (String) conversation.get("lastMessage");
        String contactName = getContactName(conversation);
        String contactAvatarUrl = getContactAvatarUrl(conversation);
        Long lastUpdate = (Long) conversation.get("lastUpdate");

        // Thiết lập tên liên hệ
        holder.contactName.setText(contactName);

        // Thiết lập lastMessage
        holder.lastMessage.setText(lastMessage != null ? lastMessage : "");

        // Cập nhật số tin chưa đọc
        Map<String, Long> unreadCountMap = (Map<String, Long>) conversation.get("unreadCount");
        long unread = unreadCountMap != null ? unreadCountMap.getOrDefault(userId, 0L) : 0L;
        if (unread > 0) {
            holder.unreadCount.setText(String.valueOf(unread));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        // Tô đậm tên và lastMessage nếu có tin nhắn chưa đọc
        if (unread > 0) {
            holder.contactName.setTypeface(null, Typeface.BOLD);
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.contactName.setTypeface(null, Typeface.NORMAL);
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
        }

        // Hiển thị lastUpdate với định dạng thời gian
        if (lastUpdate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy");
            holder.lastUpdate.setText(sdf.format(new java.util.Date(lastUpdate)));
        } else {
            holder.lastUpdate.setText("N/A");
        }

        // Load avatar
        if (contactAvatarUrl != null) {
            Glide.with(context).load(contactAvatarUrl).into(holder.contactAvatar);
        } else {
            holder.contactAvatar.setImageResource(R.drawable.default_avatar); // Cần thêm default_avatar
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    private String getContactName(Map<String, Object> conversation) {
        List<UserInfo> users = (List<UserInfo>) conversation.get("users");
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(userId)) { // Lọc contact không phải user hiện tại
                    String name = user.getFullName() != null ? user.getFullName() : user.getUsername();
                    Log.d("ConversationAdapter", "Contact name for convId " + conversation.get("conversationId") + ": " + name);
                    return name;
                }
            }
        }
        Log.w("ConversationAdapter", "No contact name found for convId " + conversation.get("conversationId"));
        return "Unknown"; // Giá trị mặc định
    }

    private String getContactAvatarUrl(Map<String, Object> conversation) {
        List<UserInfo> users = (List<UserInfo>) conversation.get("users");
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(userId)) { // Lọc contact không phải user hiện tại
                    String avatarUrl = user.getAvatarUrl();
                    if (avatarUrl != null) {
                        Log.d("ConversationAdapter", "Contact avatarUrl for convId " + conversation.get("conversationId") + ": " + avatarUrl);
                        return avatarUrl;
                    }
                }
            }
        }
        Log.w("ConversationAdapter", "No contact avatarUrl found for convId " + conversation.get("conversationId"));
        return null;
    }

    public interface OnConversationClickListener {
        void onConversationClick(Map<String, Object> conversation);
    }
}