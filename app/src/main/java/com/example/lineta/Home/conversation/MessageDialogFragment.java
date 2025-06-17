package com.example.lineta.Home.conversation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lineta.R;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.client.ApiClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_TARGET_USER_ID = "target_user_id";
    private static final String ARG_LIST_USER_STRING = "list_user_string";

    private String userId;
    private String targetUserId;
    private String listUserString;
    private EditText messageInput;

    public static MessageDialogFragment newInstance(String userId, String targetUserId, String listUserString) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TARGET_USER_ID, targetUserId);
        args.putString(ARG_LIST_USER_STRING, listUserString);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            targetUserId = getArguments().getString(ARG_TARGET_USER_ID);
            listUserString = getArguments().getString(ARG_LIST_USER_STRING);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_message, null);

        messageInput = view.findViewById(R.id.messageInput);
        Button sendButton = view.findViewById(R.id.sendButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        builder.setView(view);

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return builder.create();
    }

    private void sendMessage(String context) {
        if (!listUserString.isEmpty()) {
            Map<String, RequestBody> params = new HashMap<>();
            params.put("list-user", toRequestBody(listUserString));
            params.put("context", toRequestBody(context));
            params.put("userId", toRequestBody(userId));

            ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
            Call<Map<String, Object>> call = apiConversation.sendMessage(params, new MultipartBody.Part[0]);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("MessageDialog", "Gửi tin nhắn thành công: " + response.body());
                        Toast.makeText(requireContext(), "Tin nhắn đã được gửi", Toast.LENGTH_SHORT).show();
                        dismiss(); // Ẩn dialog sau khi gửi thành công
                    } else {
                        Log.e("MessageDialog", "Gửi tin nhắn thất bại: " + response.code() + ", Message: " + response.message());
                        Toast.makeText(requireContext(), "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("MessageDialog", "Lỗi gửi tin nhắn: " + t.getMessage());
                    Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("MessageDialog", "listUserString null/rỗng");
        }
    }

    private RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}