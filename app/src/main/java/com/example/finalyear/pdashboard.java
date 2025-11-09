package com.example.finalyear;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class pdashboard extends AppCompatActivity {

    SearchView searchView;
    Button searchButton;
    RecyclerView recyclerView;
    crecycler adapter;
    List<cinfo> cinfoList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdashboard);

        searchView = findViewById(R.id.bar);
        searchButton = findViewById(R.id.button9);
        recyclerView = findViewById(R.id.recycle);

        db = FirebaseFirestore.getInstance();
        cinfoList = new ArrayList<>();

        adapter = new crecycler(this, cinfoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(view -> {
            String query = searchView.getQuery().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Enter a skill to search", Toast.LENGTH_SHORT).show();
                return;
            }

            String skillField = getSkillField(query);
            if (skillField == null) {
                Toast.makeText(this, "Skill not recognized", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchCustomersBySkill(skillField);
        });
    }

    private String getSkillField(String skill) {
        // normalize case-insensitively
        String s = skill.toLowerCase();
        switch (s) {
            case "construction":
                return "checkbox1";
            case "plumbing":
                return "checkbox2";
            case "electrical":
                return "checkbox3";
            case "heavy works":
            case "heavy":
                return "checkbox4";
            case "carpentry":
                return "checkbox5";
            default:
                return null;
        }
    }

    private void fetchCustomersBySkill(String skillField) {
        db.collection("Users")
                .whereEqualTo(skillField, true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cinfoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String mobile = doc.getString("mobile");

                        // ⭐ Read rating if present; default to 0
                        Double r = doc.getDouble("rating");
                        float rating = (r == null) ? 5f : r.floatValue();

                        cinfoList.add(new cinfo(name, mobile, rating));
                    }
                    adapter.notifyDataSetChanged();

                    if (cinfoList.isEmpty()) {
                        Toast.makeText(this, "No customers found with that skill", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}






