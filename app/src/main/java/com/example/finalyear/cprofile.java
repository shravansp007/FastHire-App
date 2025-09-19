package com.example.finalyear;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class cprofile extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etMobile, etDOB, etGender;
    CheckBox cb1, cb2, cb3, cb4;
    Button btnEdit, btnSave;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    boolean isEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cprofile);

        // Bind UI
        etName = findViewById(R.id.editTextText29);
        etMobile = findViewById(R.id.editTextText30);
        etPassword = findViewById(R.id.editTextText31);
        etEmail = findViewById(R.id.editTextText32);
        etGender = findViewById(R.id.editTextText33);
        etDOB = findViewById(R.id.editTextText34);

        cb1 = findViewById(R.id.checkBox5);   // Heavy
        cb2 = findViewById(R.id.checkBox6);   // Construction
        cb3 = findViewById(R.id.checkBox7);   // Carpentry
        cb4 = findViewById(R.id.checkBox8);   // Plumbing

        btnEdit = findViewById(R.id.button19);
        btnSave = findViewById(R.id.button20);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            loadUserData();
        }

        btnEdit.setOnClickListener(v -> setEditable(true));
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        String userId = currentUser.getUid();
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etEmail.setText(documentSnapshot.getString("email"));
                        etDOB.setText(documentSnapshot.getString("dob"));
                        etMobile.setText(documentSnapshot.getString("mobile"));
                        etGender.setText(documentSnapshot.getString("gender"));

                        // Load skills list
                        List<String> skills = (List<String>) documentSnapshot.get("skills");
                        if (skills != null) {
                            cb1.setChecked(skills.contains("Heavy Work"));
                            cb2.setChecked(skills.contains("Construction"));
                            cb3.setChecked(skills.contains("Carpentry"));
                            cb4.setChecked(skills.contains("Plumbing"));
                        }

                        setEditable(false); // lock fields initially
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void saveUserData() {
        String userId = currentUser.getUid();

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim(); // only updates in FirebaseAuth
        String dob = etDOB.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        ArrayList<String> skills = new ArrayList<>();
        if (cb1.isChecked()) skills.add("Heavy Work");
        if (cb2.isChecked()) skills.add("Construction");
        if (cb3.isChecked()) skills.add("Carpentry");
        if (cb4.isChecked()) skills.add("Plumbing");

        // Update Firestore (no password stored)
        db.collection("Users").document(userId).update(
                "name", name,
                "email", email,
                "dob", dob,
                "mobile", mobile,
                "gender", gender,
                "skills", skills
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();

            // ✅ Update password in Firebase Authentication if user entered one
            if (!newPassword.isEmpty()) {
                currentUser.updatePassword(newPassword)
                        .addOnSuccessListener(a -> Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            setEditable(false);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void setEditable(boolean enable) {
        isEditable = enable;

        etName.setEnabled(enable);
        etEmail.setEnabled(enable);
        etPassword.setEnabled(enable); // left empty initially, used only if user wants to change
        etDOB.setEnabled(enable);
        etMobile.setEnabled(enable);
        etGender.setEnabled(enable);

        cb1.setEnabled(enable);
        cb2.setEnabled(enable);
        cb3.setEnabled(enable);
        cb4.setEnabled(enable);

        btnSave.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }
}

