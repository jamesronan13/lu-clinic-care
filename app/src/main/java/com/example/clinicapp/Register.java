package com.example.clinicapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    // Declare variables for UI components
    private EditText etFirstName, etMiddleName, etLastName, editTextDOB, editTextEmail, ediTextPhoneNumber, editTextPassword, editTextConfirmPassword;
    private Spinner spinnerGender;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView loginNow;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        editTextDOB = findViewById(R.id.editText_register_dob);
        editTextEmail = findViewById(R.id.editText_register_email);
        editTextPassword = findViewById(R.id.editText_register_password);
        editTextConfirmPassword = findViewById(R.id.editText_register_confirm_password);
        ediTextPhoneNumber = findViewById(R.id.editText_register_phone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        loginNow = findViewById(R.id.loginNow);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Gender", "Male", "Female"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Find the checkbox
        CheckBox checkboxTerms = findViewById(R.id.checkbox_terms);

// Create a SpannableString with clickable parts
        SpannableString spannableString = new SpannableString("I agree to the Terms and Conditions and Privacy Policy");

        setupPasswordToggle(editTextPassword);
        setupPasswordToggle(editTextConfirmPassword);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");


        // Add TextWatcher to First Name
        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String input = charSequence.toString();

                if (input.trim().isEmpty() && !input.isEmpty()) {
                    etFirstName.setError("First name cannot be spaces only");
                } else if (!input.isEmpty() && !input.matches("[a-zA-Z ]+")) {
                    etFirstName.setError("First name should only contain letters");
                } else {
                    etFirstName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Add TextWatcher to Last Name
        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String input = charSequence.toString();

                if (input.trim().isEmpty() && !input.isEmpty()) {
                    etLastName.setError("Last name cannot be spaces only");
                } else if (!input.isEmpty() && !input.matches("[a-zA-Z ]+")) {
                    etLastName.setError("Last name should only contain letters");
                } else {
                    etLastName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etMiddleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String input = charSequence.toString();

                if (input.trim().isEmpty() && !input.isEmpty()) {
                    etMiddleName.setError("Middle name cannot be spaces only");
                } else if (!input.isEmpty() && !input.matches("[a-zA-Z ]+")) {
                    etMiddleName.setError("Middle name should only contain letters");
                } else {
                    etMiddleName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // Add TextWatcher to Email
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
                    editTextEmail.setError("Please enter a valid Gmail address");
                } else {
                    editTextEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

// Inside onCreate method
        ediTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String input = charSequence.toString();

                // Check if it starts with "09" or "63" and adjust max length accordingly
                if (input.startsWith("09")) {
                    ediTextPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
                } else if (input.startsWith("63")) {
                    ediTextPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                } else {
                    ediTextPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                }

                // Philippine phone number validation
                if (!input.matches("^(09\\d{9}|63\\d{10})$")) {
                    ediTextPhoneNumber.setError("Enter a valid Philippine phone number in format 09XXXXXXXXX or 63XXXXXXXXXX");
                } else {
                    ediTextPhoneNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // Add TextWatcher to Password
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!isValidPassword(charSequence.toString())) {
                    editTextPassword.setError("Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a number, and a special character");
                } else {
                    editTextPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Add TextWatcher to Confirm Password
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().equals(editTextPassword.getText().toString())) {
                    editTextConfirmPassword.setError("Passwords do not match");
                } else {
                    editTextConfirmPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show DatePickerDialog when user clicks on the DOB field
                showDatePickerDialog();
            }
        });


        // OnClickListener for Register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Redirect to login if user clicks "Login Now"
        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }

        });

        // Reuse existing variables or rename them appropriately

// Define ClickableSpan for Terms and Conditions
        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Redirect to TermsAndConditions activity
                Intent termsIntent = new Intent(Register.this, TermsAndConditions.class);
                startActivity(termsIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Register.this, R.color.Green_blue)); // Link color
                ds.setUnderlineText(true); // Optional underline
            }
        };

// Define ClickableSpan for Privacy Policy
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Redirect to PrivacyPolicy activity
                Intent privacyIntent = new Intent(Register.this, PrivacyPolicy.class);
                startActivity(privacyIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Register.this, R.color.Green_blue)); // Link color
                ds.setUnderlineText(true); // Optional underline
            }
        };

