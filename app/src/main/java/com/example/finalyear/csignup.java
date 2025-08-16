package com.example.finalyear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class csignup extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnCreate, btnSignIn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csignup);

        etEmail = findViewById(R.id.editTextText3);
        etPassword = findViewById(R.id.editTextText4);
        btnCreate = findViewById(R.id.button6);
        btnSignIn = findViewById(R.id.button5);

        mAuth = FirebaseAuth.getInstance();


        btnCreate.setOnClickListener(view -> {
            Intent intent = new Intent(csignup.this, ccreate.class);
            startActivity(intent);
        });


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
                            Toast.makeText(csignup.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(csignup.this, cdashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(csignup.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
