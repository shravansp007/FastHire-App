package com.example.finalyear;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class crecycler extends RecyclerView.Adapter<crecycler.ViewHolder> {

    private List<cinfo> customerList;
    private Context context;

    public crecycler(Context context, List<cinfo> cinfoList) {
        this.context = context;
        this.customerList = cinfoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMobile, tvRating;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.nameText);
            tvMobile = view.findViewById(R.id.mobileText);
            tvRating = view.findViewById(R.id.ratingText);
        }
    }

    @NonNull
    @Override
    public crecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.citem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        cinfo cinfo = customerList.get(position);
        holder.tvName.setText("Name: " + cinfo.getName());
        holder.tvMobile.setText("Mobile: " + cinfo.getMobile());
        holder.tvRating.setText("Rating: " + cinfo.getRating());

        // ✅ Handle click → open pinvite.java
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, pinvite.class);
            intent.putExtra("name", cinfo.getName());
            intent.putExtra("mobile", cinfo.getMobile());
            intent.putExtra("rating", cinfo.getRating());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }
}







