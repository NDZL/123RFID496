package com.zebra.demo.rfidreader.reader_connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.common.Inventorytimer;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.settings.AdvancedOptionsContent;
import com.zebra.demo.rfidreader.settings.SettingsDetailActivity;
import com.zebra.demo.scanner.helpers.AvailableScanner;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.DCSSDKDefs;


import java.util.ArrayList;
import java.util.HashSet;


import static android.os.AsyncTask.Status.FINISHED;
import static com.zebra.demo.application.Application.RFD_DEVICE_MODE;
import static com.zebra.demo.rfidreader.rfid.RFIDController.AUTO_RECONNECT_READERS;
import static com.zebra.demo.rfidreader.rfid.RFIDController.LAST_CONNECTED_READER;
import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedDevice;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.readersList;
import static com.zebra.demo.rfidreader.settings.AdvancedOptionsContent.DPO_ITEM_INDEX;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.DEVICE_PAIR_TAB;



/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link RFIDReadersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to maintain the list of readers
 */
public class RFIDReadersListFragment extends Fragment implements IRFIDConnectTaskHandlers, IScanConnectHandlers, PairedReaderListAdapter.ListItemClickListener {
    private PasswordDialog passwordDialog;
    private DeviceConnectTask deviceConnectTask;
    private ReaderListAdapter readerListAdapter;
    private ListView pairedListView;
    private TextView tv_emptyView, reader_description ,tv_emptyPairedReader,tv_serialNo;
    LinearLayout ll_pairedreader;
    private CustomProgressDialog progressDialog;
    private static ActiveDeviceActivity activity = null;
    private static RFIDReadersListFragment rlf = null;
    private ScanAndPairFragment scanAndPairFragment;
    private EditText scanCode;
    private boolean isOnStopCalled = false;
    private ScanConnectTask scanConnectTask;
    private AvailableScanner curAvailableScanner;
    private ExtendedFloatingActionButton fabPairReader;
    public IRFIDConnectTaskHandlers handlers;
    private boolean mConnectioninProgress = false;
    RelativeLayout rl_myLayout;
    private LinearLayout linearLayout;
    private ImageView batteryLevelImage;
    private PairedReaderListAdapter pairedReaderListAdapter;
    private RecyclerView rv_pairedReader;
    TextView updateConnectReader , tv_model;
    ImageView connectedImgView, iv_pairedreader_icon ;
    private ScanPair scanPair;
    private BluetoothHandler btConnection = null;
    private boolean mConnectionProgress = false;
    TextView serialno;
    private EditText edit_Readername;
    private String newName, FriendlyName;

    @Override
    public void CancelReconnect() {
        if (RFIDBaseActivity.DisconnectTask != null && AUTO_RECONNECT_READERS) {
            int timeout = 20;
            while (FINISHED != RFIDBaseActivity.DisconnectTask.getStatus() && timeout > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
                timeout--;
            }
        }
    }

    @Override
    public void setConnectionProgressState(boolean prgressState) {
        mConnectioninProgress = prgressState;
    }

    public RFIDReadersListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadersListFragment.
     */
    public static RFIDReadersListFragment newInstance() {
        return new RFIDReadersListFragment();
    }

    public static RFIDReadersListFragment getInstance() {
        if (rlf == null)
            rlf = new RFIDReadersListFragment();
        return rlf;
    }

    private void clearSettings() {
        RFIDController.clearSettings();
        RFIDController.clearAllInventoryData();
        RFIDController.stopTimer();
        getActivity().invalidateOptionsMenu();
        Inventorytimer.getInstance().stopTimer();
        RFIDController.mIsInventoryRunning = false;
        if (RFIDController.mIsInventoryRunning) {
            RFIDController.isBatchModeInventoryRunning = false;
        }
        if (RFIDController.isLocatingTag) {
            RFIDController.isLocatingTag = false;
        }
        //update dpo icon in settings list
        AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
        RFIDController.mConnectedDevice = null;
        RFIDController.isAccessCriteriaRead = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ActiveDeviceActivity) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handlers = this;
        activity = (ActiveDeviceActivity) getActivity();
        // registerReceivers();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_readers_list, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeViews();
        //readersList.clear();
       pairedReaderListAdapter = new PairedReaderListAdapter(getActivity(),R.layout.paired_reader_list,RFIDController.readersList, this);

