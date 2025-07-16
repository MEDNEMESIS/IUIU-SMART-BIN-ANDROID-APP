package com.example.iuiutrash.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.UserAdapter;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementFragment extends Fragment {
    private static final String TAG = "UserManagementFragment";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private FloatingActionButton fabAddUser;
    private ServerApi api;
    private User userData;
    private List<User> userList;
    private UserAdapter adapter;

    public static UserManagementFragment newInstance(User userData) {
        UserManagementFragment fragment = new UserManagementFragment();
        Bundle args = new Bundle();
        args.putParcelable("userData", userData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userData = getArguments().getParcelable("userData");
        }
        api = new ServerApi(requireContext());
        userList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddUser = view.findViewById(R.id.fabAddUser);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter(userList, api, userData, this::loadUsers);
        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);

        // Set up FAB - only show for admin users
        fabAddUser.setVisibility(userData.isAdmin() ? View.VISIBLE : View.GONE);
        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        // Initial data load
        loadUsers();

        return view;
    }

    private void loadUsers() {
        if (!userData.canManageUsers()) {
            Toast.makeText(requireContext(), "Unauthorized access", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", userData.getEmail());

        api.users(params, result -> {
            if (isAdded()) {
                swipeRefreshLayout.setRefreshing(false);
                if (result.status) {
                    if (result.hasData()) {
                        userList.clear();
                        userList.addAll(result.getValues());
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

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);
        TextInputEditText emailInput = dialogView.findViewById(R.id.emailEditText);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordEditText);
        TextInputEditText fullNameInput = dialogView.findViewById(R.id.fullNameEditText);
        TextInputLayout emailLayout = dialogView.findViewById(R.id.emailLayout);
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.passwordLayout);
        TextInputLayout fullNameLayout = dialogView.findViewById(R.id.fullNameLayout);
        TextInputLayout roleLayout = dialogView.findViewById(R.id.roleLayout);
        AutoCompleteTextView roleDropdown = dialogView.findViewById(R.id.roleDropdown);

        // Set up role dropdown
        String[] roles = new String[]{"admin", "collector"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        roleDropdown.setAdapter(adapter);
        roleDropdown.setText(roles[1], false); // Set default to collector

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view -> {
                // Get input values
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String fullName = fullNameInput.getText().toString().trim();
                String role = roleDropdown.getText().toString().trim();

                // Clear previous errors
                emailLayout.setError(null);
                passwordLayout.setError(null);
                fullNameLayout.setError(null);
                roleLayout.setError(null);

                // Validate inputs
                if (email.isEmpty()) {
                    emailLayout.setError("Email is required");
                    return;
                }
                if (password.isEmpty()) {
                    passwordLayout.setError("Password is required");
                    return;
                }
                if (fullName.isEmpty()) {
                    fullNameLayout.setError("Full name is required");
                    return;
                }
                if (role.isEmpty()) {
                    roleLayout.setError("Role is required");
                    return;
                }

                // Prepare parameters for API call
                Map<String, String> params = new HashMap<>();
                params.put("action", "new");
                params.put("username", userData.getEmail());
                params.put("email", email);
                params.put("password", password);
                params.put("fullname", fullName);
                params.put("userrole", role);

                // Call API to add user
                api.users(params, result -> {
                    if (result.status) {
                        Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUsers(); // Refresh the list
                    } else {
                        Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
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
} 