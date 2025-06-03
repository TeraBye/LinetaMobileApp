package com.example.lineta.Home.conversation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.ChatAdapter;
import com.example.lineta.Entity.Message;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.client.ApiClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageInput;
    private TextView contactName;
    private String conversationId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        messageInput = findViewById(R.id.messageInput);
        contactName = findViewById(R.id.contactName);

        String contactNameText = getIntent().getStringExtra("CONTACT_NAME");
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        userId = getIntent().getStringExtra("USER_ID");
        if (!TextUtils.isEmpty(contactNameText)) {
            contactName.setText(contactNameText);
        }

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, userId);
        recyclerViewChat.setAdapter(chatAdapter);

        // Lấy tin nhắn từ API
        fetchMessages();

        // Xử lý gửi tin nhắn
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String messageText = messageInput.getText().toString().trim();
                if (!TextUtils.isEmpty(messageText)) {
                    sendMessage(messageText);
                    messageInput.setText("");
                }
                return true;
            }
            return false;
        });
    }

    private void fetchMessages() {
        if (conversationId != null) {
            ApiConversation apiService = ApiClient.getRetrofit().create(ApiConversation.class);
            Call<List<Message>> call = apiService.getMessages(conversationId);
            call.enqueue(new Callback<List<Message>>() {
                @Override
                public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        messages.clear();
                        messages.addAll(response.body());
                        chatAdapter.notifyDataSetChanged();
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    } else {
                        Toast.makeText(ChatActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Message>> call, Throwable t) {
                    Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendMessage(String text) {
        Toast.makeText(ChatActivity.this, "Chưa tích hợp API gửi tin nhắn", Toast.LENGTH_SHORT).show();
    }
}