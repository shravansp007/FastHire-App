package com.example.finalyear;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class cjobsavai extends AppCompatActivity {

    private RecyclerView recycler;
    private JobFeedAdapter adapter;
    private final List<JobFeedModel> items = new ArrayList<>();
    private FirebaseFirestore db;
    private String workerMobile;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cjobsavai);

        db = FirebaseFirestore.getInstance();

        // Get worker mobile from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        workerMobile = prefs.getString("workerMobile", null);

        if (workerMobile == null || workerMobile.trim().isEmpty()) {
            Toast.makeText(this, "Worker mobile not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recycler = findViewById(R.id.jobsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Pass workerMobile to adapter (used when writing interest back)
        adapter = new JobFeedAdapter(this, items, workerMobile);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void attachListener() {
        if (workerMobile == null) return;

        reg = db.collection("JobFeeds")
                .document(workerMobile)
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<JobFeedModel> fresh = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String jobId         = d.getId(); // jobId used as doc id
                            String employerName  = safe(d.getString("employerName"));
                            String employerMob   = safe(d.getString("employerMobile"));
                            String date          = safe(d.getString("date"));
                            String from          = safe(d.getString("from"));
                            String to            = safe(d.getString("to"));
                            String interest      = safe(d.getString("interest"));
                            if (interest.isEmpty()) interest = "pending";

                            fresh.add(new JobFeedModel(jobId, employerName, employerMob, date, from, to, interest));
                        }
                    }
                    items.clear();
                    items.addAll(fresh);
                    adapter.notifyDataSetChanged();
                });
    }

    private static String safe(String s) { return s == null ? "" : s; }
}

