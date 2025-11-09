package com.example.finalyear;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobFeedAdapter extends RecyclerView.Adapter<JobFeedAdapter.VH> {

    private final Context ctx;
    private final List<JobFeedModel> list;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String workerMobile; // used for writes + name lookup

    public JobFeedAdapter(Context ctx, List<JobFeedModel> list, String workerMobile) {
        this.ctx = ctx;
        this.list = list;
        this.workerMobile = workerMobile;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, mobile, date, from, to;
        Button btnInterested, btnNotInterested;
        VH(@NonNull View itemView) {
            super(itemView);
            // Ensure your row has these ids
            name   = itemView.findViewById(R.id.feedEmployerName);
            mobile = itemView.findViewById(R.id.feedEmployerMobile);
            date   = itemView.findViewById(R.id.feedDate);
            from   = itemView.findViewById(R.id.feedFrom);
            to     = itemView.findViewById(R.id.feedTo);
            btnInterested    = itemView.findViewById(R.id.btnInterested);
            btnNotInterested = itemView.findViewById(R.id.btnNotInterested);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_feed_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        JobFeedModel m = list.get(position);
        h.name.setText(m.getEmployerName());
        h.mobile.setText(m.getEmployerMobile());
        h.date.setText(m.getDate());
        h.from.setText(m.getFrom());
        h.to.setText(m.getTo());

        boolean pending = "pending".equalsIgnoreCase(safe(m.getInterest()));
        styleButtons(h, pending, m);

        h.btnInterested.setOnClickListener(v -> setInterestOnce(h, m, "interested"));
        h.btnNotInterested.setOnClickListener(v -> setInterestOnce(h, m, "not_interested"));
    }

    @Override
    public int getItemCount() { return list.size(); }

    private void styleButtons(@NonNull VH h, boolean pending, JobFeedModel m) {
        h.btnInterested.setEnabled(pending);
        h.btnNotInterested.setEnabled(pending);
        h.btnInterested.setAlpha(pending ? 1f : 0.4f);
        h.btnNotInterested.setAlpha(pending ? 1f : 0.4f);

        if (!pending) {
            if ("interested".equalsIgnoreCase(m.getInterest())) {
                h.btnInterested.setText("Interested");
            } else if ("not_interested".equalsIgnoreCase(m.getInterest())) {
                h.btnNotInterested.setText("Not interested");
            }
        } else {
            h.btnInterested.setText("Interested");
            h.btnNotInterested.setText("Not interested");
        }
    }

    private void setInterestOnce(@NonNull VH h, @NonNull JobFeedModel m, @NonNull String newValue) {
        int pos = h.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        // Optimistic UI
        h.btnInterested.setEnabled(false);
        h.btnNotInterested.setEnabled(false);
        h.btnInterested.setAlpha(0.4f);
        h.btnNotInterested.setAlpha(0.4f);

        DocumentReference feedRef = db.collection("JobFeeds")
                .document(workerMobile)
                .collection("items")
                .document(m.getJobId());

        // Transaction: only from pending
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snap = transaction.get(feedRef);
            String cur = snap.getString("interest");
            if (cur != null && !"pending".equalsIgnoreCase(cur)) {
                return null; // already chosen
            }
            transaction.update(feedRef,
                    "interest", newValue,
                    "updatedAt", FieldValue.serverTimestamp());
            return null;
        }).addOnSuccessListener(unused -> {
            m.setInterest(newValue);
            notifyItemChanged(h.getAdapterPosition());

            if ("interested".equals(newValue)) {
                sendInterestedNotificationToEmployer(m);
            } else {
                Toast.makeText(ctx, "Marked as Not interested", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ctx, "Already handled or error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void sendInterestedNotificationToEmployer(@NonNull JobFeedModel m) {
        // Get worker name (fallback to mobile)
        db.collection("Users")
                .whereEqualTo("mobile", workerMobile)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    String workerName = workerMobile;
                    if (!snap.isEmpty()) {
                        String n = snap.getDocuments().get(0).getString("name");
                        if (n != null && !n.trim().isEmpty()) workerName = n.trim();
                    }

                    Map<String, Object> notif = new HashMap<>();
                    notif.put("type", "worker_interested");
                    notif.put("workerName", workerName);
                    notif.put("workerMobile", workerMobile);
                    notif.put("jobId", m.getJobId());
                    notif.put("date", m.getDate());
                    notif.put("from", m.getFrom());
                    notif.put("to", m.getTo());
                    notif.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("EmployerNotifications")
                            .document(m.getEmployerMobile())
                            .collection("items")
                            .add(notif)
                            .addOnSuccessListener(r ->
                                    Toast.makeText(ctx, "Marked Interested — employer notified", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(ctx, "Interest saved, but failed to notify employer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Interest saved, but failed to lookup name: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static String safe(String s) { return s == null ? "" : s; }
}



