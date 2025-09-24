package com.example.clinicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.clinicapp.UsersFragment.AboutFragment;
import com.example.clinicapp.UsersFragment.AppointmentFragment;
import com.example.clinicapp.UsersFragment.HomeFragment;
import com.example.clinicapp.UsersFragment.MainProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavigationItemSelected);

        // Load the home fragment on startup
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        try {
            // Handle Bottom Navigation
            int id = item.getItemId();
            Fragment fragment = null;
            String tag = "";

            // Determine which fragment to load based on selected item
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
                tag = HomeFragment.class.getSimpleName();
            } else if (id == R.id.nav_profile) {
                fragment = new MainProfileFragment();
                tag = MainProfileFragment.class.getSimpleName();
            } else if (id == R.id.nav_appointment) {
                fragment = new AppointmentFragment();
                tag = AppointmentFragment.class.getSimpleName();
            }

            // Check if the selected fragment is already displayed
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment != null && currentFragment.getClass().getSimpleName().equals(tag)) {
                // If the current fragment is the same as the selected one, don’t replace it
                return false;
            }

            // Set the appropriate custom animations based on the current fragment
            int enterAnim = 0, exitAnim = 0, popEnterAnim = 0, popExitAnim = 0;

            // Define animations based on the transition
            if (currentFragment instanceof HomeFragment) {
                if (fragment instanceof AppointmentFragment) {
                    enterAnim = R.anim.slide_in_right;
                    exitAnim = R.anim.slide_out_left;
                    popEnterAnim = R.anim.slide_in_left;
                    popExitAnim = R.anim.slide_out_right;
                } else if (fragment instanceof MainProfileFragment) {
                    enterAnim = R.anim.slide_in_right;
                    exitAnim = R.anim.slide_out_left;
                    popEnterAnim = R.anim.slide_in_left;
                    popExitAnim = R.anim.slide_out_right;
                }
            } else if (currentFragment instanceof AppointmentFragment) {
                if (fragment instanceof MainProfileFragment) {
                    enterAnim = R.anim.slide_in_right;
                    exitAnim = R.anim.slide_out_left;
                    popEnterAnim = R.anim.slide_in_left;
                    popExitAnim = R.anim.slide_out_right;
                } else if (fragment instanceof HomeFragment) {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                    popEnterAnim = R.anim.slide_in_right;
                    popExitAnim = R.anim.slide_out_left;
                }
            } else if (currentFragment instanceof MainProfileFragment) {
                if (fragment instanceof AppointmentFragment) {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                    popEnterAnim = R.anim.slide_in_right;
                    popExitAnim = R.anim.slide_out_left;
                } else if (fragment instanceof HomeFragment) {
                    enterAnim = R.anim.slide_in_left;
                    exitAnim = R.anim.slide_out_right;
                    popEnterAnim = R.anim.slide_in_right;
                    popExitAnim = R.anim.slide_out_left;
                }
            }

            // Ensure the fragment transaction is performed with custom animations
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                        .replace(R.id.fragment_container, fragment, tag)
                        .addToBackStack(tag) // Add fragment to back stack to allow navigating back
                        .commit();
            }

            return true;
        } catch (Exception e) {
            // Log and show a toast if any unexpected exception occurs
            Log.e("BottomNavError", "Error handling bottom navigation item: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while processing the navigation.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
