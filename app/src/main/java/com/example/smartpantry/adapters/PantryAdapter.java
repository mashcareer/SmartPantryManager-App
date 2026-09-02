package com.example.smartpantry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.models.PantryItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {
    public interface Listener { void onEdit(PantryItem item); void onDelete(PantryItem item); }
    private final Listener listener;
    private final List<PantryItem> items = new ArrayList<>();

    public PantryAdapter(Listener listener) { this.listener = listener; }
    public void setItems(List<PantryItem> newItems) { items.clear(); items.addAll(newItems); notifyDataSetChanged(); }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.getName());
        h.quantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            h.expiry.setVisibility(View.GONE);
        } else {
            h.expiry.setVisibility(View.VISIBLE);
            h.expiry.setText("Expiry: " + item.getExpiryDate());
        }
        h.edit.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    private String formatQuantity(double value) { return value == Math.rint(value) ? String.valueOf((long) value) : String.format(java.util.Locale.getDefault(), "%.2f", value); }
    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, expiry; MaterialButton edit, delete;
        ViewHolder(View v) { super(v); name=v.findViewById(R.id.itemName); quantity=v.findViewById(R.id.itemQuantity); expiry=v.findViewById(R.id.itemExpiry); edit=v.findViewById(R.id.editButton); delete=v.findViewById(R.id.deleteButton); }
    }
}
