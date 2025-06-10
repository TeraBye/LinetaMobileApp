package com.example.lineta.Home.conversation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lineta.Adapter.ChatAdapter;
import com.example.lineta.Entity.Conversation.FileUtil;
import com.example.lineta.Entity.Conversation.Message;
import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.service.client.WebSocketService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private ImageView attachImageButton;
    private ImageView sendButton;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> pickImageLauncher;

    private WebSocketService webSocketService;
    private boolean isServiceBound;
    private String listUserString;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;

            // Lắng nghe cập nhật real-time với interface mới
            webSocketService.setOnConversationUpdateListener((convId, unreadCount, lastUpdate) -> {
                if (convId.equals(conversationId)) {
                    getActivity().runOnUiThread(() -> fetchMessages());
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    public static ChatFragment newInstance(String contactName, String contactAvatarUrl, String conversationId, String userId, String token, String listUserString) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTACT_NAME, contactName);
        args.putString(ARG_CONTACT_AVATAR, contactAvatarUrl);
        args.putString(ARG_CONVERSATION_ID, conversationId);
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TOKEN, token);
        args.putString("listUserString", listUserString); // Thêm listUserString
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
            listUserString = getArguments().getString("listUserString", "");
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Log.d("ChatFragment", "Image selected: " + uri.toString());
                    }
                }
        );

        // Kết nối với WebSocketService
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().startService(intent);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        messageInput = view.findViewById(R.id.messageInput);
        contactNameTextView = view.findViewById(R.id.contactName);
        contactAvatar = view.findViewById(R.id.contactAvatar);
        attachImageButton = view.findViewById(R.id.attach_image_button);
        sendButton = view.findViewById(R.id.send_button);

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
        chatAdapter = new ChatAdapter(messages, userId, contactName, contactAvatarUrl);
        recyclerViewChat.setAdapter(chatAdapter);

        fetchMessages();

        attachImageButton.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pickImageLauncher.launch("image/*");
            } else {
                pickImageLauncher.launch("image/*");
            }
        });

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText) || selectedImageUri != null) {
                sendMessage(messageText);
                messageInput.setText("");
                selectedImageUri = null;
            }
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
                        Log.e("ChatFragment", "Không tải được tin nhắn: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<Message>> call, Throwable t) {
                    Log.e("ChatFragment", "Lỗi kết nối: " + t.getMessage());
                }
            });
        }
    }

    private void sendMessage(String context) {
        if (!listUserString.isEmpty()) {
            List<String> listUser = Arrays.asList(listUserString.split(","));
            Map<String, RequestBody> params = new HashMap<>();
            params.put("list-user", toRequestBody(listUserString));
            params.put("context", toRequestBody(context));
            params.put("userId", toRequestBody(userId));

            List<MultipartBody.Part> mediaParts = new ArrayList<>();
            if (selectedImageUri != null) {
                try {
                    File file = FileUtil.from(requireContext(), selectedImageUri);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    MultipartBody.Part mediaPart = MultipartBody.Part.createFormData("listMediaFile", file.getName(), requestFile);
                    mediaParts.add(mediaPart);
                } catch (IOException e) {
                    Log.e("ChatFragment", "Lỗi khi xử lý ảnh: " + e.getMessage());
                    return;
                }
            }

            ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
            Call<Map<String, Object>> call = apiConversation.sendMessage(
                    params,
                    mediaParts.toArray(new MultipartBody.Part[0])
            );
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("ChatFragment", "Gửi tin nhắn thành công: " + response.body());
                        fetchMessages();
                    } else {
                        Log.e("ChatFragment", "Gửi tin nhắn thất bại: " + response.code() + ", Message: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("ChatFragment", "Lỗi gửi tin nhắn: " + t.getMessage(), t);
                }
            });
        } else {
            Log.e("ChatFragment", "listUserString null/rỗng");
        }
    }

    // Helper method
    private RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            getContext().unbindService(serviceConnection);
        }
    }
}