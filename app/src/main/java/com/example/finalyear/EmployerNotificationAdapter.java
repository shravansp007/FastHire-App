package com.example.finalyear;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EmployerNotificationAdapter extends RecyclerView.Adapter<EmployerNotificationAdapter.VH> {

    private final Context ctx;
    private final List<EmployerNotificationModel> list;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String employerMobile; // used for dismiss path

    public EmployerNotificationAdapter(Context ctx, List<EmployerNotificationModel> list, String employerMobile) {
        this.ctx = ctx;
        this.list = list;
        this.employerMobile = employerMobile == null ? "" : employerMobile;
        setHasStableIds(true);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Button dismiss;
        View root;
        VH(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            title = itemView.findViewById(R.id.empNotifTitle);
            subtitle = itemView.findViewById(R.id.empNotifSubtitle);
            dismiss = itemView.findViewById(R.id.btnDismiss);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // IMPORTANT: file name is pnotifications_item.xml (plural)
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pnotification_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EmployerNotificationModel m = list.get(position);

        String line1 = (m.getWorkerName() == null || m.getWorkerName().isEmpty())
                ? m.getWorkerMobile()
                : m.getWorkerName() + " (" + m.getWorkerMobile() + ")";
        h.title.setText(line1);

        String statusText;
        switch (safe(m.getType()).toLowerCase()) {
            case "worker_interested":
                statusText = "is INTERESTED to work";
                break;
            case "accepted":
                statusText = "WORK COMPLETED — please rate";
                break;
            case "rejected":
                statusText = "Invite REJECTED";
                break;
            default:
                statusText = safe(m.getType()); // fallback
        }
        String meta = "";
        if (!safe(m.getDate()).isEmpty() || !safe(m.getFrom()).isEmpty() || !safe(m.getTo()).isEmpty()) {
            meta = "  •  Date: " + safe(m.getDate()) + "  From: " + safe(m.getFrom()) + "  To: " + safe(m.getTo());
        }
        h.subtitle.setText(statusText + meta);

        // Open pinvite:
        // - worker_interested -> invite screen (rateOnly=false) with optional prefill
        // - accepted/rejected -> rating screen (rateOnly=true)
        h.root.setOnClickListener(v -> {
            Intent i = new Intent(ctx, pinvite.class);
            i.putExtra("name", m.getWorkerName());
            i.putExtra("mobile", m.getWorkerMobile());

            if ("worker_interested".equalsIgnoreCase(safe(m.getType()))) {
                i.putExtra("rateOnly", false);
                i.putExtra("prefillDate", m.getDate());
                i.putExtra("prefillFrom", m.getFrom());
                i.putExtra("prefillTo", m.getTo());
            } else {
                i.putExtra("rateOnly", true);
                i.putExtra("inviteId", m.getInviteId());
            }
            ctx.startActivity(i);
        });

        // Dismiss
        h.dismiss.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (employerMobile.isEmpty()) {
                Toast.makeText(ctx, "Employer mobile missing", Toast.LENGTH_SHORT).show();
                return;
            }

            String docId = m.getDocId();
            db.collection("EmployerNotifications")
                    .document(employerMobile)
                    .collection("items")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        // Remove locally for instant UI feedback
                        int p = h.getAdapterPosition();
                        if (p != RecyclerView.NO_POSITION) {
                            list.remove(p);
                            notifyItemRemoved(p);
                        } else {
                            // fallback
                            notifyDataSetChanged();
                        }
                        Toast.makeText(ctx, "Dismissed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    @Override
    public long getItemId(int position) {
        // Stable id from docId hash to avoid flicker
        String id = list.get(position).getDocId();
        return id == null ? RecyclerView.NO_ID : id.hashCode();
    }

    private static String safe(String s) { return s == null ? "" : s; }
}







