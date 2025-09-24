package com.example.clinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPassword extends AppCompatActivity {

    private EditText editTextEmail;
    private Button btnResetPassword;
    private TextView btnBackToLogin; // Adjusted to TextView
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        editTextEmail = findViewById(R.id.email);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
    });

        // Reset password button click listener
        btnResetPassword.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();

            // Validate email
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgotPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidGmail(email)) {
                showAlertDialog("Invalid Email", "Please enter a valid Gmail address.");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Send password reset email directly (Firebase Authentication handles the validation)
            sendPasswordResetEmail(email);
        });

        // Back to login button click listener
        btnBackToLogin.setOnClickListener(view -> {
            startActivity(new Intent(ForgotPassword.this, Login.class));
            finish();
        });
    }

    // Validate Gmail address format
    private boolean isValidGmail(String email) {
        try {
            // Check if the email matches the pattern and ends with '@gmail.com' or '@googlemail.com'
            return Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                    (email.endsWith("@gmail.com") || email.endsWith("@googlemail.com"));
        } catch (Exception e) {
            // Log the error and return false in case of an exception
            Log.e("EmailValidationError", "Error while validating email: " + e.getMessage(), e);
            return false;
        }
    }

    private void sendPasswordResetEmail(String email) {
        try {
            progressBar.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Success message
                            showAlertDialog("Password Reset", "If this email is associated with an account, you will receive a password reset link. Please check your inbox (or spam folder).", true);
                        } else {
                            // Error handling with specific user not found case
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                showAlertDialog("Error", "This email is not associated with an account. Please try again.");
                            } else {
                                Toast.makeText(ForgotPassword.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            // Log and show error message if an exception occurs
            Log.e("PasswordResetError", "Error while sending password reset email: " + e.getMessage(), e);
            Toast.makeText(ForgotPassword.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Show AlertDialog with optional redirect to Login
    private void showAlertDialog(String title, String message) {
        showAlertDialog(title, message, false);
    }

    private void showAlertDialog(String title, String message, boolean goToLoginAfter) {
        try {
            new AlertDialog.Builder(ForgotPassword.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (goToLoginAfter) {
                            startActivity(new Intent(ForgotPassword.this, Login.class));
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            // Log the error and show a toast message if an exception occurs
            Log.e("AlertDialogError", "Error while displaying the alert dialog: " + e.getMessage(), e);
            Toast.makeText(ForgotPassword.this, "An error occurred while displaying the alert. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}


