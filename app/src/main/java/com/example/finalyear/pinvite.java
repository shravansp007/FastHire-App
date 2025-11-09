package com.example.finalyear;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class pinvite extends AppCompatActivity {

    private EditText etName, etDate, etTimeFrom, etTimeTo, etRating;
    private Button btnDone, btnInvite;

    private FirebaseFirestore db;

    private String workerName, workerMobile;
    private float workerRating;

    private String employerName = "";
    private String employerMobile = "";

    private boolean employerLoaded = false;
    private boolean sending = false;
    private boolean rateOnly = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinvite);

        etName     = findViewById(R.id.editTextText35);
        etDate     = findViewById(R.id.editTextText36);
        etTimeFrom = findViewById(R.id.editTextText38);
        etTimeTo   = findViewById(R.id.editTextText39);
        etRating   = findViewById(R.id.editTextText40);
        btnDone    = findViewById(R.id.button21);
        btnInvite  = findViewById(R.id.button22);

        db = FirebaseFirestore.getInstance();

        // Get worker details from intent
        workerName   = getIntent().getStringExtra("name");
        workerMobile = getIntent().getStringExtra("mobile");
        workerRating = getIntent().getFloatExtra("rating", 0f);
        rateOnly     = getIntent().getBooleanExtra("rateOnly", false);
        final String inviteId   = getIntent().getStringExtra("inviteId");    // for rate-only accepted/rejected
        final String prefillDate = getIntent().getStringExtra("prefillDate"); // for worker_interested flow
        final String prefillFrom = getIntent().getStringExtra("prefillFrom");
        final String prefillTo   = getIntent().getStringExtra("prefillTo");

        if (TextUtils.isEmpty(workerMobile)) {
            Toast.makeText(this, "Missing worker details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill name & rating (rating shown but disabled in normal mode)
        etName.setText(workerName != null ? workerName : "");
        if (workerRating > 0f) etRating.setText(String.valueOf(workerRating));

        // Date/time pickers (active only when fields enabled)
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker());
        etTimeFrom.setFocusable(false);
        etTimeFrom.setOnClickListener(v -> showTimePicker(etTimeFrom));
        etTimeTo.setFocusable(false);
        etTimeTo.setOnClickListener(v -> showTimePicker(etTimeTo));

        if (rateOnly) {
            // ★ RATE-ONLY MODE (Accept/Reject completed → employer should rate)
            enableRateOnlyMode();
            etRating.setEnabled(true); // only rating editable
            // show original invite details if provided
            if (!TextUtils.isEmpty(inviteId)) {
                loadInviteDetails(inviteId);
            } else {
                // fallback: at least show the latest known rating
                preloadCurrentWorkerRating();
            }
        } else {
            // ★ BEGIN-INVITE MODE (worker clicked "Interested")
            etRating.setEnabled(false);
            // Optional prefill from employer notification
            if (!TextUtils.isEmpty(prefillDate)) etDate.setText(prefillDate);
            if (!TextUtils.isEmpty(prefillFrom)) etTimeFrom.setText(prefillFrom);
            if (!TextUtils.isEmpty(prefillTo))   etTimeTo.setText(prefillTo);

            btnInvite.setEnabled(false); // wait until employer profile loads
            loadEmployer();
        }

        btnDone.setOnClickListener(v -> {
            if (rateOnly) {
                saveWorkerRating(); // save rating & finish
            } else {
                finish();
            }
        });

        btnInvite.setOnClickListener(v -> {
            if (!rateOnly) saveInvite();
        });
    }

    private void enableRateOnlyMode() {
        // Lock non-rating fields (visible but not editable)
        etName.setEnabled(false);
        etDate.setEnabled(false);
        etTimeFrom.setEnabled(false);
        etTimeTo.setEnabled(false);

        // Hide Invite button
        btnInvite.setEnabled(false);
        btnInvite.setAlpha(0.5f);
        btnInvite.setClickable(false);
        btnInvite.setVisibility(android.view.View.GONE);

        // Done button saves rating
        btnDone.setText("Save Rating");
    }

    private void preloadCurrentWorkerRating() {
        db.collection("Users")
                .whereEqualTo("mobile", workerMobile)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentSnapshot d = snap.getDocuments().get(0);
                        Double r = d.getDouble("rating");
                        if (r != null) etRating.setText(String.valueOf(r.floatValue()));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load rating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Load invite's date/from/to so employer sees what was originally proposed (rate-only flow)
    private void loadInviteDetails(String inviteId) {
        db.collection("Invites")
                .document(workerMobile)
                .collection("requests")
                .document(inviteId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String date = safe(d.getString("date"));
                        String from = safe(d.getString("from"));
                        String to   = safe(d.getString("to"));
                        etDate.setText(date);
                        etTimeFrom.setText(from);
                        etTimeTo.setText(to);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invite: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveWorkerRating() {
        String ratingText = String.valueOf(etRating.getText()).trim();
        if (ratingText.isEmpty()) {
            Toast.makeText(this, "Enter a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        double parsed;
        try {
            parsed = Double.parseDouble(ratingText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clamp & freeze for lambda
        final double ratingToSave = Math.max(0, Math.min(5, parsed));

        android.util.Log.d("RATE", "Looking up worker by mobile='" + workerMobile + "'");

        db.collection("Users")
                .whereEqualTo("mobile", workerMobile)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "Worker profile not found for mobile: " + workerMobile, Toast.LENGTH_SHORT).show();
                        android.util.Log.e("RATE", "No Users doc found with mobile=" + workerMobile);
                        return;
                    }
                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    String docId = doc.getId();
                    android.util.Log.d("RATE", "Updating Users/" + docId + " rating=" + ratingToSave);

                    db.collection("Users").document(docId)
                            .update("rating", Double.valueOf(ratingToSave))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Rating saved", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                android.util.Log.e("RATE", "Update failed", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error finding worker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("RATE", "Query failed", e);
                });
    }

    private void loadEmployer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        employerName = safe(snapshot.getString("name"));
                        employerMobile = safe(snapshot.getString("mobile"));
                        employerLoaded = !employerName.isEmpty() && !employerMobile.isEmpty();
                        btnInvite.setEnabled(employerLoaded);
                        if (!employerLoaded) {
                            Toast.makeText(this, "Incomplete employer profile", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Employer profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load employer: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveInvite() {
        if (!employerLoaded) {
            Toast.makeText(this, "Employer details not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sending) return; // debounce
        sending = true;
        btnInvite.setEnabled(false);

        String date = safe(String.valueOf(etDate.getText())).trim();
        String from = safe(String.valueOf(etTimeFrom.getText())).trim();
        String to   = safe(String.valueOf(etTimeTo.getText())).trim();

        if (date.isEmpty() || from.isEmpty() || to.isEmpty()) {
            Toast.makeText(this, "Please fill Date and Time", Toast.LENGTH_SHORT).show();
            resetSendState();
            return;
        }

        if (!isTimeRangeValid(from, to)) {
            Toast.makeText(this, "Time range is invalid", Toast.LENGTH_SHORT).show();
            resetSendState();
            return;
        }

        Map<String, Object> invite = new HashMap<>();
        invite.put("employerName", employerName);
        invite.put("employerMobile", employerMobile);
        invite.put("date", date);     // string as used across app
        invite.put("from", from);     // HH:mm
        invite.put("to", to);         // HH:mm
        invite.put("workerName", safe(workerName));
        invite.put("workerMobile", workerMobile);
        invite.put("workerRating", parseOrZero(etRating.getText().toString()));
        invite.put("status", "pending");
        invite.put("createdAt", FieldValue.serverTimestamp());

        db.collection("Invites")
                .document(workerMobile)
                .collection("requests")
                .add(invite)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(pinvite.this, "Invitation sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(pinvite.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetSendState();
                });
    }

    private void resetSendState() {
        sending = false;
        btnInvite.setEnabled(employerLoaded);
    }

    private static String safe(@Nullable String s) {
        return s == null ? "" : s;
    }

    private float parseOrZero(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0f; }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String text = String.format(Locale.US, "%d/%d/%d", d, (m + 1), y); // matches your format elsewhere
            etDate.setText(text);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, min) -> {
            String t = String.format(Locale.US, "%02d:%02d", h, min);
            target.setText(t);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    // expects HH:mm
    private boolean isTimeRangeValid(String from, String to) {
        try {
            int fh = Integer.parseInt(from.substring(0, 2));
            int fm = Integer.parseInt(from.substring(3, 5));
            int th = Integer.parseInt(to.substring(0, 2));
            int tm = Integer.parseInt(to.substring(3, 5));
            return (th > fh) || (th == fh && tm > fm);
        } catch (Exception e) {
            return false;
        }
    }
}



