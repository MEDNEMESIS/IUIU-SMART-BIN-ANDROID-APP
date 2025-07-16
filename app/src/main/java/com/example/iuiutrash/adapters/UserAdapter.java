package com.example.iuiutrash.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iuiutrash.R;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> userList;
    private final ServerApi api;
    private final User currentUser;
    private final Runnable onDataChangeListener;

    public UserAdapter(List<User> userList, ServerApi api, User currentUser, Runnable onDataChangeListener) {
        this.userList = userList;
        this.api = api;
        this.currentUser = currentUser;
        this.onDataChangeListener = onDataChangeListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<User> newUsers) {
        userList.clear();
        userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView emailText;
        private final TextView fullNameText;
        private final TextView roleText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.emailText);
            fullNameText = itemView.findViewById(R.id.fullNameText);
            roleText = itemView.findViewById(R.id.roleText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(User user) {
            emailText.setText(user.getEmail());
            fullNameText.setText(user.getFullname());
            roleText.setText(user.getUserrole());

            // Only show edit/delete buttons for admin users
            boolean showControls = currentUser.canManageUsers() && !user.equals(currentUser);
            editButton.setVisibility(showControls ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(showControls ? View.VISIBLE : View.GONE);

            editButton.setOnClickListener(v -> showEditDialog(user));
            deleteButton.setOnClickListener(v -> showDeleteDialog(user));
        }

        private void showEditDialog(User user) {
            View dialogView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.dialog_add_user, null);

            TextInputEditText emailInput = dialogView.findViewById(R.id.emailEditText);
            TextInputEditText fullNameInput = dialogView.findViewById(R.id.fullNameEditText);
            TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordEditText);
            AutoCompleteTextView roleDropdown = dialogView.findViewById(R.id.roleDropdown);

            // Pre-fill existing data
            emailInput.setText(user.getEmail());
            fullNameInput.setText(user.getFullname());
            
            // Set up role dropdown
            String[] roles = new String[]{"admin", "collector"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_dropdown_item_1line, roles);
            roleDropdown.setAdapter(adapter);
            roleDropdown.setText(user.getUserrole(), false);

            AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Edit User")
                    .setView(dialogView)
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String email = emailInput.getText().toString().trim();
                    String fullName = fullNameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String role = roleDropdown.getText().toString().trim();

                    if (email.isEmpty() || fullName.isEmpty()) {
                        if (email.isEmpty()) emailInput.setError("Email is required");
                        if (fullName.isEmpty()) fullNameInput.setError("Full name is required");
                        return;
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("action", "edit");
                    params.put("username", currentUser.getEmail());
                    params.put("userid", String.valueOf(user.getUsid()));
                    params.put("email", email);
                    params.put("fullname", fullName);
                    params.put("userrole", role);
                    if (!password.isEmpty()) {
                        params.put("password", password);
                    }

                    api.users(params, result -> {
                        if (result.status) {
                            Toast.makeText(itemView.getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            onDataChangeListener.run();
                        } else {
                            Toast.makeText(itemView.getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });

            dialog.show();
        }

        private void showDeleteDialog(User user) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Map<String, String> params = new HashMap<>();
                        params.put("action", "delt");
                        params.put("username", currentUser.getEmail());
                        params.put("userid", String.valueOf(user.getUsid()));

                        api.users(params, result -> {
                            if (result.status) {
                                Toast.makeText(itemView.getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                onDataChangeListener.run();
                            } else {
                                Toast.makeText(itemView.getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
} 