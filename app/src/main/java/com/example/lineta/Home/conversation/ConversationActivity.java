package com.example.lineta.Home.conversation;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.ConversationAdapter;
import com.example.lineta.Entity.Conversation;
import com.example.lineta.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Khởi tạo RecyclerView
        recyclerViewConversations = findViewById(R.id.recyclerViewConversations);
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(this));
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, new ConversationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Conversation conversation) {
                Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
                intent.putExtra("CONTACT_NAME", conversation.getName());
                startActivity(intent);
            }
        });
        recyclerViewConversations.setAdapter(conversationAdapter);

        // Thêm dữ liệu mẫu
        conversations.add(new Conversation("Nguyen Van A", "Xin chào!", "12:00 PM", 3));
        conversations.add(new Conversation("Tran Thi B", "Bạn khỏe không?", "11:30 AM", 0));
        conversationAdapter.notifyDataSetChanged();
    }
}