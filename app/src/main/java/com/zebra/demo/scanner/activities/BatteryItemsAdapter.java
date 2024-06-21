package com.zebra.demo.scanner.activities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.demo.R;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import java.util.List;

public class BatteryItemsAdapter extends RecyclerView.Adapter<BatteryItemsAdapter.ViewHolder> {
    List<String> batteryItems;
    List<String> batteryItemsValue;

    public BatteryItemsAdapter(List<String> batteryItems, List<String> batteryItemsValue) {
        this.batteryItems = batteryItems;
        this.batteryItemsValue = batteryItemsValue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_bttery_items,parent,false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_BatteryItemName.setText(batteryItems.get(position));
        holder.tv_BatteryItemValue.setText(batteryItemsValue.get(position));
    }

    @Override
    public int getItemCount() {
        return batteryItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_BatteryItemName , tv_BatteryItemValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_BatteryItemName = itemView.findViewById(R.id.battery_item_name);
            tv_BatteryItemValue = itemView.findViewById(R.id.battery_item_value);
        }
    }
}
