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

import java.util.ArrayList;
import java.util.List;

public class cnotifications extends AppCompatActivity {

    RecyclerView recyclerView;
    notificationAdapter adapter;
    List<notificationModel> notificationList;
    FirebaseFirestore db;

    String workerMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cnotifications);

        recyclerView = findViewById(R.id.notificationRecycler);
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        // ✅ Get worker mobile from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        workerMobile = prefs.getString("workerMobile", null);

        if (workerMobile == null || workerMobile.isEmpty()) {
            Toast.makeText(this, "No worker logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("DEBUG", "Fetching invites for workerMobile: " + workerMobile);

        adapter = new notificationAdapter(this, notificationList, workerMobile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchNotifications();
    }

    private void fetchNotifications() {
        db.collection("Invites")
                .document(workerMobile)   // ✅ use mobile as key
                .collection("requests")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationList.clear();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot) {
                            String employerName = doc.getString("employerName");
                            String employerMobile = doc.getString("employerMobile");
                            String date = doc.getString("date");
                            String from = doc.getString("from");
                            String to = doc.getString("to");
                            String docId = doc.getId();

                            notificationList.add(new notificationModel(
                                    employerName,
                                    employerMobile,
                                    date,
                                    from,
                                    to,
                                    docId
                            ));
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        Toast.makeText(this, "No invitations found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}










