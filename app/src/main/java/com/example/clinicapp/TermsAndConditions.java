package com.example.clinicapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TermsAndConditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable Back Button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Terms and Conditions");
        }

        // Set the terms and conditions text with bold tags and controlled size
        String termsAndConditionsText = "<b><font size='5'>Terms and Conditions for LU Clinic Care</font></b><br><br>" +
                "<b>1. Introduction</b><br>" +
                "By using LU Clinic Care, a clinic appointment booking system exclusively for Laguna University (LU) students and staff, you agree to comply with and be bound by these terms and conditions. Please read them carefully before using the app.<br><br>" +
                "<b>2. Eligibility</b><br>" +
                "The app is intended solely for Laguna University students and staff. By registering and using LU Clinic Care, you confirm that you are a current LU student or staff member.<br><br>" +
                "<b>3. Account Registration and Security</b><br>" +
                "- Users are required to provide accurate personal information during registration.<br>" +
                "- You are responsible for maintaining the confidentiality of your account credentials.<br>" +
                "- LU Clinic Care reserves the right to suspend or terminate any account if there is suspected misuse of the service.<br><br>" +
                "<b>4. Appointment Booking</b><br>" +
                "- Booking Process: Appointments can be made through the app based on availability. You are allowed to book one appointment per month per clinic.<br>" +
                "- Booking Confirmation: Once an appointment is confirmed, you will not be able to cancel it. Please double-check your details (date, time, service) before confirming the booking.<br>" +
                "- Appointment Limit: You must complete your existing appointment before booking a new one for the same clinic.<br><br>" +
                "<b>5. Appointment Cancellations</b><br>" +
                "- No Cancellation Policy: Once an appointment is confirmed, it cannot be canceled or rescheduled. Users are encouraged to carefully review their booking details before confirming.<br><br>" +
                "<b>6. User Responsibilities</b><br>" +
                "- You agree to provide accurate and complete information when making an appointment.<br>" +
                "- You are responsible for ensuring you arrive on time for your appointment.<br>" +
                "- You agree not to misuse the app or book appointments under false pretenses.<br><br>" +
                "<b>7. Limited Availability</b><br>" +
                "- Appointment Availability: All appointments are subject to clinic availability. The app operates on a first-come, first-served basis, and appointments are limited.<br>" +
                "- Booking Restrictions: Users are only allowed one appointment per clinic per month. If all time slots are fully booked for a given day, you will not be able to book an appointment for that day.<br><br>" +
                "<b>8. Liability Limitations</b><br>" +
                "- LU Clinic Care will not be liable for any damages, loss, or issues arising from the use of the app or appointment booking, including but not limited to system errors, appointment scheduling conflicts, or technical issues.<br>" +
                "- The clinic is not responsible for missed appointments due to user error.<br><br>" +
                "<b>9. Changes to Terms and Conditions</b><br>" +
                "- LU Clinic Care reserves the right to update, modify, or change these terms and conditions at any time. Users will be notified of any significant changes, and it is your responsibility to review them regularly.<br><br>" +
                "<b>10. Dispute Resolution</b><br>" +
                "- If you have any concerns or disputes regarding these terms and conditions, please contact LU Clinic Care support. Disputes will be handled according to Laguna University’s policies and procedures.<br><br>" +
                "<b>11. Governing Law</b><br>" +
                "These terms and conditions are governed by the laws of the Philippines. Any disputes arising from these terms will be resolved under the jurisdiction of Philippine courts.<br><br>" +
                "<b>12. Contact Information</b><br>" +
                "For any questions or concerns, please contact LU Clinic Care support at:<br>" +
                "- Email: clinic@lu.edu.ph";

        // Set the TextView with the terms and conditions text (HTML format)
        TextView termsTextView = findViewById(R.id.terms_text);
        termsTextView.setText(android.text.Html.fromHtml(termsAndConditionsText));
    }

    // Handle Back Button Click
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Return to the previous activity
        return true;
    }
}
