package com.zebra.demo.rfidreader.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.demo.R;
import com.zebra.demo.scanner.activities.BatteryItemsAdapter;

import java.util.List;

public class BatteryStaticsAdapter extends RecyclerView.Adapter<BatteryStaticsAdapter.ViewHolder> {
    List<BatteryStatisticsData> batteryItemList;

    public BatteryStaticsAdapter(List<BatteryStatisticsData> batteryItemList) {
        this.batteryItemList = batteryItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.battery_items,parent,false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
              BatteryStatisticsData batteryStatisticsData = batteryItemList.get(position);
        String str = batteryStatisticsData.getBatteryHeader();
        List<String> batteryHeaderTitle = batteryStatisticsData.getBatteryItems();
        List<String> batteryItemsValue = batteryStatisticsData.getBatteryItemData();
        holder.tv_BatteryHeader.setText(str);

        BatteryItemsAdapter batteryItemsAdapter = new BatteryItemsAdapter(batteryHeaderTitle,batteryItemsValue);
        holder.rv_BatteryItems.setAdapter(batteryItemsAdapter);

    }

    @Override
    public int getItemCount() {
        return batteryItemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_BatteryHeader;
        RecyclerView rv_BatteryItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_BatteryHeader = itemView.findViewById(R.id.battery_header);
            rv_BatteryItems = itemView.findViewById(R.id.batteryitem_recyclerview);
        }
    }
}
