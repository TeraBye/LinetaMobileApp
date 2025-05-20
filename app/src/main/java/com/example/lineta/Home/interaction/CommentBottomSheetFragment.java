package com.example.lineta.Home.interaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.CommentAdapter;
import com.example.lineta.Entity.Comment;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.CommentApi;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

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
        adapter = new CommentAdapter(getContext());
        recyclerComments.setAdapter(adapter);

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
                    List<Comment> comments = response.body().getResult();
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


}
