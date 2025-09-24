package com.example.clinicapp.UsersFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.clinicapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdditionalInfoFragment extends Fragment {

    private Spinner spinnerPastMedicalHistory, spinnerAllergies, spinnerMedications, spinnerImmunizations, spinnerFamilyHistory;
    private Spinner spinnerSmoking, spinnerAlcoholUse, spinnerDrugUse, spinnerExercise, spinnerDiet, spinnerOccupation;
    private EditText editTextPastMedicalHistoryOther, editTextAllergiesOther, editTextMedicationsOther, editTextImmunizationsOther, editTextFamilyHistory;
    private Button buttonSave;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additional_info, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("medicalRecords");

        // Bind UI elements for Medical History
        spinnerPastMedicalHistory = view.findViewById(R.id.spinnerPastMedicalHistory);
        editTextPastMedicalHistoryOther = view.findViewById(R.id.editTextPastMedicalHistoryOther);

        spinnerAllergies = view.findViewById(R.id.spinnerAllergies);
        editTextAllergiesOther = view.findViewById(R.id.editTextAllergiesOther);

        spinnerMedications = view.findViewById(R.id.spinnerMedications);
        editTextMedicationsOther = view.findViewById(R.id.editTextMedicationsOther);

        spinnerImmunizations = view.findViewById(R.id.spinnerImmunizations);
        editTextImmunizationsOther = view.findViewById(R.id.editTextImmunizationsOther);

        spinnerFamilyHistory = view.findViewById(R.id.spinnerFamilyHistory);
        editTextFamilyHistory = view.findViewById(R.id.editTextFamilyHistory);

        // Bind UI elements for Social History
        spinnerSmoking = view.findViewById(R.id.spinnerSmoking);
        spinnerAlcoholUse = view.findViewById(R.id.spinnerAlcoholUse);
        spinnerDrugUse = view.findViewById(R.id.spinnerDrugUse);
        spinnerExercise = view.findViewById(R.id.spinnerExercise);
        spinnerDiet = view.findViewById(R.id.spinnerDiet);
        spinnerOccupation = view.findViewById(R.id.spinnerOccupation);

        buttonSave = view.findViewById(R.id.buttonSave);

        // Populate and configure Spinners
        configureSpinner(spinnerPastMedicalHistory, editTextPastMedicalHistoryOther, new String[]{
                "None", "Hypertension", "Diabetes Type 1", "Diabetes Type 2", "Asthma",
                "High Cholesterol", "Stroke", "Arthritis", "Heart Disease", "Obesity",
                "Influenza", "Pneumonia", "Chickenpox", "Hepatitis", "Tuberculosis",
                "Appendectomy", "Gallbladder Removal", "C-section", "Hip/Knee Replacement",
                "Surgery", "ICU Stay", "Childbirth", "Mental Health Treatment", "Other"
        });

        configureSpinner(spinnerAllergies, editTextAllergiesOther, new String[]{
                "None", "Penicillin", "Aspirin", "Sulfa Drugs", "NSAIDs", "Peanuts", "Tree Nuts",
                "Dairy", "Eggs", "Shellfish", "Wheat/Gluten", "Pollen", "Dust Mites",
                "Animal Dander", "Mold", "Other"
        });

        configureSpinner(spinnerMedications, editTextMedicationsOther, new String[]{
                "None", "Metformin", "Lisinopril", "Levothyroxine", "Atorvastatin", "Insulin",
                "Albuterol", "Omeprazole", "Sertraline", "Acetaminophen", "Ibuprofen",
                "Antihistamines", "Tums", "Vitamin D", "Multivitamins", "Fish Oil",
                "Magnesium", "Probiotics", "Other"
        });

        configureSpinner(spinnerImmunizations, editTextImmunizationsOther, new String[]{
                "None", "Flu Vaccine", "COVID-19 Vaccine", "Tetanus", "MMR", "Polio", "Hepatitis B",
                "Pneumococcal", "Chickenpox", "HPV", "Hepatitis A", "Shingles",
                "Meningococcal", "Typhoid", "Other"
        });

        configureSpinner(spinnerFamilyHistory, editTextFamilyHistory, new String[]{
                "None", "Heart Disease", "Diabetes", "Cancer", "Mental Health", "Hypertension",
                "Alzheimer’s", "Osteoporosis", "Other"
        });

        // Configure Social History Spinners
        configureSimpleSpinner(spinnerSmoking, new String[]{"Never Smoked", "Current Smoker", "Former Smoker"});
        configureSimpleSpinner(spinnerAlcoholUse, new String[]{"Never Drink", "Occasional/Light Drinker", "Regular Drinker", "Heavy Drinker"});
        configureSimpleSpinner(spinnerDrugUse, new String[]{"Never Used", "Occasional Use", "Former User"});
        configureSimpleSpinner(spinnerExercise, new String[]{"Sedentary (No Exercise)", "Light Exercise (Walking, Yoga)", "Moderate Exercise (Jogging, Cycling)", "Intense Exercise (Weightlifting, Running)"});
        configureSimpleSpinner(spinnerDiet, new String[]{"No Special Diet", "Vegetarian", "Vegan", "Gluten-Free", "Low Carb"});
        configureSimpleSpinner(spinnerOccupation, new String[]{ "None", "Desk Job", "Physical Labor", "Healthcare Worker", "Student", "Homemaker"});

        // Save button click listener
        buttonSave.setOnClickListener(v -> saveMedicalRecords());

        loadMedicalRecords();

        return view;
    }

    private void configureSpinner(Spinner spinner, EditText editText, String[] options) {
        try {
            if (options == null || options.length == 0) {
                throw new IllegalArgumentException("Options for spinner cannot be null or empty.");
            }

            // Add default value to the options
            String[] modifiedOptions = new String[options.length + 1];
            modifiedOptions[0] = "Select Here"; // Default value
            System.arraycopy(options, 0, modifiedOptions, 1, options.length);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, modifiedOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // Enable/Disable EditText based on spinner selection
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (parent.getAdapter() == null || position < 0 || position >= parent.getAdapter().getCount()) {
                            return; // Prevent crashes
                        }

                        String selectedValue = parent.getItemAtPosition(position).toString();
                        if ("Other".equals(selectedValue)) {
                            editText.setEnabled(true);
                        } else {
                            editText.setText("");
                            editText.setEnabled(false);
                        }
                    } catch (Exception e) {
                        Log.e("configureSpinner", "Error handling spinner item selection: " + e.getMessage(), e);
                        // Optionally, handle the error gracefully (e.g., show a toast or dialog)
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Handle the case when nothing is selected (optional)
                }
            });
        } catch (Exception e) {
            Log.e("configureSpinner", "Error configuring spinner: " + e.getMessage(), e);
            // Optionally, handle the error gracefully (e.g., show a toast or dialog)
        }
    }

    private void configureSimpleSpinner(Spinner spinner, String[] options) {
        try {
            if (options == null || options.length == 0) {
                throw new IllegalArgumentException("Options cannot be null or empty.");
            }

            // Add default value to the options
            String[] modifiedOptions = new String[options.length + 1];
            modifiedOptions[0] = "Select Here"; // Default value
            System.arraycopy(options, 0, modifiedOptions, 1, options.length);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, modifiedOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        } catch (IllegalArgumentException e) {
            Log.e("configureSimpleSpinner", "IllegalArgumentException: " + e.getMessage(), e);
            // Optionally, show a Toast or a Dialog to notify the user about the error
        } catch (Exception e) {
            Log.e("configureSimpleSpinner", "Error configuring spinner: " + e.getMessage(), e);
            // Optionally, show a Toast or a Dialog to notify the user about the error
        }
    }

    private void saveMedicalRecords() {
        try {
            String userId = auth.getCurrentUser().getUid();

            if (userId == null) {
                Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect data
            Map<String, Object> medicalData = new HashMap<>();

            if (!validateAndCollectData(spinnerPastMedicalHistory, editTextPastMedicalHistoryOther, medicalData, "pastMedicalHistory")) return;
            if (!validateAndCollectData(spinnerAllergies, editTextAllergiesOther, medicalData, "allergies")) return;
            if (!validateAndCollectData(spinnerMedications, editTextMedicationsOther, medicalData, "medications")) return;
            if (!validateAndCollectData(spinnerImmunizations, editTextImmunizationsOther, medicalData, "immunizations")) return;
            if (!validateAndCollectData(spinnerFamilyHistory, editTextFamilyHistory, medicalData, "familyHistory")) return;

            // Collect social history data
            Map<String, String> socialHistory = collectSocialHistory();
            if (socialHistory == null) {
                // Stop execution if social history validation fails
                return;
            }

            medicalData.put("socialHistory", socialHistory);

            // Save data to Firebase
            databaseReference.child(userId).setValue(medicalData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Medical records saved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to save medical records.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            // Handle any unexpected errors here
            Toast.makeText(getContext(), "Error occurred while saving medical records: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private Map<String, String> collectSocialHistory() {
        Map<String, String> socialHistory = new HashMap<>();

        try {
            // Validate each social history field
            if (!validateSocialSpinner(spinnerSmoking, "Smoking")) return null;
            if (!validateSocialSpinner(spinnerAlcoholUse, "Alcohol Use")) return null;
            if (!validateSocialSpinner(spinnerDrugUse, "Drug Use")) return null;
            if (!validateSocialSpinner(spinnerExercise, "Exercise")) return null;
            if (!validateSocialSpinner(spinnerDiet, "Diet")) return null;
            if (!validateSocialSpinner(spinnerOccupation, "Occupation")) return null;

            // Collect data after validation
            socialHistory.put("Smoking", spinnerSmoking.getSelectedItem().toString());
            socialHistory.put("AlcoholUse", spinnerAlcoholUse.getSelectedItem().toString());
            socialHistory.put("DrugUse", spinnerDrugUse.getSelectedItem().toString());
            socialHistory.put("Exercise", spinnerExercise.getSelectedItem().toString());
            socialHistory.put("Diet", spinnerDiet.getSelectedItem().toString());
            socialHistory.put("Occupation", spinnerOccupation.getSelectedItem().toString());

        } catch (Exception e) {
            // Handle any unexpected errors here
            Toast.makeText(getContext(), "Error occurred while collecting social history: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }

        return socialHistory;
    }

    private boolean validateSocialSpinner(Spinner spinner, String fieldName) {
        try {
            String selectedValue = spinner.getSelectedItem().toString();
            if ("Select Here".equals(selectedValue)) {
                Toast.makeText(getContext(), "Please select a valid option for " + fieldName + ".", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (Exception e) {
            // Handle unexpected errors here
            Toast.makeText(getContext(), "Error occurred while validating " + fieldName + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateAndCollectData(Spinner spinner, EditText editText, Map<String, Object> medicalData, String key) {
        try {
            String selectedValue = spinner.getSelectedItem().toString();

            if ("Select Here".equals(selectedValue)) {
                Toast.makeText(getContext(), "Please select a valid option for " + key + ".", Toast.LENGTH_SHORT).show();
                return false;
            }

            if ("Other".equals(selectedValue)) {
                String otherValue = editText.getText().toString();
                if (otherValue.isEmpty()) {
                    Toast.makeText(getContext(), "Please provide details for 'Other' in " + key + ".", Toast.LENGTH_SHORT).show();
                    return false;
                }
                medicalData.put(key, "Other: " + otherValue);
            } else {
                medicalData.put(key, selectedValue);
            }

            return true;

        } catch (Exception e) {
            // Handle any unexpected errors here
            Toast.makeText(getContext(), "Error occurred while collecting data for " + key + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private void loadMedicalRecords() {
        String userId = auth.getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            Map<String, Object> medicalData = (Map<String, Object>) snapshot.getValue();

                            if (medicalData != null) {
                                populateFields(medicalData);
                            }
                        } else {
                            Toast.makeText(getContext(), "No medical records found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Handle any errors while processing the snapshot
                        Toast.makeText(getContext(), "Error occurred while processing medical records: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    try {
                        Toast.makeText(getContext(), "Failed to load medical records: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Handle any errors while showing the error message
                        Toast.makeText(getContext(), "Error occurred while handling database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            // Handle any unexpected errors with the Firebase listener setup
            Toast.makeText(getContext(), "Error occurred while loading medical records: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void populateFields(Map<String, Object> medicalData) {
        try {
            // Populate medical history fields
            populateSpinnerWithOther(spinnerPastMedicalHistory, editTextPastMedicalHistoryOther, (String) medicalData.get("pastMedicalHistory"));
            populateSpinnerWithOther(spinnerAllergies, editTextAllergiesOther, (String) medicalData.get("allergies"));
            populateSpinnerWithOther(spinnerMedications, editTextMedicationsOther, (String) medicalData.get("medications"));
            populateSpinnerWithOther(spinnerImmunizations, editTextImmunizationsOther, (String) medicalData.get("immunizations"));
            populateSpinnerWithOther(spinnerFamilyHistory, editTextFamilyHistory, (String) medicalData.get("familyHistory"));

            // Populate social history fields
            if (medicalData.containsKey("socialHistory")) {
                Map<String, String> socialHistory = (Map<String, String>) medicalData.get("socialHistory");
                populateSimpleSpinner(spinnerSmoking, socialHistory.get("Smoking"));
                populateSimpleSpinner(spinnerAlcoholUse, socialHistory.get("AlcoholUse"));
                populateSimpleSpinner(spinnerDrugUse, socialHistory.get("DrugUse"));
                populateSimpleSpinner(spinnerExercise, socialHistory.get("Exercise"));
                populateSimpleSpinner(spinnerDiet, socialHistory.get("Diet"));
                populateSimpleSpinner(spinnerOccupation, socialHistory.get("Occupation"));
            }
        } catch (Exception e) {
            // Handle any unexpected errors while populating fields
            Toast.makeText(getContext(), "Error occurred while populating fields: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void populateSpinnerWithOther(Spinner spinner, EditText editText, String value) {
        try {
            if (value == null || spinner.getAdapter() == null) return;

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            int position = adapter.getPosition(value);

            if (position >= 0) {
                spinner.setSelection(position);
                editText.setEnabled(false);
            } else if (value.startsWith("Other: ")) {
                spinner.setSelection(adapter.getPosition("Other"));
                editText.setText(value.replace("Other: ", ""));
                editText.setEnabled(true);
            } else {
                spinner.setSelection(0); // Default selection
                editText.setText("");
                editText.setEnabled(false);
            }
        } catch (Exception e) {
            // Handle any unexpected errors while populating the spinner and edit text
            Toast.makeText(getContext(), "Error occurred while populating the spinner: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void populateSimpleSpinner(Spinner spinner, String value) {
        try {
            if (value == null) return;

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            if (adapter == null) return;  // Added a null check for the adapter to avoid potential NPE

            int position = adapter.getPosition(value);

            if (position >= 0) {
                spinner.setSelection(position);
            } else {
                spinner.setSelection(0); // Default selection
            }
        } catch (Exception e) {
            // Handle any unexpected errors while populating the spinner
            Toast.makeText(getContext(), "Error occurred while populating the spinner: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