        rv_pairedReader.setVisibility(View.VISIBLE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_pairedReader.setLayoutManager(linearLayoutManager);
        rv_pairedReader.setAdapter(pairedReaderListAdapter);


        refreshDeviceList(null, false);
        fabPairReader = (ExtendedFloatingActionButton) getActivity().findViewById(R.id.fab_pair_reader);
        pairedReaderListAdapter.notifyDataSetChanged();
        fabPairReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                  //  BluetoothAdapter.getDefaultAdapter().enable();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    ((ActiveDeviceActivity) getActivity()).startActivityForResult(enableBtIntent, 1101);
                }
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(DEVICE_PAIR_TAB);

            }
        });

        batteryLevelImage = getActivity().findViewById(R.id.appbar_batteryLevelImage);
        if (RFIDController.BatteryData != null){
            deviceStatusReceived(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
        }

    }

    private void refreshDeviceList(Object o, boolean b) {
        if (mConnectedDevice != null && mConnectedReader != null ) {
            updateConnectReader.setText(mConnectedReader.getHostName());
            String model = mConnectedDevice.getDeviceCapability(mConnectedReader.getHostName());
            setImage(model);
            tv_model.setText(String.format(getActivity().getResources().getString(R.string.readermodel),model));

            if(readersList.contains(mConnectedDevice)) {
                int index = readersList.indexOf(mConnectedDevice);
                readersList.remove(index);
                updateConnectReader.setText(mConnectedReader.getHostName());

            }
            if(pairedReaderListAdapter.getItemCount() == 0) {
                tv_emptyPairedReader.setVisibility(View.VISIBLE);
                tv_serialNo.setVisibility(View.GONE);
            } else {
                tv_emptyPairedReader.setVisibility(View.GONE);
                tv_serialNo.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setImage(String model) {
        if(model.equals("STANDARD") || model.equals("RFD8500") || model.startsWith("MC33")) {
            iv_pairedreader_icon.setImageResource(R.drawable.ic_standard);
        } else if(model.equals("PREMIUM (WiFi)")) {
            iv_pairedreader_icon.setImageResource(R.drawable.ic_premium);
        } else {
            iv_pairedreader_icon.setImageResource(R.drawable.ic_premium_plus);
        }
    }

    public void loadUIData() {
        //readersList.clear();
        refreshPairedDevices();
        refreshDeviceList(null, false);
    }

    private void refreshPairedDevices() {


            Log.d(TAG, "loadAvailableReaders");
            HashSet<BluetoothDevice> btAvailableReaders = new HashSet();
            btConnection = new BluetoothHandler();
            btConnection.getAvailableDevices(btAvailableReaders);
            for (BluetoothDevice device : btAvailableReaders) {
                int count = 0;
                for(count = 0; count < readersList.size(); count++){
                    if(readersList.get(count).getName().equals(device.getName())) break;
                    //Missing paired device add it to the list
                }
                if( count >= readersList.size()){

                    ReaderDevice readerDevice = new ReaderDevice(device.getName(), device.getAddress(), new RFIDReader(device.getName(), 0, 0, "ASCII", "BLUETOOTH"));
                    if(readersList.contains(readerDevice) == false)
                        readersList.add(readerDevice);
                }

            }
    }

    private void initializeViews() {
        linearLayout = (LinearLayout) getActivity().findViewById(R.id.ll_empty);
        rv_pairedReader = getActivity().findViewById(R.id.rv_pairedreaders);
        updateConnectReader =  getActivity().findViewById(R.id.pairedreader_serialno);
        rl_myLayout = getActivity().findViewById(R.id.rl_connectedreader);
        reader_description = getActivity().findViewById(R.id.reader_description);
        connectedImgView = getActivity().findViewById(R.id.options_menu);
        iv_pairedreader_icon = getActivity().findViewById(R.id.pairedreader_icon);
        tv_model = getActivity().findViewById(R.id.model);
        tv_emptyPairedReader = getActivity().findViewById(R.id.empty_paired_reader);
        tv_serialNo = getActivity().findViewById(R.id.serial_no);
        ll_pairedreader = getActivity().findViewById(R.id.ll_pairedreader);

        rl_myLayout.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        reader_description.setVisibility(View.GONE);
        serialno = getActivity().findViewById(R.id.serial_no);
        serialno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Constants.showSerialNo) {
                    Constants.showSerialNo = true;
                    serialno.setText("Hide Serial No.");
                } else {
                    Constants.showSerialNo = false;
                    serialno.setText("Show Serial No.");
                }

                pairedReaderListAdapter.notifyDataSetChanged();
            }
        });

        rl_myLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectConnectedReader();
            }
        });

        connectedImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(getActivity(), connectedImgView);
                menu.inflate(R.menu.connected_options_menu);
                if(mConnectedDevice != null) {
                    String str = mConnectedDevice.getDeviceCapability(mConnectedReader.getHostName());
                    if (str.contains("PREMIUM")) {
                        //SetVisibility to true for enabling WifiReader Settings on premium devices
                        menu.getMenu().getItem(1).setVisible(false);
                    }
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    menu.setForceShowIcon(true);
                }
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.disconnect:

                                disconnectConnectedReader();
                                break;
                            case R.id.reader_wifisettings:
                                if (getActivity() instanceof ActiveDeviceActivity ) {
                                    ((ActiveDeviceActivity)getActivity()).loadWifiReaderSettings();
                                }
                                break;
                            case R.id.fw_update:
                                if (getActivity() instanceof ActiveDeviceActivity) {
                                    ((ActiveDeviceActivity)getActivity()).loadUpdateFirmware(item.getActionView());
                                }
                                break;
                            case R.id.connected_reader_details:
                                ReaderDevice readerDevice = mConnectedDevice;
                                ((ActiveDeviceActivity)getActivity()).loadReaderDetails( readerDevice);


                                break;
                            case R.id.rename_reader:
                                rename_reader();
                                break;
                        }
                        return false;
                    }
                });
                menu.show();
            }
        });
    }

    public void rename_reader( ){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rename_reader, null);
        edit_Readername = view.findViewById(R.id.edit_ReaderName);
        try {
            edit_Readername.setText(mConnectedReader.Config.getFriendlyName());
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, e.getStackTrace()[0].toString());
        }
        builder.setView(view)
                .setTitle("Rename Reader")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        newName = edit_Readername.getText().toString();
                        try {
                            RFIDResults rfidResults = mConnectedReader.Config.setFriendlyName(newName);

                            if(rfidResults == RFIDResults.RFID_API_SUCCESS){
                                Toast.makeText(getActivity(),"Rename Success. To see changes" +
                                        "\nUSB connection: Deattach and attach the reader " +
                                        "\nBluetooth: Unpair and pair the device",Toast.LENGTH_LONG).show();
                            }
                            else if(rfidResults == RFIDResults.RFID_COMMAND_OPTION_WITHOUT_DELIMITER){
                                Toast.makeText(getActivity(),"Renaming failed. Don't include Space in between words", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(),"Rename fail. Unknown error",Toast.LENGTH_SHORT).show();
                            }

                        } catch (InvalidUsageException e) {
                            if(e.getStackTrace().length>0) {
                                Log.e(TAG, e.getStackTrace()[0].toString());
                                Log.e(TAG, e.getInfo());
                                Toast.makeText(getActivity(),e.getInfo(),Toast.LENGTH_SHORT).show();
                            }
                        } catch (OperationFailureException e) {
                            if( e!= null && e.getStackTrace().length>0)
                            { Log.e(TAG, e.getStackTrace()[0].toString()); }
                        }


                      /*  try {
                           FriendlyName = mConnectedReader.Config.getFriendlyName();
                        }catch (InvalidUsageException e) {
                            if( e!= null && e.getStackTrace().length>0)
                            { Log.e(TAG, e.getStackTrace()[0].toString()); }
                        } catch (OperationFailureException e) {
                            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        }
                        String name = mConnectedDevice.getName();
                   //     name= mConnectedDevice.getRFIDReader().ReaderCapabilities.getModelName();
                        Toast.makeText(getActivity(),"mCOnnectedReader =" +name +"\n FriendlyName = " + FriendlyName, Toast.LENGTH_LONG).show();
                      *//*  */
                    }

                });

        builder.show();


    }

    public void disconnectConnectedReader() {
        if(Application.mIsMultiTagLocatingRunning == true || RFIDController.isLocatingTag == true){
            Toast.makeText(getActivity(),"Cannot disconnect when Tag locate is running!", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(null);
        alertDialog.setMessage("Are you sure you want to disconnect reader?");
        alertDialog.setPositiveButton("disconnect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mConnectedReader.isConnected() ) {
                    activity.disconnect(Application.currentConnectedScannerID);
                    RFIDController.is_disconnection_requested = true;
                    try {

                        if(RFIDController.mIsInventoryRunning)
                            mConnectedReader.Actions.Inventory.stop();

                        mConnectedReader.disconnect();
                        mConnectedReader.Dispose();
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (Exception e) {
                        //if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                    }

                    if (RFIDController.NOTIFY_READER_CONNECTION) {
                        if (mConnectedReader != null)
                            sendNotification(Constants.ACTION_READER_DISCONNECTED, "Disconnected from " + mConnectedReader.getHostName());
                    }
                    ReaderDeviceDisConnected(mConnectedDevice);
                    deviceDisconnected();
                    clearSettings();

                    return;
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialog.show();
    }

    private void refreshPairedDevices(final String deviceId, final boolean isClick) {
        new AsyncTask<Void, Void, Boolean>() {
            InvalidUsageException exception;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                ArrayList<ReaderDevice> readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
                readersList.addAll(readersListArray);
                if (mConnectedDevice != null) {
                    int index = readersList.indexOf(mConnectedDevice);
                    readersList.remove(index);
                    //pairedReaderListAdapter.notifyItemRemoved(index);
                    pairedReaderListAdapter.notifyDataSetChanged();
                    updateConnectReader.setText(mConnectedReader.getHostName());

                }

                } catch (InvalidUsageException ex) {
                    exception = ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                    if(pairedReaderListAdapter.getItemCount() == 0) {
                        tv_emptyPairedReader.setVisibility(View.VISIBLE);
                        tv_serialNo.setVisibility(View.GONE);
                    } else {
                        tv_emptyPairedReader.setVisibility(View.GONE);
                        tv_serialNo.setVisibility(View.VISIBLE);
                    }

               /*San if (exception != null)
                    Toast.makeText(getActivity(), exception.getInfo(), Toast.LENGTH_SHORT).show();
                else {
                    if (RFIDController.mConnectedDevice != null) {
                        int pos = getPosition(mConnectedDevice.getName());
                        int index = readersList.indexOf(RFIDController.mConnectedDevice);
                       if (index != -1) {
                            readersList.remove(index);
                            pairedReaderListAdapter.notifyItemRemoved(index);

                           //San readersList.add(index, RFIDController.mConnectedDevice);
                        } else {
                            RFIDController.mConnectedDevice = null;
                            RFIDController.mConnectedReader = null;
                        }
                    }
                    if (pairedReaderListAdapter.getItemCount() != 0) {
                       // tv_emptyView.setVisibility(View.GONE);
                       linearLayout.setVisibility(View.GONE);
                       // pairedListView.setAdapter(readerListAdapter);
                    }
                   //San pairedReaderListAdapter.notifyItemRemoved();
                }


             /*   // Toast.makeText(getActivity(), "loadPairedDevices" + readersList.size(), Toast.LENGTH_SHORT).show();
                if (isClick && deviceId != null && readerListAdapter != null && readerListAdapter.getCount() >= 1) {

                    int position = getPosition(deviceId);
                    if (position >= 0 && position < pairedListView.getAdapter().getCount())
                        pairedListView.performItemClick(
                                pairedListView.getAdapter().getView(position, null, null),
                                position,
                                pairedListView.getAdapter().getItemId(position));
                }

*/
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    private void connectDevice(String id) {
        if (getPosition("RFD8500" + id) != -1) {

            if (mConnectedDevice == null || !mConnectedDevice.getName().equals("RFD8500" + id)) {
                if (readersList != null)
                    readersList.clear();
                refreshPairedDevices("RFD8500" + id, true);
            } else {

                Toast.makeText(activity, "Device already connected", Toast.LENGTH_SHORT).show();
            }

        } else {

            scanAndPairFragment = ScanAndPairFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("device_id", id);
            scanAndPairFragment.setArguments(bundle);
            scanAndPairFragment.show(getFragmentManager(), "fragment_edit_name");
            getFragmentManager().executePendingTransactions();

        }


    }

    private int getPosition(String name) {

        for (int i = 0; i < readersList.size(); i++) {

            if (readersList.get(i).getName().equals(name))
                return i;
        }

        return -1;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (scanCode != null)
            scanCode.setInputType(0);
        isOnStopCalled = false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanCode != null)
            scanCode.setInputType(0);
        if (PasswordDialog.isDialogShowing) {
            if (passwordDialog == null || !passwordDialog.isShowing()) {
                showPasswordDialog(RFIDController.mConnectedDevice);
            }
        }
        capabilitiesRecievedforDevice();
        //pairedReaderListAdapter.notifyDataSetChanged();
        if(mConnectedReader != null) {
           rl_myLayout.setVisibility(View.VISIBLE);
        }
        if(mConnectedReader == null) {
            rl_myLayout.setVisibility(View.GONE);
            reader_description.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (passwordDialog != null && passwordDialog.isShowing()) {
            PasswordDialog.isDialogShowing = true;
            passwordDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnStopCalled = true;
    }

    private void connectToScanner() {
        //Intent intent = new Intent(this, ScannerHomeActivity.class);
        // Intent intent = new Intent(this, ScannersActivity.class);
        //startActivity(intent);

        scanConnectTask = new ScanConnectTask(activity, RFIDController.mConnectedDevice, "Connecting with " + RFIDController.mConnectedDevice.getName(),
                RFIDController.mConnectedDevice.getPassword(),
                this);
        scanConnectTask.execute();

    }


    public DCSSDKDefs.DCSSDK_RESULT connect(int scannerId) {
        return activity.connect(scannerId);
    }

    public void reInit() {


        if(activity != null )
            activity.reInit();

        if( RFIDController.regionNotSet != true) {

        }else
        {
            Intent detailsIntent = new Intent(getActivity().getApplicationContext(), SettingsDetailActivity.class);
            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ITEM_ID, R.id.regulatory);
            detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ON_FACTORY, true);
            startActivityForResult(detailsIntent, 0);

        }
    }

    @Override
    public void scanTaskDone(ReaderDevice connectingDevice) {
        mConnectioninProgress = false;
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
        if (RFIDController.BatteryData != null) {
            deviceStatusReceived(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
        } else if (mConnectedReader !=null  && mConnectedReader.isConnected() ) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(RFIDController.BatteryData != null)
                        deviceStatusReceived(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
                }
            }, 1000);

        }
        //new Thread(new Runnable() {
        //    @Override
        //    public void run() {
        //        ((ActiveDeviceActivity)getActivity()).initBatchRequest();
        //    }
        //}).start();

        if(scanConnectTask != null )
            scanConnectTask.cancel(true);

        scanConnectTask = null;
        if(mConnectedDevice != null && isAdded() && requireActivity().getApplication() != null) {
            int tabCount = ((com.zebra.demo.application.Application) requireActivity().getApplication()).getTabCount(mConnectedDevice);
            if(RFD_DEVICE_MODE != tabCount)
                ((ActiveDeviceActivity) requireActivity()).changeAdapter(tabCount);

        }


    }

    public void removePairedDeviceList(ReaderDevice device) {

       if( readersList.contains(device) == true)
           readersList.remove(device);

       pairedReaderListAdapter.notifyDataSetChanged();

    }

    public void addPairedDeviceList(ReaderDevice device) {

        if(!readersList.contains(device))
            readersList.add(device);

        pairedReaderListAdapter.notifyDataSetChanged();
    }

    public void disconnect(int scannerId) {
        if (Application.sdkHandler != null) {
            DCSSDKDefs.DCSSDK_RESULT ret = Application.sdkHandler.dcssdkTerminateCommunicationSession(scannerId);
            curAvailableScanner=null;
            activity.updateScannersList();
        }
    }

    /**
     * method to update connected reader device in the readers list on device connected event
     *
     * @param device device to be updated
     */
    @Override
    public void ReaderDeviceConnected(ReaderDevice device) {
        if (device != null) {
            updateConnectReader.setText(device.getName());
            if(getActivity() != null) {
                String model = device.getDeviceCapability(device.getName());
                tv_model.setText(String.format(getActivity().getResources().getString(R.string.readermodel), model));
                setImage(model);
            }

            if (!Application.isReaderConnectedThroughBluetooth ||BluetoothHandler.isDevicePaired(device.getName())) {
                RFIDController.mConnectedDevice = device;
                RFIDController.mConnectedReader = device.getRFIDReader();
                RFIDController.is_connection_requested = false;
                rl_myLayout.setVisibility(View.VISIBLE);
                if (mConnectedDevice != null) {
                    int index = readersList.indexOf(mConnectedDevice);
                    updateConnectReader.setText(mConnectedReader.getHostName());
                    reader_description.setVisibility(View.GONE);
                    if(index != -1) {
                        readersList.remove(index);
                        pairedReaderListAdapter.notifyItemRemoved(index);
                        pairedReaderListAdapter.notifyDataSetChanged();
                    }
                    if(pairedReaderListAdapter.getItemCount() != 0) {
                        tv_serialNo.setVisibility(View.VISIBLE);
                        tv_emptyPairedReader.setVisibility(View.GONE);
                    } else {
                        tv_serialNo.setVisibility(View.GONE);
                        tv_emptyPairedReader.setVisibility(View.VISIBLE);
                    }
                }
                connectToScanner();
            } else {

                try {
                    mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
                RFIDController.mConnectedReader = null;
                clearConnectedReader();

            }
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
    }

    void clearConnectedReader() {
        SharedPreferences settings = getContext().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_READER, "");
        editor.commit();
        LAST_CONNECTED_READER = "";
        RFIDController.mConnectedDevice = null;
    }

    public void ReaderDeviceDisConnected(ReaderDevice device) {
        if (deviceConnectTask != null && !deviceConnectTask.isCancelled() && deviceConnectTask.getConnectingDevice().getName().equalsIgnoreCase(device.getName())) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (deviceConnectTask != null)
                deviceConnectTask.cancel(true);
        }
        if (device != null) {
            if(readersList.contains(device) == false)
                readersList.add(device);
            rl_myLayout.setVisibility(View.GONE);
            reader_description.setVisibility(View.VISIBLE);
            if(pairedReaderListAdapter.getItemCount() != 0 ) {
                tv_serialNo.setVisibility(View.VISIBLE);
                tv_emptyPairedReader.setVisibility(View.GONE);
            } else {
                tv_serialNo.setVisibility(View.GONE);
                tv_emptyPairedReader.setVisibility(View.VISIBLE);
            }

            if (mConnectedDevice != null) {
                pairedReaderListAdapter.notifyDataSetChanged();
            }
        } else {
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        }
        //RFIDController.clearSettings();
        deviceDisconnected();

    }

    public void readerDisconnected(ReaderDevice device, boolean forceDisconnect) {
        if (device != null) {
            if (RFIDController.mConnectedReader != null && (!AUTO_RECONNECT_READERS || forceDisconnect)) {
                try {
                    RFIDController.mConnectedReader.disconnect();
                    RFIDController.mConnectedReader.Dispose();
                    RFIDController.mConnectedReader = null;

                } catch (InvalidUsageException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (Exception e) {
                    //if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }


            }

        }
    }

    /**
     * method to update reader device in the readers list on device connection failed event
     *
     * @param device device to be updated
     */
    @Override
    public void ReaderDeviceConnFailed(ReaderDevice device) {
        if (isVisible() && progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
        if (device != null) {
            // changeTextStyle(device);
        }
        else {
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        }
        sendNotification(Constants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");
        if ( device.getTransport() != null && device.getTransport().equalsIgnoreCase("BLUETOOTH") == true )
        {
            if(activity != null )
                Toast.makeText(activity,"Make sure BT is enabled",Toast.LENGTH_SHORT).show();
        }
        
        RFIDController.is_connection_requested = false;
        RFIDController.mConnectedReader = null;
        RFIDController.mConnectedDevice = null;
    }

    @Override
    public void onTaskDataCleanUp() {
        deviceConnectTask = null;
        try {
            if( (RFIDController.regionNotSet == false) && (RFIDController.mConnectedReader != null)) {
                RFIDController.getInstance().updateReaderConnection(false);
            }
        } catch (InvalidUsageException e) {
           Log.d(TAG,  "Returned SDK Exception");
        } catch (OperationFailureException e) {
           Log.d(TAG,  "Returned SDK Exception");
        }
    }

    /**
     * check/un check the connected/disconnected reader list item
     *
     * @param device device to be updated
     */
    private void changeTextStyle(final ReaderDevice device) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int i = readerListAdapter.getPosition(device);
                    if (i >= 0) {
                        readerListAdapter.remove(device);
                        readerListAdapter.insert(device, i);
                        readerListAdapter.notifyDataSetChanged();

                    }
                }
            });
        }
    }

    public void RFIDReaderAppeared(final ReaderDevice readerDevice) {
        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pairedReaderListAdapter != null && readerDevice != null) {
                        if (pairedReaderListAdapter.getItemCount() == 0) {
                           // linearLayout.setVisibility(View.GONE);
                            //pairedListView.setAdapter(readerListAdapter);
                        }
                        if (readersList.contains(readerDevice)) {
                            Log.d(TAG, "Reader already added into the list");
                        } else {
                            readersList.add(readerDevice);
                        }
                        pairedReaderListAdapter.notifyDataSetChanged();

                        autoConnectDevice(readerDevice);
                    }
                }
            });
        }

    }


    private void autoConnectDevice(ReaderDevice readerDevice) {
        int index = (pairedReaderListAdapter.getItemCount()-1);

        Log.d("autoConnectDevice", "mConnectionProgress = " + mConnectionProgress );
        if(mConnectioninProgress == true) {
            return;
        }

        if(AUTO_RECONNECT_READERS == false )
            return;

        for(;index >= 0; index--)
        {
            if ( (AUTO_RECONNECT_READERS && readerDevice.getAddress().equalsIgnoreCase("USB_PORT") == true ) || (AUTO_RECONNECT_READERS && LAST_CONNECTED_READER != null && LAST_CONNECTED_READER.length() !=0 && LAST_CONNECTED_READER.equals(readersList.get(index).getName())))
            {
                int finalIndex = index;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ReaderDevice readerDevice = readersList.get(finalIndex);
                        if(mConnectedReader !=null && mConnectedReader.isConnected()) {
                            Toast.makeText(getActivity()," Disconnect the connected reader to proceed",Toast.LENGTH_SHORT).show();
                            return;

                        }
                        if(mConnectedReader ==null) {
                            showProgressDialog(readerDevice);
                        }
                    }
                });

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                   @Override
                    public void run() {
                        ConnectReader(finalIndex);
                    }
                }, Application.AUTORECONNECT_DELAY);


                break;
            }
        }

    }
    
    public void RFIDReaderDisappeared(final ReaderDevice readerDevice) {

        if( readersList.contains(readerDevice) == true)
            readersList.remove(readerDevice);

    }

    /**
     * method to update serial and model of connected reader device
     */
    public void capabilitiesRecievedforDevice() {

    }

    /**
     * method to show connect password pairTaskDailog
     *
     * @param connectingDevice
     */
    @Override
    public void showPasswordDialog(ReaderDevice connectingDevice) {
        if (ActiveDeviceActivity.isActivityVisible()) {
            passwordDialog = new PasswordDialog(activity, connectingDevice);
            passwordDialog.show();
        } else
            PasswordDialog.isDialogShowing = true;
    }


    public void showProgressDialog(ReaderDevice connectingDevice)
    {
        if (progressDialog == null) {

            progressDialog = new CustomProgressDialog(activity, "Connecting to device " + connectingDevice.getName() );
            progressDialog.setCancelable(false);
            progressDialog.show();

        }
    }



    /**
     * method to cancel progress pairTaskDailog
     */
    public void cancelProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
            progressDialog.dismiss();
        }
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
    }

    public void showScanProgressDialog(ReaderDevice connectingDevice)
    {
        if(progressDialog == null){
            progressDialog =  new CustomProgressDialog(activity, "Connecting to device " + connectingDevice.getName() );
            progressDialog.setCancelable(false);
            progressDialog.show();

        }
    }



    /**
     * method to cancel progress pairTaskDailog
     */
    public void cancelScanProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
    }



    public void ConnectwithPassword(String password, ReaderDevice readerDevice) {
        try {
            if (mConnectedReader != null)
                mConnectedReader.disconnect();
        } catch (InvalidUsageException e) {
           Log.d(TAG,  "Returned SDK Exception");
        } catch (OperationFailureException e) {
           Log.d(TAG,  "Returned SDK Exception");
        }
        deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(),
                password,
                handlers);

        deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * method to get connect password for the reader
     *
     * @param address - device BT address
     * @return connect password of the reader
     */
    private String getReaderPassword(String address) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.READER_PASSWORDS, 0);
        if(sharedPreferences != null)
            return sharedPreferences.getString(address, null);
        else
            return "";
    }

    public void sendNotification(String action, String data) {
        if (activity != null) {
            if (activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_settings_detail)) || activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_readers_list)))
                ((ActiveDeviceActivity) activity).sendNotification(action, data);
            else
                ((ActiveDeviceActivity) activity).sendNotification(action, data);
        }
    }

    @Override
    public void StoreConnectedReader() {
        try {
            if (isAdded()) {
                SharedPreferences settings = requireActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                AUTO_RECONNECT_READERS = settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true);

                if (AUTO_RECONNECT_READERS && mConnectedReader != null) {
                    LAST_CONNECTED_READER = RFIDController.mConnectedReader.getHostName();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.LAST_READER, LAST_CONNECTED_READER);
                    editor.commit();
                }
            }
        }
        catch (NullPointerException e){
           Log.d(TAG,  "Returned SDK Exception");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void deviceStatusReceived(final int level, final boolean charging, final String cause) {

        if(getActivity() != null ) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    batteryLevelImage.setImageLevel(level);
                }
            });
        }
    }
    public void deviceDisconnected() {
        batteryLevelImage.setImageLevel(0);
    }

    @Override
    public void onListItemClick(View view) {
    }

    @Override
    public void ConnectReader(int position) {
        Log.d(TAG, "Connect Reader " + position  );
        int tabCount = 0;
        if(position >= readersList.size())
            return;

        ReaderDevice readerDevice = readersList.get(position);

        if(mConnectedReader !=null && mConnectedReader.isConnected()) {
            if( getActivity() != null )
                Toast.makeText(getActivity(),"Disconnect the connected reader to proceed",Toast.LENGTH_SHORT).show();

            return;
        } else
        if ((RFIDController.is_connection_requested != true)&&(RFIDController.mConnectedReader == null)) {
            if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                RFIDController.is_connection_requested = true;
                mConnectionProgress = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(),
                            getReaderPassword(readerDevice.getName()),
                            handlers);
                    deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(),
                            getReaderPassword(readerDevice.getName()),
                            handlers);
                    deviceConnectTask.execute();
                }

                return;
            }
        }

    }

    @Override
    public void unPair(int position ,String transportType) {
        if(transportType.equals("BLUETOOTH")) {
            scanPair = new ScanPair();
            scanAndPairFragment = ScanAndPairFragment.newInstance();
            scanPair.Init(getActivity(),scanAndPairFragment);
            btConnection = new BluetoothHandler();
            btConnection.init(getActivity(),scanPair);
            ReaderDevice readerDevice = readersList.get(position);
            btConnection.unpairReader(readerDevice.getName());
            ((ActiveDeviceActivity)getActivity()).nfcData = null;
        } else {
            Toast.makeText(getActivity(),"Not a Bletooth Device",Toast.LENGTH_SHORT).show();
            return;
        }

    }

}
