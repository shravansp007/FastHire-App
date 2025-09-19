package com.example.finalyear;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class pinvite extends AppCompatActivity {

    EditText etName, etDate, etTimeFrom, etTimeTo, etRating;
    Button btnDone, btnInvite;

    FirebaseFirestore db;

    String workerName, workerMobile;
    float workerRating;

    String employerName = "";
    String employerMobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinvite);

        etName = findViewById(R.id.editTextText35);
        etDate = findViewById(R.id.editTextText36);
        etTimeFrom = findViewById(R.id.editTextText38);
        etTimeTo = findViewById(R.id.editTextText39);
        etRating = findViewById(R.id.editTextText40);
        btnDone = findViewById(R.id.button21);
        btnInvite = findViewById(R.id.button22);

        db = FirebaseFirestore.getInstance();

        // ✅ Get worker details from intent
        workerName = getIntent().getStringExtra("name");
        workerMobile = getIntent().getStringExtra("mobile");
        workerRating = getIntent().getFloatExtra("rating", 0f);

        // ✅ Pre-fill
        etName.setText(workerName);
        etRating.setText(String.valueOf(workerRating));

        // ✅ Fetch employer details from Firestore using UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot snapshot = task.getResult();
                        employerName = snapshot.getString("name");
                        employerMobile = snapshot.getString("mobile");
                    } else {
                        Toast.makeText(pinvite.this, "Employer profile not found", Toast.LENGTH_SHORT).show();
                    }
                });

        btnDone.setOnClickListener(v -> finish());
        btnInvite.setOnClickListener(v -> saveInvite());
    }

    private void saveInvite() {
        String date = etDate.getText().toString().trim();
        String from = etTimeFrom.getText().toString().trim();
        String to = etTimeTo.getText().toString().trim();

        if (date.isEmpty() || from.isEmpty() || to.isEmpty()) {
            Toast.makeText(this, "Please fill Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (employerName.isEmpty() || employerMobile.isEmpty()) {
            Toast.makeText(this, "Employer details not loaded yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> invite = new HashMap<>();
        invite.put("employerName", employerName);
        invite.put("employerMobile", employerMobile);
        invite.put("date", date);
        invite.put("from", from);
        invite.put("to", to);
        invite.put("workerName", workerName);
        invite.put("workerMobile", workerMobile);
        invite.put("workerRating", workerRating);

        db.collection("Invites")
                .document(workerMobile) // better to use workerMobile instead of workerName as ID
                .collection("requests")
                .add(invite)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(pinvite.this, "Invitation sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(pinvite.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}





