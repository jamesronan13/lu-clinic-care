package com.example.clinicapp.AdminFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.clinicapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminManageAvailableDayFragment extends Fragment {

    private DatabaseReference dbRef;

    private DatePicker datePicker;
    private Switch switchMedicalClinic, switchDentalClinic;
    private Button btnSaveChanges;

    private String selectedDateFormatted;
    private String selectedDateKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_manage_available_day, container, false);

        // Initialize Firebase Database reference
        dbRef = FirebaseDatabase.getInstance().getReference("clinic_availability");

        // Initialize UI components
        datePicker = view.findViewById(R.id.datePicker);
        switchMedicalClinic = view.findViewById(R.id.switchMedicalClinic);
        switchDentalClinic = view.findViewById(R.id.switchDentalClinic);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        // Clean up past dates
        cleanupPastDates();

        // Set up DatePicker and load initial availability
        setupDatePicker();

        // Set up Save button listener
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void cleanupPastDates() {
        try {
            DatabaseReference closedClinicsRef = FirebaseDatabase.getInstance().getReference("closed_clinics");

            // Get the current date for comparison
            Calendar today = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String todayFormatted = dateFormat.format(today.getTime());

            closedClinicsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        for (DataSnapshot clinicTypeSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot dateSnapshot : clinicTypeSnapshot.getChildren()) {
                                String dateKey = dateSnapshot.getKey();

                                if (dateKey != null) {
                                    try {
                                        // Parse the date from the database
                                        Calendar dateFromDb = Calendar.getInstance();
                                        dateFromDb.setTime(dateFormat.parse(dateKey));

                                        // Compare the database date with today's date
                                        if (dateFromDb.before(today)) {
                                            // Delete the past date entry
                                            clinicTypeSnapshot.getRef().child(dateKey).removeValue()
                                                    .addOnSuccessListener(aVoid -> Log.d("Cleanup", "Removed past date: " + dateKey))
                                                    .addOnFailureListener(e -> Log.e("Cleanup", "Failed to remove past date: " + dateKey, e));
                                        }
                                    } catch (ParseException e) {
                                        Log.e("Cleanup", "Invalid date format for key: " + dateKey, e);
                                    } catch (Exception e) {
                                        Log.e("Cleanup", "Unexpected error processing date key: " + dateKey, e);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Cleanup", "Error iterating over clinicTypeSnapshot: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Cleanup", "Failed to load data for cleanup: " + error.getMessage(), error.toException());
                }
            });
        } catch (Exception e) {
            Log.e("Cleanup", "Unexpected error in cleanupPastDates method: " + e.getMessage(), e);
        }
    }

    private void setupDatePicker() {
        try {
            // Disable past dates
            Calendar today = Calendar.getInstance();
            datePicker.setMinDate(today.getTimeInMillis());

            datePicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
                try {
                    updateSelectedDate(year, monthOfYear, dayOfMonth);
                    handleDateSelection();
                } catch (Exception e) {
                    Log.e("DatePicker", "Error handling date change: " + e.getMessage(), e);
                }
            });

            // Initialize selected date
            try {
                updateSelectedDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                handleDateSelection();
            } catch (Exception e) {
                Log.e("DatePicker", "Error initializing selected date: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e("DatePicker", "Error setting up the date picker: " + e.getMessage(), e);
        }
    }

    private void updateSelectedDate(int year, int monthOfYear, int dayOfMonth) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);

            selectedDateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            selectedDateFormatted = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(calendar.getTime());
        } catch (Exception e) {
            Log.e("DateSelection", "Error updating selected date: " + e.getMessage(), e);
        }
    }

    private void handleDateSelection() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

            // Always enable the switches, regardless of the day of the week
            switchMedicalClinic.setEnabled(true);
            switchDentalClinic.setEnabled(true);

            // Reload clinic availability for the selected date
            loadDateAvailability(calendar.get(Calendar.DAY_OF_WEEK));
        } catch (Exception e) {
            Log.e("DateSelection", "Error handling date selection: " + e.getMessage(), e);
        }
    }

    private void loadDateAvailability(int dayOfWeek) {
        try {
            if (selectedDateKey == null) return;

            // Log the selected date for debugging
            Log.d("AdminManageAvailableDay", "Loading availability for Date: " + selectedDateFormatted);

            // Reference to the "closed_clinics" node
            DatabaseReference closedClinicsRef = FirebaseDatabase.getInstance().getReference("closed_clinics");

            // Check availability for the Medical Clinic
            closedClinicsRef.child("Medical Clinic").child(selectedDateFormatted).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists() && snapshot.getValue(Boolean.class) != null) {
                            boolean isClosed = snapshot.getValue(Boolean.class);
                            switchMedicalClinic.setChecked(!isClosed); // If closed, it's unchecked
                        } else {
                            switchMedicalClinic.setChecked(true); // Default to open if no data found
                        }
                    } catch (Exception e) {
                        Log.e("DateAvailability", "Error checking availability for Medical Clinic: " + e.getMessage(), e);
                        showErrorDialog("Error checking availability for Medical Clinic.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DateAvailability", "Failed to load availability for Medical Clinic: " + error.getMessage(), error.toException());
                    showErrorDialog("Failed to load availability for Medical Clinic.");
                }
            });

            // Check availability for the Dental Clinic
            closedClinicsRef.child("Dental Clinic").child(selectedDateFormatted).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists() && snapshot.getValue(Boolean.class) != null) {
                            boolean isClosed = snapshot.getValue(Boolean.class);
                            switchDentalClinic.setChecked(!isClosed); // If closed, it's unchecked
                        } else {
                            switchDentalClinic.setChecked(true); // Default to open if no data found
                        }
                    } catch (Exception e) {
                        Log.e("DateAvailability", "Error checking availability for Dental Clinic: " + e.getMessage(), e);
                        showErrorDialog("Error checking availability for Dental Clinic.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DateAvailability", "Failed to load availability for Dental Clinic: " + error.getMessage(), error.toException());
                    showErrorDialog("Failed to load availability for Dental Clinic.");
                }
            });
        } catch (Exception e) {
            Log.e("DateAvailability", "Error loading date availability: " + e.getMessage(), e);
            showErrorDialog("Error loading date availability.");
        }
    }

    private void saveChanges() {
        try {
            if (selectedDateKey == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

            // Enable clinics for weekdays or weekends based on selection
            DatabaseReference closedClinicsRef = FirebaseDatabase.getInstance().getReference("closed_clinics");

            // Save availability for Medical Clinic
            try {
                if (!switchMedicalClinic.isChecked()) {
                    closedClinicsRef.child("Medical Clinic").child(selectedDateFormatted).setValue(true); // Set to closed
                } else {
                    closedClinicsRef.child("Medical Clinic").child(selectedDateFormatted).removeValue(); // Set to open
                }
            } catch (Exception e) {
                Log.e("SaveChanges", "Error saving Medical Clinic availability: " + e.getMessage(), e);
                showErrorDialog("Error saving Medical Clinic availability.");
            }

            // Save availability for Dental Clinic
            try {
                if (!switchDentalClinic.isChecked()) {
                    closedClinicsRef.child("Dental Clinic").child(selectedDateFormatted).setValue(true); // Set to closed
                } else {
                    closedClinicsRef.child("Dental Clinic").child(selectedDateFormatted).removeValue(); // Set to open
                }
            } catch (Exception e) {
                Log.e("SaveChanges", "Error saving Dental Clinic availability: " + e.getMessage(), e);
                showErrorDialog("Error saving Dental Clinic availability.");
            }

            showSuccessDialog("Changes saved for " + selectedDateFormatted);
        } catch (Exception e) {
            Log.e("SaveChanges", "Error saving changes: " + e.getMessage(), e);
            showErrorDialog("Error saving changes.");
        }
    }

    private void showErrorDialog(String message) {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Log.e("ShowErrorDialog", "Error displaying error dialog: " + e.getMessage(), e);
        }
    }

    private void showSuccessDialog(String message) {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Success")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Log.e("ShowSuccessDialog", "Error displaying success dialog: " + e.getMessage(), e);
        }
    }
}
