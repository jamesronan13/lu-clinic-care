package com.example.clinicapp.UsersFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.clinicapp.PrivacyPolicy;
import com.example.clinicapp.R;
import com.example.clinicapp.TermsAndConditions;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        // Facebook TextView
        TextView facebookTextView = rootView.findViewById(R.id.facebookTextView);
        facebookTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the Facebook page URL
                String url = "https://www.facebook.com/lumdc2020";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        // Terms and Conditions TextView
        TextView termsTextView = rootView.findViewById(R.id.termsTextView);
        termsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Terms and Conditions Activity
                Intent intent = new Intent(getContext(), TermsAndConditions.class);
                startActivity(intent);
            }
        });

        // Privacy Policy TextView
        TextView privacyPolicyTextView = rootView.findViewById(R.id.privacyPolicyTextView);
        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Privacy Policy Activity
                Intent intent = new Intent(getContext(), PrivacyPolicy.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
