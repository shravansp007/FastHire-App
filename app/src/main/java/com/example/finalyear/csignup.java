package com.example.finalyear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class csignup extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnCreate, btnSignIn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csignup);

        etEmail = findViewById(R.id.editTextText3);
        etPassword = findViewById(R.id.editTextText4);
        btnCreate = findViewById(R.id.button6);
        btnSignIn = findViewById(R.id.button5);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Go to account creation screen
        btnCreate.setOnClickListener(view -> {
            Intent intent = new Intent(csignup.this, ccreate.class);
            startActivity(intent);
        });

        // Sign In button
        btnSignIn.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(csignup.this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // ✅ Look inside Users collection (not workers)
                            db.collection("Users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                                            String workerName = doc.getString("name");
                                            String workerMobile = doc.getString("mobile");

                                            // ✅ Save worker details locally
                                            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("workerName", workerName);
                                            editor.putString("workerMobile", workerMobile);
                                            editor.apply();

                                            Toast.makeText(csignup.this, "Welcome " + workerName, Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(csignup.this, cdashboard.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(csignup.this, "Worker profile not found in Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(csignup.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );

                        } else {
                            Toast.makeText(csignup.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}


