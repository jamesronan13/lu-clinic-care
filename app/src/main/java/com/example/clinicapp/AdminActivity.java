package com.example.clinicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.clinicapp.AdminFragment.AdminAppointmentFragment;
import com.example.clinicapp.AdminFragment.AdminHomeFragment;
import com.example.clinicapp.AdminFragment.AdminAppointmentHistoryFragment;
import com.example.clinicapp.AdminFragment.AdminManageAvailableDayFragment;
import com.example.clinicapp.AdminFragment.AdminViewProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private androidx.fragment.app.Fragment currentFragment; // To keep track of the current fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = findViewById(R.id.toolbar); // Ignore red line errors
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            // Initially load the AdminHomeFragment
            currentFragment = new AdminHomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current fragment's tag
        if (currentFragment != null) {
            getSupportFragmentManager().putFragment(outState, "CURRENT_FRAGMENT", currentFragment);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the current fragment
        if (savedInstanceState.containsKey("CURRENT_FRAGMENT")) {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "CURRENT_FRAGMENT");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();
            androidx.fragment.app.Fragment selectedFragment = null;

            if (id == R.id.nav_adminhome) {
                selectedFragment = new AdminHomeFragment();
            } else if (id == R.id.nav_manage_available_day) {
                selectedFragment = new AdminManageAvailableDayFragment();
            } else if (id == R.id.nav_admin_view_appointment) {
                selectedFragment = new AdminAppointmentFragment();
            } else if (id == R.id.nav_admin_manage_appointment) {
                selectedFragment = new AdminAppointmentHistoryFragment();
            } else if (id == R.id.nav_admin_view_profile) {
                selectedFragment = new AdminViewProfileFragment();
            } else if (id == R.id.nav_logout) {
                logout(); // Call the logout method
            }

            // Check if the fragment to be replaced is the same as the current one
            if (selectedFragment != null && !selectedFragment.getClass().equals(currentFragment.getClass())) {
                currentFragment = selectedFragment;
                // Add the transaction to the back stack
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null) // Add to back stack
                        .commit();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            Log.e("NavigationError", "Error during navigation: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while navigating.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void logout() {
        try {
            // Sign out the user from Firebase (if you haven't already done so)
            FirebaseAuth.getInstance().signOut();

            // Navigate back to LoginActivity
            Intent intent = new Intent(AdminActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close AdminActivity
        } catch (Exception e) {
            Log.e("LogoutError", "Error during logout: " + e.getMessage(), e);
            Toast.makeText(AdminActivity.this, "An error occurred while logging out.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            // Check if the drawer is open and close it if so
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                // Check if there is a fragment in the back stack
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Pop the fragment from the back stack
                    getSupportFragmentManager().popBackStack();
                } else {
                    // If no fragments are in the back stack, close the activity
                    super.onBackPressed();
                }
            }
        } catch (Exception e) {
            Log.e("BackPressError", "Error during onBackPressed: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while navigating back.", Toast.LENGTH_SHORT).show();
        }
    }
}