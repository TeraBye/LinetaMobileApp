package com.example.lineta.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.NotificationAdapter;
import com.example.lineta.Entity.Notification;
import com.example.lineta.Entity.User;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.NotificationService;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.dto.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerNotification;
    private NotificationAdapter adapter;
    private CurrentUserViewModel currentUserViewModel;
    private List<Notification> notificationList = new ArrayList<>();
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerNotification = view.findViewById(R.id.recyclerNotifications);
        recyclerNotification.setLayoutManager(new LinearLayoutManager(getContext()));


        adapter = new NotificationAdapter(notificationList, getContext());
        recyclerNotification.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        // Giả sử bạn có tên người dùng hiện tại
        currentUserViewModel = new ViewModelProvider(requireActivity()).get(CurrentUserViewModel.class);
        currentUserViewModel.fetchCurrentUserInfo();

        currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            currentUser = user;
            NotificationService apiService = ApiClient.getRetrofit().create(NotificationService.class);
            apiService.getNotifications(currentUser.getUsername()).enqueue(new Callback<ApiResponse<List<Notification>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        notificationList.clear();
                        notificationList.addAll(response.body().getResult());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        });

    }
}
