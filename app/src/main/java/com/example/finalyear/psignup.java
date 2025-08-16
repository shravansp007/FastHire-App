package com.example.finalyear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class psignup extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnCreate, btnSignIn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psignup);

        etEmail = findViewById(R.id.editTextText);
        etPassword = findViewById(R.id.editTextText2);
        btnCreate = findViewById(R.id.button4);
        btnSignIn = findViewById(R.id.button3);

        mAuth = FirebaseAuth.getInstance();


        btnCreate.setOnClickListener(view -> {
            Intent intent = new Intent(psignup.this, pcreate.class);
            startActivity(intent);
        });


        btnSignIn.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(psignup.this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(psignup.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(psignup.this, pjob.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(psignup.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}