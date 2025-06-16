package com.example.lineta.Home.profile;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lineta.Adapter.SettingAdapter;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.ViewModel.UserViewModel;
import com.example.lineta.dto.request.UserFollowRequest;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.FriendService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    RecyclerView recyclerView;
    SettingAdapter adapter;
    TextView tvUsername, tvFullname, tvPostNum, tvFollowerNum, tvFollowingNum, tvBio;
    ShapeableImageView avatar;
    UserViewModel userViewModel;
    CurrentUserViewModel currentUserViewModel;
    String userId;
    LinearLayout followersLayout, followingLayout;
    FrameLayout btnFollowLayout;
    Button btnFollow;
    ProgressBar progressBarFollow;


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
        adapter = new SettingAdapter(Arrays.asList(settings), setting -> {
            if ("Change Password".equals(setting)) {
                Fragment changePasswordFragment = ChangePasswordFragment.newInstance(null, null);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.frame_layout, changePasswordFragment)
                        .addToBackStack(null)
                        .commit();
            } else if ("Profile Settings".equals(setting)) {
                Fragment editProfileFragment = new EditProfileFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.frame_layout, editProfileFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
//                requireActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .replace(R.id.frame_layout, changePasswordFragment)
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });
        recyclerView.setAdapter(adapter);

        // Ánh xạ
        tvUsername = view.findViewById(R.id.tvUsername);
        tvFullname = view.findViewById(R.id.tvFullName);
        tvPostNum = view.findViewById(R.id.tvPostNum);
        tvFollowerNum = view.findViewById(R.id.tvFollowerNum);
        tvFollowingNum = view.findViewById(R.id.tvFollowingNum);
        tvBio = view.findViewById(R.id.tvBio);
        avatar = view.findViewById(R.id.avatar);

        btnFollowLayout = view.findViewById(R.id.btnFollowLayout);
        btnFollow = view.findViewById(R.id.btnFollow);
        progressBarFollow = view.findViewById(R.id.progressBarFollow);

        followersLayout = view.findViewById(R.id.followersLayout);
        followingLayout = view.findViewById(R.id.followingLayout);
        setupClickFollowListeners();

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        currentUserViewModel = new ViewModelProvider(requireActivity()).get(CurrentUserViewModel.class);

        // Disappear follow button for current user
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (userId == null || userId.equals(currentUserId)) {
            btnFollowLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            btnFollowLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            checkFollowStatus(currentUserId, userId);
            setupFollowButtonListener(currentUserId, userId);
        }

        if (userId != null) { // Get other user
            recyclerView.setVisibility(View.GONE);
            userViewModel.fetchUserInfo(userId);
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                tvUsername.setText(user.getUsername());
                tvFullname.setText(user.getFullName());
                tvBio.setText(user.getBio());
                tvPostNum.setText(String.valueOf(user.getPostNum()));
                tvFollowerNum.setText(String.valueOf(user.getFollowerNum()));
                tvFollowingNum.setText(String.valueOf(user.getFollowingNum()));



                if (user.getProfilePicURL() != null) {
                    Glide.with(requireContext())
                            .load(user.getProfilePicURL())
                            .placeholder(R.drawable.default_avatar)
                            .into(avatar);
                }

                tvPostNum.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), UserPostListActivity.class);
//                    intent.putExtra("user_id", userId);
//                    intent.putExtra("user", user);
                    intent.putExtra("username", user.getUsername());
                    startActivity(intent);
                });
                userViewModel.fetchPostsByUser(user.getUsername(), 0, 50);
            });

            userViewModel.getPostCountLiveData().observe(getViewLifecycleOwner(), count -> {
                tvPostNum.setText(String.valueOf(count));
            });
            // Observe errors
            userViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
//            loadingProgressBar.setVisibility(View.GONE);
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });


        } else { // get current user
            recyclerView.setVisibility(View.VISIBLE);
            currentUserViewModel.fetchCurrentUserInfo();
            currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
                tvUsername.setText(user.getUsername());
                tvFullname.setText(user.getFullName());
                tvBio.setText(user.getBio());
                tvPostNum.setText(String.valueOf(user.getPostNum()));
                tvFollowerNum.setText(String.valueOf(user.getFollowerNum()));
                tvFollowingNum.setText(String.valueOf(user.getFollowingNum()));

                if (user.getProfilePicURL() != null) {
                    Glide.with(requireContext())
                            .load(user.getProfilePicURL())
                            .placeholder(R.drawable.default_avatar)
                            .into(avatar);
                }

                tvPostNum.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), UserPostListActivity.class);
