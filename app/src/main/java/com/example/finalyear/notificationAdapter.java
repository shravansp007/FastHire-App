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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class notificationAdapter extends RecyclerView.Adapter<notificationAdapter.ViewHolder> {

    private Context context;
    private List<notificationModel> notificationList;
    private FirebaseFirestore db;
    private String workerMobile; // worker who receives invites

    public notificationAdapter(Context context, List<notificationModel> notificationList, String workerMobile) {
        this.context = context;
        this.notificationList = notificationList;
        this.workerMobile = workerMobile;
        this.db = FirebaseFirestore.getInstance();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvTime;
        Button btnAccept, btnDeny;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.notifyName);
            tvDate = view.findViewById(R.id.notifyDate);
            tvTime = view.findViewById(R.id.notifyTime);
            btnAccept = view.findViewById(R.id.btnAccept);
            btnDeny = view.findViewById(R.id.btnDeny);
        }
    }

    @NonNull
    @Override
    public notificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cnotification_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull notificationAdapter.ViewHolder holder, int position) {
        notificationModel model = notificationList.get(position);
        // show employer name + mobile
        holder.tvName.setText("From: " + model.getEmployerName() + " (" + model.getEmployerMobile() + ")");
        holder.tvDate.setText("Date: " + model.getDate());
        holder.tvTime.setText("Time: " + model.getFromTime() + " - " + model.getToTime());

        holder.btnAccept.setOnClickListener(v -> {
            updateStatusAndRemove(model.getDocId(), "accepted", holder.getAdapterPosition());
        });

        holder.btnDeny.setOnClickListener(v -> {
            updateStatusAndRemove(model.getDocId(), "denied", holder.getAdapterPosition());
        });
    }

    private void updateStatusAndRemove(String docId, String status, int adapterPosition) {
        if (docId == null || docId.isEmpty()) {
            Toast.makeText(context, "Invalid document id", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Invites")
                .document(workerMobile)
                .collection("requests")
                .document(docId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    // remove from list and notify adapter
                    if (adapterPosition >= 0 && adapterPosition < notificationList.size()) {
                        notificationList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }
                    Toast.makeText(context, "Invite " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}


