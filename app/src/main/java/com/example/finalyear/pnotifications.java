package com.example.finalyear;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class pnotifications extends AppCompatActivity {

    private RecyclerView recycler;
    private EmployerNotificationAdapter adapter;
    private final List<EmployerNotificationModel> items = new ArrayList<>();
    private FirebaseFirestore db;

    private String employerMobile;   // key under EmployerNotifications/{employerMobile}
    private ListenerRegistration reg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pnotifications);

        db = FirebaseFirestore.getInstance();

        recycler = findViewById(R.id.notificationsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        // temporary adapter until we know employerMobile; we’ll replace it after loading
        adapter = new EmployerNotificationAdapter(this, items, "");
        recycler.setAdapter(adapter);

        // Load employer mobile from Users/{uid}
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        employerMobile = safe(snap.getString("mobile"));
                        if (employerMobile.isEmpty()) {
                            Toast.makeText(this, "Employer mobile missing in profile", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // set real adapter now that we have the key used for dismiss path
                        adapter = new EmployerNotificationAdapter(this, items, employerMobile);
                        recycler.setAdapter(adapter);

                        // ✅ attach AFTER employerMobile is known
                        attach();
                    } else {
                        Toast.makeText(this, "Employer profile not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void attach() {
        if (employerMobile == null || employerMobile.isEmpty()) return;
        if (reg != null) { reg.remove(); reg = null; }

        reg = db.collection("EmployerNotifications")
                .document(employerMobile)
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<EmployerNotificationModel> fresh = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String docId    = d.getId();
                            String type     = safe(d.getString("type"));
                            if (type.isEmpty()) type = safe(d.getString("status")); // legacy
                            String wName    = safe(d.getString("workerName"));
                            String wMobile  = safe(d.getString("workerMobile"));
                            String jobId    = safe(d.getString("jobId"));
                            String inviteId = safe(d.getString("inviteId"));
                            String date     = safe(d.getString("date"));
                            String from     = safe(d.getString("from"));
                            String to       = safe(d.getString("to"));

                            fresh.add(new EmployerNotificationModel(
                                    docId, type, wName, wMobile, jobId, inviteId, date, from, to
                            ));
                        }
                    }
                    items.clear();
                    items.addAll(fresh);
                    adapter.notifyDataSetChanged();
                });
    }

    private static String safe(String s) { return s == null ? "" : s; }
}


