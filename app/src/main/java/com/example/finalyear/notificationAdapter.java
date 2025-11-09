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

public class notificationAdapter extends RecyclerView.Adapter<notificationAdapter.ViewHolder> {

    private final Context context;
    private final List<notificationModel> notificationList;
    private final String workerMobile;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public notificationAdapter(Context context, List<notificationModel> notificationList, String workerMobile) {
        this.context = context;
        this.notificationList = notificationList;
        this.workerMobile = workerMobile;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notifyName, notifyMobile, notifyDate, notifyFrom, notifyTo;
        Button btnAccept, btnDeny;

        ViewHolder(View view) {
            super(view);
            // Ensure your row layout has these ids: notifyName, notifyMobile, notifyDate, notifyFrom, notifyTo, btnAccept, btnDeny
            notifyName   = view.findViewById(R.id.notifyName);
            notifyMobile = view.findViewById(R.id.notifyMobile);
            notifyDate   = view.findViewById(R.id.notifyDate);
            notifyFrom   = view.findViewById(R.id.notifyFrom);
            notifyTo     = view.findViewById(R.id.notifyTo);
            btnAccept    = view.findViewById(R.id.btnAccept);
            btnDeny      = view.findViewById(R.id.btnDeny);
        }
    }

    @NonNull
    @Override
    public notificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use your actual item layout resource here:
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cnotification_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull notificationAdapter.ViewHolder holder, int position) {
        notificationModel m = notificationList.get(position);

        holder.notifyName.setText(safe(m.getEmployerName()));
        holder.notifyMobile.setText(safe(m.getEmployerMobile()));
        holder.notifyDate.setText(safe(m.getDate()));
        holder.notifyFrom.setText(safe(m.getFromTime()));
        holder.notifyTo.setText(safe(m.getToTime()));

        boolean isPending = "pending".equalsIgnoreCase(safe(m.getStatus()));

        // Initial state
        styleButtons(holder, isPending, m);

        holder.btnAccept.setOnClickListener(v -> actOnce(holder, m, "accepted"));
        holder.btnDeny.setOnClickListener(v -> actOnce(holder, m, "rejected"));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private void styleButtons(@NonNull ViewHolder h, boolean pending, notificationModel m) {
        h.btnAccept.setEnabled(pending);
        h.btnDeny.setEnabled(pending);
        h.btnAccept.setAlpha(pending ? 1f : 0.4f);
        h.btnDeny.setAlpha(pending ? 1f : 0.4f);

        if (!pending) {
            if ("accepted".equalsIgnoreCase(m.getStatus())) {
                h.btnAccept.setText("Accepted");
                h.btnDeny.setText("Deny");
            } else if ("rejected".equalsIgnoreCase(m.getStatus())) {
                h.btnDeny.setText("Rejected");
                h.btnAccept.setText("Accept");
            }
        } else {
            h.btnAccept.setText("Accept");
            h.btnDeny.setText("Deny");
        }
    }

    private void actOnce(@NonNull ViewHolder holder, @NonNull notificationModel model, @NonNull String newStatus) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        // Optimistic UI disable
        holder.btnAccept.setEnabled(false);
        holder.btnDeny.setEnabled(false);
        holder.btnAccept.setAlpha(0.4f);
        holder.btnDeny.setAlpha(0.4f);

        String docId = model.getDocId();
        DocumentReference inviteRef = db.collection("Invites")
                .document(workerMobile)
                .collection("requests")
                .document(docId);

        // Transaction: only update if still pending
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snap = transaction.get(inviteRef);
            String cur = snap.getString("status");
            if (cur != null && !"pending".equalsIgnoreCase(cur)) {
                return null; // already handled
            }
            transaction.update(inviteRef,
                    "status", newStatus,
                    "updatedAt", FieldValue.serverTimestamp());
            return null;
        }).addOnSuccessListener(unused -> {
            // Update local + refresh item
            model.setStatus(newStatus);
            notifyItemChanged(holder.getAdapterPosition());

            // Notify employer
            inviteRef.get().addOnSuccessListener(snapshot -> {
                String employerMobile = safe(snapshot.getString("employerMobile"));
                String workerName     = safe(snapshot.getString("workerName"));
                String workerMobDoc   = safe(snapshot.getString("workerMobile"));
                String date           = safe(snapshot.getString("date"));
                String from           = safe(snapshot.getString("from"));
                String to             = safe(snapshot.getString("to"));

                if (!employerMobile.isEmpty()) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("type", newStatus);                 // "accepted" | "rejected"
                    notif.put("workerName", workerName);
                    notif.put("workerMobile", workerMobDoc);
                    notif.put("inviteId", docId);
                    notif.put("date", date);
                    notif.put("from", from);
                    notif.put("to", to);
                    notif.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("EmployerNotifications")
                            .document(employerMobile)
                            .collection("items")
                            .add(notif)
                            .addOnSuccessListener(r ->
                                    Toast.makeText(context, "Invite " + newStatus, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Status set, but failed to notify employer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(context, "Invite " + newStatus, Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Already handled or error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private static String safe(String s) { return s == null ? "" : s; }
}






