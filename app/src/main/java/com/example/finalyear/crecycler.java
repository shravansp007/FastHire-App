package com.example.finalyear;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class crecycler extends RecyclerView.Adapter<crecycler.ViewHolder> {

    private List<cinfo> customerList;

    public crecycler(List<cinfo> cinfoList) {
        this.customerList = cinfoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMobile, tvRating;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.textView21);
            tvMobile = view.findViewById(R.id.textView22);
            tvRating = view.findViewById(R.id.textView25);
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
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }
}
