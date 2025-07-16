package com.example.iuiutrash.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iuiutrash.BinSummaryActivity;
import com.example.iuiutrash.MainActivity;
import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.BinAdapter;
import com.example.iuiutrash.model.Bin;
import com.example.iuiutrash.model.BinModel;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.libs.HttpResult;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinListFragment extends Fragment {
    private static final String TAG = "BinListFragment";
    private static final long MIN_REFRESH_INTERVAL = 10000; // 10 seconds
    private long lastRefreshTime = 0;
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingRefresh = null;
    private User userData;
    
    public static BinListFragment newInstance(User userData) {
        BinListFragment fragment = new BinListFragment();
        Bundle args = new Bundle();
        args.putParcelable("userData", userData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userData = getArguments().getParcelable("userData");
        }
        api = new ServerApi(requireContext());
        api.setUsername(userData.getEmail());
        binList = new ArrayList<>();
    }
    
    private LinearLayout errViewer;
    private MaterialButton refreshButton;
    private TextView txtErrorLabel;
    private RecyclerView recyclerView;
    private ServerApi api;
    private MainActivity mainActivity;
    private BinAdapter adapter;
    private List<Bin> binList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private ProgressBar progressBar;
    private boolean isInitialized = false;
    private boolean isRefreshing = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_bin_list, container, false);
            
            // Initialize views
            recyclerView = view.findViewById(R.id.recyclerView);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            emptyView = view.findViewById(R.id.emptyView);
            progressBar = view.findViewById(R.id.progressBar);
            
            // Set up RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new BinAdapter(binList, api, userData, this::loadBins);
            recyclerView.setAdapter(adapter);
            
            // Set up SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener(this::loadBins);
            
            // Initial data load
            loadBins();
            
            isInitialized = true;
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing bin list: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }

    private void loadBins() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", userData.getEmail());

        api.bins(params, result -> {
            if (isAdded()) {
                swipeRefreshLayout.setRefreshing(false);
                if (result.status) {
                    if (result.hasData()) {
                        binList.clear();
                        // Convert BinModel objects to Bin objects
                        for (Object obj : result.getValues()) {
                            if (obj instanceof BinModel) {
                                BinModel model = (BinModel) obj;
                                Bin bin = new Bin();
                                bin.setBinId(model.getBinid());
                                bin.setBinCode(model.getBincode());
                                bin.setCurrentLevel(model.getCurrentLevel());
                                bin.setBinStatus(model.getBinStatus());
                                bin.setLocation(model.getLocation());
                                binList.add(bin);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView(false);
                    } else {
                        updateEmptyView(true);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    updateEmptyView(true);
                }
            }
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingRefresh != null) {
            refreshHandler.removeCallbacks(pendingRefresh);
            pendingRefresh = null;
        }
    }
}