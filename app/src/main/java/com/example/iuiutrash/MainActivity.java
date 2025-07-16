package com.example.iuiutrash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.iuiutrash.fragments.BinListFragment;
import com.example.iuiutrash.fragments.HomeFragment;
import com.example.iuiutrash.fragments.TeamFragment;
import com.example.iuiutrash.fragments.ShareFragment;
import com.example.iuiutrash.fragments.AboutFragment;
import com.example.iuiutrash.fragments.ProfileFragment;
import com.example.iuiutrash.fragments.GreenInfoFragment;
import com.example.iuiutrash.fragments.GalleryFragment;
import com.example.iuiutrash.fragments.UserFeedbackFragment;
import com.example.iuiutrash.fragments.AddBinFragment;
import com.example.iuiutrash.fragments.UserManagementFragment;
import com.google.android.material.navigation.NavigationView;
import android.widget.Toast;
import android.widget.ImageView;
import com.example.iuiutrash.model.User;
import android.app.AlertDialog;
import com.example.iuiutrash.utils.UserManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private User currentUser;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private BinListFragment homeFragment;
    private UserFeedbackFragment userFeedbackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout inflated");

            // Initialize toolbar
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            // Get current user
            currentUser = UserManager.getInstance(this).getUser();
            if (currentUser == null) {
                Log.e(TAG, "No user logged in");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            // Initialize navigation drawer
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Set up drawer toggle
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // Update navigation header with user info
            updateNavigationHeader(currentUser);

            // Show/hide menu items based on user role
            updateMenuVisibility();

            // Load default fragment
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment());
                navigationView.setCheckedItem(R.id.nav_home);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing main screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateMenuVisibility() {
        MenuItem usersItem = navigationView.getMenu().findItem(R.id.nav_users);
        MenuItem addBinItem = navigationView.getMenu().findItem(R.id.nav_add_bin);
        MenuItem binsItem = navigationView.getMenu().findItem(R.id.nav_bins);

        if (currentUser != null) {
            // Admin-specific items
            if (usersItem != null) usersItem.setVisible(currentUser.canManageUsers());
            if (addBinItem != null) addBinItem.setVisible(currentUser.canManageBins());
            if (binsItem != null) binsItem.setVisible(true); // Always visible for logged-in users
            
            // Update the navigation header
            updateNavigationHeader(currentUser);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        } else if (itemId == R.id.nav_feedback) {
            if (currentUser.getUserrole().equals("admin")) {
                fragment = new com.example.iuiutrash.fragments.AdminFeedbackFragment();
                title = "Feedback Management";
            } else {
                fragment = new UserFeedbackFragment();
                title = "Submit Feedback";
            }
        } else if (itemId == R.id.nav_team) {
            fragment = new TeamFragment();
            title = "Team";
        } else if (itemId == R.id.nav_gallery) {
            fragment = new GalleryFragment();
            title = "Gallery";
        } else if (itemId == R.id.nav_share) {
            fragment = new ShareFragment();
            title = "Share";
        } else if (itemId == R.id.nav_green_info) {
            fragment = new GreenInfoFragment();
            title = "Green Info";
        } else if (itemId == R.id.nav_profile) {
            fragment = ProfileFragment.newInstance(currentUser);
            title = "Profile";
        } else if (itemId == R.id.nav_about) {
            fragment = new AboutFragment();
            title = "About";
        } else if (itemId == R.id.nav_bins) {
            fragment = BinListFragment.newInstance(currentUser);
            title = "Bins";
        } else if (itemId == R.id.nav_add_bin && currentUser.canManageBins()) {
            fragment = AddBinFragment.newInstance(currentUser);
            title = "Add Bin";
        } else if (itemId == R.id.nav_users && currentUser.canManageUsers()) {
            fragment = UserManagementFragment.newInstance(currentUser);
            title = "Users";
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
            getSupportActionBar().setTitle(title);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear any saved user data if needed
                    UserManager.getInstance(this).clearUser();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void updateNavigationHeader(User user) {
        try {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                ImageView profileImageView = headerView.findViewById(R.id.profileImageView);
                TextView userNameText = headerView.findViewById(R.id.userNameText);
                TextView userEmailText = headerView.findViewById(R.id.userEmailText);
                TextView userRoleText = headerView.findViewById(R.id.userRoleText);

                profileImageView.setImageResource(R.drawable.ic_launcher_foreground2);
                userNameText.setText(user.getFullname());
                userEmailText.setText(user.getEmail());
                userRoleText.setText(user.getUserrole());
            } else {
                Log.e(TAG, "Navigation header not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating navigation header: " + e.getMessage(), e);
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            if (fragment != null) {
                Log.d(TAG, "Loading fragment: " + fragment.getClass().getSimpleName());
                
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                Log.d(TAG, "Fragment transaction committed: " + fragment.getClass().getSimpleName());
            } else {
                Log.e(TAG, "Attempted to load null fragment");
                Toast.makeText(this, "Error loading content", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading content: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}