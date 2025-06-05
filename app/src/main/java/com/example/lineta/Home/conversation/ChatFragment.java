package com.example.lineta.Home.conversation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Adapter.ChatAdapter;
import com.example.lineta.Entity.Message;
import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private static final String ARG_CONTACT_NAME = "contactName";
    private static final String ARG_CONTACT_AVATAR = "contactAvatar";
    private static final String ARG_CONVERSATION_ID = "conversationId";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TOKEN = "token";

    private String contactName;
    private String contactAvatarUrl;
    private String conversationId;
    private String userId;
    private String token;

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageInput;
    private TextView contactNameTextView;
    private ImageView contactAvatar;

    public static ChatFragment newInstance(String contactName, String contactAvatarUrl, String conversationId, String userId, String token) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTACT_NAME, contactName);
        args.putString(ARG_CONTACT_AVATAR, contactAvatarUrl);
        args.putString(ARG_CONVERSATION_ID, conversationId);
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactName = getArguments().getString(ARG_CONTACT_NAME);
            contactAvatarUrl = getArguments().getString(ARG_CONTACT_AVATAR);
            conversationId = getArguments().getString(ARG_CONVERSATION_ID);
            userId = getArguments().getString(ARG_USER_ID);
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        messageInput = view.findViewById(R.id.messageInput);
        contactNameTextView = view.findViewById(R.id.contactName);
        contactAvatar = view.findViewById(R.id.contactAvatar);

        // Hiển thị thông tin người chat
        contactNameTextView.setText(contactName != null ? contactName : "Unknown");
        if (contactAvatarUrl != null && !contactAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(contactAvatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(contactAvatar);
        } else {
            contactAvatar.setImageResource(R.drawable.default_avatar);
        }

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, userId, contactName, contactAvatarUrl); // Truyền thông tin người đối diện
        recyclerViewChat.setAdapter(chatAdapter);

        fetchMessages();

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

        return view;
    }

    private void fetchMessages() {
        if (conversationId != null && token != null) {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("conversationId", conversationId);

            ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
            Call<List<Message>> call = apiConversation.getMessages(requestData);
            call.enqueue(new Callback<List<Message>>() {
                @Override
                public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        messages.clear();
                        messages.addAll(response.body());
                        chatAdapter.notifyDataSetChanged();
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    } else {
                        String errorMessage = "Không tải được tin nhắn: " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMessage += " - " + response.errorBody().string();
                            } catch (IOException e) {
                                errorMessage += " - Không thể đọc chi tiết lỗi";
                                e.printStackTrace();
                            }
                        }
                        Log.e("ChatFragment", errorMessage);
                    }
                }

                @Override
                public void onFailure(Call<List<Message>> call, Throwable t) {
                    String errorMessage = "Lỗi kết nối: " + t.getMessage();
                    Log.e("ChatFragment", errorMessage, t);
                }
            });
        } else {
            Log.e("ChatFragment", "Thiếu conversationId hoặc token");
        }
    }

    private void sendMessage(String text) {
    }
}