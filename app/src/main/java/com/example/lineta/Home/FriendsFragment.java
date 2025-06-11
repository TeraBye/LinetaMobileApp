package com.example.lineta.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lineta.Adapter.FriendAdapter;
import com.example.lineta.Home.profile.AccountFragment;
import com.example.lineta.Home.profile.FollowListActivity;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;
import com.example.lineta.service.FriendService;
import com.example.lineta.service.client.ApiClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FriendsFragment extends Fragment {
    RecyclerView recyclerView;
    EditText searchEditText;
    FriendAdapter friendAdapter;
    List<UserFollowResponse> userList;

    int currentPage = 1;
    final int PAGE_SIZE = 5;
    boolean isLoading = false;
    boolean isLastPage = false;

    String currrentUserId = FirebaseAuth.getInstance().getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);

        // Initialize data and adapter
        userList = new ArrayList<>();
        friendAdapter = new FriendAdapter(userList, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(friendAdapter);

        friendAdapter.setOnItemClickListener(userId -> {
            Fragment accountFragment = AccountFragment.newInstance(userId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.frame_layout, accountFragment)
                    .addToBackStack(null)
                    .commit();
        });

        setupSearch();
        setupRecyclerViewScrollListener();
        loadData();

        return view;
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterUsers(String query) {
        List<UserFollowResponse> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(userList);
        } else {
            for (UserFollowResponse user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(user);
                }
            }
        }

        friendAdapter.setUsers(filtered); // Sử dụng setUsers
    }

    private void setupRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisiblePosition = firstVisibleItemPosition + visibleItemCount - 1;

                    if (lastVisiblePosition == totalItemCount - 1 && totalItemCount > 0) {
                        loadMoreData();
                    }
                }
            }
        });
    }

    private void loadData() {
        currentPage = 1;
        isLastPage = false;
        userList.clear();
        searchEditText.setText("");
        friendAdapter.clearUsers();
        loadMoreData();
    }

    private void loadMoreData() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        recyclerView.post(() -> friendAdapter.setLoading(true));

        FriendService friendService = ApiClient.getRetrofit().create(FriendService.class);
        Call<ApiResponse<List<UserFollowResponse>>> call = friendService.getFriends(currrentUserId, currentPage, PAGE_SIZE);

        call.enqueue(new Callback<ApiResponse<List<UserFollowResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserFollowResponse>>> call, Response<ApiResponse<List<UserFollowResponse>>> response) {
                isLoading = false;
                friendAdapter.setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UserFollowResponse> newUsers = response.body().getResult();
                    if (newUsers.isEmpty() || newUsers.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }
                    if (currentPage == 1) {
                        userList.clear();
                    }
                    userList.addAll(newUsers);
                    friendAdapter.addUsers(newUsers);
                    currentPage++;
                } else {
                    Toast.makeText(getContext(), "Failed to load data " + response.body(), Toast.LENGTH_SHORT).show();
                    Log.e("Failed to load data", response.body().getResult().toString());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserFollowResponse>>> call, Throwable t) {
                isLoading = false;
                friendAdapter.setLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}