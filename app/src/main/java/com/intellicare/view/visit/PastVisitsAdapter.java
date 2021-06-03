package com.intellicare.view.visit;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.intellicare.databinding.ItemVisitBinding;
import com.intellicare.model.pastvisits.Consult;

import java.util.List;

public class PastVisitsAdapter extends RecyclerView.Adapter<PastVisitsAdapter.ViewHolder> {
    private final List<Consult> visitDetailsList;
    private ItemVisitBinding binding;
    private Context context;
    private int lastClickedPosition = -1;

    public PastVisitsAdapter(List<Consult> visitDetailsList) {
        this.visitDetailsList = visitDetailsList;
    }

    public void addVisits(List<Consult> newVisits) {
        this.visitDetailsList.addAll(newVisits);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PastVisitsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        binding = ItemVisitBinding.inflate(LayoutInflater.from(context), null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        binding.getRoot().setLayoutParams(lp);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PastVisitsAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Consult consult = visitDetailsList.get(position);
        holder.bind(consult);

        holder.itemView.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current state of the item
                boolean expanded = consult.isExpanded();
                // Change the state
                consult.setExpanded(!expanded);
                // Notify the adapter that item has changed
                notifyItemChanged(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return visitDetailsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemVisitBinding itemView;

        public ViewHolder(ItemVisitBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
        }

        // Method in ViewHolder class
        private void bind(Consult consult) {
            // Get the state
            boolean expanded = consult.isExpanded();
            // Set the visibility based on state

            itemView.notes.setVisibility(expanded ? View.VISIBLE : View.GONE);
            itemView.dateTime.setVisibility(expanded ? View.VISIBLE : View.GONE);

            itemView.tvComplaint.setText(consult.getComplaint());
            itemView.dateTime.setText(consult.getCreatedAt());
            itemView.doctor.setText(consult.getClinicianName());
            itemView.notes.setText(consult.getNotes());
            String notes = consult.getNotes();
            if (TextUtils.isEmpty(notes)) {
                itemView.notes.setVisibility(View.GONE);
            } else {
                itemView.notes.setText(notes);
            }
        }
    }
}