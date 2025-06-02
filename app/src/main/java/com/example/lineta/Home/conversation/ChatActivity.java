package com.example.lineta.Home.conversation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lineta.Adapter.ChatAdapter;
import com.example.lineta.Entity.Message;
import com.example.lineta.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageInput;
    private TextView contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Khởi tạo các thành phần
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        messageInput = findViewById(R.id.messageInput);
        contactName = findViewById(R.id.contactName);

        // Lấy tên liên hệ từ Intent
        String contactNameText = getIntent().getStringExtra("CONTACT_NAME");
        if (!TextUtils.isEmpty(contactNameText)) {
            contactName.setText(contactNameText);
        }

        // Khởi tạo RecyclerView
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        recyclerViewChat.setAdapter(chatAdapter);

        // Dữ liệu mẫu
        messages.add(new Message("Xin chào!", "12:00 PM", true));
        messages.add(new Message("Chào bạn!", "12:01 PM", false));
        chatAdapter.notifyDataSetChanged();

        // Xử lý gửi tin nhắn
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String messageText = messageInput.getText().toString().trim();
                if (!TextUtils.isEmpty(messageText)) {
                    messages.add(new Message(messageText, "12:02 PM", true));
                    chatAdapter.notifyDataSetChanged();
                    messageInput.setText("");
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                }
                return true;
            }
            return false;
        });
    }
}
