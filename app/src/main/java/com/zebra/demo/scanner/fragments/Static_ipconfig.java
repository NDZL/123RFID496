package com.zebra.demo.scanner.fragments;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomToast;
import com.zebra.demo.rfidreader.notifications.NotificationUtil;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.Network_IPConfig;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;

import static com.zebra.rfid.api3.RFIDResults.RFID_API_SUCCESS;

public class Static_ipconfig extends Fragment {

    View rootview=null;
    LinearLayout StaticLayout, DHCPLayout , NonpremiumLayout , IpsettingLayout;
    Spinner ipSettingsspin;
    Network_IPConfig network_ipConfig;
    Button applyIpconfig;
    EditText staticIp, staticNetmask, staticGateway, staticDNS;
    private static final String TAG = "AssertFragment";
    String[] IpsettingType = {"DHCP","Static"};
    public static Static_ipconfig newInstance() {

        return new Static_ipconfig();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootview = inflater.inflate(R.layout.content_network_ipconfig,container, false);
        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp< Application.minScreenWidth){
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(configuration.screenWidthDp<Application.minScreenWidth){
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        network_ipConfig = new Network_IPConfig();
        StaticLayout = rootview.findViewById(R.id.layoutStatic);
        DHCPLayout = rootview.findViewById(R.id.layoutDHCP);
        NonpremiumLayout = rootview.findViewById(R.id.layout_nonpremium);
        IpsettingLayout = rootview.findViewById(R.id.layoutIpsettings);
        applyIpconfig = rootview.findViewById(R.id.applyIpconfig);
        ipSettingsspin =  rootview.findViewById(R.id.ipsetting);
        String model = getDeviceModelName(RFIDController.mConnectedDevice.getName());
        if(!model.equals("PREMIUM (WiFi)") && !model.equals("PREMIUM (WiFi & SCAN)")){
            NonpremiumLayout.setVisibility(View.VISIBLE);

            StaticLayout.setVisibility(View.GONE);
            DHCPLayout.setVisibility(View.GONE);
            IpsettingLayout.setVisibility(View.GONE);
        }
        ipSettingsspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if(item.equals("DHCP")){
                    DHCPLayout.setVisibility(View.VISIBLE);
                    StaticLayout.setVisibility(View.GONE);
                    showDHCPIP();
                }
                if(item.equals("Static")){
                    StaticLayout.setVisibility(View.VISIBLE);
                    DHCPLayout.setVisibility(View.GONE);
                    showStaticIp();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter aa = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_dropdown_item,IpsettingType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        ipSettingsspin.setAdapter(aa);
        staticIp = rootview.findViewById(R.id.editipAddress);
        staticDNS = rootview.findViewById(R.id.editDNS);
        staticGateway = rootview.findViewById(R.id.editgateway);
        staticNetmask = rootview.findViewById(R.id.editnetmask);

        ipSettingsspinSelecion();

        applyIpconfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStaticIp();
            }
        });

