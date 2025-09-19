package com.example.finalyear;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class pcreatejob extends AppCompatActivity {

    EditText nameEdit, mobileEdit, jobEdit, dateEdit, fromTimeEdit, toTimeEdit;
    Button createBtn;
    CheckBox carpentryCheck, constructionCheck, plumbingCheck, heavyCheck;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pcreatejob);

        // Inputs
        nameEdit = findViewById(R.id.editTextText18);
        mobileEdit = findViewById(R.id.editTextText19);
        jobEdit = findViewById(R.id.editTextText20);
        dateEdit = findViewById(R.id.editTextText21);
        fromTimeEdit = findViewById(R.id.editTextText23);
        toTimeEdit = findViewById(R.id.editTextText24);
        createBtn = findViewById(R.id.button12);

        // Checkboxes
        carpentryCheck = findViewById(R.id.checkBox9);
        constructionCheck = findViewById(R.id.checkBox10);
        plumbingCheck = findViewById(R.id.checkBox12);
        heavyCheck = findViewById(R.id.checkBox11);

        db = FirebaseFirestore.getInstance();

        // Date Picker
        dateEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    pcreatejob.this,
                    (view, selectedYear, selectedMonth, selectedDay) ->
                            dateEdit.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                    year, month, day);
            datePickerDialog.show();
        });

        // From Time Picker
        fromTimeEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    pcreatejob.this,
                    (view, hourOfDay, minute1) ->
                            fromTimeEdit.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });

        // To Time Picker
        toTimeEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    pcreatejob.this,
                    (view, hourOfDay, minute1) ->
                            toTimeEdit.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Create Job Button
        createBtn.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String mobile = mobileEdit.getText().toString().trim();
            String job = jobEdit.getText().toString().trim();
            String date = dateEdit.getText().toString().trim();
            String from = fromTimeEdit.getText().toString().trim();
            String to = toTimeEdit.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty() || job.isEmpty() || date.isEmpty() || from.isEmpty() || to.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect selected skills
            ArrayList<String> skills = new ArrayList<>();
            if (carpentryCheck.isChecked()) skills.add("Carpentry");
            if (constructionCheck.isChecked()) skills.add("Construction");
            if (plumbingCheck.isChecked()) skills.add("Plumbing");
            if (heavyCheck.isChecked()) skills.add("Heavy");

            // Job Data
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("name", name);
            jobData.put("mobile", mobile);
            jobData.put("job", job);
            jobData.put("date", date);
            jobData.put("from", from);
            jobData.put("to", to);
            jobData.put("skills", skills);  // <-- Add selected skills

            db.collection("jobs")
                    .add(jobData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(pcreatejob.this, "Job Created Successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(pcreatejob.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}


