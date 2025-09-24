package com.example.clinicapp.AdminFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.example.clinicapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminViewProfileFragment extends Fragment {

    private ListView listView;
    private ArrayList<ClientProfile> clientProfiles;
    private ClientProfileAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_view_profiles, container, false);

        listView = view.findViewById(R.id.listView);
        clientProfiles = new ArrayList<>();
        adapter = new ClientProfileAdapter(getContext(), clientProfiles);
        listView.setAdapter(adapter);

        fetchClientProfiles();

        return view;
    }

    private void fetchClientProfiles() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    clientProfiles.clear();
                    for (DataSnapshot profileSnapshot : dataSnapshot.getChildren()) {
                        String userId = profileSnapshot.getKey();  // Get the user ID to fetch corresponding medical records
                        String firstName = profileSnapshot.child("firstName").getValue(String.class);
                        String middleName = profileSnapshot.child("middleName").getValue(String.class);
                        String lastName = profileSnapshot.child("lastName").getValue(String.class);
                        String dob = profileSnapshot.child("dob").getValue(String.class);
                        String email = profileSnapshot.child("email").getValue(String.class);
                        String gender = profileSnapshot.child("gender").getValue(String.class);

                        // Fetch medical records using the userId
                        fetchMedicalRecords(userId, firstName, middleName, lastName, dob, email, gender);
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("FetchClientProfiles", "Error fetching client profiles: " + e.getMessage(), e);
                    // Optionally, show a message to the user about the error.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("FetchClientProfiles", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchMedicalRecords(String userId, String firstName, String middleName, String lastName, String dob, String email, String gender) {
        DatabaseReference medicalRecordsReference = FirebaseDatabase.getInstance().getReference("medicalRecords").child(userId);

        medicalRecordsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot medicalDataSnapshot) {
                try {
                    // Fetch medical records data
                    String allergies = medicalDataSnapshot.child("allergies").getValue(String.class);
                    String familyHistory = medicalDataSnapshot.child("familyHistory").getValue(String.class);
                    String immunizations = medicalDataSnapshot.child("immunizations").getValue(String.class);
                    String medications = medicalDataSnapshot.child("medications").getValue(String.class);
                    String pastMedicalHistory = medicalDataSnapshot.child("pastMedicalHistory").getValue(String.class);

                    // Fetch social history
                    String alcoholUse = medicalDataSnapshot.child("socialHistory/AlcoholUse").getValue(String.class);
                    String diet = medicalDataSnapshot.child("socialHistory/Diet").getValue(String.class);
                    String drugUse = medicalDataSnapshot.child("socialHistory/DrugUse").getValue(String.class);
                    String exercise = medicalDataSnapshot.child("socialHistory/Exercise").getValue(String.class);
                    String occupation = medicalDataSnapshot.child("socialHistory/Occupation").getValue(String.class);
                    String smoking = medicalDataSnapshot.child("socialHistory/Smoking").getValue(String.class);

                    // Create a new ClientProfile including medical and social data
                    clientProfiles.add(new ClientProfile(firstName, middleName, lastName, dob, email, gender, allergies, familyHistory, immunizations, medications,
                            pastMedicalHistory, alcoholUse, diet, drugUse, exercise, occupation, smoking));

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("FetchMedicalRecords", "Error fetching medical records for user " + userId + ": " + e.getMessage(), e);
                    // Optionally, show an error message to the user.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchMedicalRecords", "Error accessing medical records for user " + userId + ": " + databaseError.getMessage());
                // Optionally, show a message to the user about the error.
            }
        });
    }

    private static class ClientProfile {
        String firstName, middleName, lastName, dob, email, gender;
        String allergies, familyHistory, immunizations, medications, pastMedicalHistory;
        String alcoholUse, diet, drugUse, exercise, occupation, smoking;

        ClientProfile(String firstName, String middleName, String lastName, String dob, String email, String gender,
                      String allergies, String familyHistory, String immunizations, String medications, String pastMedicalHistory,
                      String alcoholUse, String diet, String drugUse, String exercise, String occupation, String smoking) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.dob = dob;
            this.email = email;
            this.gender = gender;
            this.allergies = allergies;
            this.familyHistory = familyHistory;
            this.immunizations = immunizations;
            this.medications = medications;
            this.pastMedicalHistory = pastMedicalHistory;
            this.alcoholUse = alcoholUse;
            this.diet = diet;
            this.drugUse = drugUse;
            this.exercise = exercise;
            this.occupation = occupation;
            this.smoking = smoking;
        }

        String getFullName() {
            return firstName + " " + middleName + " " + lastName;
        }

        SpannableString getDetails() {
            try {
                String detailsText = "Name: " + getFullName() + "\nDate of Birth: " + dob + "\nEmail: " + email +
                        "\nGender: " + gender + "\n\nMedical Records:\nAllergies: " + allergies +
                        "\nFamily History: " + familyHistory + "\nImmunizations: " + immunizations +
                        "\nMedications: " + medications + "\nPast Medical History: " + pastMedicalHistory +
                        "\n\nSocial History:\nAlcohol Use: " + alcoholUse + "\nDiet: " + diet +
                        "\nDrug Use: " + drugUse + "\nExercise: " + exercise + "\nOccupation: " + occupation +
                        "\nSmoking: " + smoking;

                SpannableString spannableDetails = new SpannableString(detailsText);

                // Apply bold style to the labels
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, 0); // "Name: "
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Date of Birth:"), detailsText.indexOf("Date of Birth:") + 14, 0); // "Date of Birth:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Email:"), detailsText.indexOf("Email:") + 6, 0); // "Email:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Gender:"), detailsText.indexOf("Gender:") + 7, 0); // "Gender:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Medical Records:"), detailsText.indexOf("Medical Records:") + 17, 0); // "Medical Records:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Allergies:"), detailsText.indexOf("Allergies:") + 9, 0); // "Allergies:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Family History:"), detailsText.indexOf("Family History:") + 15, 0); // "Family History:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Immunizations:"), detailsText.indexOf("Immunizations:") + 14, 0); // "Immunizations:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Medications:"), detailsText.indexOf("Medications:") + 11, 0); // "Medications:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Past Medical History:"), detailsText.indexOf("Past Medical History:") + 21, 0); // "Past Medical History:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Social History:"), detailsText.indexOf("Social History:") + 15, 0); // "Social History:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Alcohol Use:"), detailsText.indexOf("Alcohol Use:") + 12, 0); // "Alcohol Use:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Diet:"), detailsText.indexOf("Diet:") + 4, 0); // "Diet:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Drug Use:"), detailsText.indexOf("Drug Use:") + 8, 0); // "Drug Use:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Exercise:"), detailsText.indexOf("Exercise:") + 8, 0); // "Exercise:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Occupation:"), detailsText.indexOf("Occupation:") + 11, 0); // "Occupation:"
                spannableDetails.setSpan(new StyleSpan(Typeface.BOLD), detailsText.indexOf("Smoking:"), detailsText.indexOf("Smoking:") + 8, 0); // "Smoking:"

                return spannableDetails;
            } catch (Exception e) {
                Log.e("ClientProfile", "Error formatting details for " + getFullName() + ": " + e.getMessage(), e);
                return new SpannableString("Error retrieving details.");
            }
        }
    }

    private class ClientProfileAdapter extends ArrayAdapter<ClientProfile> {
        Context context;
        ArrayList<ClientProfile> profiles;

        ClientProfileAdapter(Context context, ArrayList<ClientProfile> profiles) {
            super(context, R.layout.fragment_admin_view_profiles, profiles);
            this.context = context;
            this.profiles = profiles;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    // Create a parent LinearLayout
                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setPadding(16, 16, 16, 16);
                    layout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    // Create TextView for the name
                    TextView nameTextView = new TextView(context);
                    nameTextView.setId(View.generateViewId());
                    nameTextView.setTextSize(18);
                    nameTextView.setTextColor(getResources().getColor(R.color.black));
                    nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1)); // Assign weight to fill remaining space
                    layout.addView(nameTextView);

                    // Create Button for "View Profile"
                    Button viewProfileButton = new Button(context);
                    viewProfileButton.setText("View Profile");
                    viewProfileButton.setBackgroundColor(Color.parseColor("#ADD8E6"));
                    viewProfileButton.setId(View.generateViewId());
                    viewProfileButton.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    layout.addView(viewProfileButton);

                    // Assign layout as the convertView
                    convertView = layout;

                    // Set click listener for the button
                    viewProfileButton.setOnClickListener(v -> {
                        try {
                            ClientProfile profile = profiles.get(position);
                            showProfileDialog(profile);
                        } catch (Exception e) {
                            Log.e("ClientProfileAdapter", "Error displaying profile: " + e.getMessage(), e);
                            // Optionally, show a toast or alert dialog to notify the user of the error
                        }
                    });

                    // Bind the profile data to the views
                    ClientProfile profile = profiles.get(position);
                    nameTextView.setText(profile.getFullName());
                }
            } catch (Exception e) {
                Log.e("ClientProfileAdapter", "Error creating view for position " + position + ": " + e.getMessage(), e);
                // Optionally, return a fallback view with an error message
                TextView errorView = new TextView(context);
                errorView.setText("Error loading profile");
                return errorView;
            }
            return convertView;
        }

        private void showProfileDialog(ClientProfile profile) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Client Profile");

                // Set the spannable string as message
                builder.setMessage(profile.getDetails());

                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            } catch (Exception e) {
                Log.e("ClientProfileAdapter", "Error showing profile dialog: " + e.getMessage(), e);
                // Optionally, show a toast or alert dialog to notify the user of the error
            }
        }
    }
}
