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
import com.example.lineta.Entity.ReplyComment;
import com.example.lineta.R;

import java.util.List;

public class ReplyCommentAdapter extends RecyclerView.Adapter<ReplyCommentAdapter.ViewHolder> {
    private List<ReplyComment> replyList;
    private Context context;

    public ReplyCommentAdapter(List<ReplyComment> replyList, Context context) {
        this.replyList = replyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reply_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReplyComment reply = replyList.get(position);
        holder.tvFullName.setText(reply.getFullName());
        holder.tvContent.setText(reply.getContent());

        Glide.with(context)
                .load(reply.getProfilePicURL())
                .placeholder(R.drawable.main_logo)
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvContent;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullNameReply);
            tvContent = itemView.findViewById(R.id.tvReplyContent);
            imgAvatar = itemView.findViewById(R.id.imgAvatarReply);
        }
    }
}

