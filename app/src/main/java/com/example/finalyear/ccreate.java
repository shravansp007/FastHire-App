package com.example.finalyear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class ccreate extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword, etDOB, etMobile,genderGroup;

    CheckBox cb1, cb2, cb3, cb4;
    Button btnSubmit;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ccreate);


        etName = findViewById(R.id.editTextText5);
        etEmail = findViewById(R.id.editTextText11);
        etPassword = findViewById(R.id.editTextText7);
        etConfirmPassword = findViewById(R.id.editTextText8);
        etDOB = findViewById(R.id.editTextText10);
        etMobile = findViewById(R.id.editTextText6);
        genderGroup = findViewById(R.id.editTextText9);
        cb1 = findViewById(R.id.checkBox);
        cb2 = findViewById(R.id.checkBox2);
        cb3 = findViewById(R.id.checkBox3);
        cb4 = findViewById(R.id.checkBox4);
        btnSubmit = findViewById(R.id.button7);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        etDOB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ccreate.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etDOB.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String dob = etDOB.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String gender = genderGroup.getText().toString().trim();


            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                    || dob.isEmpty() || mobile.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();

                    HashMap<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("dob", dob);
                    userData.put("password",password );
                    userData.put("mobile", mobile);
                    userData.put("gender", gender);
                    userData.put("checkbox1", cb1.isChecked());
                    userData.put("checkbox2", cb2.isChecked());
                    userData.put("checkbox3", cb3.isChecked());
                    userData.put("checkbox4", cb4.isChecked());

                    db.collection("Users").document(userId).set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ccreate.this, cdashboard.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                } else {
                    Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}


