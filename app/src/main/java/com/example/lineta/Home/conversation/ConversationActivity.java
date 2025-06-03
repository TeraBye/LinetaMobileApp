package com.example.lineta.Home.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.ConversationAdapter;
import com.example.lineta.Entity.Conversation;
import com.example.lineta.Entity.Message;
import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        recyclerViewConversations = findViewById(R.id.recyclerViewConversations);
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(this));
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, userId -> conversation -> {
            Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
            intent.putExtra("CONTACT_NAME", conversation.getName(userId));
            intent.putExtra("CONVERSATION_ID", conversation.getId());
            intent.putExtra("USER_ID", userId); // Truyền userId sang ChatActivity
            startActivity(intent);
        });
        recyclerViewConversations.setAdapter(conversationAdapter);

        // Lấy userId (giả sử từ SharedPreferences hoặc nguồn xác thực)
        userId = "uidA"; // Thay bằng userId thực tế

        // Gọi API để lấy danh sách cuộc trò chuyện
        fetchConversations(userId);
    }

    private void fetchConversations(String userId) {
        ApiConversation apiService = ApiClient.getRetrofit().create(ApiConversation.class);
        Call<List<Conversation>> call = apiService.getConversations(userId);
        call.enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    conversations.clear();
                    conversations.addAll(response.body());
                    conversationAdapter.setUserId(userId); // Cập nhật userId cho adapter
                    conversationAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ConversationActivity.this, "Không tải được cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                Toast.makeText(ConversationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}