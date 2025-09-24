package com.example.clinicapp.AdminFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.clinicapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminHomeFragment extends Fragment {

    // Current appointments statistics
    private TextView tvCurrentTotalAppointments, tvCurrentMedicalAppointments, tvCurrentDentalAppointments,
            tvCurrentMostRequestedServices, tvCurrentPeakTimes;

    // Appointment history statistics
    private TextView tvHistoryTotalAppointments, tvHistoryCompletedAppointments, tvHistoryCancelledAppointments,
            tvHistoryMedicalAppointments, tvHistoryDentalAppointments, tvHistoryMostRequestedServices, tvHistoryPeakTimes;

    private DatabaseReference currentAppointmentsRef;
    private DatabaseReference appointmentHistoryRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Initialize UI components for current appointments
        tvCurrentTotalAppointments = view.findViewById(R.id.tv_total_appointments);
        tvCurrentMedicalAppointments = view.findViewById(R.id.tv_medical_appointments);
        tvCurrentDentalAppointments = view.findViewById(R.id.tv_dental_appointments);
        tvCurrentMostRequestedServices = view.findViewById(R.id.tv_most_requested_services);
        tvCurrentPeakTimes = view.findViewById(R.id.tv_peak_times);

        // Initialize UI components for appointment history
        tvHistoryTotalAppointments = view.findViewById(R.id.tv_total_appointments_appointmentHistory);
        tvHistoryCompletedAppointments = view.findViewById(R.id.tv_total_appointments_completed_appointmentHistory);
        tvHistoryCancelledAppointments = view.findViewById(R.id.tv_total_appointments_cancelled_appointmentHistory);
        tvHistoryMedicalAppointments = view.findViewById(R.id.tv_medical_appointments_appointmentHistory);
        tvHistoryDentalAppointments = view.findViewById(R.id.tv_dental_appointments_appointmentHistory);
        tvHistoryMostRequestedServices = view.findViewById(R.id.tv_most_requested_services_appointmentHistory);
        tvHistoryPeakTimes = view.findViewById(R.id.tv_peak_times_appointmentHistory);

        Button btnViewUpcoming = view.findViewById(R.id.btn_view_upcoming);
        Button btnViewHistory = view.findViewById(R.id.btn_view_history);
        Button btnManageAvailableDay = view.findViewById(R.id.btn_manage_available_day);

                // Initialize Firebase Database references
        currentAppointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentHistoryRef = FirebaseDatabase.getInstance().getReference("appointmentHistory");

        // Fetch and display current appointment statistics
        fetchCurrentAppointmentStatistics();

        // Fetch and display appointment history statistics
        fetchAppointmentHistoryStatistics();

        // Button actions
        btnViewUpcoming.setOnClickListener(v -> {
            // Navigate to Admin Appointment Fragment
            AdminAppointmentFragment adminAppointmentFragment = new AdminAppointmentFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, adminAppointmentFragment)
                    .addToBackStack(null)  // This ensures you can go back to the previous fragment
                    .commit();
        });

        btnViewHistory.setOnClickListener(v -> {
            // Navigate to Admin Appointment History Fragment
            AdminAppointmentHistoryFragment adminAppointmentHistoryFragment = new AdminAppointmentHistoryFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, adminAppointmentHistoryFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnManageAvailableDay.setOnClickListener(v -> {
            // Navigate to Admin Manage Available Day Fragment
            AdminManageAvailableDayFragment adminManageAvailableDayFragment = new AdminManageAvailableDayFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, adminManageAvailableDayFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void fetchCurrentAppointmentStatistics() {
        try {
            currentAppointmentsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        int totalAppointments = 0;
                        int medicalAppointments = 0;
                        int dentalAppointments = 0;
                        Map<String, Integer> serviceCount = new HashMap<>();
                        Map<String, Integer> timeSlotCount = new HashMap<>();

                        for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                    try {
                                        totalAppointments++;

                                        String clinicType = appointmentSnapshot.child("clinicType").getValue(String.class);
                                        String serviceType = appointmentSnapshot.child("serviceType").getValue(String.class);
                                        String timeSlot = appointmentSnapshot.child("timeSlot").getValue(String.class);

                                        if ("Medical Clinic".equals(clinicType)) {
                                            medicalAppointments++;
                                        } else if ("Dental Clinic".equals(clinicType)) {
                                            dentalAppointments++;
                                        }

                                        // Count services
                                        if (serviceType != null) {
                                            serviceCount.put(serviceType, serviceCount.getOrDefault(serviceType, 0) + 1);
                                        }

                                        // Count peak booking times
                                        if (timeSlot != null) {
                                            timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
                                        }
                                    } catch (Exception e) {
                                        Log.e("fetchCurrentStats", "Error processing appointment data: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }

                        // Update UI
                        try {
                            updateStatisticsUI(tvCurrentTotalAppointments, totalAppointments, "Total Appointments");
                            updateStatisticsUI(tvCurrentMedicalAppointments, medicalAppointments, "Medical Appointments");
                            updateStatisticsUI(tvCurrentDentalAppointments, dentalAppointments, "Dental Appointments");
                            tvCurrentMostRequestedServices.setText("Most Requested Services: " + getMaxKey(serviceCount));
                            tvCurrentPeakTimes.setText("Peak Booking Times: " + getMaxKey(timeSlotCount));
                        } catch (Exception e) {
                            Log.e("fetchCurrentStats", "Error updating UI: " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        Log.e("fetchCurrentStats", "Error processing snapshot: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    try {
                        Log.e("fetchCurrentStats", "Database read error: " + error.getMessage(), error.toException());
                    } catch (Exception e) {
                        Log.e("fetchCurrentStats", "Error handling onCancelled: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("fetchCurrentStats", "Error initializing database listener: " + e.getMessage(), e);
        }
    }

    private void fetchAppointmentHistoryStatistics() {
        try {
            appointmentHistoryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        int totalAppointments = 0;
                        int completedAppointments = 0;
                        int cancelledAppointments = 0;
                        int medicalAppointments = 0;
                        int dentalAppointments = 0;
                        Map<String, Integer> serviceCount = new HashMap<>();
                        Map<String, Integer> timeSlotCount = new HashMap<>();

                        for (DataSnapshot clinicSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                    try {
                                        totalAppointments++;

                                        String clinicType = appointmentSnapshot.child("clinicType").getValue(String.class);
                                        String status = appointmentSnapshot.child("status").getValue(String.class);
                                        String serviceType = appointmentSnapshot.child("serviceType").getValue(String.class);
                                        String timeSlot = appointmentSnapshot.child("timeSlot").getValue(String.class);

                                        if ("Completed".equals(status)) {
                                            completedAppointments++;
                                        } else if ("Cancelled".equals(status)) {
                                            cancelledAppointments++;
                                        }

                                        if ("Medical Clinic".equals(clinicType)) {
                                            medicalAppointments++;
                                        } else if ("Dental Clinic".equals(clinicType)) {
                                            dentalAppointments++;
                                        }

                                        // Count services
                                        if (serviceType != null) {
                                            serviceCount.put(serviceType, serviceCount.getOrDefault(serviceType, 0) + 1);
                                        }

                                        // Count peak booking times
                                        if (timeSlot != null) {
                                            timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
                                        }
                                    } catch (Exception e) {
                                        Log.e("fetchHistoryStats", "Error processing appointment data: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }

                        // Update UI
                        try {
                            updateStatisticsUI(tvHistoryTotalAppointments, totalAppointments, "Total Appointments");
                            updateStatisticsUI(tvHistoryCompletedAppointments, completedAppointments, "Completed Appointments");
                            updateStatisticsUI(tvHistoryCancelledAppointments, cancelledAppointments, "Cancelled Appointments");
                            updateStatisticsUI(tvHistoryMedicalAppointments, medicalAppointments, "Medical Appointments");
                            updateStatisticsUI(tvHistoryDentalAppointments, dentalAppointments, "Dental Appointments");
                            tvHistoryMostRequestedServices.setText("Most Requested Services: " + getMaxKey(serviceCount));
                            tvHistoryPeakTimes.setText("Peak Booking Times: " + getMaxKey(timeSlotCount));
                        } catch (Exception e) {
                            Log.e("fetchHistoryStats", "Error updating UI: " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        Log.e("fetchHistoryStats", "Error processing snapshot: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    try {
                        Log.e("fetchHistoryStats", "Database read error: " + error.getMessage(), error.toException());
                    } catch (Exception e) {
                        Log.e("fetchHistoryStats", "Error handling onCancelled: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("fetchHistoryStats", "Error initializing database listener: " + e.getMessage(), e);
        }
    }

    private void updateStatisticsUI(TextView textView, int value, String label) {
        try {
            if (textView != null) {
                textView.setText(label + ": " + value);
            } else {
                Log.w("updateStatisticsUI", "TextView is null for label: " + label);
            }
        } catch (Exception e) {
            Log.e("updateStatisticsUI", "Error updating statistics UI for label: " + label, e);
        }
    }

    private String getMaxKey(Map<String, Integer> map) {
        try {
            if (map == null || map.isEmpty()) {
                Log.w("getMaxKey", "The provided map is null or empty.");
                return "N/A";
            }

            String maxKey = "N/A";
            int maxValue = 0;

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                try {
                    if (entry.getValue() > maxValue) {
                        maxKey = entry.getKey();
                        maxValue = entry.getValue();
                    }
                } catch (Exception e) {
                    Log.e("getMaxKey", "Error processing entry: " + entry, e);
                }
            }
            return maxKey;
        } catch (Exception e) {
            Log.e("getMaxKey", "Error determining max key: " + e.getMessage(), e);
            return "Error";
        }
    }
}
