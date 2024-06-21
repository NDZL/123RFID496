package com.zebra.demo.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.demo.R;
import com.zebra.rfid.api3.WifiScanData;

import java.util.List;

public class AvailableWifiNetworkAdapter extends RecyclerView.Adapter<AvailableWifiNetworkAdapter.ViewHolder> {

    Context mContext;
    List<WifiScanData> results;

    public AvailableWifiNetworkAdapter(Context mContext, List<WifiScanData> results) {
        this.mContext = mContext;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.other_wifi_networks, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiScanData scanResult = results.get(position);

        int level = 5 ;
        holder.tv_connectedWifiName.setText(scanResult.getssid());
        holder.iv_connectedWifiIcon.setImageLevel(level);
        if ( scanResult.getkey().contains("WPA") )  {
            holder.iv_wifiLock.setImageResource(R.drawable.ic_lock);
        }

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_connectedWifiIcon, iv_wifiLock;
        TextView tv_connectedWifiName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_connectedWifiIcon = itemView.findViewById(R.id.connected_wifi_icon);
            tv_connectedWifiName = itemView.findViewById(R.id.connected_wifi_name);
            iv_wifiLock = itemView.findViewById(R.id.lock);
            itemView.setOnClickListener(v -> ReaderWifiSettingsFragment.getInstance().openDialog(results.get(getAbsoluteAdapterPosition())));
        }
    }

}
