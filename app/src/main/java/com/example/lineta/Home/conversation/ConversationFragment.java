package com.example.lineta.Home.conversation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.ConversationAdapter;
import com.example.lineta.Entity.Conversation.Conversation;
import com.example.lineta.Entity.Conversation.UserInfo;
import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.service.client.WebSocketService;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TOKEN = "token";
    private static final String TAG = "WebSocketMessageFragment";

    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Map<String, Object>> conversations;

    private WebSocketService webSocketService;
    private boolean isServiceBound;

    private String userId;
    private String token;

    // Lưu trữ các cập nhật tạm thời khi fragment không active
    private Map<String, Map<String, Object>> pendingUpdates = new HashMap<>();

    public static ConversationFragment newInstance(String userId, String token) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;
            Log.d(TAG, "Service connected, attempting to setup listener");

            if (isAdded()) {
                setupListener();
            } else {
                Log.w(TAG, "Fragment not added during service connect, will setup later");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    private void setupListener() {
        if (webSocketService != null) {
            webSocketService.setOnConversationUpdateListener((conversationId, unreadCount, lastUpdate) -> {
                Log.d(TAG, "Received WebSocket update for convId: " + conversationId + ", unreadCount: " + unreadCount + ", lastUpdate: " + lastUpdate);
                Long newLastUpdate = parseLastUpdate(lastUpdate);
                Map<String, Object> updateData = new HashMap<>();
                Long effectiveUnreadCount =unreadCount;
                updateData.put("unreadCount", effectiveUnreadCount);
                updateData.put("lastUpdate", newLastUpdate);
                updateData.put("lastMessage", "Updated via WS"); // Giả định, cần lấy từ WebSocket
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Running UI thread for update");
                        updateConversation(conversationId, effectiveUnreadCount);
                        boolean updated = false;
                        for (Map<String, Object> conv : conversations) {
                            if (conv.get("conversationId").equals(conversationId)) {
                                Long currentLastUpdate = (Long) conv.get("lastUpdate");
                                if (currentLastUpdate == null || newLastUpdate > currentLastUpdate) {
                                    conv.put("lastUpdate", newLastUpdate);
                                    conv.put("lastMessage", updateData.get("lastMessage"));
                                    Log.d(TAG, "Updated lastUpdate to: " + newLastUpdate + ", lastMessage to: " + updateData.get("lastMessage"));
                                    updated = true;
                                }
                                break;
                            }
                        }
                        // Tính tổng số tin nhắn chưa đọc chỉ của userId hiện tại
                        long totalUnread = 0;
                        for (Map<String, Object> conv : conversations) {
                            Map<String, Long> unreadCountMap = (Map<String, Long>) conv.get("unreadCount");
                            if (unreadCountMap != null && userId != null) {
                                Long userUnread = unreadCountMap.get(userId); // Chỉ lấy unreadCount của userId hiện tại
                                totalUnread += userUnread != null ? userUnread : 0;
                            }
                        }
                        // Gửi broadcast để cập nhật badge với package giới hạn
                        Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE").setPackage(requireContext().getPackageName());
                        intent.putExtra("totalUnread", totalUnread);
                        requireContext().sendBroadcast(intent);

                        if (updated || effectiveUnreadCount >= 0) {
                            fetchConversations();
                            Log.d(TAG, "Reloaded conversations due to lastUpdate or unreadCount change");
                        } else {
                            Log.d(TAG, "No significant change, skipping reload");
                        }
                    });
                } else {
                    Log.w(TAG, "Fragment not added, queuing update");
                    pendingUpdates.put(conversationId, updateData);
                }
            });
            Log.d(TAG, "Listener setup completed");
        } else {
            Log.e(TAG, "webSocketService is null, cannot setup listener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            token = getArguments().getString(ARG_TOKEN);
        }

        Intent intent = new Intent(requireContext(), WebSocketService.class);
        intent.putExtra("userId", userId);
        requireContext().startService(intent);
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Service binding initiated for userId: " + userId);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isAdded() && webSocketService != null) {
            setupListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pendingUpdates.isEmpty()) {
            for (Map.Entry<String, Map<String, Object>> entry : pendingUpdates.entrySet()) {
                String conversationId = entry.getKey();
                Map<String, Object> updateData = entry.getValue();
                Long lastUpdate = (Long) updateData.get("lastUpdate");
                Long unreadCount = (Long) updateData.get("unreadCount");
                updateConversation(conversationId, unreadCount != null ? unreadCount : 0L);
                for (Map<String, Object> conv : conversations) {
                    if (conv.get("conversationId").equals(conversationId)) {
                        conv.put("lastUpdate", lastUpdate);
                        conv.put("lastMessage", updateData.get("lastMessage"));
                        break;
                    }
                }
                fetchConversations();
            }
            pendingUpdates.clear();
            Log.d(TAG, "Processed pending updates");
        }
        if (isAdded() && webSocketService != null) {
            setupListener();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);

        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, conversation -> {
            String conversationId = (String) conversation.get("conversationId");
            String contactName = getContactName(conversation);
            String contactAvatarUrl = getContactAvatarUrl(conversation);
            Log.d(TAG, "Navigating to chat with convId: " + conversationId + ", name: " + contactName + ", avatar: " + contactAvatarUrl);
            markConversationAsRead(conversationId);
            navigateToChat(conversationId, contactName, contactAvatarUrl);
        }, userId);
        recyclerViewConversations.setAdapter(conversationAdapter);

        fetchConversations();
        Log.d(TAG, "View created and initial fetch triggered");

        return view;
    }

    private void fetchConversations() {
        Log.d(TAG, "Fetching conversations for userId: " + userId);
        ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
        Call<List<Conversation>> call = apiConversation.getConversations();
        call.enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Conversations fetched: " + response.body().size() + ", Data: " + new Gson().toJson(response.body()));
                    conversations.clear();
                    for (Conversation conv : response.body()) {
                        Map<String, Object> convMap = new HashMap<>();
                        convMap.put("conversationId", conv.getId());
                        convMap.put("listUser", conv.getListUser());
                        convMap.put("users", conv.getUsers());
                        convMap.put("lastMessage", conv.getLastMessage());
                        convMap.put("lastSender", conv.getLastSender());
                        convMap.put("lastUpdate", conv.getLastUpdate() != null ? conv.getLastUpdate() : 0L);
                        convMap.put("unreadCount", conv.getUnreadCount() != null ? conv.getUnreadCount() : new HashMap<>());
                        conversations.add(convMap);
                    }
                    sortConversationsByLastUpdate();
                    conversationAdapter.notifyDataSetChanged();
                    // Tính lại totalUnread sau khi fetch
                    long totalUnread = 0;
                    for (Map<String, Object> conv : conversations) {
                        Map<String, Long> unreadCountMap = (Map<String, Long>) conv.get("unreadCount");
                        if (unreadCountMap != null && userId != null) {
                            Long userUnread = unreadCountMap.get(userId);
                            totalUnread += userUnread != null ? userUnread : 0;
                        }
                    }
                    Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE").setPackage(requireContext().getPackageName());
                    intent.putExtra("totalUnread", totalUnread);
                    requireContext().sendBroadcast(intent);
                    Log.d(TAG, "Reloaded conversations with totalUnread: " + totalUnread);
                } else {
                    Log.e(TAG, "Failed to fetch conversations: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                Log.e(TAG, "Error fetching conversations: " + t.getMessage(), t);
            }
        });
    }

    private void updateConversation(String conversationId, long unreadCount) {
        for (Map<String, Object> conversation : conversations) {
            if (conversationId.equals(conversation.get("conversationId"))) {
                Map<String, Long> unreadCountMap = (Map<String, Long>) conversation.get("unreadCount");
                if (unreadCountMap == null) {
                    unreadCountMap = new HashMap<>();
                }
                unreadCountMap.put(userId, unreadCount);
                conversation.put("unreadCount", unreadCountMap);
                Log.d(TAG, "Updated unreadCount for convId: " + conversationId + " to " + unreadCount);
                break;
            }
        }
        conversationAdapter.notifyDataSetChanged();
    }

    private void navigateToChat(String conversationId, String contactName, String contactAvatarUrl) {
        List<String> listUser = null;
        for (Map<String, Object> conv : conversations) {
            if (conv.get("conversationId").equals(conversationId)) {
                listUser = (List<String>) conv.get("listUser");
                if (listUser == null) {
                    Log.e(TAG, "listUser is null for conversationId: " + conversationId);
                    listUser = new ArrayList<>();
                }
                break;
            }
        }
        if (listUser == null) {
            Log.e(TAG, "No conversation found for conversationId: " + conversationId);
            listUser = new ArrayList<>();
        }
        String listUserString = String.join(",", listUser);

        Log.d(TAG, "Navigating to chat with listUserString: " + listUserString);

        if (isAdded()) {
            ChatFragment chatFragment = ChatFragment.newInstance(
                    contactName,
                    contactAvatarUrl,
                    conversationId,
                    userId,
                    token,
                    listUserString
            );
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, chatFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void markConversationAsRead(String conversationId) {
        Log.d(TAG, "Marking conversation " + conversationId + " as read for user " + userId);
        Map<String, String> requestData = new HashMap<>();
        requestData.put("conversationId", conversationId);
        requestData.put("unreadCount", "0"); // Đặt unreadCount về 0 cho userId hiện tại

        ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
        Call<Void> call = apiConversation.updateUnread(requestData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully marked conversation " + conversationId + " as read");
                    // Cập nhật local data
                    for (Map<String, Object> conv : conversations) {
                        if (conv.get("conversationId").equals(conversationId)) {
                            Map<String, Long> unreadCountMap = (Map<String, Long>) conv.get("unreadCount");
                            if (unreadCountMap != null) {
                                unreadCountMap.put(userId, 0L);
                            }
                            break;
                        }
                    }
                    conversationAdapter.notifyDataSetChanged();

                    // Tính lại tổng số tin nhắn chưa đọc
                    long totalUnread = 0;
                    for (Map<String, Object> conv : conversations) {
                        Map<String, Long> unreadCountMap = (Map<String, Long>) conv.get("unreadCount");
                        if (unreadCountMap != null && userId != null) {
                            Long userUnread = unreadCountMap.get(userId);
                            totalUnread += userUnread != null ? userUnread : 0;
                        }
                    }

                    // Gửi broadcast để cập nhật badge
                    Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE").setPackage(requireContext().getPackageName());
                    intent.putExtra("totalUnread", totalUnread);
                    requireContext().sendBroadcast(intent);

                    // Gửi thông báo WebSocket để cập nhật các subscribers
                    if (webSocketService != null) {
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("conversationId", conversationId);
                        updateData.put("unreadCount", new HashMap<String, Long>() {{ put(userId, 0L); }});
                        updateData.put("lastUpdate", String.valueOf(System.currentTimeMillis()));
                        webSocketService.sendConversationUpdateWithBroadcast(updateData); // Sử dụng phương thức mới
                        Log.d(TAG, "Sent WebSocket update with broadcast for conversationId: " + conversationId);
                    } else {
                        Log.w(TAG, "webSocketService is null, cannot send WebSocket update");
                    }

                    // Đồng bộ lại dữ liệu từ backend
                    fetchConversations();
                } else {
                    Log.e(TAG, "Failed to mark as read: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error marking as read: " + t.getMessage());
            }
        });
    }
    private String getContactName(Map<String, Object> conversation) {
        List<UserInfo> users = (List<UserInfo>) conversation.get("users");
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(userId)) {
                    return user.getFullName() != null ? user.getFullName() : user.getUsername();
                }
            }
        }
        return "Unknown";
    }

    private String getContactAvatarUrl(Map<String, Object> conversation) {
        List<UserInfo> users = (List<UserInfo>) conversation.get("users");
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(userId)) {
                    String avatarUrl = user.getAvatarUrl();
                    if (avatarUrl != null) {
                        return avatarUrl;
                    }
                }
            }
        }
        return null;
    }

    private void sortConversationsByLastUpdate() {
        Collections.sort(conversations, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> c1, Map<String, Object> c2) {
                Long lastUpdate1 = (Long) c1.get("lastUpdate");
                Long lastUpdate2 = (Long) c2.get("lastUpdate");
                if (lastUpdate1 == null) return 1;
                if (lastUpdate2 == null) return -1;
                return lastUpdate2.compareTo(lastUpdate1); // Sắp xếp giảm dần
            }
        });
    }

    private Long parseLastUpdate(String lastUpdateStr) {
        try {
            if (lastUpdateStr != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return sdf.parse(lastUpdateStr.split("\\.")[0]).getTime();
            }
            return System.currentTimeMillis();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing lastUpdate: " + e.getMessage() + ", Input: " + lastUpdateStr);
            try {
                long seconds = Long.parseLong(lastUpdateStr);
                return seconds * 1000;
            } catch (NumberFormatException ne) {
                Log.e(TAG, "Error parsing lastUpdate as seconds: " + ne.getMessage());
                return System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection);
        }
    }
}