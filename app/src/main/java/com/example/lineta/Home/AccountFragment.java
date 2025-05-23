package com.example.lineta.Home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lineta.Adapter.SettingAdapter;
import com.example.lineta.R;

import java.util.Arrays;


public class AccountFragment extends Fragment {
    RecyclerView recyclerView;
    SettingAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String[] settings = {"Profile Settings", "Change Password", "Notification Settings"};
        adapter = new SettingAdapter(Arrays.asList(settings));
        recyclerView.setAdapter(adapter);

        return view;

    }
}