// Reuse or modify the existing SpannableString
        // Correct indexing for "Terms and Conditions" and "Privacy Policy"
        spannableString.setSpan(termsClickableSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Correct indices for "Terms and Conditions"
        spannableString.setSpan(privacyClickableSpan, 40, 54, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Correct indices for "Privacy Policy"

// Set the spannable string to the checkbox
        checkboxTerms.setText(spannableString);
        checkboxTerms.setMovementMethod(LinkMovementMethod.getInstance());
// "Privacy Policy"

// Reuse the existing checkbox
        checkboxTerms.setText(spannableString);
        checkboxTerms.setMovementMethod(LinkMovementMethod.getInstance());

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
                    Register.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            try {
                                // Set the selected date to the EditText
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(year, monthOfYear, dayOfMonth);

                                // Format the date to show in the desired format
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                                String formattedDate = dateFormat.format(selectedDate.getTime());

                                if (selectedDate.after(Calendar.getInstance())) {
                                    // If the selected date is in the future, show an alert dialog
                                    new AlertDialog.Builder(Register.this)
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
                                    editTextDOB.setError(null);
                                }
                            } catch (Exception e) {
                                // Handle any exceptions that may occur in the onDateSet method
                                e.printStackTrace();
                                Toast.makeText(Register.this, "An error occurred while setting the date. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    year, month, day);

            datePickerDialog.show();
        } catch (Exception e) {
            // Handle any exceptions that may occur in the showDatePickerDialog method
            e.printStackTrace();
            Toast.makeText(Register.this, "An error occurred while showing the date picker. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        try {
            // Get the user input
            String firstName = etFirstName.getText().toString().trim().toUpperCase();
            String middleName = etMiddleName.getText().toString().trim().toUpperCase();
            String lastName = etLastName.getText().toString().trim().toUpperCase();
            String dob = editTextDOB.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String phoneNumber = ediTextPhoneNumber.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Get selected gender
            String gender = spinnerGender.getSelectedItem().toString();

            if ("Select Gender".equals(gender)) {
                Toast.makeText(Register.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                return; // Exit the method if no gender is selected
            }

            // Check if the Terms and Conditions checkbox is checked
            CheckBox checkboxTerms = findViewById(R.id.checkbox_terms);
            if (!checkboxTerms.isChecked()) {
                Toast.makeText(Register.this, "Please accept the terms and conditions to proceed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Input validation
            if (firstName.isEmpty()) {
                Toast.makeText(Register.this, "Please enter your first name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastName.isEmpty()) {
                Toast.makeText(Register.this, "Please enter your last name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty()) {
                Toast.makeText(Register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phoneNumber.isEmpty()) {
                Toast.makeText(Register.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Register.this, "Please enter and confirm your password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstName.trim().isEmpty() || !firstName.matches("[a-zA-Z ]+")) {
                Toast.makeText(Register.this, "First name should only contain letters and cannot be spaces only", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastName.trim().isEmpty() || !lastName.matches("[a-zA-Z ]+")) {
                Toast.makeText(Register.this, "Last name should only contain letters and cannot be spaces only", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!middleName.isEmpty()) {
                if (middleName.trim().isEmpty() || !middleName.matches("[a-zA-Z ]+")) {
                    Toast.makeText(Register.this, "Middle name should only contain letters and cannot be spaces only", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Date of Birth Validation: Ensure Date is Not in the Future
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            try {
                Calendar dobCalendar = Calendar.getInstance();
                dobCalendar.setTime(dateFormat.parse(dob));
                if (dobCalendar.after(Calendar.getInstance())) {
                    Toast.makeText(Register.this, "Date of Birth cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(Register.this, "Invalid Date of Birth", Toast.LENGTH_SHORT).show();
                return;
            }

            // Email Validation: Only Allow Gmail Addresses
            if (!email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
                Toast.makeText(Register.this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Phone Number Validation: Philippine format
            if (!phoneNumber.matches("^(\\+63|09)\\d{9}$")) {
                Toast.makeText(Register.this, "Please enter a valid Philippine phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Password Validation: Strong Password Policy
            if (!isValidPassword(password)) {
                Toast.makeText(Register.this, "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a number, and a special character", Toast.LENGTH_LONG).show();
                return;
            }

            // Confirm Password Validation
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress bar while registering
            progressBar.setVisibility(View.VISIBLE);

            // Register user with Firebase Authentication
            String finalGender = gender;
            try {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            String userId = user.getUid();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("firstName", firstName);
                                            userData.put("middleName", middleName);
                                            userData.put("lastName", lastName);
                                            userData.put("dob", dob);
                                            userData.put("gender", gender);
                                            userData.put("email", email);
                                            userData.put("phoneNumber", phoneNumber);
                                            userData.put("role", "user");

                                            databaseRef.child(userId).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressBar.setVisibility(View.GONE);

                                                    if (task.isSuccessful()) {
                                                        FirebaseAuth.getInstance().signOut();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                                        builder.setTitle("Registration Successful")
                                                                .setMessage("A verification email has been sent to your email address. Please verify to log in.")
                                                                .setCancelable(false)
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Intent intent = new Intent(Register.this, Login.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                        builder.create().show();
                                                    } else {
                                                        showAlertDialog("Error", "Failed to save user data.");
                                                    }
                                                }
                                            });
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            showAlertDialog("Error", "Failed to send verification email. Please try again later.");
                                        }
                                    }
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                editTextEmail.setError("Email is already registered");
                            } else {
                                showAlertDialog("Registration Failed", "Registration failed: " + task.getException().getMessage());
                            }
                        }
                    }
                });
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Register.this, "An error occurred during registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Register.this, "An error occurred during registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

            private void showAlertDialog(String title, String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", null);
            builder.create().show();
        } catch (Exception e) {
            // Handle any exception that occurs during dialog creation or display
            e.printStackTrace();  // Log the exception for debugging
            Toast.makeText(Register.this, "An error occurred while showing the alert dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        try {
            // Password must have at least one uppercase letter, one lowercase letter, one number, and one special character
            String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
            return password.matches(passwordPattern);
        } catch (Exception e) {
            // Handle any exception that occurs during password validation
            e.printStackTrace();  // Log the exception for debugging
            Toast.makeText(Register.this, "An error occurred while validating the password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void setupPasswordToggle(EditText editText) {
        try {
            editText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable drawableEnd = editText.getCompoundDrawables()[2]; // Right drawable
                    if (drawableEnd != null && event.getRawX() >= (editText.getRight() - drawableEnd.getBounds().width())) {
                        // Toggle password visibility
                        if (editText.getInputType() == 129) { // 129 = TYPE_TEXT_VARIATION_PASSWORD
                            editText.setInputType(144); // 144 = TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                        } else {
                            editText.setInputType(129);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        }
                        editText.setSelection(editText.getText().length()); // Keep cursor at the end
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception for debugging
            Toast.makeText(editText.getContext(), "An error occurred while setting up the password toggle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}