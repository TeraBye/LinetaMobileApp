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
import com.example.lineta.Entity.Notification;
import com.example.lineta.R;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notifications;
    private final Context context;

    public NotificationAdapter(List<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvFullName, tvContent, tvTime;

        public ViewHolder(View view) {
            super(view);
            imgAvatar = view.findViewById(R.id.imgAvatar);
            tvFullName = view.findViewById(R.id.tvFullName);
            tvContent = view.findViewById(R.id.tvContent);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Notification item = notifications.get(position);

        holder.tvFullName.setText(item.getSenderUsername());
        holder.tvContent.setText(item.getContent());
        holder.tvTime.setText(getPrettyTime(item.getTimestamp().toTimestamp()));


        Glide.with(context)
                .load(item.getProfilePicURL())
                .placeholder(R.drawable.main_logo)
                .circleCrop()
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
    public String getPrettyTime(Timestamp timestamp) {
        Date date = new Date(timestamp.getTime());
        PrettyTime prettyTime = new PrettyTime(Locale.ENGLISH);
        return prettyTime.format(date);
    }

    private String formatTime(String isoDate) {
        // Parse and format if needed
        return isoDate.substring(0, 10); // Hoặc dùng SimpleDateFormat nếu muốn "2 phút trước"
    }
}

