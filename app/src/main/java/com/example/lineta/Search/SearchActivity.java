package com.example.lineta.Search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.UserSearchAdapter;
import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSearchResponse;
import com.example.lineta.service.SearchService;
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
    LinearLayout recentSearchLayout;
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
        recentSearchLayout = findViewById(R.id.recent_searches_header);

        // Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(null, this, searcherId);
        recyclerView.setAdapter(adapter);

        // Thiết lập item click
        adapter.setOnItemClickListener(userId -> {
            Intent intent = new Intent(SearchActivity.this, HomeViewActivity.class);
            intent.putExtra("selected_user_id", userId);
            intent.putExtra("navigate_to_profile", true);
            startActivity(intent);
            finish();
        });

        // Thiết lập listener cho lịch sử tìm kiếm
        // Handle history item clicks
        adapter.setOnHistoryItemClickListener(userId -> {
            Intent intent = new Intent(SearchActivity.this, HomeViewActivity.class);
            intent.putExtra("selected_user_id", userId);
            intent.putExtra("navigate_to_profile", true);
            startActivity(intent);
            finish();
        });

        // Hiển thị lịch sử tìm kiếm ban đầu
        showSearchHistory();

        final Handler handler = new Handler(Looper.getMainLooper());
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
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        callSearchApi(query, adapter, noResultsText);
                    } else {
                        recentSearchLayout.setVisibility(View.VISIBLE);
                        showSearchHistory();
                    }
                }, 0);
            }
        });

        cancelButton.setOnClickListener(v -> finish());

        clearHistoryButton.setOnClickListener(v -> {
            deleteAllSearchHistory();
        });
    }

    private void deleteAllSearchHistory() {
        SearchService searchService = ApiClient.getRetrofit().create(SearchService.class);
        Call<ApiResponse<Void>> call = searchService.deleteAllSearchHistory(searcherId);
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
        SearchService searchService = ApiClient.getRetrofit().create(SearchService.class);
        Call<ApiResponse<List<UserSearchResponse>>> call = searchService.getSearchHistory(searcherId);
        call.enqueue(new Callback<ApiResponse<List<UserSearchResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearchResponse>>> call, Response<ApiResponse<List<UserSearchResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    List<UserSearchResponse> history = response.body().getResult();
                    adapter.setUserSearchResponses(new ArrayList<>());
                    if (history == null || history.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        noResultsText.setText("No recent searches");
                        noResultsText.setVisibility(View.VISIBLE);
                        clearHistoryButton.setVisibility(View.GONE);
                        recentSearchLayout.setVisibility(View.GONE);
                    } else {
                        adapter.setSearchHistory(history);
                        recyclerView.setVisibility(View.VISIBLE);
                        noResultsText.setVisibility(View.GONE);
                        clearHistoryButton.setVisibility(View.VISIBLE);
                        recentSearchLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setText("No recent searches");
                    noResultsText.setVisibility(View.VISIBLE);
                    clearHistoryButton.setVisibility(View.GONE);
                    recentSearchLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserSearchResponse>>> call, Throwable t) {
                recyclerView.setVisibility(View.GONE);
                noResultsText.setText("No recent searches");
                noResultsText.setVisibility(View.VISIBLE);
                clearHistoryButton.setVisibility(View.GONE);
                recentSearchLayout.setVisibility(View.GONE);
            }
        });
    }

    private void callSearchApi(String query, UserSearchAdapter adapter, TextView noResultsText) {
        SearchService searchService = ApiClient.getRetrofit().create(SearchService.class);
        Call<ApiResponse<List<UserSearchResponse>>> call = searchService.getSearch(query);

        call.enqueue(new Callback<ApiResponse<List<UserSearchResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearchResponse>>> call, Response<ApiResponse<List<UserSearchResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserSearchResponse> searchResponses = response.body().getResult();
                    adapter.setUserSearchResponses(searchResponses);
                    adapter.setContext(SearchActivity.this);
                    if (searchResponses != null && !searchResponses.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noResultsText.setVisibility(View.GONE);
                        recentSearchLayout.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noResultsText.setVisibility(View.VISIBLE);
                        recentSearchLayout.setVisibility(View.VISIBLE);
                    }
                } else {
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