package com.example.lineta.Home.conversation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.ConversationAdapter;
import com.example.lineta.Entity.Conversation;
import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TOKEN = "token";

    private String userId;
    private String token;
    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;

    public static ConversationFragment newInstance(String userId, String token) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);

        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, (uid, conversation) -> {
            ChatFragment chatFragment = ChatFragment.newInstance(
                    conversation.getName(uid),
                    conversation.getAvatarUrl(uid),
                    conversation.getId(),
                    uid,
                    token
            );
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, chatFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        recyclerViewConversations.setAdapter(conversationAdapter);

        if (userId != null && token != null) {
            fetchConversations();
        } else {
            Log.e("MessageFragment: ", "token-userId missing");

        }

        return view;
    }

    private void fetchConversations() {
        ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
        Call<List<Conversation>> call = apiConversation.getConversations();
        call.enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    conversations.clear();
                    conversations.addAll(response.body());
                    conversationAdapter.setUserId(userId);
                    conversationAdapter.notifyDataSetChanged();
                } else {
                    String errorMessage = "Không tải được cuộc trò chuyện: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            errorMessage += " - Không thể đọc chi tiết lỗi";
                            e.printStackTrace();
                        }
                    }
                    Log.e("MessageFragment", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                String errorMessage = "Lỗi kết nối: " + t.getMessage();
                Log.e("MessageFragment", errorMessage, t);
            }
        });
    }
}