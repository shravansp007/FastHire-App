package com.example.finalyear;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class pprofile extends AppCompatActivity {

    EditText etName, etMobile, etEmail, etDOB, etPassword;
    Button btnEdit, btnSave;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pprofile);

        etName = findViewById(R.id.editTextText22);
        etMobile = findViewById(R.id.editTextText25);
        etEmail = findViewById(R.id.editTextText28);
        etDOB = findViewById(R.id.editTextText27);
        etPassword = findViewById(R.id.editTextText26);

        btnEdit = findViewById(R.id.button14);
        btnSave = findViewById(R.id.button15);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // Load user data from Firestore including password
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etMobile.setText(documentSnapshot.getString("mobile"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        etDOB.setText(documentSnapshot.getString("dob"));
                        etPassword.setText(documentSnapshot.getString("password")); // show password
                    }
                });

        // Disable all fields initially
        setFieldsEnabled(false);

        // Edit button makes fields editable
        btnEdit.setOnClickListener(v -> setFieldsEnabled(true));

        // DOB picker
        etDOB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    pprofile.this,
                    (view, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day);
            dialog.show();
        });

        // Save button updates Firestore and FirebaseAuth
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newMobile = etMobile.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newDOB = etDOB.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            // Update password in FirebaseAuth if changed
            if (!newPassword.isEmpty() && !newPassword.equals(currentUser.getEmail())) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Password update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }

            // Update email in FirebaseAuth if changed
            if (!newEmail.equals(currentUser.getEmail())) {
                currentUser.updateEmail(newEmail).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Email update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }

            // Update Firestore (including password)
            HashMap<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("mobile", newMobile);
            updatedData.put("email", newEmail);
            updatedData.put("dob", newDOB);
            updatedData.put("password", newPassword); // store password in Firestore (optional)

            db.collection("Users").document(userId).update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setFieldsEnabled(false); // make fields read-only again
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // Enable or disable all fields
    private void setFieldsEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etMobile.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etDOB.setEnabled(enabled);
        etPassword.setEnabled(enabled);
    }

    public static class pnotifications {
    }
}



