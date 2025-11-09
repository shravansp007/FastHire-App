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

    private final List<cinfo> customerList;
    private final Context context;

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
        cinfo item = customerList.get(position);
        holder.tvName.setText("Name: " + item.getName());
        holder.tvMobile.setText("Mobile: " + item.getMobile());
        holder.tvRating.setText("Rating: " + item.getRating());

        // Open pinvite in normal mode from search (rateOnly = false)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, pinvite.class);
            intent.putExtra("name", item.getName());
            intent.putExtra("mobile", item.getMobile());
            intent.putExtra("rating", item.getRating());  // ⭐ pass actual rating (0 if none yet)
            intent.putExtra("rateOnly", false);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }
}








