package com.zebra.demo.rfidreader.reader_connection;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.discover_connect.nfc.PairOperationsFragment;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.common.Inventorytimer;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.settings.AdvancedOptionsContent;
import com.zebra.demo.scanner.helpers.AvailableScanner;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.DCSSDKDefs;

import java.util.ArrayList;

import static android.os.AsyncTask.Status.FINISHED;
import static com.zebra.demo.application.Application.AUTORECONNECT_DELAY;
import static com.zebra.demo.application.Application.TAG_LIST_MATCH_MODE;
import static com.zebra.demo.rfidreader.rfid.RFIDController.AUTO_DETECT_READERS;
import static com.zebra.demo.rfidreader.rfid.RFIDController.AUTO_RECONNECT_READERS;
import static com.zebra.demo.rfidreader.rfid.RFIDController.EXPORT_DATA;
import static com.zebra.demo.rfidreader.rfid.RFIDController.LAST_CONNECTED_READER;
import static com.zebra.demo.rfidreader.rfid.RFIDController.NON_MATCHING;
import static com.zebra.demo.rfidreader.rfid.RFIDController.NOTIFY_BATTERY_STATUS;
import static com.zebra.demo.rfidreader.rfid.RFIDController.NOTIFY_READER_AVAILABLE;
import static com.zebra.demo.rfidreader.rfid.RFIDController.NOTIFY_READER_CONNECTION;
import static com.zebra.demo.rfidreader.rfid.RFIDController.SHOW_CSV_TAG_NAMES;
import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.asciiMode;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedDevice;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mReaderDisappeared;
import static com.zebra.demo.rfidreader.rfid.RFIDController.readersList;
import static com.zebra.demo.rfidreader.settings.AdvancedOptionsContent.DPO_ITEM_INDEX;


/**
 * A simple {@link android.support.v4.app} subclass.
 * <p/>
 * Use the {@link RFIDReadersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to maintain the list of readers
 */
public class InitReadersListFragment extends Fragment implements IRFIDConnectTaskHandlers, IScanConnectHandlers, PairedReaderListAdapter.ListItemClickListener{
    private PasswordDialog passwordDialog;
    private DeviceConnectTask deviceConnectTask;
    private static final String RFD8500 = "RFD8500";
    private ReaderListAdapter readerListAdapter;
    private ListView pairedListView;
    private TextView tv_emptyView;
    LinearLayout ll_pairedreader , linearLayout;
    private CustomProgressDialog progressDialog;
    private DeviceDiscoverActivity activity = null;
    private static InitReadersListFragment rlf = null;
    private ScanAndPairFragment scanAndPairFragment;
    private EditText scanCode;
    private boolean isOnStopCalled = false;
    private ScanConnectTask scanConnectTask;
    private AvailableScanner curAvailableScanner;
    private ExtendedFloatingActionButton fabPairReader;
    private IRFIDConnectTaskHandlers handlers;
    private DataSetObserver mDataSetObserver;
    private boolean mConnectionProgress = false;
    private PairedReaderListAdapter pairedReaderListAdapter;
    private RecyclerView rv_pairedReader;
    private RelativeLayout rl_myLayout;
    TextView updateConnectReader , tv_model;
    ImageView connectedImgView, iv_pairedreader_icon ;
    private BluetoothHandler btConnection = null;
    TextView serialno;


