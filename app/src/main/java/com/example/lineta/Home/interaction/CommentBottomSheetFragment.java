package com.example.lineta.Home.interaction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.CommentAdapter;
import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.User;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.config.WebSocketManager;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.CommentApi;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment {

    private String postId;
    private RecyclerView recyclerComments;
    private CommentAdapter adapter;
    private int currentPage = 1;
    private final int pageSize = 5;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private CurrentUserViewModel currentUserViewModel;

    private  User currentUser;

    // Trong CommentBottomSheetFragment
    private List<Comment> comments = new ArrayList<>(); // Khởi tạo ngay lập tức

    EditText inputComment;
    ImageView btnSend;


    public static CommentBottomSheetFragment newInstance(String postId) {
        CommentBottomSheetFragment fragment = new CommentBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet, container, false);

        recyclerComments = view.findViewById(R.id.recyclerComments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));

        inputComment = view.findViewById(R.id.inputComment);
        btnSend = view. findViewById(R.id.btnSend);

        currentUserViewModel = new ViewModelProvider(requireActivity()).get(CurrentUserViewModel.class);
        currentUserViewModel.fetchCurrentUserInfo();

        currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
                    if (user == null) return;
                    currentUser = user;
                    adapter = new CommentAdapter(requireContext(), currentUser);
                    recyclerComments.setAdapter(adapter);

        });



        recyclerComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { // scroll xuống
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoading && !isLastPage) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                    && firstVisibleItemPosition >= 0) {
                                loadComments(currentPage + 1);
                            }
                        }
                    }
                }
            }
        });

        loadComments(currentPage);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> {
            String content = inputComment.getText().toString().trim();
            if (!content.isEmpty()) {
                String date2 = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(new Date());
                Comment commentRequest = Comment.builder()
                        .username(currentUser.getUsername())
                        .commentId(currentUser.getUsername() + date2)
                        .content(content)
                        .date(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()))
                        .postID(postId)
                        .fullName(currentUser.getFullName())
                        .profilePicURL(currentUser.getProfilePicURL())
                        .build();



                // Gửi lên server
                sendCommentToApi(commentRequest);
                inputComment.setText("");
            }
        });

        setupWebSocket();

        return view;
    }

    private void loadComments(int page) {
        isLoading = true;
        CommentApi commentApi = ApiClient.getRetrofit().create(CommentApi.class);

        // Sửa đúng kiểu dữ liệu trả về từ API
        Call<ApiResponse<List<Comment>>> call = commentApi.getComments(postId, page, pageSize);

        call.enqueue(new Callback<ApiResponse<List<Comment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Comment>>> call, Response<ApiResponse<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comments = response.body().getResult();
                    if (page == 1) {
                        adapter.setComments(comments);
                    } else {
                        adapter.addComments(comments);
                    }
                    if (comments.size() < pageSize) {
                        isLastPage = true;
                    }
                    currentPage = page;
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Comment>>> call, Throwable t) {
                isLoading = false;
            }
        });
    }

    private void sendCommentToApi(Comment comment) {
        ApiService api = ApiClient.getRetrofit().create(ApiService.class);
        api.createComment(comment).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("COMMENT", "Gửi bình luận thành công");
                } else {
                    Log.e("COMMENT", "Gửi thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("COMMENT", "Lỗi: " + t.getMessage());
            }
        });
    }
    private void setupWebSocket() {
        WebSocketManager.getInstance().setListener(jsonObject -> {
            try {
                Comment commentResponse = Comment.builder()
                        .commentId(jsonObject.getString("commentId"))
                        .username(jsonObject.getString("username"))
                        .content(jsonObject.getString("content"))
                        .profilePicURL(jsonObject.getString("profilePicURL"))
                        .fullName(jsonObject.getString("fullName"))
                        .postID(jsonObject.getString("postID"))
                        .date(jsonObject.getString("date"))
                        .numberOfLike(jsonObject.getInt("numberOfLike"))
                        .build();


                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (comments == null) return;
                    if (adapter != null) {
                        adapter.addCommentAtTop(commentResponse);  // Thay vì comments.add(0)
                        recyclerComments.scrollToPosition(0);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Thay đổi URL websocket phù hợp với backend của bạn
//        WebSocketManager.getInstance().connect("ws://localhost:9000/ws/websocket", () -> {
//            WebSocketManager.getInstance().subscribe("/topic/comments/" + postId);
//        });

        WebSocketManager.getInstance().subscribe("/topic/comments/" + postId);

    }



}
