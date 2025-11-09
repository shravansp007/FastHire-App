package com.example.finalyear;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class pcreatejob extends AppCompatActivity {

    // Inputs (match your XML ids)
    EditText nameEdit, mobileEdit, jobEdit, dateEdit, fromTimeEdit, toTimeEdit;
    Button createBtn;
    CheckBox carpentryCheck, constructionCheck, plumbingCheck, heavyCheck;

    FirebaseFirestore db;

    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pcreatejob);

        // Bind views
        nameEdit     = findViewById(R.id.editTextText18);
        mobileEdit   = findViewById(R.id.editTextText19);
        jobEdit      = findViewById(R.id.editTextText20);
        dateEdit     = findViewById(R.id.editTextText21);
        fromTimeEdit = findViewById(R.id.editTextText23);
        toTimeEdit   = findViewById(R.id.editTextText24);
        createBtn    = findViewById(R.id.button12);

        carpentryCheck    = findViewById(R.id.checkBox9);
        constructionCheck = findViewById(R.id.checkBox10);
        plumbingCheck     = findViewById(R.id.checkBox12);
        heavyCheck        = findViewById(R.id.checkBox11);

        db = FirebaseFirestore.getInstance();

        // Date picker
        dateEdit.setFocusable(false);
        dateEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    pcreatejob.this,
                    (view, y, m, d) -> dateEdit.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // From time picker
        fromTimeEdit.setFocusable(false);
        fromTimeEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(
                    pcreatejob.this,
                    (view, h, min) -> fromTimeEdit.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        // To time picker
        toTimeEdit.setFocusable(false);
        toTimeEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(
                    pcreatejob.this,
                    (view, h, min) -> toTimeEdit.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        // Create Job
        createBtn.setOnClickListener(v -> {
            if (sending) return;
            sending = true;
            createBtn.setEnabled(false);

            String employerName   = safe(nameEdit.getText()).trim();
            String employerMobile = safe(mobileEdit.getText()).trim();
            String title          = safe(jobEdit.getText()).trim();
            String date           = safe(dateEdit.getText()).trim();      // "d/M/yyyy" per your picker
            String from           = safe(fromTimeEdit.getText()).trim();  // "HH:mm"
            String to             = safe(toTimeEdit.getText()).trim();    // "HH:mm"

            if (employerName.isEmpty() || employerMobile.isEmpty() || title.isEmpty() ||
                    date.isEmpty() || from.isEmpty() || to.isEmpty()) {
                toast("Please fill all fields");
                resetSendState();
                return;
            }

            if (!isTimeRangeValid(from, to)) {
                toast("Invalid time range");
                resetSendState();
                return;
            }

            // Collect selected skills (UI labels)
            List<String> skills = new ArrayList<>();
            if (carpentryCheck.isChecked())    skills.add("Carpentry");
            if (constructionCheck.isChecked()) skills.add("Construction");
            if (plumbingCheck.isChecked())     skills.add("Plumbing");
            if (heavyCheck.isChecked())        skills.add("Heavy");
            if (skills.isEmpty()) {
                toast("Select at least one skill");
                resetSendState();
                return;
            }

            // Build job doc (keep your original keys; also save employerName/employerMobile for consistency)
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("name", employerName);
            jobData.put("mobile", employerMobile);
            jobData.put("job", title);
            jobData.put("date", date);
            jobData.put("from", from);
            jobData.put("to", to);
            jobData.put("skills", skills);
            jobData.put("employerName", employerName);
            jobData.put("employerMobile", employerMobile);
            jobData.put("status", "open");
            jobData.put("createdAt", FieldValue.serverTimestamp());

            db.collection("jobs")
                    .add(jobData)
                    .addOnSuccessListener(ref -> {
                        // Broadcast to workers by selected skills
                        broadcastJobToWorkers(ref.getId(), employerName, employerMobile, title, date, from, to, skills);
                        toast("Job Created Successfully");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        toast("Error: " + e.getMessage());
                        resetSendState();
                    });
        });
    }

    private void resetSendState() {
        sending = false;
        createBtn.setEnabled(true);
    }

    private static String safe(CharSequence cs) {
        return cs == null ? "" : cs.toString();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }

    // expects "HH:mm"
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

    // === Broadcast the job to all workers whose Users doc matches ANY selected skill ===
    private void broadcastJobToWorkers(String jobId,
                                       String employerName,
                                       String employerMobile,
                                       String title,
                                       String date,
                                       String from,
                                       String to,
                                       List<String> uiSkillNames) {

        // Map UI labels -> Users boolean field names
        List<String> userFields = new ArrayList<>();
        for (String s : uiSkillNames) {
            String f = mapUiSkillToUserField(s);
            if (f != null) userFields.add(f);
        }
        if (userFields.isEmpty()) { resetSendState(); return; }

        // Collect deduped worker mobiles across all skill queries
        final Set<String> dedupMobiles = new HashSet<>();

        // Query each field sequentially to avoid complex OR queries
        fetchWorkersByFieldsSequential(userFields, 0, dedupMobiles, () -> {
            if (dedupMobiles.isEmpty()) { resetSendState(); return; }

            WriteBatch batch = db.batch();
            for (String workerMob : dedupMobiles) {
                // Optional: don't notify the employer even if they have worker skills set
                if (employerMobile.equals(workerMob)) continue;

                DocumentReference feedDoc = db.collection("JobFeeds")
                        .document(workerMob)
                        .collection("items")
                        .document(jobId); // idempotent: same jobId

                Map<String, Object> feed = new HashMap<>();
                feed.put("jobId", jobId);
                feed.put("title", title);
                feed.put("employerName", employerName);
                feed.put("employerMobile", employerMobile);
                feed.put("date", date);
                feed.put("from", from);
                feed.put("to", to);
                feed.put("interest", "pending");            // for Interested / Not interested buttons
                feed.put("createdAt", FieldValue.serverTimestamp());
                batch.set(feedDoc, feed);
            }
            batch.commit()
                    .addOnCompleteListener(task -> resetSendState());
        });
    }

    // Recursively query Users by each boolean field and collect mobiles
    private void fetchWorkersByFieldsSequential(List<String> fields,
                                                int idx,
                                                Set<String> dedupMobiles,
                                                Runnable onDone) {
        if (idx >= fields.size()) { onDone.run(); return; }
        String field = fields.get(idx);

        db.collection("Users")
                .whereEqualTo(field, true)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String mob = doc.getString("mobile");
                        if (mob != null && !mob.trim().isEmpty()) {
                            dedupMobiles.add(mob.trim());
                        }
                    }
                    fetchWorkersByFieldsSequential(fields, idx + 1, dedupMobiles, onDone);
                })
                .addOnFailureListener(e -> {
                    // Skip this field on failure, continue
                    fetchWorkersByFieldsSequential(fields, idx + 1, dedupMobiles, onDone);
                });
    }

    // Your Users schema booleans (adjust if yours differ)
    // checkbox1: Construction, checkbox2: Plumbing, checkbox3: Electrical, checkbox4: Heavy works, checkbox5: Carpentry
    private String mapUiSkillToUserField(String ui) {
        String k = ui.toLowerCase(Locale.US).trim();
        switch (k) {
            case "construction": return "checkbox1";
            case "plumbing":     return "checkbox2";
            // "electrical" not present in this screen, but documented for completeness:
            // case "electrical":   return "checkbox3";
            case "heavy":        // your checkbox text is "Heavy"
            case "heavy works":  return "checkbox4";
            case "carpentry":    return "checkbox5"; // if you don't have checkbox5 yet, this will simply match none
            default: return null;
        }
    }
}



