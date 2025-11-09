package com.example.finalyear;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class cnotifications extends AppCompatActivity {

    private static final String TAG = "cnotifications";

    RecyclerView recyclerView;
    notificationAdapter adapter;
    final List<notificationModel> notificationList = new ArrayList<>();
    FirebaseFirestore db;

    String workerMobile;
    private ListenerRegistration invitesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cnotifications);

        recyclerView = findViewById(R.id.notificationRecycler);
        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        workerMobile = prefs.getString("workerMobile", null);

        if (workerMobile == null || workerMobile.trim().isEmpty()) {
            Toast.makeText(this, "No worker logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Fetching invites for workerMobile: " + workerMobile);

        adapter = new notificationAdapter(this, notificationList, workerMobile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (invitesListener != null) {
            invitesListener.remove();
            invitesListener = null;
        }
    }

    private void attachListener() {
        if (invitesListener != null) return;

        invitesListener = db.collection("Invites")
                .document(workerMobile)
                .collection("requests")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    List<notificationModel> fresh = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            String employerName = safe(doc.getString("employerName"));
                            String employerMobile = safe(doc.getString("employerMobile"));
                            String date = safe(doc.getString("date"));
                            String from = safe(doc.getString("from"));
                            String to = safe(doc.getString("to"));
                            String status = safe(doc.getString("status")); // may be "", treat as pending
                            if (status.isEmpty()) status = "pending";

                            fresh.add(new notificationModel(
                                    employerName, employerMobile,
                                    date, from, to,
                                    doc.getId(), status
                            ));
                        }
                    }

                    notificationList.clear();
                    notificationList.addAll(fresh);
                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty() && !isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static String safe(String s) { return s == null ? "" : s; }
}












