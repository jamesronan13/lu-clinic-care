    package com.example.clinicapp.AdminFragment;
    
    import android.graphics.Color;
    import android.os.Bundle;
    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;

    import android.text.Spannable;
    import android.text.SpannableString;
    import android.text.SpannableStringBuilder;
    import android.text.style.StyleSpan;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.appcompat.app.AlertDialog;
    import com.example.clinicapp.R;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Locale;

    import android.widget.ArrayAdapter;
    import android.widget.Spinner;
    import android.widget.AdapterView;
    
    public class AdminAppointmentFragment extends Fragment {
    
        private LinearLayout appointmentLayout;
        private DatabaseReference databaseReference;
        private Spinner clinicFilterSpinner, dateFilterSpinner; // Declare the new Spinner
        private List<String> uniqueDates; // List to store unique dates
    
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_admin_upcoming_appointment, container, false);

            // Initialize Firebase reference
            databaseReference = FirebaseDatabase.getInstance().getReference("appointments");

            // Initialize UI elements
            appointmentLayout = view.findViewById(R.id.appointmentLayout);
            clinicFilterSpinner = view.findViewById(R.id.clinicFilterSpinner);
            dateFilterSpinner = view.findViewById(R.id.dateFilterSpinner); // Initialize the new Spinner

            // Set up the Spinner for filtering clinics
            String[] clinicOptions = {"Show All", "Dental Clinic", "Medical Clinic"};
            ArrayAdapter<String> clinicAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, clinicOptions);
            clinicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            clinicFilterSpinner.setAdapter(clinicAdapter);

            // Set listener for clinic filter Spinner
            clinicFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedOption = clinicOptions[position];
                    String clinicTypeFilter = selectedOption.equals("Show All") ? null : selectedOption;
                    fetchAppointments(clinicTypeFilter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    fetchAppointments(null);
                }
            });

            // Set up the Spinner for filtering dates
            uniqueDates = new ArrayList<>(); // Initialize the list for unique dates
            ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, uniqueDates);
            dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dateFilterSpinner.setAdapter(dateAdapter);

            // Set listener for date filter Spinner
            dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (uniqueDates != null && !uniqueDates.isEmpty() && position >= 0 && position < uniqueDates.size()) {
                        String selectedDate = uniqueDates.get(position);
                        String clinicTypeFilter = clinicFilterSpinner.getSelectedItem().toString();
                        clinicTypeFilter = clinicTypeFilter.equals("Show All") ? null : clinicTypeFilter;
                        fetchAppointmentsByDate(selectedDate, clinicTypeFilter); // Pass both date and clinic type
                    } else {
                        Log.e("AdminFragment", "Invalid selection or empty date list.");
                        Toast.makeText(getContext(), "No available dates to filter.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    fetchAppointments(null);
                }
            });

            // Fetch all appointments by default
            fetchAppointments(null);

            return view;
        }

        private void fetchAppointments(String clinicTypeFilter) {
            Log.d("AdminFragment", "Fetching appointments with filter: " + clinicTypeFilter);
            appointmentLayout.removeAllViews(); // Clear previous views

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        Log.w("AdminFragment", "No appointments found in the database.");
                        uniqueDates.clear();
                        uniqueDates.add("No dates available"); // Placeholder for empty state
                        ArrayAdapter<String> dateAdapter = (ArrayAdapter<String>) dateFilterSpinner.getAdapter();
                        dateAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "No appointments available.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uniqueDates.clear(); // Clear dates only when valid data exists
                    Log.d("AdminFragment", "Appointments snapshot retrieved. Processing data...");

                    try {
                        for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                            String clinicType = clinicSnapshot.getKey();

                            // Apply filter if it's set
                            if (clinicTypeFilter == null || clinicTypeFilter.equals(clinicType)) {
                                for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                    String date = dateSnapshot.getKey();

                                    // Add unique dates to the list
                                    if (!uniqueDates.contains(date)) {
                                        uniqueDates.add(date);
                                        Log.d("AdminFragment", "Added unique date: " + date);
                                    }
                                }
                            }
                        }

                        // Sort unique dates
                        uniqueDates.sort((date1, date2) -> {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                            try {
                                return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
                            } catch (ParseException e) {
                                Log.e("AdminFragment", "Error parsing date: " + e.getMessage(), e);
                                return 0; // Return 0 to leave the order unchanged on error
                            }
                        });

                        // Update Spinner and notify adapter
                        ArrayAdapter<String> dateAdapter = (ArrayAdapter<String>) dateFilterSpinner.getAdapter();
                        dateAdapter.notifyDataSetChanged();

                        // Select first date if available and fetch data for it
                        if (!uniqueDates.isEmpty() && !uniqueDates.contains("No dates available")) {
                            String firstDate = uniqueDates.get(0);
                            dateFilterSpinner.setSelection(0); // Set the spinner to the first date
                            String initialClinicTypeFilter = clinicFilterSpinner.getSelectedItem().toString();
                            initialClinicTypeFilter = initialClinicTypeFilter.equals("Show All") ? null : initialClinicTypeFilter;

                            // Fetch data for the first date
                            fetchAppointmentsByDate(firstDate, initialClinicTypeFilter);
                        } else {
                            Toast.makeText(getContext(), "No dates available for filtering.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("AdminFragment", "Error fetching appointments: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "An error occurred while fetching appointments.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AdminFragment", "Failed to load appointments: " + error.getMessage(), error.toException());
                    Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void fetchAppointmentsByDate(String selectedDate, String clinicTypeFilter) {
            Log.d("AdminFragment", "Fetching appointments for date: " + selectedDate + " with clinic filter: " + clinicTypeFilter);
            appointmentLayout.removeAllViews(); // Clear previous views

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!snapshot.exists()) {
                            Log.w("AdminFragment", "No appointments found for date: " + selectedDate);
                            Toast.makeText(getContext(), "No appointments available for the selected date.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("AdminFragment", "Appointments snapshot retrieved for date: " + selectedDate);
                        for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                            String clinicType = clinicSnapshot.getKey();

                            // Only fetch appointments if clinicType matches the filter (or if no filter is set)
                            if (clinicTypeFilter == null || clinicTypeFilter.equals(clinicType)) {
                                for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                    String date = dateSnapshot.getKey();

                                    // Only fetch appointments for the selected date
                                    if (selectedDate.equals(date)) {
                                        for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                            HashMap<String, Object> appointmentData = (HashMap<String, Object>) appointmentSnapshot.getValue();
                                            if (appointmentData == null) {
                                                Log.e("AdminFragment", "Appointment data is null for snapshot: " + appointmentSnapshot.getKey());
                                                continue;
                                            }

                                            String serviceType = (String) appointmentData.get("serviceType");
                                            String timeSlot = (String) appointmentData.get("timeSlot");
                                            String email = (String) appointmentData.get("email");
                                            String userId = (String) appointmentData.get("userId");
                                            String appointmentId = appointmentSnapshot.getKey();

                                            // Fetch user details
                                            fetchUserDetails(userId, clinicType, serviceType, date, timeSlot, email, appointmentId);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("AdminFragment", "Error fetching appointments by date: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "An error occurred while fetching appointments.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AdminFragment", "Failed to load appointments by date: " + error.getMessage(), error.toException());
                    Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void fetchUserDetails(String userId, String clinicType, String serviceType, String date, String timeSlot, String email, String appointmentId) {
            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    // Ensure the fragment is still attached to an activity before proceeding
                    if (getContext() == null || !isAdded()) {
                        Log.w("AdminFragment", "Fragment is not attached. Skipping update.");
                        return;
                    }

                    try {
                        if (userSnapshot.exists()) {
                            String firstName = userSnapshot.child("firstName").getValue(String.class);
                            String middleName = userSnapshot.child("middleName").getValue(String.class);
                            String lastName = userSnapshot.child("lastName").getValue(String.class);
                            String dob = userSnapshot.child("dob").getValue(String.class); // Fetch Date of Birth
                            String gender = userSnapshot.child("gender").getValue(String.class); // Fetch Gender
                            String patientName = firstName + " " + (middleName != null ? middleName + " " : "") + lastName;

                            // Display appointment details
                            LinearLayout appointmentItem = new LinearLayout(getContext());
                            appointmentItem.setOrientation(LinearLayout.VERTICAL);
                            appointmentItem.setPadding(32, 55, 32, 55);

                            // Create SpannableString for appointment details with bold labels
                            SpannableString appointmentDetails = new SpannableString(
                                    "Clinic: " + clinicType + "\n" +
                                            "Patient's Name: " + patientName + "\n" +
                                            "Service: " + serviceType + "\n" +
                                            "Date: " + date + "\n" +
                                            "Time Slot: " + timeSlot + "\n" +
                                            "Email: " + email + "\n" +
                                            "Appointment ID: " + appointmentId + "\n"
                            );

                            // Making specific parts bold
                            setBold(appointmentDetails, "Clinic:");
                            setBold(appointmentDetails, "Patient's Name:");
                            setBold(appointmentDetails, "Service:");
                            setBold(appointmentDetails, "Date:");
                            setBold(appointmentDetails, "Time Slot:");
                            setBold(appointmentDetails, "Email:");
                            setBold(appointmentDetails, "Appointment ID:");

                            TextView appointmentDetailsView = new TextView(getContext());
                            appointmentDetailsView.setText(appointmentDetails);

                            appointmentItem.addView(appointmentDetailsView);
                            appointmentLayout.addView(appointmentItem);

                            // Action buttons
                            Button cancelButton = new Button(getContext());
                            cancelButton.setBackgroundColor(Color.parseColor("#F08080"));
                            cancelButton.setText("Cancel");
                            cancelButton.setOnClickListener(v -> updateAppointmentStatus(clinicType, date, appointmentId, "Cancelled"));

                            Button completeButton = new Button(getContext());
                            completeButton.setBackgroundColor(Color.parseColor("#98FB98"));
                            completeButton.setText("Complete");
                            completeButton.setOnClickListener(v -> updateAppointmentStatus(clinicType, date, appointmentId, "Completed"));

                            Button viewProfileButton = new Button(getContext());
                            viewProfileButton.setBackgroundColor(Color.parseColor("#ADD8E6"));
                            viewProfileButton.setText("View Profile");
                            viewProfileButton.setOnClickListener(v -> fetchMedicalRecords(userId, firstName, middleName, lastName, dob, email, gender));

                            appointmentItem.addView(cancelButton);
                            appointmentItem.addView(completeButton);
                            appointmentItem.addView(viewProfileButton);

                            // Add divider between appointments
                            View divider = new View(getContext());
                            divider.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2 // Thickness of the divider
                            ));
                            divider.setBackgroundColor(Color.GRAY); // Divider color

                            appointmentLayout.addView(divider);
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "User not found.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("AdminFragment", "Error fetching user details: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "An error occurred while fetching user details.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AdminFragment", "Failed to load user details: " + error.getMessage(), error.toException());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load user details.", Toast.LENGTH_SHORT).show();
                    }
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

                        // Construct details with bold labels
                        SpannableStringBuilder medicalDetails = new SpannableStringBuilder();

                        appendBoldText(medicalDetails, "Name: ");
                        medicalDetails.append(firstName).append(" ").append(middleName != null ? middleName + " " : "").append(lastName).append("\n");

                        appendBoldText(medicalDetails, "Date of Birth: ");
                        medicalDetails.append(dob).append("\n");

                        appendBoldText(medicalDetails, "Email: ");
                        medicalDetails.append(email).append("\n");

                        appendBoldText(medicalDetails, "Gender: ");
                        medicalDetails.append(gender).append("\n\n");

                        appendBoldText(medicalDetails, "Medical History:\n");
                        appendBoldText(medicalDetails, "Allergies: ");
                        medicalDetails.append(allergies != null ? allergies : "None").append("\n");

                        appendBoldText(medicalDetails, "Family History: ");
                        medicalDetails.append(familyHistory != null ? familyHistory : "None").append("\n");

                        appendBoldText(medicalDetails, "Immunizations: ");
                        medicalDetails.append(immunizations != null ? immunizations : "None").append("\n");

                        appendBoldText(medicalDetails, "Medications: ");
                        medicalDetails.append(medications != null ? medications : "None").append("\n");

                        appendBoldText(medicalDetails, "Past Medical History: ");
                        medicalDetails.append(pastMedicalHistory != null ? pastMedicalHistory : "None").append("\n\n");

                        appendBoldText(medicalDetails, "Social History:\n");
                        appendBoldText(medicalDetails, "Alcohol Use: ");
                        medicalDetails.append(alcoholUse != null ? alcoholUse : "None").append("\n");

                        appendBoldText(medicalDetails, "Diet: ");
                        medicalDetails.append(diet != null ? diet : "None").append("\n");

                        appendBoldText(medicalDetails, "Drug Use: ");
                        medicalDetails.append(drugUse != null ? drugUse : "None").append("\n");

                        appendBoldText(medicalDetails, "Exercise: ");
                        medicalDetails.append(exercise != null ? exercise : "None").append("\n");

                        appendBoldText(medicalDetails, "Occupation: ");
                        medicalDetails.append(occupation != null ? occupation : "None").append("\n");

                        appendBoldText(medicalDetails, "Smoking: ");
                        medicalDetails.append(smoking != null ? smoking : "None");

                        // Display medical details in an AlertDialog
                        new AlertDialog.Builder(getContext())
                                .setTitle("Client's Profile and Medical Records")
                                .setMessage(medicalDetails)
                                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                .show();

                    } catch (Exception e) {
                        // Handle any exceptions that occur while fetching or processing the data
                        Toast.makeText(getContext(), "Error retrieving medical records: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load medical records.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Helper method to append bold text
        private void appendBoldText(SpannableStringBuilder builder, String text) {
            try {
                int start = builder.length();
                builder.append(text);
                builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                // Handle any exceptions that may occur
                Log.e("appendBoldText", "Error while appending bold text: " + e.getMessage());
            }
        }

        // Helper method to make specific part of text bold
        private void setBold(SpannableString spannableString, String textToBold) {
            try {
                int start = spannableString.toString().indexOf(textToBold);
                int end = start + textToBold.length();
                if (start != -1) {
                    spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Exception e) {
                // Handle any exceptions that may occur
                Log.e("setBold", "Error while setting bold text: " + e.getMessage());
            }
        }

        private void updateAppointmentStatus(String clinicType, String date, String appointmentId, String status) {
            try {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm " + status)
                        .setMessage("Are you sure you want to mark this appointment as " + status + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            DatabaseReference clinicReference = databaseReference.child(clinicType).child(date).child(appointmentId);

                            // Update appointment status
                            clinicReference.child("status").setValue(status)
                                    .addOnSuccessListener(aVoid -> {
                                        try {
                                            archiveAndRemoveAppointment(clinicType, date, appointmentId, status);
                                            Toast.makeText(getContext(), "Appointment marked as " + status, Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e("updateAppointmentStatus", "Error in archiving or removing appointment: " + e.getMessage());
                                            Toast.makeText(getContext(), "Error occurred while processing the appointment.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("updateAppointmentStatus", "Failed to update status: " + e.getMessage());
                                        Toast.makeText(getContext(), "Failed to update status.", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            } catch (Exception e) {
                Log.e("updateAppointmentStatus", "Error in updating appointment status: " + e.getMessage());
                Toast.makeText(getContext(), "Error occurred while updating appointment status.", Toast.LENGTH_SHORT).show();
            }
        }

        private void archiveAndRemoveAppointment(String clinicType, String date, String appointmentId, String status) {
            try {
                DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("appointmentHistory")
                        .child(clinicType).child(date).child(appointmentId);

                // Copy data to history and remove from main appointments
                databaseReference.child(clinicType).child(date).child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                HashMap<String, Object> appointmentData = (HashMap<String, Object>) snapshot.getValue();
                                if (appointmentData != null) {
                                    // Prepare the data to be stored in the history
                                    HashMap<String, Object> historyData = new HashMap<>();
                                    historyData.put("clinicType", appointmentData.get("clinicType"));
                                    historyData.put("clinicLocation", appointmentData.get("clinicLocation"));
                                    historyData.put("date", appointmentData.get("date"));
                                    historyData.put("status", status);
                                    historyData.put("serviceType", appointmentData.get("serviceType"));
                                    historyData.put("timeSlot", appointmentData.get("timeSlot"));
                                    historyData.put("email", appointmentData.get("email"));
                                    historyData.put("timestamp", appointmentData.get("timestamp"));
                                    historyData.put("userId", appointmentData.get("userId"));

                                    // Save the data to appointmentHistory
                                    historyRef.setValue(historyData)
                                            .addOnSuccessListener(aVoid -> {
                                                try {
                                                    // Remove the appointment from the main appointments after archiving
                                                    databaseReference.child(clinicType).child(date).child(appointmentId).removeValue()
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Show AlertDialog instead of Toast
                                                                new AlertDialog.Builder(getContext())
                                                                        .setTitle("Appointment " + status)
                                                                        .setMessage("Appointment has been successfully " + status.toLowerCase() +
                                                                                ". You can check it in the appointment history.")
                                                                        .setPositiveButton("OK", (dialog, which) -> {
                                                                            // Refresh data after clicking OK
                                                                            fetchAppointments(null);
                                                                        })
                                                                        .show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("archiveAndRemove", "Failed to remove appointment: " + e.getMessage());
                                                                Toast.makeText(getContext(), "Failed to remove appointment.", Toast.LENGTH_SHORT).show();
                                                            });
                                                } catch (Exception e) {
                                                    Log.e("archiveAndRemove", "Error removing appointment: " + e.getMessage());
                                                    Toast.makeText(getContext(), "Error occurred while removing appointment.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("archiveAndRemove", "Failed to archive appointment: " + e.getMessage());
                                                Toast.makeText(getContext(), "Failed to archive appointment.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }
                        } catch (Exception e) {
                            Log.e("archiveAndRemove", "Error in processing appointment data: " + e.getMessage());
                            Toast.makeText(getContext(), "Error occurred while processing appointment data.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("archiveAndRemove", "Failed to retrieve appointment data: " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to retrieve appointment data.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("archiveAndRemove", "Error in archiving and removing appointment: " + e.getMessage());
                Toast.makeText(getContext(), "Error occurred while archiving and removing appointment.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
