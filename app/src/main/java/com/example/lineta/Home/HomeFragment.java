package com.example.lineta.Home;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    List<Post> list = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPost);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list.add(new Post("Professor", "Hello World!", R.drawable.mountain_background));
        list.add(new Post("Professor", "Hello everyone, I'm Professor from Money Heist! Nice to meet you! 🔥", R.drawable.post_founded));
        list.add(new Post("Professor", "Hello World Post 2!", R.drawable.mountain_background));
        list.add(new Post("Professor", "Hello World Post 3!", R.drawable.mountain_background));

        PostAdapter adapter = new PostAdapter(list);
        recyclerView.setAdapter(adapter);

        return view;
    }
}