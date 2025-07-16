package com.example.iuiutrash;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iuiutrash.adapters.BinDataAdapter;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.libs.HttpResult;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class BinSummaryActivity extends AppCompatActivity {
    private LinearLayout errViewer;
    private MaterialButton refreshButton;
    private TextView txtErrorLabel;
    private RecyclerView recyclerView;
    private ServerApi api;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.futurebuilder_view);
        Context context = this;
        int binID = getIntent().getIntExtra("binid", -1);
        api = new ServerApi(context);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_divider));

        errViewer = findViewById(R.id.errViewer);
        refreshButton = findViewById(R.id.refreshButton);
        txtErrorLabel = findViewById(R.id.txt_error);
        recyclerView = findViewById(R.id.listview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(divider);

        refreshButton.setOnClickListener(v -> LoadData(context, binID));

        errViewer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        LoadData(context, binID);
    }

    void LoadData(Context context, int binID) {
        recyclerView.setVisibility(View.GONE);
        errViewer.setVisibility(View.GONE);

        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("binid", String.valueOf(binID));

        api.binData(params, result -> {
            if (!result.status || (result.status && !result.hasData())) {
                errViewer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                if (!result.status) {
                    txtErrorLabel.setText(result.message);
                } else {
                    txtErrorLabel.setText("No Results found");
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                errViewer.setVisibility(View.GONE);
                BinDataAdapter adapter = getAdapter(context, result);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @NonNull
    private BinDataAdapter getAdapter(Context context, HttpResult result) {
        BinDataAdapter adapter = new BinDataAdapter();
        adapter.updateData(result.getValues());
        return adapter;
    }
}