    public void CancelReconnect() {
        if (RFIDBaseActivity.DisconnectTask != null && AUTO_RECONNECT_READERS) {
            int timeout = 20;
            while (FINISHED != RFIDBaseActivity.DisconnectTask.getStatus() && timeout > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }
                timeout--;
            }
        }
    }

    @Override
    public void setConnectionProgressState(boolean progressState) {
        mConnectionProgress = progressState;
    }

    public InitReadersListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadersListFragment.
     */
    public static InitReadersListFragment newInstance() {
        return new InitReadersListFragment();
    }

    public static InitReadersListFragment getInstance() {
        if (rlf == null)
            rlf = new InitReadersListFragment();
        return rlf;
    }

    private void clearSettings() {
        RFIDController.clearSettings();
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
        activity = (DeviceDiscoverActivity) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handlers = this;
        initializeStoredSettings();
        // registerReceivers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_readers_list, menu);
        boolean isScanAndPairVisible = Application.isReaderConnectedThroughBluetooth;

        isScanAndPairVisible = true;
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
        readersList.clear();
      //  ArrayList<ReaderDevice> readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
       // readersList.addAll(readersListArray);
        //Log.d(TAG, "readersList: " + readersList.size());
        pairedReaderListAdapter = new PairedReaderListAdapter(getActivity(), R.layout.readers_pair_connect, RFIDController.readersList, this);
        // rv_pairedReader.setVisibility(View.VISIBLE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_pairedReader.setLayoutManager(linearLayoutManager);
        rv_pairedReader.setAdapter(pairedReaderListAdapter);
        rl_myLayout.setVisibility(View.GONE);

        if (pairedReaderListAdapter.getItemCount() == 0) {
            ll_pairedreader.setVisibility(View.GONE);

        } else {
            //rv_pairedReader.setAdapter(pairedReaderListAdapter);
            linearLayout.setVisibility(View.GONE);
        }

        loadPairedDevices(null, false);

        fabPairReader =(ExtendedFloatingActionButton) getActivity().findViewById(R.id.fab_pair_reader);
/*        if(getArguments().getBoolean("launch_app_home", false)) {
            fabPairReader.setVisibility(View.VISIBLE);
        }*/
        fabPairReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                   // BluetoothAdapter.getDefaultAdapter().enable();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    ((DeviceDiscoverActivity) getActivity()).startActivityForResult(enableBtIntent, 1101);
                }
                PairOperationsFragment pairOpFragment = PairOperationsFragment.newInstance();
                ((DeviceDiscoverActivity)getActivity()).switchToFragment(pairOpFragment);
            }
        });
       /* mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
               // autoConnectDevice();
            }
        };*/

        // readerListAdapter.registerDataSetObserver(mDataSetObserver);
    }
    public void loadUIData(){
        readersList.clear();
        loadPairedDevices(null, false);
    }

    private void initializeViews() {
        linearLayout = (LinearLayout) getActivity().findViewById(R.id.ll_empty);
        ll_pairedreader = (LinearLayout) getActivity().findViewById(R.id.ll_pairedreader);
        rv_pairedReader = getActivity().findViewById(R.id.rv_pairedreaders);
        rl_myLayout = getActivity().findViewById(R.id.rl_connectedreader);
        updateConnectReader =  getActivity().findViewById(R.id.pairedreader_serialno);
        rl_myLayout = getActivity().findViewById(R.id.rl_connectedreader);
        connectedImgView = getActivity().findViewById(R.id.options_menu);
        iv_pairedreader_icon = getActivity().findViewById(R.id.pairedreader_icon);
        tv_model = getActivity().findViewById(R.id.model);

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

       /*  scanCode = getActivity().findViewById(R.id.et_barcodevalue);
        scanCode.setInputType(0);
        scanCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 14 && !s.toString().contains(":")) {

                    String id = scanCode.getText().toString();
                    scanCode.setText("");
                    scanCode.setInputType(0);
                    connectDevice(id);
                }

            }

        });*/
    }

    private void loadPairedDevices(final String deviceId, final boolean isClick) {
        new AsyncTask<Void, Void, Boolean>() {
            InvalidUsageException exception;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                readersList.clear();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                ArrayList<ReaderDevice> readersListArray = null;
                readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
                readersList.addAll(readersListArray);
                    Log.d(TAG, "readersList: " + readersList.size());

                } catch (InvalidUsageException ex) {
                    exception = ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (exception != null)
                    Toast.makeText(getActivity(), exception.getInfo(), Toast.LENGTH_SHORT).show();
                else {
                    if (RFIDController.mConnectedDevice != null) {
                        int index = readersList.indexOf(RFIDController.mConnectedDevice);
                        Log.d(TAG, "index: " + index);
                        if (index != -1) {
                            readersList.remove(index);
                            readersList.add(index, RFIDController.mConnectedDevice);
                        } else {
                            RFIDController.mConnectedDevice = null;
                            RFIDController.mConnectedReader = null;
                        }
                    }
                    if (pairedReaderListAdapter.getItemCount() != 0) {
                        linearLayout.setVisibility(View.GONE);
                        ll_pairedreader.setVisibility(View.VISIBLE);
                    } else {
                        linearLayout.setVisibility(View.VISIBLE);
                        ll_pairedreader.setVisibility(View.GONE);
                    }
                    pairedReaderListAdapter.notifyDataSetChanged();
                    autoConnectDevice();
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


    private Boolean autoConnectUSBDevice(){
        int index = (pairedReaderListAdapter.getItemCount()-1);

        Log.d("autoConnectDevice", "mConnectionProgress = " + mConnectionProgress );
        if(mConnectionProgress == true) {
            return false;
        }

        if( mReaderDisappeared  != null ) {
            return false;
        }

        if(AUTO_RECONNECT_READERS == false )
            return false;

        for(;index >= 0; index--) {

            //Look for USB device presence
            if (readersList.get(index).getAddress().equalsIgnoreCase("USB_PORT") == true  && AUTO_RECONNECT_READERS ) {

                int finalIndex = index;
                if (mConnectedReader != null && mConnectedReader.isConnected()) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Disconnect the connected reader to proceed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    return false;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReaderDevice readerDevice = readersList.get(finalIndex);
                        if (mConnectedReader == null) {
                            showProgressDialog(readerDevice);
                        }
                    }
                });


                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        ConnectReader(finalIndex);

                    }

                }, AUTORECONNECT_DELAY);
                return true;
            }
        }
        return false;
    }



    private void autoConnectDevice() {
        int index = (pairedReaderListAdapter.getItemCount()-1);

        Log.d("autoConnectDevice", "mConnectionProgress = " + mConnectionProgress );
        if(mConnectionProgress == true) {
            return;
        }

        if( mReaderDisappeared  != null ) {
            return;
        }

        if(AUTO_RECONNECT_READERS == false )
            return;


        if(autoConnectUSBDevice() == true)
            return;

        for(;index >= 0; index--)
        {
            if ( AUTO_RECONNECT_READERS && LAST_CONNECTED_READER != null && LAST_CONNECTED_READER.length() !=0 && LAST_CONNECTED_READER.equals(readersList.get(index).getName()))
            {
                int finalIndex = index;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ReaderDevice readerDevice = readersList.get(finalIndex);
                        if(mConnectedReader !=null && mConnectedReader.isConnected()) {
                            Toast.makeText(getActivity(),"Disconnect the connected reader to proceed",Toast.LENGTH_SHORT).show();
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

//                        try {
//                            if(mDataSetObserver != null )
//                                readerListAdapter.unregisterDataSetObserver(mDataSetObserver);
//
//                        }catch (IllegalStateException ex)
//                        {
//                            if( ex!= null && ex.getStackTrace().length>0){ Log.e(TAG, ex.getStackTrace()[0].toString()); }
//                        }
//
//                        pairedListView.performItemClick(
//                                pairedListView.getAdapter().getView(finalIndex, null, null),
//                                finalIndex,
//                                pairedListView.getAdapter().getItemId(finalIndex));
                    }
                }, AUTORECONNECT_DELAY);


                break;
            }
        }

    }


    private void connectDevice(String id) {
        if (getPosition("RFD8500" + id) != -1) {

            if (mConnectedDevice == null || !mConnectedDevice.getName().equals("RFD8500" + id)) {
                if (readersList != null)
                    readersList.clear();
                loadPairedDevices("RFD8500" + id, true);
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
      //  ((DeviceDiscoverActivity)getActivity()).setActionBarTitle(getResources().getString(R.string.title_empty_readers));
        if (scanCode != null)
            scanCode.setInputType(0);
        if (PasswordDialog.isDialogShowing) {
            if (passwordDialog == null || !passwordDialog.isShowing()) {
                showPasswordDialog(RFIDController.mConnectedDevice);
            }
        }
        capabilitiesRecievedforDevice();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (passwordDialog != null && passwordDialog.isShowing()) {
            PasswordDialog.isDialogShowing = true;
            passwordDialog.dismiss();
        }
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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
        try {
            RFIDController.mConnectedReader.Config.getDeviceVersionInfo(Application.versionInfo);
        } catch (InvalidUsageException e) {
            //if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            //if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
        scanConnectTask = new ScanConnectTask(activity, RFIDController.mConnectedDevice, "Connecting with " + RFIDController.mConnectedDevice.getName(), RFIDController.mConnectedDevice.getPassword(), this);
        scanConnectTask.execute();
    }




    public void disconnect(int scannerId) {
        if (Application.sdkHandler != null) {
            DCSSDKDefs.DCSSDK_RESULT ret = Application.sdkHandler.dcssdkTerminateCommunicationSession(scannerId);
            curAvailableScanner=null;
            activity.updateScannersList();
        }
    }

    @Override
    public DCSSDKDefs.DCSSDK_RESULT connect(int scannerId) {
        return activity.connect(scannerId);

    }

    @Override
    public void reInit() {

    }

    @Override
    public void scanTaskDone(ReaderDevice connectingDevice) {
        curAvailableScanner = Application.curAvailableScanner;
        mConnectionProgress = false;
        if (RFIDController.mConnectedReader == null) {
            //connection failure
            //attempt to connect again
            //autoConnectDevice();
            if(progressDialog != null) {
                progressDialog.cancel();
                progressDialog = null;
            }

            if(scanConnectTask != null )
                scanConnectTask.cancel(true);

            scanConnectTask = null;
            //Toast.makeText(getActivity(),"Reader Connection failed ",Toast.LENGTH_SHORT).show();
            return;
        }
        if (curAvailableScanner == null && RFIDController.mConnectedReader != null) {
            //connection failure
            //attempt to connect again
            //autoConnectDevice();
            if (RFIDController.mConnectedReader.getHostName().contains("MC33") == false) {

                Toast.makeText(getActivity(), "Reader Connection failed ", Toast.LENGTH_SHORT).show();
                try {
                    RFIDController.mConnectedReader.disconnect();
                    if (connectingDevice.getTransport().equals("BLUETOOTH")) {
                        unPair(connectingDevice);
                        readersList.remove(connectingDevice);
                        pairedReaderListAdapter.notifyDataSetChanged();
                    }
                    if (progressDialog != null) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }

                    if (scanConnectTask != null)
                        scanConnectTask.cancel(true);

                    scanConnectTask = null;
                } catch (InvalidUsageException e) {
                    //e.printStackTrace();
                } catch (OperationFailureException e) {
                    //e.printStackTrace();
                }

                return;
            }
        }

        Intent intent = new Intent(getActivity(), ActiveDeviceActivity.class);
        if (RFIDController.regionNotSet == true) {
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.REGULATORY_SET, false);
        }

        if ((curAvailableScanner != null) && (curAvailableScanner.isConnected())) {


            //Intent intent = new Intent(getActivity(), ActiveDeviceActivity.class);
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.SCANNER_NAME, curAvailableScanner.getScannerName());
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.SCANNER_ADDRESS, curAvailableScanner.getScannerAddress());
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.SCANNER_ID, curAvailableScanner.getScannerId());
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.AUTO_RECONNECTION, curAvailableScanner.isAutoReconnection());
            intent.putExtra(com.zebra.demo.scanner.helpers.Constants.CONNECTED, true);
            if (RFIDController.regionNotSet == true) {
                intent.putExtra(com.zebra.demo.scanner.helpers.Constants.REGULATORY_SET, false);
            }
            startActivity(intent);

        } else {
            startActivity(intent);
        }

        if(progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }

        if(scanConnectTask != null )
            scanConnectTask.cancel(true);

        scanConnectTask = null;
        getActivity().finish();


    }

    @Override
    public void showScanProgressDialog(ReaderDevice connectingDevice) {
       // progressDialog = new CustomProgressDialog(activity, "Scan connect in progress..");
        //progressDialog.show(getActivity(), "", "Scan connect in progress..", false, false);
    }

    @Override
    public void cancelScanProgressDialog() {
        if( progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    /**
     * method to update connected reader device in the readers list on device connected event
     *
     * @param device device to be updated
     */
    public void ReaderDeviceConnected(ReaderDevice device) {
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);


        if (device != null) {

            if (!Application.isReaderConnectedThroughBluetooth ||BluetoothHandler.isDevicePaired(device.getName())) {
                RFIDController.mConnectedDevice = device;
                RFIDController.is_connection_requested = false;
                RFIDController.mConnectedReader = device.getRFIDReader();
                changeTextStyle(device);
                //Time to connect scanner
                if(RFIDController.mConnectedReader != null ) {
                    connectToScanner();
                    //scanTaskDone();
                }else
                    ReaderDeviceConnFailed(device);

            } else {

                try {
                    mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                } catch (OperationFailureException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
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
            changeTextStyle(device);
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        RFIDController.clearSettings();

    }

    public void readerDisconnected(ReaderDevice device, boolean forceDisconnect) {
        if (device != null) {
            if (RFIDController.mConnectedReader != null && (!AUTO_RECONNECT_READERS || forceDisconnect)) {
                try {
                    RFIDController.mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                } catch (OperationFailureException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }
                RFIDController.mConnectedReader = null;

            }
            for (int idx = 0; idx < readersList.size(); idx++) {
                if (readersList.get(idx).getName().equalsIgnoreCase(device.getName()))
                    changeTextStyle(readersList.get(idx));
            }
        }
    }

    /**
     * method to update reader device in the readers list on device connection failed event
     *
     * @param device device to be updated
     */
    public void ReaderDeviceConnFailed(ReaderDevice device) {

        Toast.makeText(getActivity(),"Reader Connection failed ",Toast.LENGTH_SHORT).show();
        if (isVisible() && progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null) {
            deviceConnectTask.cancel(true);
        }
        if (device != null)
            changeTextStyle(device);
        else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
        sendNotification(Constants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");
        RFIDController.mConnectedReader = null;
        RFIDController.mConnectedDevice = null;
        //unpair the device
        unPair(device);
    }

    @Override
    public void onTaskDataCleanUp() {

        mConnectionProgress = false;
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

                  /*  int i = readerListAdapter.getPosition(device);
                    if (i >= 0) {
                        readerListAdapter.remove(device);
                        readerListAdapter.insert(device, i);
                        readerListAdapter.notifyDataSetChanged();

                    }*/
                }
            });
        }
    }

    public void RFIDReaderAppeared(final ReaderDevice readerDevice) {
        if (getActivity() != null) {

            Log.d("RFIDReaderAppeared", "mConnectionProgress = " + mConnectionProgress );
            if(mConnectionProgress == true) {
                Log.d("RFIDReaderAppeared", "mConnectionProgress cannot reconnect" );
                return;
            }
            Log.d("RFIDReaderAppeared", "Initiating new connection" );
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            //getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                     if (pairedReaderListAdapter != null && readerDevice != null) {
                        if (pairedReaderListAdapter.getItemCount() == 0) {
                            //tv_emptyView.setVisibility(View.GONE);
                             linearLayout.setVisibility(View.GONE);
                            // pairedListView.setAdapter(readerListAdapter);
                        }
                        if (readersList.contains(readerDevice)) {
                            Log.d(TAG, "Reader already added into the list");
                        }
                        else {
                            readersList.add(readerDevice);
                        }
                         pairedReaderListAdapter.notifyDataSetChanged();

                        // Connect automatically with the latest paired device.
                         if (AUTO_RECONNECT_READERS && !isOnStopCalled && LAST_CONNECTED_READER != null && LAST_CONNECTED_READER.length() !=0 &&
                                LAST_CONNECTED_READER.equals(readerDevice.getName())) {
                            int position = getPosition(readerDevice.getName());
                            //if (position >= 0 && position < pairedListView.getAdapter().getCount()) {
                            //    pairedListView.performItemClick(
                            //            pairedListView.getAdapter().getView(position, null, null),
                            //            position,
                            //           pairedListView.getAdapter().getItemId(position));
                             ConnectReader(position);

                        }

                    }
                }
            }, AUTORECONNECT_DELAY);

    }
    }

    public void RFIDReaderDisappeared(final ReaderDevice readerDevice) {
        Log.d("RFIDDisAppeared", "mConnectionProgress = " + mConnectionProgress );
        if (getActivity() != null) {
            if(mConnectionProgress == true) {
                Log.d("RFIDReaderDisappeared", "RFIDReaderDisappeared" );
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loadPairedDevices(null,false);
                    if (pairedReaderListAdapter != null && readerDevice != null) {
                        //pairedReaderListAdapter.remove(readerDevice);
                        readersList.remove(readerDevice);
                        if (pairedReaderListAdapter.getItemCount() == 0) {
                            //pairedListView.setEmptyView(tv_emptyView);
                            Log.d(TAG,"linearlayout setemptyview initreaderlist");
                         //san   pairedListView.setEmptyView(linearLayout);
                        }
                        pairedReaderListAdapter.notifyDataSetChanged();
                    }
                }
            });


        }
    }

    /**
     * method to update serial and model of connected reader device
     */
    public void capabilitiesRecievedforDevice() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              /*  if (readerListAdapter.getPosition(RFIDController.mConnectedDevice) >= 0) {
                    ReaderDevice readerDevice = readerListAdapter.getItem(readerListAdapter.getPosition(RFIDController.mConnectedDevice));
                    //readerDevice.setModel(RFIDController.mConnectedDevice.getModel());
                    //readerDevice.setSerial(RFIDController.mConnectedDevice.getSerial());
                    readerListAdapter.notifyDataSetChanged();
                }*/
            }
        });
    }

    /**
     * method to show connect password pairTaskDailog
     *
     * @param connectingDevice
     */
    public void showPasswordDialog(ReaderDevice connectingDevice) {
        if (ActiveDeviceActivity.isActivityVisible()) {
            passwordDialog = new PasswordDialog(activity, connectingDevice);
            passwordDialog.show();
        } else
            PasswordDialog.isDialogShowing = true;
    }


    public void showProgressDialog(ReaderDevice connectingDevice) {
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            progressDialog = new CustomProgressDialog(activity, "Connecting to device " + connectingDevice.getName());
            progressDialog.show();
    }

                /**
     * method to cancel progress pairTaskDailog
     */
    public void cancelProgressDialog()
    {
        if (progressDialog != null /*&& progressDialog.isShowing()*/) {
            progressDialog.dismiss();

        }
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
    }

    public void ConnectwithPassword(String password, ReaderDevice readerDevice) {
        try {
            if (mConnectedReader != null)
                mConnectedReader.disconnect();
        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
        deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(), password, handlers);

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
        return sharedPreferences.getString(address, null);
    }

    public void sendNotification(String action, String data) {
        if (activity != null) {
            if (activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_settings_detail)) || activity.getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_readers_list)))
                ((DeviceDiscoverActivity) activity).sendNotification(action, data);
            else
                ((DeviceDiscoverActivity) activity).sendNotification(action, data);
        }
    }

    public void StoreConnectedReader() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        AUTO_RECONNECT_READERS = settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true);

        if (AUTO_RECONNECT_READERS && mConnectedReader != null) {
            LAST_CONNECTED_READER = RFIDController.mConnectedReader.getHostName();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Constants.LAST_READER, LAST_CONNECTED_READER);
            editor.commit();
        }
    }


    public void initializeStoredSettings() {
        SharedPreferences settings = activity.getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        AUTO_DETECT_READERS = settings.getBoolean(Constants.AUTO_DETECT_READERS, true);
        AUTO_RECONNECT_READERS = settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true);
        NOTIFY_READER_AVAILABLE = settings.getBoolean(Constants.NOTIFY_READER_AVAILABLE, false);
        NOTIFY_READER_CONNECTION = settings.getBoolean(Constants.NOTIFY_READER_CONNECTION, false);
        if (Build.MODEL.contains("MC33"))
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, false);
        else
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true);
        EXPORT_DATA = settings.getBoolean(Constants.EXPORT_DATA, false);
        TAG_LIST_MATCH_MODE = settings.getBoolean(Constants.TAG_LIST_MATCH_MODE, false);
        SHOW_CSV_TAG_NAMES = settings.getBoolean(Constants.SHOW_CSV_TAG_NAMES, false);
        asciiMode = settings.getBoolean(Constants.ASCII_MODE, false);
        NON_MATCHING = settings.getBoolean(Constants.NON_MATCHING, false);
        LAST_CONNECTED_READER = settings.getString(Constants.LAST_READER, "");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onListItemClick(View view) {
        Log.d(TAG, "onListItemClick");

    }

    @Override
    public void ConnectReader(int position) {
        if(position >= readersList.size())
            return;

        ReaderDevice readerDevice = readersList.get(position);
        if(mConnectedReader !=null && mConnectedReader.isConnected()) {
            Toast.makeText(getActivity(),"Disconnect the connected reader to proceed",Toast.LENGTH_SHORT).show();
            return;
        } else
        if (RFIDController.mConnectedReader == null) {
            if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                RFIDController.is_connection_requested = true;
                mConnectionProgress = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()), handlers);
                    deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    deviceConnectTask = new DeviceConnectTask(getActivity(), readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()), handlers);
                    deviceConnectTask.execute();
                }
                return;
            }
        }
    }

    public void switchReaderMode(int position) {
        ReaderDevice readerDevice = readersList.get(position);

      //  if (RFIDController.mConnectedReader == null) {
      //      readerDevice.getRFIDReader().switchMode();
      //  }
    }

    @Override
    public void unPair(int position,String transportType) {
        if(transportType.equals("BLUETOOTH")) {
            if(Application.scanPair == null ) {
                Application.scanPair = new ScanPair();

            }
            Application.scanPair.Init(getActivity(), scanAndPairFragment);
            scanAndPairFragment = ScanAndPairFragment.newInstance();
            btConnection = new BluetoothHandler();
            btConnection.init(getActivity(),Application.scanPair);
            ReaderDevice readerDevice = readersList.get(position);
            btConnection.unpairReader(readerDevice.getName());
            ((DeviceDiscoverActivity)getActivity()).nfcData = null;
        } else {
            Toast.makeText(getActivity(),"Not a Bletooth Device",Toast.LENGTH_SHORT).show();
            return;
        }


    }

    public void unPair(ReaderDevice readerDevice) {
        if(readerDevice.getTransport().equals("BLUETOOTH")) {
            if(Application.scanPair == null ) {
                Application.scanPair = new ScanPair();

            }
            Application.scanPair.Init(getActivity(), scanAndPairFragment);
            scanAndPairFragment = ScanAndPairFragment.newInstance();
            btConnection = new BluetoothHandler();
            btConnection.init(getActivity(),Application.scanPair);
            btConnection.unpairReader(readerDevice.getName());
            ((DeviceDiscoverActivity)getActivity()).nfcData = null;
        } else {
            Toast.makeText(getActivity(),"Not a Bletooth Device",Toast.LENGTH_SHORT).show();
            return;
        }


    }

}
