package com.example.lineta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Entity.Message;
import com.example.lineta.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String userId;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView timeText;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.messageText);
            timeText = view.findViewById(R.id.timeText);
        }
    }

    public ChatAdapter(List<Message> messages, String userId) {
        this.messages = messages;
        this.userId = userId;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getContext());
        holder.timeText.setText(message.getTimestamp());

        // Hiển thị ảnh (nếu có) - hiện tại chỉ hiển thị nội dung văn bản
        if (message.getMedia() != null && !message.getMedia().isEmpty()) {
            holder.messageText.append("\n[Image]");
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (message.getSender().equals(userId)) {
            params.setMargins(64, 0, 16, 0);
            holder.messageText.setBackgroundResource(R.drawable.bg_sent_message);
        } else {
            params.setMargins(16, 0, 64, 0);
            holder.messageText.setBackgroundResource(R.drawable.bg_received_message);
        }
        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}