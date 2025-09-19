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

        // ✅ Pass context + list to adapter
        adapter = new crecycler(this, cinfoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(view -> {
            String query = searchView.getQuery().toString().trim().toLowerCase();
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
        switch (skill) {
            case "construction":
            case "Construction":
                return "checkbox1";
            case "plumbing":
            case "Plumbing":
                return "checkbox2";
            case "electrical":
            case "Electrical":
                return "checkbox3";
            case "heavy works":
            case "Heavy works":
                return "checkbox4";
            case "carpentry":
            case "Carpentry":
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
                        cinfoList.add(new cinfo(name, mobile, 5.0f));
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





