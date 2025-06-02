package com.example.lineta.Home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lineta.Adapter.SettingAdapter;
import com.example.lineta.Entity.User;
import com.example.lineta.R;
import com.example.lineta.ViewModel.UserViewModel;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.UserService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    RecyclerView recyclerView;
    SettingAdapter adapter;
    TextView tvUsername, tvFullname, tvPostNum, tvFollowerNum, tvFollowingNum;
    ShapeableImageView avatar;
    UserViewModel userViewModel;
    String userId;

    public static AccountFragment newInstance(String userId) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String[] settings = {"Profile Settings", "Change Password", "Notification Settings"};
        adapter = new SettingAdapter(Arrays.asList(settings));
        recyclerView.setAdapter(adapter);

        // Ánh xạ
        tvUsername = view.findViewById(R.id.tvUsername);
        tvFullname = view.findViewById(R.id.tvFullName);
        tvPostNum = view.findViewById(R.id.tvPostNum);
        tvFollowerNum = view.findViewById(R.id.tvFollowerNum);
        tvFollowingNum = view.findViewById(R.id.tvFollowingNum);
        avatar = view.findViewById(R.id.avatar);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.fetchUserInfo(userId);

        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            tvUsername.setText(user.getUsername());
            tvFullname.setText(user.getFullName());
            tvPostNum.setText(String.valueOf(user.getPostNum()));
            tvFollowerNum.setText(String.valueOf(user.getFollowerNum()));
            tvFollowingNum.setText(String.valueOf(user.getFollowingNum()));

            if (user.getProfilePicURL() != null) {
                Glide.with(requireContext())
                        .load(user.getProfilePicURL())
                        .placeholder(R.drawable.default_avatar)
                        .into(avatar);
            }
        });

        // Observe errors
        userViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
//            loadingProgressBar.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        return view;

    }
}