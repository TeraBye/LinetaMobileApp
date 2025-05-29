package com.example.lineta.Search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.UserSearchAdapter;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSearchResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.SearchApi;
import com.example.lineta.service.client.ApiClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    EditText searchEditText;
    RecyclerView recyclerView;
    TextView noResultsText;
    TextView cancelButton;
    UserSearchAdapter adapter;
    TextView clearHistoryButton;
    String searcherId = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.users_recycler_view);
        noResultsText = findViewById(R.id.no_results_text);
        cancelButton = findViewById(R.id.cancel_button);
        clearHistoryButton = findViewById(R.id.clear_history_button);

        // Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(null, this, searcherId);
        recyclerView.setAdapter(adapter);

        // Thiết lập listener cho lịch sử tìm kiếm
        adapter.setOnHistoryItemClickListener(query -> {
            searchEditText.setText(query);
            callSearchApi(query, adapter, noResultsText);
        });
        
        // Hiển thị lịch sử tìm kiếm ban đầu
        showSearchHistory();

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable searchRunnable = new Runnable() {
            @Override
            public void run() {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    callSearchApi(query, adapter, noResultsText);
                } else {
                    adapter.setUserSearchResponses(new ArrayList<>());
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                }
            }
        };
        // Xử lý sự kiện Search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Hủy các runnable trước đó để tránh gọi API quá nhiều
                handler.removeCallbacks(searchRunnable);
                // Trì hoãn 500ms trước khi gọi API
                handler.postDelayed(searchRunnable, 0);
            }
        });

        cancelButton.setOnClickListener(v -> finish());

        clearHistoryButton.setOnClickListener(v -> {
            deleteAllSearchHistory();
        });
    }

    private void deleteAllSearchHistory() {
        SearchApi searchApi = ApiClient.getRetrofit().create(SearchApi.class);
        Call<ApiResponse<Void>> call = searchApi.deleteAllSearchHistory(searcherId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    showSearchHistory();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {

            }
        });
    }

    private void showSearchHistory() {
        SearchApi searchApi = ApiClient.getRetrofit().create(SearchApi.class);
        Call<ApiResponse<List<UserSearchResponse>>> call = searchApi.getSearchHistory(searcherId);
        call.enqueue(new Callback<ApiResponse<List<UserSearchResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearchResponse>>> call, Response<ApiResponse<List<UserSearchResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    List<UserSearchResponse> history = response.body().getResult();
                    if (history == null || history.isEmpty()) {
                        adapter.setUserSearchResponses(new ArrayList<>());
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.GONE);
                        noResultsText.setText("No recent searches");
                        noResultsText.setVisibility(View.VISIBLE);
                        clearHistoryButton.setVisibility(View.GONE);
                    } else {
                        adapter.setSearchHistory(history);
                        recyclerView.setVisibility(View.VISIBLE);
                        noResultsText.setVisibility(View.GONE);
                        clearHistoryButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setText("No recent searches");
                    noResultsText.setVisibility(View.VISIBLE);
                    clearHistoryButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserSearchResponse>>> call, Throwable t) {
                recyclerView.setVisibility(View.GONE);
                noResultsText.setText("No recent searches");
                noResultsText.setVisibility(View.VISIBLE);
                clearHistoryButton.setVisibility(View.GONE);
            }
        });
    }

    private void callSearchApi(String query, UserSearchAdapter adapter, TextView noResultsText) {
        SearchApi searchApi = ApiClient.getRetrofit().create(SearchApi.class);
        Call<ApiResponse<List<UserSearchResponse>>> call = searchApi.getSearch(query);

        call.enqueue(new Callback<ApiResponse<List<UserSearchResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearchResponse>>> call, Response<ApiResponse<List<UserSearchResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserSearchResponse> searchResponses = response.body().getResult();
                    adapter.setUserSearchResponses(searchResponses);
                    adapter.setContext(SearchActivity.this);
                    adapter.notifyDataSetChanged();
                    if (searchResponses != null && !searchResponses.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noResultsText.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserSearchResponse>>> call, Throwable t) {
                recyclerView.setVisibility(View.GONE);
                noResultsText.setVisibility(View.VISIBLE);
            }
        });

    }
}