package com.example.lineta.Home.profile;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.UserSearchAdapter;
import com.example.lineta.Entity.User;
import com.example.lineta.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FollowListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private UserSearchAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredList;
    private String listType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_follow_list);

        initViews();
        setupToolbar();
//        loadData();
//        setupSearch();
    }

    // Thêm vào phần initViews() trong FollowListActivity.java
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listType = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");
        setTitle(title);

        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText("Followers"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));

        // Select the appropriate tab based on the type
        if ("following".equals(listType)) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        } else {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    listType = "followers";
                    setTitle("Followers");
                } else {
                    listType = "following";
                    setTitle("Following");
                }
//                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

//    private void loadData() {
//        userList = new ArrayList<>();
//
//        if ("followers".equals(listType)) {
//            // Sample followers data
//            userList.add(new User("zuck", "Mark Zuckerberg", "https://example.com/avatar1.jpg", false));
//            userList.add(new User("mkbhd", "Marques Brownlee", "https://example.com/avatar2.jpg", false));
//            userList.add(new User("vfriedman", "Vitaly Friedman", "https://example.com/avatar3.jpg", false));
//        } else {
//            // Sample following data
//            userList.add(new User("gal_gadot", "Gal Gadot", "https://example.com/avatar4.jpg", true));
//            userList.add(new User("danielbenol", "Daniel Benol", "https://example.com/avatar5.jpg", true));
//            userList.add(new User("bof", "The Business of Fashion", "https://example.com/avatar6.jpg", true));
//            userList.add(new User("nike", "Nike", "https://example.com/avatar7.jpg", true));
//            userList.add(new User("adidas", "Adidas", "https://example.com/avatar8.jpg", true));
//        }
//
//        filteredList = new ArrayList<>(userList);
//        userAdapter = new UserAdapter(filteredList, listType);
//        recyclerView.setAdapter(userAdapter);
//    }
//
//    private void setupSearch() {
//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterUsers(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//    }
//
//    private void filterUsers(String query) {
//        filteredList.clear();
//        if (query.isEmpty()) {
//            filteredList.addAll(userList);
//        } else {
//            for (User user : userList) {
//                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
//                        user.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
//                    filteredList.add(user);
//                }
//            }
//        }
//        userAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}