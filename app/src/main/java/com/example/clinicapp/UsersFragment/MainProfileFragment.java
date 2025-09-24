package com.example.clinicapp.UsersFragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.clinicapp.Login;
import com.example.clinicapp.PrivacyPolicy;
import com.example.clinicapp.R;
import com.example.clinicapp.TermsAndConditions;
import com.google.firebase.auth.FirebaseAuth;

public class MainProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_profile, container, false);

        // Setting up the click listeners for each section
        LinearLayout profileSection = view.findViewById(R.id.profileSection);
        LinearLayout healthProfileSection = view.findViewById(R.id.healthProfileSection);
        LinearLayout aboutUsSection = view.findViewById(R.id.aboutUsSection);
        LinearLayout termsAndConditionsSection = view.findViewById(R.id.termsAndConditionsSection);
        LinearLayout privacyPolicySection = view.findViewById(R.id.privacyPolicySection);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Navigation to ProfileFragment
        profileSection.setOnClickListener(v -> navigateToProfileFragment());

        // Navigation to AdditionalInfoFragment
        healthProfileSection.setOnClickListener(v -> navigateToHealthProfileFragment());

        // Navigation to AboutFragment
        aboutUsSection.setOnClickListener(v -> navigateToAboutUsFragment());

        // Navigation to TermsAndConditionsActivity
        termsAndConditionsSection.setOnClickListener(v -> navigateToTermsActivity());

        // Navigation to PrivacyPolicyActivity
        privacyPolicySection.setOnClickListener(v -> navigateToPrivacyPolicyActivity());

        // Logout Logic
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void navigateToProfileFragment() {
        try {
            // Create an instance of ProfileFragment
            ProfileFragment profileFragment = new ProfileFragment();

            // Begin the fragment transaction with custom animations
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left); // Animation for fragment transition
            transaction.replace(R.id.fragment_container, profileFragment);  // Adjust fragment container ID as necessary
            transaction.addToBackStack(null);  // Add the transaction to the back stack
            transaction.commit();  // Commit the transaction
        } catch (Exception e) {
            // Handle any exceptions that occur during the fragment transaction
            Log.e("FragmentNavigationError", "Error navigating to ProfileFragment: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating to your profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHealthProfileFragment() {
        try {
            // Create an instance of AdditionalInfoFragment
            AdditionalInfoFragment additionalInfoFragment = new AdditionalInfoFragment();

            // Begin the fragment transaction with custom animations
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left); // Animation for fragment transition
            transaction.replace(R.id.fragment_container, additionalInfoFragment);  // Adjust fragment container ID as necessary
            transaction.addToBackStack(null);  // Add the transaction to the back stack
            transaction.commit();  // Commit the transaction
        } catch (Exception e) {
            // Handle any exceptions that occur during the fragment transaction
            Log.e("FragmentNavigationError", "Error navigating to AdditionalInfoFragment: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating to the health profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAboutUsFragment() {
        try {
            // Create an instance of AboutFragment
            AboutFragment aboutFragment = new AboutFragment();

            // Begin the fragment transaction with custom animations
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left); // Animation for fragment transition
            transaction.replace(R.id.fragment_container, aboutFragment);  // Replace the current fragment
            transaction.addToBackStack(null);  // Add to the back stack
            transaction.commit();  // Commit the transaction
        } catch (Exception e) {
            // Handle any exceptions that occur during the fragment transaction
            Log.e("FragmentNavigationError", "Error navigating to AboutFragment: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating to the About Us section. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToTermsActivity() {
        try {
            // Create an intent to navigate to TermsAndConditions activity
            Intent intent = new Intent(getActivity(), TermsAndConditions.class);
            startActivity(intent);  // Start the activity
        } catch (Exception e) {
            // Handle any exceptions that might occur
            Log.e("ActivityNavigationError", "Error navigating to Terms and Conditions activity: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating to the Terms and Conditions. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPrivacyPolicyActivity() {
        try {
            // Create an intent to navigate to PrivacyPolicy activity
            Intent intent = new Intent(getActivity(), PrivacyPolicy.class);
            startActivity(intent);  // Start the activity
        } catch (Exception e) {
            // Handle any exceptions that might occur
            Log.e("ActivityNavigationError", "Error navigating to Privacy Policy activity: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating to the Privacy Policy. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        try {
            // Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Navigate back to LoginActivity
            Intent intent = new Intent(getActivity(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Close MainActivity (or the current activity)
            getActivity().finish();
        } catch (Exception e) {
            // Handle any exceptions that might occur
            Log.e("LogoutError", "Error during logout: " + e.getMessage(), e);

            // Notify the user in case of an error
            Toast.makeText(getContext(), "An error occurred during logout. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