        return rootview;

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void showStaticIp() {
        try {
            if(RFIDController.mConnectedReader.Config.Nw_getNetworkStatus(network_ipConfig)== RFID_API_SUCCESS){
                  if(  RFIDController.mConnectedReader.Config.Nw_getDhcpStatus() == false){
                        Log.d("StaticIp","Ip = "+network_ipConfig.getipaddress());
                        Log.d("StaticIp","Netmask = "+network_ipConfig.getnetmask());
                        Log.d("StaticIp","Dns = "+network_ipConfig.getdns());
                        Log.d("StaticIp","gateway = "+network_ipConfig.getgateway());
                        staticIp.setText(network_ipConfig.getipaddress());
                        staticNetmask.setText(network_ipConfig.getnetmask());
                        staticGateway.setText(network_ipConfig.getgateway());
                        staticDNS.setText(network_ipConfig.getdns());

                    }
                  else {
                      staticIp.getText().clear();
                      staticGateway.getText().clear();
                      staticNetmask.getText().clear();
                      staticDNS.getText().clear();
                  }
        }
        } catch (InvalidUsageException e) {
            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
    }

    private void showDHCPIP()
    {
        if(RFIDController.mConnectedReader != null) {
            try {
                Log.d("StaticIp","DHCP status = "+RFIDController.mConnectedReader.Config.Nw_getDhcpStatus());

                if(RFIDController.mConnectedReader.Config.Nw_getDhcpStatus() == false){
                    RFIDController.mConnectedReader.Config.Nw_setDhcpEnable();
                    Log.d("StaticIp","Enabling DHCP "+RFIDController.mConnectedReader.Config.Nw_getDhcpStatus());
                }

                Log.d("StaticIp","Network status"+RFIDController.mConnectedReader.Config.Nw_getNetworkStatus(network_ipConfig));
                Log.d("StaticIp","Ip="+network_ipConfig.getipaddress());
                Log.d("StaticIp","Netmask="+network_ipConfig.getnetmask());
                Log.d("StaticIp","Dns="+network_ipConfig.getdns());
                Log.d("StaticIp","gateway="+network_ipConfig.getgateway());


                ((TextView) (getActivity()).findViewById(R.id.ipAddress)).setText(network_ipConfig.getipaddress());
                ((TextView) (getActivity()).findViewById(R.id.netmask)).setText(network_ipConfig.getnetmask());
                ((TextView) (getActivity()).findViewById(R.id.gateway)).setText(network_ipConfig.getgateway());
                ((TextView) (getActivity()).findViewById(R.id.DNS)).setText(network_ipConfig.getdns());

            } catch (Exception e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }

        }


    }

    public void setStaticIp(){
        String ip, netmask, gateway, dns;
        ip = staticIp.getText().toString();
        netmask =staticNetmask.getText().toString();
        gateway = staticGateway.getText().toString();
        dns = staticDNS.getText().toString();

        network_ipConfig.setipaddress(ip);
        network_ipConfig.setnetmask(netmask);
        network_ipConfig.setgateway(gateway);
        network_ipConfig.setdns(dns);
        try {
            RFIDController.mConnectedReader.Config.Nw_setStaticIP(network_ipConfig);
            if(RFIDController.mConnectedReader.Config.Nw_setStaticIP(network_ipConfig) == RFID_API_SUCCESS)
                Toast.makeText(getActivity(),"Applied Ip configuration successfully",Toast.LENGTH_SHORT).show();

        } catch (InvalidUsageException e) {
            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        } catch (OperationFailureException e) {
            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        }

        showStaticIp();
     /*   try {
            Log.d("StaticIp","Network status = "+RFIDController.mConnectedReader.Config.Nw_getNetworkStatus(network_ipConfig));
        } catch (InvalidUsageException e) {
            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        } catch (OperationFailureException e) {
            sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
        }*/

        Log.d("StaticIp","Ip = "+network_ipConfig.getipaddress());
        Log.d("StaticIp","Netmask = "+network_ipConfig.getnetmask());
        Log.d("StaticIp","Dns = "+network_ipConfig.getdns());
        Log.d("StaticIp","gateway = "+network_ipConfig.getgateway());

    }


    private void ipSettingsspinSelecion() {
        try {
            if(RFIDController.mConnectedReader.Config.Nw_getDhcpStatus() == false){
                ipSettingsspin.setSelection(1);
             //   showStaticIp();
            }
            else {
                ipSettingsspin.setSelection(0);
                showDHCPIP();
            }
        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
    }

    public void sendNotification(String action, String data) {
     {
            /*Intent i = new Intent(this, NotificationsService.class);
            i.putExtra(Constants.INTENT_ACTION, action);
            i.putExtra(Constants.INTENT_DATA, data);
            startService(i);*/

            if(getActivity() != null)
                NotificationUtil.displayNotificationforSettingsDeialActivity(getActivity(), action, data);
        }
    }


    public String getDeviceModelName(String modelName) {
        if (modelName.startsWith("MC33")) {
            return "MC330XR";
        } else if (modelName.startsWith("RFD40+")) {
            return "PREMIUM (WiFi & SCAN)";
        } else if (modelName.startsWith("RFD90")) {
            return "PREMIUM (WiFi & SCAN)";
        } else if (modelName.startsWith("RFD90+")) {
            return "PREMIUM (WiFi & SCAN)";
        } else if (modelName.startsWith("RFD40P")) {
            return "PREMIUM (WiFi)";
        } else if (modelName.startsWith("RFD40")) {
            String[] splitStr = modelName.split("-");

            if (splitStr[0].equals("RFD4030")) {
                if (splitStr[1].contains("G0")) {
                    return "STANDARD";
                }

            } else if (splitStr[0].equals("RFD4031")) {
                if (splitStr[1].contains("G0")) {
                    return "PREMIUM (WiFi)";
                } else if (splitStr[1].contains("G1")) {
                    return "PREMIUM (WiFi & SCAN)";
                }

            }
        } else if (modelName.startsWith("RFD8500")) {
            return "RFD8500";
        }


        return "RFDXXXX";
    }


    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {

        if (RFIDController.NOTIFY_READER_AVAILABLE)
            sendNotification(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_AVAILABLE, readerDevice.getName() + " is unavailable.");
        // RFIDController.mReaderDisappeared = readerDevice;
        getActivity().onBackPressed();

    }
}