//                    intent.putExtra("user_id", userId);
//                    intent.putExtra("user", user);
                    intent.putExtra("username", user.getUsername());
                    startActivity(intent);
                });
                currentUserViewModel.fetchPostsByUser(user.getUsername(), 0, 50);
            });
            currentUserViewModel.getPostCountLiveData().observe(getViewLifecycleOwner(), count -> {
                tvPostNum.setText(String.valueOf(count));
            });

            // Observe errors
            currentUserViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
//            loadingProgressBar.setVisibility(View.GONE);
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });

        }
        return view;

    }

    private void checkFollowStatus(String followerId, String followedId) {
        FriendService friendService = ApiClient.getRetrofit().create(FriendService.class);
        Call<ApiResponse<Boolean>> call = friendService.isFollowing(followerId, followedId);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFollowing = response.body().getResult();
                    btnFollow.setText(isFollowing ? "Following" : "Follow");
                    btnFollow.setBackgroundTintList(isFollowing
                            ? ResourcesCompat.getColorStateList(getContext().getResources(), R.color.following_text_color, getContext().getTheme())
                            : ResourcesCompat.getColorStateList(getContext().getResources(), R.color.blue_accent, getContext().getTheme()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error checking follow status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFollowButtonListener(String followerId, String followedId) {
        btnFollow.setOnClickListener(v -> {
            // Show loading
            btnFollow.setVisibility(View.GONE);
            progressBarFollow.setVisibility(View.VISIBLE);

            // Used for unfollow
            Map<String, String> body = new HashMap<>();
            body.put("followerId", followerId);
            body.put("followedId", followedId);

            // Used for follow
            /*AtomicReference được sử dụng để quản lý một tham chiếu (reference) đến một đối tượng theo cách an toàn
            trong môi trường đa luồng (thread-safe).
            Nó cho phép thực hiện các thao tác nguyên tử (atomic operations) như get(), set(),
            và compareAndSet() mà không cần sử dụng synchronized.*/
            AtomicReference<String> followerFullname = new AtomicReference<>();
            AtomicReference<String> followerUsername = new AtomicReference<>();
            AtomicReference<String> followedUsername = new AtomicReference<>();
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                followedUsername.set(user.getUsername());
            });
            currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
                followerFullname.set(user.getFullName());
                followerUsername.set(user.getUsername());
            });

            FriendService friendService = ApiClient.getRetrofit().create(FriendService.class);
            Call<ApiResponse<Void>> call;
            if (btnFollow.getText().equals("Following")) {
                // Unfollow using body
                Map<String, String> unfollowBody = new HashMap<>();
                unfollowBody.put("followerId", followerId);
                unfollowBody.put("followedId", followedId);
                call = friendService.unfollow(unfollowBody);
            } else {
                // Follow using request body
                call = friendService.follow(UserFollowRequest.builder()
                        .followerFullname(followerFullname.get())
                        .followerUsername(followerUsername.get())
                        .followedUsername(followedUsername.get())
                        .followerId(followerId)
                        .followedId(followedId)
                        .build());
            }

            call.enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    // Hide loading
                    btnFollow.setVisibility(View.VISIBLE);
                    progressBarFollow.setVisibility(View.GONE);

                    if (response.isSuccessful() && response.body().getCode() == 1000) {
                        btnFollow.setText(btnFollow.getText().equals("Following") ? "Follow" : "Following");

                        btnFollow.setBackgroundTintList(btnFollow.getText().equals("Following")
                                ? ResourcesCompat.getColorStateList(getContext().getResources(), R.color.following_text_color, getContext().getTheme())
                                : ResourcesCompat.getColorStateList(getContext().getResources(), R.color.blue_accent, getContext().getTheme()));

                        userViewModel.fetchUserInfo(userId);
                    } else {
                        Toast.makeText(requireContext(), "Operation failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    btnFollow.setVisibility(View.VISIBLE);
                    progressBarFollow.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error updating follow status", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupClickFollowListeners() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        followersLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FollowListActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("title", "Followers");
            intent.putExtra("user_id", userId != null ? userId : currentUserId);
            startActivity(intent);
        });

        followingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FollowListActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("title", "Following");
            intent.putExtra("user_id", userId != null ? userId : currentUserId);
            startActivity(intent);
        });
    }
}