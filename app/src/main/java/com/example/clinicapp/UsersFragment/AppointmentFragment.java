package com.example.clinicapp.UsersFragment;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.fragment.app.Fragment;

import com.example.clinicapp.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AppointmentFragment extends Fragment {

    private static final String DATE_FORMAT = "MMMM dd, yyyy";
    private static final String[] TIME_SLOTS = {
            "8:00 AM - 8:30 AM", "8:30 AM - 9:00 AM",
            "9:00 AM - 9:30 AM", "9:30 AM - 10:00 AM",
            "10:00 AM - 10:30 AM", "10:30 AM - 11:00 AM",
            "11:00 AM - 11:30 AM", "11:30 AM - 12:00 PM",
            "1:00 PM - 1:30 PM", "1:30 PM - 2:00 PM",
            "2:00 PM - 2:30 PM", "2:30 PM - 3:00 PM",
            "3:00 PM - 3:30 PM", "3:30 PM - 4:00 PM",
            "4:00 PM - 4:30 PM", "4:30 PM - 5:00 PM"
    };

    private static final String[] DENTAL_SERVICES = {
            "Dental Consultation", "Teeth Cleaning (Prophylaxis)", "Tooth Extraction"
    };
    private static final String[] MEDICAL_SERVICES = {
            "General Consultation", "Health Counseling", "Medical Certificate Issuance"
    };

    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlertDialog("Authentication Error", "Please log in to book an appointment.");
            return view;
        }

        // Check if the medical record exists before setting up the UI
        checkMedicalRecordExists(() -> setupUI(view));
        return view;
    }

    private void setupUI(View view) {
        try {
            RadioGroup radioGroupService = view.findViewById(R.id.radioGroupService);
            Spinner spinnerService = view.findViewById(R.id.spinnerService);
            EditText editTextDate = view.findViewById(R.id.editTextDate);
            Spinner spinnerTimeSlot = view.findViewById(R.id.spinnerTimeSlot);
            Button btnBookAppointment = view.findViewById(R.id.btnBookAppointment);

            setupServiceSpinner(radioGroupService, spinnerService, editTextDate, spinnerTimeSlot);

            // Set onClickListener for the date input
            editTextDate.setOnClickListener(v -> showDatePicker(editTextDate, spinnerTimeSlot, radioGroupService)); // Updated
            btnBookAppointment.setOnClickListener(v -> confirmBooking(editTextDate, spinnerService, spinnerTimeSlot, radioGroupService));

            setupTimeSlotSpinner(spinnerTimeSlot);

            // Setting up the onCheckedChangeListener for service selection
            radioGroupService.setOnCheckedChangeListener((group, checkedId) -> {
                String[] selectedServices;

                // Reset date and time slot when radio button changes
                editTextDate.setText(""); // Reset Date

                // Create an adapter with "Select Time Slot" as the first item
                ArrayAdapter<String> timeSlotAdapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.spinner_font_color,
                        new String[]{"Select Time Slot"}  // Default value in the spinner
                );
                timeSlotAdapter.setDropDownViewResource(R.layout.spinner_font_color); // Custom layout for dropdown view
                spinnerTimeSlot.setAdapter(timeSlotAdapter);
                spinnerTimeSlot.setSelection(0); // Set to "Select Time Slot"

                if (checkedId == R.id.radioMedical) {
                    selectedServices = MEDICAL_SERVICES;
                } else if (checkedId == R.id.radioDental) {
                    selectedServices = DENTAL_SERVICES;
                } else {
                    selectedServices = new String[]{};
                }

                ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_font_color, selectedServices);
                serviceAdapter.setDropDownViewResource(R.layout.spinner_font_color);
                spinnerService.setAdapter(serviceAdapter);

                // Set the spinner to "Select Service" when the radio button changes
                spinnerService.setSelection(0);  // This will ensure the first item ("Select Service") is selected
            });

        } catch (Exception e) {
            // Handle any unexpected errors in setupUI
            Toast.makeText(getContext(), "Error occurred while setting up UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();  // Print stack trace for debugging
        }
    }

    private void setupTimeSlotSpinner(Spinner spinnerTimeSlot) {
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_font_color, new String[]{"Select Time Slot"});
            adapter.setDropDownViewResource(R.layout.spinner_font_color); // Apply the same custom layout to dropdown items
            spinnerTimeSlot.setAdapter(adapter);
        } catch (Exception e) {
            // Handle any unexpected errors while setting up the spinner
            Toast.makeText(getContext(), "Error occurred while setting up time slot spinner: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();  // Print stack trace for debugging
        }
    }

    private void confirmBooking(EditText editTextDate, Spinner spinnerService, Spinner spinnerTimeSlot, RadioGroup radioGroupService) {
        try {
            String selectedDate = editTextDate.getText().toString();
            String selectedTimeSlot = spinnerTimeSlot.getSelectedItem() != null ? spinnerTimeSlot.getSelectedItem().toString() : "";
            String selectedService = spinnerService.getSelectedItem() != null ? spinnerService.getSelectedItem().toString() : "";

            if (radioGroupService.getCheckedRadioButtonId() == -1) {
                showAlertDialog("Invalid Booking", "Please select a clinic service first.");
                return;
            }

            if (selectedService.isEmpty() || selectedService.equals("Select Service")) {
                showAlertDialog("Invalid Booking", "Please select a service.");
                return;
            }

            if (selectedDate.isEmpty()) {
                showAlertDialog("Invalid Booking", "Please select a date.");
                return;
            }

            if (selectedTimeSlot.isEmpty() || selectedTimeSlot.equals("Select Time Slot") || selectedTimeSlot.equals("All time slots are fully booked")) {
                showAlertDialog("Invalid Booking", "Please select a valid time slot.");
                return;
            }

            String clinicChoice = (radioGroupService.getCheckedRadioButtonId() == R.id.radioMedical) ? "Medical Clinic" : "Dental Clinic";
            String clinicLocation = (clinicChoice.equals("Medical Clinic")) ? "Admin Building" : "Annex Building LU-07";

            // Check Firebase for existing bookings (one per month and no same-day bookings for both clinics)
            checkFirebaseForExistingBookingInClinic(selectedDate, clinicChoice, () -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Your Appointment")
                        .setMessage("Please review your appointment details carefully:\n\n" +
                                "Date: " + selectedDate + "\n" +
                                "Service: " + selectedService + "\n" +
                                "Time Slot: " + selectedTimeSlot + "\n" +
                                "Clinic: " + clinicChoice + "\n" +
                                "Location: " + clinicLocation + "\n\n" +
                                "Are you sure you want to proceed with this booking?\n\n" +
                                "Note: \n" +
                                "1. You can only book one appointment per month and cannot book appointments on the same day for both clinics.\n" +
                                "2. Once your appointment is booked, it is confirmed and cannot be canceled.")
                        .setPositiveButton("Yes, Book It", (dialog, which) -> checkIfTimeSlotBooked(selectedDate, selectedTimeSlot, clinicChoice, selectedService, clinicLocation))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } catch (Exception e) {
            // Handle unexpected errors
            Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();  // Log the error for debugging
        }
    }

    private void checkFirebaseForExistingBookingInClinic(String selectedDate, String clinicChoice, Runnable onSuccess) {
        try {
            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
            LocalDate bookingDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            int selectedYear = bookingDate.getYear();
            int selectedMonth = bookingDate.getMonthValue();

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        for (DataSnapshot clinicSnapshot : dataSnapshot.getChildren()) {
                            String currentClinic = clinicSnapshot.getKey(); // Medical Clinic or Dental Clinic

                            for (DataSnapshot dateSnapshot : clinicSnapshot.getChildren()) {
                                for (DataSnapshot appointmentSnapshot : dateSnapshot.getChildren()) {
                                    String appointmentDate = dateSnapshot.getKey();
                                    String userId = appointmentSnapshot.child("userId").getValue(String.class);
                                    String status = appointmentSnapshot.child("status").getValue(String.class);

                                    // Parse the appointment date
                                    LocalDate appointmentLocalDate = LocalDate.parse(appointmentDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"));

                                    if (userId != null && userId.equals(currentUserId)) {
                                        if ("Confirmed".equals(status)) {
                                            // Rule 1: Must finish existing appointment in the same clinic
                                            if (currentClinic.equals(clinicChoice)) {
                                                showAlertDialog("Invalid Booking", "You already have a confirmed appointment in this clinic. Please finish it before making a new booking.");
                                                return;
                                            }
                                            // Rule 2: Cannot book appointments on the same day for different clinics
                                            if (appointmentLocalDate.equals(bookingDate)) {
                                                showAlertDialog("Invalid Booking", "You cannot book appointments on the same day for both clinics.");
                                                return;
                                            }
                                        }

                                        // Rule 3: Only one appointment per month per clinic
                                        if (currentClinic.equals(clinicChoice) &&
                                                appointmentLocalDate.getYear() == selectedYear &&
                                                appointmentLocalDate.getMonthValue() == selectedMonth) {
                                            showAlertDialog("Invalid Booking", "You can only book one appointment per month in the " + clinicChoice + ".");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        // No conflicts, proceed with booking
                        onSuccess.run();
                    } catch (Exception e) {
                        // Catch errors within the onDataChange loop
                        showAlertDialog("Error", "An error occurred while processing existing appointments: " + e.getMessage());
                        e.printStackTrace();  // Log the error
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showAlertDialog("Error", "Could not check existing bookings. Please try again later.");
                }
            });
        } catch (Exception e) {
            // Catch any other errors outside the Firebase listener
            showAlertDialog("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();  // Log the error for debugging
        }
    }

    private void checkIfTimeSlotBooked(String selectedDate, String selectedTimeSlot, String clinicChoice, String selectedService, String clinicLocation) {
        try {
            // Reference to the selected date under the specified clinic type
            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                    .getReference("appointments")
                    .child(clinicChoice)
                    .child(selectedDate);

            appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        boolean isBooked = false;

                        // Check if the selected time slot already exists in this date's appointments
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (selectedTimeSlot.equals(snapshot.child("timeSlot").getValue(String.class))) {
                                isBooked = true;
                                break;
                            }
                        }

                        if (isBooked) {
                            showAlertDialog("Time Slot Unavailable", "The selected time slot is already booked in " + clinicChoice + ". Please choose another time.");
                        } else {
                            // Proceed to book the appointment directly
                            bookAppointment(clinicChoice, selectedService, selectedDate, selectedTimeSlot, clinicLocation);
                        }
                    } catch (Exception e) {
                        // Handle any errors that occur during data processing in onDataChange
                        showAlertDialog("Error", "An error occurred while checking the time slot: " + e.getMessage());
                        e.printStackTrace();  // Log the error for debugging
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    try {
                        showAlertDialog("Database Error", "Error while checking appointment availability. Please try again.");
                    } catch (Exception e) {
                        // Handle any exceptions that may occur in the onCancelled method
                        showAlertDialog("Error", "An error occurred while handling the cancellation: " + e.getMessage());
                        e.printStackTrace();  // Log the error for debugging
                    }
                }
            });
        } catch (Exception e) {
            // Handle any errors that occur during Firebase setup or before listener is triggered
            showAlertDialog("Error", "An error occurred while checking the time slot: " + e.getMessage());
            e.printStackTrace();  // Log the error for debugging
        }
    }

    private void bookAppointment(String clinicChoice, String selectedService, String selectedDate, String selectedTimeSlot, String clinicLocation) {
        try {
            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                    .getReference("appointments")
                    .child(clinicChoice)  // "Dental" or "Medical Clinic"
                    .child(selectedDate);  // Add date as a child node

            String appointmentID = appointmentsRef.push().getKey();  // Generate a unique ID for the appointment

            // Check if appointmentID is null (to avoid null pointer exception)
            if (appointmentID == null) {
                showAlertDialog("Booking Error", "Failed to generate a unique appointment ID. Please try again.");
                return;
            }

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointmentID", appointmentID);
            appointmentData.put("userId", currentUser.getUid());
            appointmentData.put("email", currentUser.getEmail());
            appointmentData.put("serviceType", selectedService);
            appointmentData.put("timeSlot", selectedTimeSlot);
            appointmentData.put("clinicLocation", clinicLocation);
            appointmentData.put("clinicType", clinicChoice);
            appointmentData.put("timestamp", System.currentTimeMillis());
            appointmentData.put("status", "Confirmed");

            // Add the appointment data under the specific date and appointment ID
            appointmentsRef.child(appointmentID).setValue(appointmentData).addOnCompleteListener(task -> {
                try {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Appointment confirmed!", Toast.LENGTH_SHORT).show();
                        resetFields();
                    } else {
                        showAlertDialog("Booking Error", "There was an error booking your appointment. Please try again.");
                    }
                } catch (Exception e) {
                    // Handle any errors that occur during task completion
                    showAlertDialog("Error", "An error occurred while confirming your appointment: " + e.getMessage());
                    e.printStackTrace();  // Log the error for debugging
                }
            });

        } catch (Exception e) {
            // Catch any errors that occur during the setup of the appointment data
            showAlertDialog("Error", "An error occurred while setting up your appointment: " + e.getMessage());
            e.printStackTrace();  // Log the error for debugging
        }
    }

    private void resetFields() {
        try {
            EditText editTextDate = getView().findViewById(R.id.editTextDate);
            Spinner spinnerService = getView().findViewById(R.id.spinnerService);
            Spinner spinnerTimeSlot = getView().findViewById(R.id.spinnerTimeSlot);
            RadioGroup radioGroupService = getView().findViewById(R.id.radioGroupService);

            // Clear or reset values
            if (editTextDate != null) {
                editTextDate.setText("");
            }

            if (radioGroupService != null) {
                radioGroupService.clearCheck();
            }

            if (spinnerService != null) {
                // Reset adapter for spinnerService to include only "Select Service" with custom layout
                ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(
                        getContext(),
                        R.layout.spinner_font_color, // Custom layout for selected view
                        new String[]{"Select Service"}
                );
                serviceAdapter.setDropDownViewResource(R.layout.spinner_font_color); // Custom layout for dropdown view
                spinnerService.setAdapter(serviceAdapter);
                spinnerService.setSelection(0); // Set to "Select Service"
            }

            if (spinnerTimeSlot != null) {
                // Reset adapter for spinnerTimeSlot to include only "Select Time Slot" with custom layout
                ArrayAdapter<String> timeSlotAdapter = new ArrayAdapter<>(
                        getContext(),
                        R.layout.spinner_font_color, // Custom layout for selected view
                        new String[]{"Select Time Slot"}
                );
                timeSlotAdapter.setDropDownViewResource(R.layout.spinner_font_color); // Custom layout for dropdown view
                    spinnerTimeSlot.setAdapter(timeSlotAdapter);
                    spinnerTimeSlot.setSelection(0); // Set to "Select Time Slot"
            }
        } catch (Exception e) {
            Log.e("ResetFields", "Error resetting fields: " + e.getMessage());
        }
    }

    private void showDatePicker(EditText editTextDate, Spinner spinnerTimeSlot, RadioGroup radioGroupService) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+08:00"));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Set the maximum date to the end of the next month
            Calendar maxDate = Calendar.getInstance(TimeZone.getTimeZone("UTC+08:00"));
            maxDate.add(Calendar.MONTH, 1); // Move to the next month
            maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH)); // Set to the last day of the next month

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                try {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    selectedDate.setTimeZone(TimeZone.getTimeZone("UTC+08:00"));

                    // Normalize time to midnight for proper date comparison
                    selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                    selectedDate.set(Calendar.MINUTE, 0);
                    selectedDate.set(Calendar.SECOND, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);

                    Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC+08:00"));
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    // Check if the selected date is in the past
                    if (selectedDate.before(calendar)) {
                        showAlertDialog("Error", "You cannot select a past date.");
                        return;
                    }

                    // Check if the selected date is today
                    if (selectedDate.equals(today)) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Same-Day Booking Not Allowed")
                                .setMessage("You cannot book an appointment on the same day. Please select a future date.")
                                .setPositiveButton("Choose Another Date", (dialog, which) -> showDatePicker(editTextDate, spinnerTimeSlot, radioGroupService))
                                .show();
                        return;
                    }

                    String dateString = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(selectedDate.getTime());
                    String clinicChoice = (radioGroupService.getCheckedRadioButtonId() == R.id.radioMedical) ? "Medical Clinic" : "Dental Clinic";

                    // Check if the clinic is closed on the selected date
                    DatabaseReference closedClinicsRef = FirebaseDatabase.getInstance().getReference("closed_clinics");
                    closedClinicsRef.child(clinicChoice).child(dateString).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                if (dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class)) {
                                    // Clinic is closed
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Clinic Closed")
                                            .setMessage(clinicChoice + " is closed on this day. Please select another date. \n\nFor available appointment dates, kindly check our announcements or contact us for assistance.")
                                            .setPositiveButton("Choose Another Date", (dialog, which) -> {
                                                // Reopen the date picker without updating editTextDate
                                                showDatePicker(editTextDate, spinnerTimeSlot, radioGroupService);
                                            })
                                            .show();
                                } else {
                                    // Clinic is not closed, proceed to check booking status
                                    DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments").child(clinicChoice);
                                    appointmentsRef.child(dateString).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            try {
                                                int bookedCount = 0;
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.child("timeSlot").exists()) bookedCount++;
                                                }

                                                if (bookedCount >= TIME_SLOTS.length) {
                                                    new AlertDialog.Builder(getContext())
                                                            .setTitle("Warning")
                                                            .setMessage("This date is fully booked. Please choose another date.")
                                                            .setPositiveButton("Choose Another Date", (dialog, which) -> {
                                                                showDatePicker(editTextDate, spinnerTimeSlot, radioGroupService);
                                                            })
                                                            .show();
                                                } else {
                                                    editTextDate.setText(dateString);
                                                    updateTimeSlots(spinnerTimeSlot, dateString, clinicChoice); // Update time slots if the date is valid
                                                }
                                            } catch (Exception e) {
                                                showAlertDialog("Error", "An unexpected error occurred while checking availability: " + e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            showAlertDialog("Database Error", "Could not retrieve availability information. Please try again.");
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                showAlertDialog("Error", "An unexpected error occurred while checking clinic closure: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            showAlertDialog("Database Error", "Could not retrieve clinic closure information. Please try again.");
                        }
                    });

                } catch (Exception e) {
                    showAlertDialog("Error", "An unexpected error occurred: " + e.getMessage());
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // Set minimum and maximum dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            datePickerDialog.show();
        } catch (Exception e) {
            showAlertDialog("Error", "An unexpected error occurred while initializing the date picker: " + e.getMessage());
        }
    }

    private void updateTimeSlots(Spinner spinnerTimeSlot, String selectedDate, String clinicChoice) {
        try {
            // Reference to the selected date within the chosen clinic
            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                    .getReference("appointments")
                    .child(clinicChoice)
                    .child(selectedDate); // Use the selected date as a child under the clinic

            appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        ArrayList<String> availableTimeSlots = new ArrayList<>();

                        // Start by adding all time slots to the list
                        for (String timeSlot : TIME_SLOTS) {
                            availableTimeSlots.add(timeSlot);
                        }

                        // Loop through all booked appointments to remove their time slots from availability
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String bookedTimeSlot = snapshot.child("timeSlot").getValue(String.class);
                            if (bookedTimeSlot != null) {
                                availableTimeSlots.remove(bookedTimeSlot);
                            }
                        }

                        // Check if any time slots are available
                        if (availableTimeSlots.isEmpty()) {
                            availableTimeSlots.add("All time slots are fully booked");
                            spinnerTimeSlot.setEnabled(false); // Disable spinner when fully booked
                        } else {
                            spinnerTimeSlot.setEnabled(true); // Enable spinner if slots are available
                        }

                        // Update the spinner with the list of available time slots using a custom layout
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_font_color, availableTimeSlots);
                        adapter.setDropDownViewResource(R.layout.spinner_font_color); // Ensure drop-down items are also styled
                        spinnerTimeSlot.setAdapter(adapter);

                    } catch (Exception e) {
                        showAlertDialog("Error", "An unexpected error occurred while processing time slots: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    try {
                        showAlertDialog("Database Error", "Error while retrieving time slots. Please try again.");
                    } catch (Exception e) {
                        // Log or handle secondary errors (e.g., dialog errors)
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            showAlertDialog("Error", "An unexpected error occurred while initializing time slots: " + e.getMessage());
        }
    }

    private void setupServiceSpinner(RadioGroup radioGroupService, Spinner spinnerService, EditText editTextDate, Spinner spinnerTimeSlot) {
        try {
            // Flag to prevent unnecessary toasts on programmatic changes
            final boolean[] isUserInteracting = {false};

            // Listener for RadioGroup changes to update services dynamically
            radioGroupService.setOnCheckedChangeListener((group, checkedId) -> {
                try {
                    isUserInteracting[0] = false; // Reset interaction flag before programmatically changing the spinner

                    // Reset fields when the service type is changed
                    spinnerService.setSelection(0); // Reset service spinner to "Select Service"
                    editTextDate.setText(""); // Clear the date field
                    spinnerTimeSlot.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Select Time Slot"})); // Reset time slots

                    // Determine the services list based on the selected radio button
                    String[] services = (checkedId == R.id.radioMedical) ? MEDICAL_SERVICES : DENTAL_SERVICES;

                    // Add the default option "Select Service" to the services list
                    String[] completeServiceList = new String[services.length + 1];
                    completeServiceList[0] = "Select Service"; // Add the default option
                    System.arraycopy(services, 0, completeServiceList, 1, services.length); // Copy other service options

                    // Set up a new ArrayAdapter with the updated service list and custom layout
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_font_color, completeServiceList);
                    adapter.setDropDownViewResource(R.layout.spinner_font_color);

                    // Update the spinner with the new adapter and reset selection
                    spinnerService.setAdapter(adapter);
                    spinnerService.setSelection(0); // Show "Select Service" as the default
                } catch (Exception e) {
                    showAlertDialog("Error", "An unexpected error occurred while updating services: " + e.getMessage());
                }
            });

            // Initialize the spinner with the default "Select Service" option
            try {
                ArrayAdapter<String> initialAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_font_color, new String[]{"Select Service"});
                initialAdapter.setDropDownViewResource(R.layout.spinner_font_color);
                spinnerService.setAdapter(initialAdapter);
            } catch (Exception e) {
                showAlertDialog("Error", "An unexpected error occurred while initializing the service spinner: " + e.getMessage());
            }

            // Add a listener to prevent invalid or null selections
            spinnerService.setOnTouchListener((v, event) -> {
                try {
                    // Set the flag to true when the user interacts with the spinner
                    isUserInteracting[0] = true;
                } catch (Exception e) {
                    showAlertDialog("Error", "An unexpected error occurred during spinner interaction: " + e.getMessage());
                }
                return false; // Allow the touch event to propagate
            });

            spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        // Only show the toast if the user manually selects "Select Service"
                        if (isUserInteracting[0] && position == 0) {
                            Toast.makeText(getContext(), "Please select a valid service.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        showAlertDialog("Error", "An unexpected error occurred while handling selection: " + e.getMessage());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    try {
                        // Ensure the spinner cannot be left without a selection
                        spinnerService.setSelection(0);
                    } catch (Exception e) {
                        showAlertDialog("Error", "An unexpected error occurred while ensuring default selection: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showAlertDialog("Error", "An unexpected error occurred while setting up the service spinner: " + e.getMessage());
        }
    }


    private void showAlertDialog(String title, String message) {
        try {
            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            // Log or handle any exception that occurs while displaying the dialog
            Log.e("AlertDialogError", "Error showing alert dialog: " + e.getMessage(), e);
            // Optionally, provide fallback user feedback (e.g., Toast)
            Toast.makeText(getContext(), "An error occurred while displaying the message: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkMedicalRecordExists(Runnable onSuccess) {
        try {
            DatabaseReference medicalRecordRef = FirebaseDatabase.getInstance()
                    .getReference("medicalRecords")
                    .child(currentUser.getUid());

            medicalRecordRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (!dataSnapshot.exists()) {
                            // Show an alert dialog if the medical record does not exist
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Missing Medical Record")
                                    .setMessage("We couldn't find your medical record. Please fill out your additional information to proceed.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        try {
                                            redirectToAdditionalInfo();
                                        } catch (Exception e) {
                                            Log.e("RedirectError", "Error while redirecting to additional info: " + e.getMessage(), e);
                                            showAlertDialog("Error", "An error occurred while redirecting. Please try again.");
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            // Medical record exists, proceed with setting up the UI
                            onSuccess.run();
                        }
                    } catch (Exception e) {
                        Log.e("DataProcessingError", "Error processing medical record data: " + e.getMessage(), e);
                        showAlertDialog("Error", "An error occurred while checking your medical record. Please try again later.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    try {
                        Log.e("FirebaseError", "Failed to fetch medical record: " + databaseError.getMessage());
                        showAlertDialog("Error", "Failed to fetch your medical record. Please try again later.");
                    } catch (Exception e) {
                        Log.e("AlertDialogError", "Error showing database error dialog: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("FirebaseSetupError", "Error initializing Firebase reference: " + e.getMessage(), e);
            showAlertDialog("Error", "An unexpected error occurred. Please try again later.");
        }
    }

    private void redirectToAdditionalInfo() {
        try {
            // Replace the current fragment with the AdditionalInfoFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AdditionalInfoFragment())
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e("FragmentNavigationError", "Error navigating to AdditionalInfoFragment: " + e.getMessage(), e);
            // Provide user feedback about the error
            showAlertDialog("Error", "An error occurred while navigating to the additional information form. Please try again.");
        }
    }
}