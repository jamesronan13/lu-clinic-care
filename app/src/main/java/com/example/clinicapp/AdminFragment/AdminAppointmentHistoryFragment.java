package com.example.clinicapp.AdminFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
import android.graphics.Color;
import android.view.Gravity;

public class AdminAppointmentHistoryFragment extends Fragment {

    private LinearLayout appointmentLayout;
    private DatabaseReference databaseReference;
    private Spinner clinicFilterSpinner, dateFilterSpinner;
    private List<String> uniqueDates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_appointment_history, container, false);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("appointmentHistory");

        // Initialize UI elements
        appointmentLayout = view.findViewById(R.id.appointmentHistoryLayout);
        clinicFilterSpinner = view.findViewById(R.id.clinicFilterSpinner);
        dateFilterSpinner = view.findViewById(R.id.dateFilterSpinner);

        // Set up the Spinner for filtering clinics
        String[] clinicOptions = {"Show All", "Dental Clinic", "Medical Clinic"};
        ArrayAdapter<String> clinicAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, clinicOptions);
        clinicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clinicFilterSpinner.setAdapter(clinicAdapter);

        // Set up the Spinner for filtering dates
        uniqueDates = new ArrayList<>();
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, uniqueDates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateFilterSpinner.setAdapter(dateAdapter);

        // Set listeners for Spinners
        clinicFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = clinicOptions[position];
                String clinicTypeFilter = selectedOption.equals("Show All") ? null : selectedOption;
                fetchAppointments(clinicTypeFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchAppointments(null);  // Fetch all appointments when nothing is selected
            }
        });

        dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (uniqueDates.isEmpty()) {
                    // Inform the user that no dates are available
                    Toast.makeText(getContext(), "No dates available to filter.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed to fetch appointments for the selected date
                String selectedDate = uniqueDates.get(position);
                String clinicTypeFilter = clinicFilterSpinner.getSelectedItem().toString();
                clinicTypeFilter = clinicTypeFilter.equals("Show All") ? null : clinicTypeFilter;
                fetchAppointmentsByDate(selectedDate, clinicTypeFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchAppointments(null);  // Fetch all appointments when nothing is selected
            }
        });

        // Fetch all appointments by default
        fetchAppointments(null);

        return view;
    }

    private void fetchAppointments(String clinicTypeFilter) {
        Log.d("AdminFragment", "Fetching all appointments with filter: " + clinicTypeFilter);
        appointmentLayout.removeAllViews(); // Clear previous appointment views

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    uniqueDates.clear(); // Clear the dates list before adding new ones
                    if (!snapshot.exists()) {
                        Log.w("AdminFragment", "No appointments found in the database.");
                        Toast.makeText(getContext(), "No appointments available.", Toast.LENGTH_SHORT).show();

                        // Clear the spinner adapter and display a message
                        ArrayAdapter<String> dateAdapter = (ArrayAdapter<String>) dateFilterSpinner.getAdapter();
                        if (dateAdapter != null) {
                            dateAdapter.notifyDataSetChanged();
                        }
                        return;
                    }

                    Log.d("AdminFragment", "Processing appointment data...");
                    for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                        String clinicType = clinicSnapshot.getKey();
                        if (clinicTypeFilter == null || clinicTypeFilter.equals(clinicType)) {
                            for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                String date = dateSnapshot.getKey();
                                if (!uniqueDates.contains(date)) {
                                    uniqueDates.add(date);
                                }
                                for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                    try {
                                        HashMap<String, Object> appointmentData = (HashMap<String, Object>) appointmentSnapshot.getValue();
                                        displayAppointment(appointmentData, clinicType, date, appointmentSnapshot.getKey());
                                    } catch (Exception e) {
                                        Log.e("AdminFragment", "Error displaying appointment: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }

                    if (!uniqueDates.isEmpty()) {
                        // Sort the dates list
                        try {
                            uniqueDates.sort((date1, date2) -> {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                                try {
                                    return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
                                } catch (ParseException e) {
                                    Log.e("AdminFragment", "Error parsing dates: " + e.getMessage(), e);
                                    return 0;
                                }
                            });
                        } catch (Exception e) {
                            Log.e("AdminFragment", "Error sorting dates: " + e.getMessage(), e);
                        }

                        // Update Spinner adapter
                        try {
                            ArrayAdapter<String> dateAdapter = (ArrayAdapter<String>) dateFilterSpinner.getAdapter();
                            if (dateAdapter != null) {
                                dateAdapter.notifyDataSetChanged();
                            } else {
                                dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, uniqueDates);
                                dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dateFilterSpinner.setAdapter(dateAdapter);
                            }

                            Log.d("AdminFragment", "Defaulting to the first date: " + uniqueDates.get(0));
                            dateFilterSpinner.setSelection(0);
                            fetchAppointmentsByDate(uniqueDates.get(0), clinicTypeFilter);
                        } catch (Exception e) {
                            Log.e("AdminFragment", "Error updating Spinner adapter: " + e.getMessage(), e);
                        }
                    } else {
                        Log.w("AdminFragment", "No dates found after processing.");
                        Toast.makeText(getContext(), "No appointments available.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("AdminFragment", "Error processing appointments: " + e.getMessage(), e);
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
        Log.d("AdminFragment", "Fetching appointments for date: " + selectedDate + ", with filter: " + clinicTypeFilter);
        appointmentLayout.removeAllViews(); // Clear views for the new date

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.w("AdminFragment", "No appointments found for date: " + selectedDate);
                        Toast.makeText(getContext(), "No appointments available for the selected date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("AdminFragment", "Processing appointment data for date: " + selectedDate);
                    for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                        try {
                            String clinicType = clinicSnapshot.getKey();

                            if (clinicTypeFilter == null || clinicTypeFilter.equals(clinicType)) {
                                for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                    try {
                                        String date = dateSnapshot.getKey();

                                        if (selectedDate.equals(date)) {
                                            for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                                try {
                                                    HashMap<String, Object> appointmentData = (HashMap<String, Object>) appointmentSnapshot.getValue();
                                                    displayAppointment(appointmentData, clinicType, date, appointmentSnapshot.getKey());
                                                } catch (Exception e) {
                                                    Log.e("AdminFragment", "Error displaying appointment: " + e.getMessage(), e);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("AdminFragment", "Error processing date snapshot: " + e.getMessage(), e);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("AdminFragment", "Error processing clinic snapshot: " + e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    Log.e("AdminFragment", "Error processing appointments by date: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "An error occurred while fetching appointments for the selected date.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminFragment", "Failed to load appointments by date: " + error.getMessage(), error.toException());
                Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAppointment(HashMap<String, Object> appointmentData, String clinicType, String date, String appointmentId) {
        try {
            // Extract relevant details from appointmentData and display them
            String serviceType = (String) appointmentData.get("serviceType");
            String timeSlot = (String) appointmentData.get("timeSlot");
            String email = (String) appointmentData.get("email");
            String userId = (String) appointmentData.get("userId");
            String status = (String) appointmentData.get("status");

            // Layout for the appointment
            LinearLayout appointmentItem = new LinearLayout(getContext());
            appointmentItem.setOrientation(LinearLayout.VERTICAL);
            appointmentItem.setPadding(32, 55, 32, 55); // Updated padding for top and bottom
            appointmentItem.setBackgroundColor(Color.parseColor("#FFFFFF")); // Optional: Set background color for the appointment card

            try {
                // Create the details text with bold labels
                SpannableString appointmentDetails = new SpannableString(
                        "Clinic: " + clinicType + "\n" +
                                "Service: " + serviceType + "\n" +
                                "Date: " + date + "\n" +
                                "Time Slot: " + timeSlot + "\n" +
                                "Email: " + email + "\n" +
                                "Status: " + status + "\n" +
                                "Appointment ID: " + appointmentId + "\n"
                );

                // Making specific parts bold
                setBold(appointmentDetails, "Clinic:");
                setBold(appointmentDetails, "Service:");
                setBold(appointmentDetails, "Date:");
                setBold(appointmentDetails, "Time Slot:");
                setBold(appointmentDetails, "Email:");
                setBold(appointmentDetails, "Status:");
                setBold(appointmentDetails, "Appointment ID:");

                TextView appointmentDetailsView = new TextView(getContext());
                appointmentDetailsView.setText(appointmentDetails);

                // Add details to the appointment item
                appointmentItem.addView(appointmentDetailsView);
            } catch (Exception e) {
                Log.e("AdminFragment", "Error creating appointment details view: " + e.getMessage(), e);
            }

            try {
                // Add a Delete button
                Button deleteButton = new Button(getContext());
                deleteButton.setText("Delete");
                deleteButton.setBackgroundColor(Color.parseColor("#F08080")); // Set background color to Light Coral
                deleteButton.setTextColor(Color.WHITE); // Set text color to white
                deleteButton.setPadding(16, 16, 16, 16); // Optional: Add padding for a better look

                // Set LayoutParams to center the button and make it take up enough space
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.setMargins(0, 16, 0, 0); // Add top margin for spacing
                deleteButton.setLayoutParams(buttonParams);

                // Set the click listener for the delete button
                deleteButton.setOnClickListener(v -> {
                    try {
                        deleteAppointment(clinicType, date, appointmentId);
                    } catch (Exception ex) {
                        Log.e("AdminFragment", "Error deleting appointment: " + ex.getMessage(), ex);
                    }
                });

                // Add delete button to the appointment item
                appointmentItem.addView(deleteButton);
            } catch (Exception e) {
                Log.e("AdminFragment", "Error creating delete button: " + e.getMessage(), e);
            }

            try {
                // Add the appointment item to the layout
                appointmentLayout.addView(appointmentItem);

                // Add a divider below the appointment item
                View divider = new View(getContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Set divider thickness
                );
                dividerParams.setMargins(0, 16, 0, 16); // Optional: Add spacing around the divider
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.GRAY); // Divider color

                // Add the divider to the layout
                appointmentLayout.addView(divider);
            } catch (Exception e) {
                Log.e("AdminFragment", "Error adding appointment item or divider to layout: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e("AdminFragment", "Error displaying appointment: " + e.getMessage(), e);
        }
    }

    private void deleteAppointment(String clinicType, String date, String appointmentId) {
        try {
            // Show a confirmation dialog
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Appointment")
                    .setMessage("Are you sure you want to delete this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            // Proceed with deletion
                            DatabaseReference appointmentRef = databaseReference.child(clinicType).child(date).child(appointmentId);

                            appointmentRef.removeValue()
                                    .addOnCompleteListener(task -> {
                                        try {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Appointment deleted successfully.", Toast.LENGTH_SHORT).show();
                                                // Refresh appointments and update spinners
                                                String clinicTypeFilter = clinicFilterSpinner.getSelectedItem().toString();
                                                clinicTypeFilter = clinicTypeFilter.equals("Show All") ? null : clinicTypeFilter;
                                                fetchAppointments(clinicTypeFilter);
                                            } else {
                                                Toast.makeText(getContext(), "Failed to delete appointment. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            Log.e("AdminFragment", "Error during appointment refresh: " + e.getMessage(), e);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("AdminFragment", "Error deleting appointment: " + e.getMessage(), e);
                                        Toast.makeText(getContext(), "Error deleting appointment.", Toast.LENGTH_SHORT).show();
                                    });
                        } catch (Exception e) {
                            Log.e("AdminFragment", "Error initiating appointment deletion: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Error initiating appointment deletion.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        try {
                            dialog.dismiss(); // Cancel the deletion
                        } catch (Exception e) {
                            Log.e("AdminFragment", "Error dismissing dialog: " + e.getMessage(), e);
                        }
                    })
                    .create()
                    .show();
        } catch (Exception e) {
            Log.e("AdminFragment", "Error showing delete confirmation dialog: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error displaying delete confirmation dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to make specific part of text bold
    private void setBold(SpannableString spannableString, String textToBold) {
        try {
            int start = spannableString.toString().indexOf(textToBold);
            if (start != -1) {
                int end = start + textToBold.length();
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                Log.w("setBold", "Text to bold not found: " + textToBold);
            }
        } catch (Exception e) {
            Log.e("setBold", "Error making text bold: " + e.getMessage(), e);
        }
    }

}
