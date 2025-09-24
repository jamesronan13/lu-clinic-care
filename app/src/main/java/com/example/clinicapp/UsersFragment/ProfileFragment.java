package com.example.clinicapp.UsersFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.clinicapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText etFirstName, etMiddleName, etLastName, editTextDOB, editTextEmail;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale;
    private Button btnUpdateProfile, btnSaveProfile;
    private ProgressBar progressBar;
    private EditText editTextPhone;
    private Button btnChangePassword;
    private Button btnClientAdditionalInfo;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnClientAdditionalInfo = view.findViewById(R.id.btnClientAdditionalInfo);

        // Initialize UI components
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        editTextDOB = view.findViewById(R.id.editText_dob);
        editTextEmail = view.findViewById(R.id.editText_email);
        radioGroupGender = view.findViewById(R.id.radio_group_gender);
        editTextPhone = view.findViewById(R.id.editText_phone);
        radioMale = view.findViewById(R.id.radioMale);
        radioFemale = view.findViewById(R.id.radioFemale);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Disable all fields initially
        setFieldsEditable(false);

        // Load user profile
        loadUserProfile();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        // Set click listener for update profile button (make fields editable)
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFieldsEditable(true); // Make fields editable
                btnUpdateProfile.setVisibility(View.GONE);
                btnSaveProfile.setVisibility(View.VISIBLE); // Show save button
            }
        });

        // Set click listener for save profile button (save updated fields)
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        // Inside onCreateView() method, add this:
        // Disable typing in DOB field and set date picker click listener
        editTextDOB.setInputType(InputType.TYPE_NULL); // Disable typing in the DOB field
        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        btnClientAdditionalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace current fragment with FragmentAdditionalInfo
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new AdditionalInfoFragment());
                transaction.addToBackStack(null); // Add the transaction to the back stack
                transaction.commit();
            }
        });


        return view;
    }

    private void showDatePickerDialog() {
        try {
            // Get current date to set as default in the date picker
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),  // Use getActivity() if it's in a Fragment
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                            // Set the selected date to the EditText
                            Calendar selectedDate = Calendar.getInstance();
                            selectedDate.set(selectedYear, selectedMonth, selectedDay);

                            // Format the date to show in the desired format
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(selectedDate.getTime());

                            // Check if the selected date is in the future
                            if (selectedDate.after(Calendar.getInstance())) {
                                // If the selected date is in the future, show an alert dialog
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Invalid Date")
                                        .setMessage("Date of Birth cannot be in the future. Please select a valid date.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Show the date picker again to allow the user to re-select a valid date
                                                showDatePickerDialog();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();

                                // Clear the EditText if an invalid date was selected
                                editTextDOB.setText("");
                            } else {
                                // Set the valid selected date to the EditText and clear any error
                                editTextDOB.setText(formattedDate);
                                editTextDOB.setError(null);  // Clear any previous error
                            }
                        }
                    },
                    year, month, day); // Set the current date as the default date

            datePickerDialog.show();
        } catch (Exception e) {
            // Handle any exceptions that may occur
            Log.e("DatePickerError", "Error showing date picker dialog: " + e.getMessage(), e);
            Toast.makeText(getContext(), "An error occurred while selecting the date. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        try {
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Get user data from Firebase Realtime Database
                databaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                // Populate fields with user data
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String middleName = snapshot.child("middleName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                String dob = snapshot.child("dob").getValue(String.class);
                                String email = snapshot.child("email").getValue(String.class);
                                String phone = snapshot.child("phoneNumber").getValue(String.class); // Phone number
                                String gender = snapshot.child("gender").getValue(String.class);

                                etFirstName.setText(firstName);
                                etMiddleName.setText(middleName);
                                etLastName.setText(lastName);
                                editTextDOB.setText(dob);
                                editTextEmail.setText(email);
                                editTextPhone.setText(phone); // Set phone number

                                if (gender.equals("Male")) {
                                    radioMale.setChecked(true);
                                } else if (gender.equals("Female")) {
                                    radioFemale.setChecked(true);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("ProfileError", "Error loading user profile data: " + e.getMessage(), e);
                            Toast.makeText(getActivity(), "Error loading profile data. Please try again.", Toast.LENGTH_SHORT).show();
                        } finally {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileError", "Failed to load user data: " + error.getMessage());
                        Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ProfileError", "Error loading user profile: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        try {
            // Inflate dialog layout
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_password, null);

            // Initialize fields in dialog layout
            EditText editTextCurrentPassword = dialogView.findViewById(R.id.editText_currentPassword);
            EditText editTextNewPassword = dialogView.findViewById(R.id.editText_newPassword);
            EditText editTextConfirmPassword = dialogView.findViewById(R.id.editText_confirmPassword);
            Button btnSubmitChangePassword = dialogView.findViewById(R.id.btnSubmitChangePassword);
            Button btnCancelChangePassword = dialogView.findViewById(R.id.btnCancelChangePassword);

            // Password validation pattern
            String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

            // Variables to track password visibility
            final boolean[] isCurrentPasswordVisible = {false};
            final boolean[] isNewPasswordVisible = {false};
            final boolean[] isConfirmPasswordVisible = {false};

            // Toggle visibility function
            View.OnTouchListener toggleVisibilityListener = (v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int rightDrawableIndex = 2;  // Drawable on the right
                    EditText editText = (EditText) v;
                    boolean[] isVisible;

                    // Determine which EditText and visibility flag to toggle
                    if (editText == editTextCurrentPassword) {
                        isVisible = isCurrentPasswordVisible;
                    } else if (editText == editTextNewPassword) {
                        isVisible = isNewPasswordVisible;
                    } else {
                        isVisible = isConfirmPasswordVisible;
                    }

                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[rightDrawableIndex].getBounds().width())) {
                        // Toggle password visibility
                        isVisible[0] = !isVisible[0];
                        editText.setInputType(isVisible[0]
                                ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                isVisible[0] ? R.drawable.baseline_visibility_off_24 : R.drawable.baseline_remove_red_eye_24, 0);
                        editText.setSelection(editText.getText().length());  // Move cursor to the end
                        return true;
                    }
                }
                return false;
            };

            // Set touch listener to toggle visibility for each EditText
            editTextCurrentPassword.setOnTouchListener(toggleVisibilityListener);
            editTextNewPassword.setOnTouchListener(toggleVisibilityListener);
            editTextConfirmPassword.setOnTouchListener(toggleVisibilityListener);

            // Create AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Handle Change Password logic
            btnSubmitChangePassword.setOnClickListener(v -> {
                try {
                    String currentPassword = editTextCurrentPassword.getText().toString().trim();
                    String newPassword = editTextNewPassword.getText().toString().trim();
                    String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                    // Clear previous errors
                    editTextCurrentPassword.setError(null);
                    editTextNewPassword.setError(null);
                    editTextConfirmPassword.setError(null);

                    // Input validation
                    boolean hasError = false;
                    if (TextUtils.isEmpty(currentPassword)) {
                        editTextCurrentPassword.setError("Please enter your current password.");
                        hasError = true;
                    }

                    if (TextUtils.isEmpty(newPassword)) {
                        editTextNewPassword.setError("Please enter a new password.");
                        hasError = true;
                    } else if (!newPassword.matches(passwordPattern)) {
                        editTextNewPassword.setError("Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character.");
                        hasError = true;
                    }

                    if (TextUtils.isEmpty(confirmPassword)) {
                        editTextConfirmPassword.setError("Please confirm your new password.");
                        hasError = true;
                    } else if (!newPassword.equals(confirmPassword)) {
                        editTextConfirmPassword.setError("New passwords do not match.");
                        hasError = true;
                    }

                    // Exit if there are errors
                    if (hasError) {
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String email = user.getEmail();
                        if (email != null) {
                            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

                            // Reauthenticate user
                            user.reauthenticate(credential).addOnCompleteListener(task -> {
                                try {
                                    if (task.isSuccessful()) {
                                        // Update password
                                        user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(getActivity(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to update password.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        editTextCurrentPassword.setError("Current password is incorrect.");
                                    }
                                } catch (Exception e) {
                                    Log.e("PasswordChangeError", "Error reauthenticating or updating password: " + e.getMessage(), e);
                                    Toast.makeText(getActivity(), "An error occurred while updating password. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("PasswordChangeError", "Error during password change process: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle Cancel button logic
            btnCancelChangePassword.setOnClickListener(v -> {
                dialog.dismiss(); // Close the dialog when cancel is clicked
            });
        } catch (Exception e) {
            Log.e("PasswordChangeError", "Error initializing change password dialog: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        try {
            if (currentUser != null) {
                String userId = currentUser.getUid();

                String updatedFirstName = etFirstName.getText().toString().trim();
                String updatedMiddleName = etMiddleName.getText().toString().trim();
                String updatedLastName = etLastName.getText().toString().trim();
                String updatedDOB = editTextDOB.getText().toString().trim();
                String updatedEmail = editTextEmail.getText().toString().trim();
                String updatedPhone = editTextPhone.getText().toString().trim(); // Phone number
                String updatedGender = radioMale.isChecked() ? "Male" : "Female";

                // First Name Validation
                if (updatedFirstName.isEmpty() || !updatedFirstName.matches("[a-zA-Z ]+")) {
                    Toast.makeText(getActivity(), "First name should only contain letters and cannot be empty or spaces only.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Middle Name Validation (Optional)
                if (!updatedMiddleName.isEmpty() && (!updatedMiddleName.matches("[a-zA-Z ]+") || updatedMiddleName.trim().isEmpty())) {
                    Toast.makeText(getActivity(), "Middle name should only contain letters and cannot be spaces only.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Last Name Validation
                if (updatedLastName.isEmpty() || !updatedLastName.matches("[a-zA-Z ]+")) {
                    Toast.makeText(getActivity(), "Last name should only contain letters and cannot be empty or spaces only.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Date of Birth Validation
                if (updatedDOB.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your Date of Birth.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                try {
                    Calendar dobCalendar = Calendar.getInstance();
                    dobCalendar.setTime(dateFormat.parse(updatedDOB));
                    if (dobCalendar.after(Calendar.getInstance())) {
                        Toast.makeText(getActivity(), "Date of Birth cannot be in the future.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid Date of Birth format. Use MMM DD, YYYY (e.g., Jan 01, 2000).", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Phone Number Validation
                if (updatedPhone.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!updatedPhone.matches("^(\\+63|09)\\d{9}$")) { // Validates Philippine phone numbers
                    Toast.makeText(getActivity(), "Please enter a valid Philippine phone number (e.g., +639123456789 or 09123456789).", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Create a map for updates
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("firstName", updatedFirstName);
                updates.put("middleName", updatedMiddleName);
                updates.put("lastName", updatedLastName);
                updates.put("dob", updatedDOB);
                updates.put("email", updatedEmail);
                updates.put("phoneNumber", updatedPhone);
                updates.put("gender", updatedGender);

                // Save updates to Firebase
                databaseRef.child(userId).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            setFieldsEditable(false);
                            btnSaveProfile.setVisibility(View.GONE);
                            btnUpdateProfile.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getActivity(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ProfileUpdateError", "Error during profile update: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setFieldsEditable(boolean editable) {
        try {
            etFirstName.setEnabled(editable);
            etMiddleName.setEnabled(editable);
            etLastName.setEnabled(editable);
            editTextDOB.setEnabled(editable);
            editTextPhone.setEnabled(editable);
            radioMale.setEnabled(editable);
            radioFemale.setEnabled(editable);

            // Email field will remain uneditable
            editTextEmail.setEnabled(false);
        } catch (Exception e) {
            Log.e("SetFieldsEditableError", "Error while setting fields editable: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "An error occurred while updating the fields.", Toast.LENGTH_SHORT).show();
        }
    }
}

