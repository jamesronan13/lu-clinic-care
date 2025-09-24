package com.example.clinicapp.UsersFragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clinicapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // Status constants
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_COMPLETED = "completed";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        view.findViewById(R.id.img_upcoming_appointments).setOnClickListener(v -> fetchAppointments("Upcoming Appointments (Confirmed)", STATUS_CONFIRMED));
        view.findViewById(R.id.img_appointment_history).setOnClickListener(v -> fetchAppointments("Appointment History (Completed)", STATUS_COMPLETED));

        view.findViewById(R.id.img_go_to_appointment).setOnClickListener(v -> navigateToAppointmentFragment());

        return view;
    }

    private void fetchAppointments(String title, String statusFilter) {
        try {
            String currentUserEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
            if (currentUserEmail == null) {
                Toast.makeText(getContext(), "Error retrieving user details!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> appointmentList = new ArrayList<>();

            // Fetch from `appointments` node
            databaseReference.child("appointments").get().addOnCompleteListener(taskAppointments -> {
                try {
                    if (taskAppointments.isSuccessful()) {
                        addAppointmentsToList(taskAppointments.getResult(), currentUserEmail, statusFilter, appointmentList);

                        // Fetch from `appointmentHistory` node
                        databaseReference.child("appointmentHistory").get().addOnCompleteListener(taskHistory -> {
                            try {
                                if (taskHistory.isSuccessful()) {
                                    addAppointmentsToList(taskHistory.getResult(), currentUserEmail, statusFilter, appointmentList);

                                    // Show dialog with combined data
                                    if (appointmentList.isEmpty()) {
                                        // Show custom dialog when no appointments are found
                                        showCustomDialog("No Appointments Found", "You don't have any " + title.toLowerCase() + " appointments.");
                                    } else {
                                        showCustomDialog(title, appointmentList);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Failed to fetch appointment history!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("FetchHistoryError", "Error processing appointment history: " + e.getMessage(), e);
                                Toast.makeText(getContext(), "An error occurred while processing appointment history!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch appointments!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("FetchAppointmentsError", "Error processing appointments: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "An error occurred while processing appointments!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("FetchAppointmentsError", "Unexpected error: " + e.getMessage(), e);
            Toast.makeText(getContext(), "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addAppointmentsToList(DataSnapshot snapshot, String userEmail, String statusFilter, ArrayList<String> appointmentList) {
        try {
            if (snapshot != null) {
                for (DataSnapshot clinicTypeSnapshot : snapshot.getChildren()) {
                    try {
                        for (DataSnapshot dateSnapshot : clinicTypeSnapshot.getChildren()) {
                            try {
                                for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                    try {
                                        String email = appointmentSnapshot.child("email").getValue(String.class);
                                        String status = appointmentSnapshot.child("status").getValue(String.class);

                                        if (userEmail.equals(email) && statusFilter.equalsIgnoreCase(status)) {
                                            String serviceType = appointmentSnapshot.child("serviceType").getValue(String.class);
                                            String clinicType = appointmentSnapshot.child("clinicType").getValue(String.class);
                                            String date = dateSnapshot.getKey();
                                            String timeSlot = appointmentSnapshot.child("timeSlot").getValue(String.class);

                                            appointmentList.add(clinicType + ": " + serviceType + "\nDate: " + date + "\nTime: " + timeSlot);
                                        }
                                    } catch (Exception e) {
                                        Log.e("AppointmentProcessingError", "Error processing an appointment: " + e.getMessage(), e);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("DateProcessingError", "Error processing date snapshot: " + e.getMessage(), e);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ClinicTypeProcessingError", "Error processing clinic type snapshot: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SnapshotProcessingError", "Error processing snapshot: " + e.getMessage(), e);
        }
    }

    private void showCustomDialog(String title, String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointments, null);

            try {
                // Set dialog title
                TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
                dialogTitle.setText(title);

                // Get the container and add a "No data found" message if no appointments
                LinearLayout container = dialogView.findViewById(R.id.appointments_container);
                TextView noDataMessage = new TextView(getContext());
                noDataMessage.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                noDataMessage.setText(message);
                noDataMessage.setTextSize(16f);
                noDataMessage.setPadding(8, 8, 8, 8);
                container.addView(noDataMessage);

                builder.setView(dialogView);
            } catch (Exception e) {
                Log.e("DialogViewError", "Error setting up dialog view: " + e.getMessage(), e);
                showAlertDialog("Error", "An error occurred while setting up the dialog. Please try again.");
                return;
            }

            // Set up the positive button and show the dialog
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        } catch (Exception e) {
            Log.e("ShowDialogError", "Error displaying dialog: " + e.getMessage(), e);
            Toast.makeText(getContext(), "An error occurred while displaying the dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDialog(String title, String message) {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            Log.e("AlertDialogError", "Error showing alert dialog: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomDialog(String title, ArrayList<String> appointmentList) {
        try {
            // Attempt to create and show the custom dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointments, null);

            // Set dialog title
            TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
            dialogTitle.setText(title);

            // Get the container and populate the appointments
            LinearLayout container = dialogView.findViewById(R.id.appointments_container);
            for (String appointment : appointmentList) {
                TextView appointmentItem = new TextView(getContext());
                appointmentItem.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                appointmentItem.setText(appointment);
                appointmentItem.setTextSize(16f);
                appointmentItem.setPadding(8, 8, 8, 8);
                container.addView(appointmentItem);
            }

            builder.setView(dialogView);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        } catch (Exception e) {
            // Handle and log any exceptions that occur during the dialog creation
            Log.e("CustomDialogError", "Error displaying the custom dialog: " + e.getMessage(), e);
            // Provide user feedback in case of an error
            Toast.makeText(getContext(), "An error occurred while displaying the appointments. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAppointmentFragment() {
        try {
            // Create an instance of AppointmentFragment
            Fragment appointmentFragment = new AppointmentFragment();

            // Begin the fragment transaction with slide-in animation
            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)  // Slide-in from right
                    .replace(R.id.fragment_container, appointmentFragment)  // Replace the current fragment
                    .addToBackStack(null)  // Optional: Add the transaction to the back stack
                    .commit();
        } catch (Exception e) {
            // Handle any exceptions that occur during the fragment transaction
            Log.e("FragmentNavigationError", "Error navigating to AppointmentFragment: " + e.getMessage(), e);
            // Provide feedback to the user in case of an error
            Toast.makeText(getContext(), "An error occurred while navigating. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
