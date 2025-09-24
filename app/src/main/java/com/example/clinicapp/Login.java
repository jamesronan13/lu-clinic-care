package com.example.clinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText editTextemail, editTextpassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();

        // Check if a user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(); // Check the user role before redirection
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextemail = findViewById(R.id.email);  // Make sure this matches the ID in your layout
        editTextpassword = findViewById(R.id.password); // Make sure this matches the ID in your layout
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        TextView forgotPasswordTextView = findViewById(R.id.forgotpassword);

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        // Password visibility toggle setup
        final boolean[] isPasswordVisible = {false}; // Track password visibility

        editTextpassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextpassword.getRight() - editTextpassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (isPasswordVisible[0]) {
                        editTextpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextpassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                    } else {
                        editTextpassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextpassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    isPasswordVisible[0] = !isPasswordVisible[0];
                    editTextpassword.setSelection(editTextpassword.getText().length());
                    v.performClick(); // Call performClick
                    return true;
                }
            }
            return false;
        });

        // Registration link click listener
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        // Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;

                email = editTextemail.getText().toString(); // Get email input
                password = editTextpassword.getText().toString(); // Get password input

                // Validate input
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Sign in with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        if (user.isEmailVerified()) {
                                            Toast.makeText(Login.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                            checkUserRole(); // Call the role checking function
                                        } else {
                                            // Email not verified
                                            Toast.makeText(Login.this,
                                                    "Please verify your email before logging in.",
                                                    Toast.LENGTH_LONG).show();
                                            mAuth.signOut(); // Sign out the user
                                        }
                                    }
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Log.e("FirebaseAuth", "Login failed: " + errorMessage);
                                    Toast.makeText(Login.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    // Check user role and redirect
    private void checkUserRole() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();

                // Check the user's role in the "users" node
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                // Get the role from the user data
                                String userRole = dataSnapshot.child("role").getValue(String.class);
                                if ("admin".equals(userRole)) {
                                    // Redirect to Admin Dashboard
                                    startActivity(new Intent(Login.this, AdminActivity.class));
                                    finish();
                                } else if ("user".equals(userRole)) {
                                    // Redirect to Main Activity
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Handle if the role is neither admin nor user
                                    Toast.makeText(Login.this, "Unknown role: " + userRole, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // User data not found
                                Toast.makeText(Login.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            // Log and handle any error that occurs within onDataChange
                            Log.e("FirebaseError", "Error parsing user role: " + e.getMessage());
                            Toast.makeText(Login.this, "Error occurred while checking user role.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Log.e("FirebaseError", "Error checking user role: " + databaseError.getMessage());
                        Toast.makeText(Login.this, "Database error occurred.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Login.this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Log the error and show a toast if any unexpected exception occurs
            Log.e("UserRoleCheckError", "Error checking user role: " + e.getMessage(), e);
            Toast.makeText(Login.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
}
