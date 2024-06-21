package com.zebra.demo.rfidreader.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.LoggerFragment;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.reader_connection.RFIDReadersListFragment;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_KEYLAYOUT_TYPE;
import com.zebra.rfid.api3.ENUM_NEW_KEYLAYOUT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import java.util.ArrayList;

import static com.zebra.demo.application.Application.DEVICE_STD_MODE;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;


public class KeyRemapFragment extends Fragment {
    Spinner upperspinner, lowerspinner;
    public static String upper, lower;
    public static int upperTval, lowerTval;
    ArrayAdapter<String> adapter;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private static final String TAG = "KeyRemapFragment";
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor editor;
    ActiveDeviceActivity mActivity;
    ENUM_NEW_KEYLAYOUT_TYPE UpperTrigger, LowerTrigger;
/*

    String[] itemsPremiumPlus = {"RFID","Sled scanner","Terminal scanner",
            "Scan notification","No action"};

    String[] itemsStandard = {"Upper(RFID)Lower(Host Scan)",
            "Upper(Host Scan)Lower(RFID)"};
*/

    String[] items = {"RFID","Sled scanner","Terminal scanner","Scan notification","No action"};
    Button apply;


    public static KeyRemapFragment newInstance() {
        KeyRemapFragment fragment = new KeyRemapFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String keylayoutType;
        super.onCreate(savedInstanceState);
        final View rootview = inflater.inflate(R.layout.activity_keymap_select, container, false);
        //getSupportActionBar().setTitle(R.string.KeyMapping);

        try {

            if ((keylayoutType = RFIDController.mConnectedReader.Config.getKeylayoutType()) != null)
            {
                upperTval = mConnectedReader.Config.getUpperTriggerValue(keylayoutType).getEnumValue();
                lowerTval = mConnectedReader.Config.getLowerTriggerValue(keylayoutType).getEnumValue();
            }
        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
        upperspinner = rootview.findViewById(R.id.upperTrigger);
        lowerspinner = rootview.findViewById(R.id.lowerTrigger);
        apply = rootview.findViewById(R.id.applyKeyremap);
      /*  if (Application.RFD_DEVICE_MODE == DEVICE_STD_MODE)
            items = itemsStandard;
        else
            items = itemsPremiumPlus;
*/
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        upperspinner.setAdapter(adapter);
        lowerspinner.setAdapter(adapter);


        upperspinner.setSelection(upperTval);
        lowerspinner.setSelection(lowerTval);


        upperspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();


                if (mConnectedReader == null) return;
                if(!RFIDController.mIsInventoryRunning) {
                    switch (position) {
                        case 0:
                            UpperTrigger = ENUM_NEW_KEYLAYOUT_TYPE.RFID;

                            break;
                        case 1:
                            UpperTrigger = ENUM_NEW_KEYLAYOUT_TYPE.SLED_SCAN;

                            break;
                        case 2:
                            UpperTrigger = ENUM_NEW_KEYLAYOUT_TYPE.TERMINAL_SCAN;

                            break;
                        case 3:
                            UpperTrigger = ENUM_NEW_KEYLAYOUT_TYPE.SCAN_NOTIFY;

                            break;
                        case 4:
                            UpperTrigger = ENUM_NEW_KEYLAYOUT_TYPE.NO_ACTION;
                            break;
                    }
                  //  upperTval = position;
                } else {
                    Toast.makeText(parent.getContext(),"Inventory inprogress TriggerMapping not allowed" ,Toast.LENGTH_SHORT).show();

                    //  position = Application.keyLayoutType;
                }
                //  upperspinner.setItemChecked(position, true);



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lowerspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();


                if (mConnectedReader == null) return;
                if(!RFIDController.mIsInventoryRunning) {
                    switch (position) {
                        case 0:
                            LowerTrigger =   ENUM_NEW_KEYLAYOUT_TYPE.RFID;

                            break;
                        case 1:
                            LowerTrigger =   ENUM_NEW_KEYLAYOUT_TYPE.SLED_SCAN;

                            break;
                        case 2:
                            LowerTrigger =   ENUM_NEW_KEYLAYOUT_TYPE.TERMINAL_SCAN;

                            break;
                        case 3:
                            LowerTrigger =   ENUM_NEW_KEYLAYOUT_TYPE.SCAN_NOTIFY;

                            break;
                        case 4:
                            LowerTrigger =   ENUM_NEW_KEYLAYOUT_TYPE.NO_ACTION;
                            break;
                    }
                 //   lowerTval = position;

                } else {
                    Toast.makeText(parent.getContext(),"Inventory inprogress TriggerMapping not allowed" ,Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

      /*  try {
            if(!RFIDController.mIsInventoryRunning) {
                keylayoutType = mConnectedReader.Config.getKeylayoutType();
                currentkeymapping = keylayoutType.getEnumValue();
                Application.keyLayoutType = currentkeymapping;
            } else {
                currentkeymapping = Application.keyLayoutType;
            }
            //   upperspinner.setSelection(currentkeymapping);
            if (currentkeymapping == -1) {
                //    upperspinner.setSelection(0);
                //  lowerspinner.setSelection(0);
            } else {
                // upperspinner.setItemChecked(currentkeymapping, true);
            }

        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
*/
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!RFIDController.mIsInventoryRunning){
                    try {
                        RFIDResults result = mConnectedReader.Config.setKeylayoutType(UpperTrigger, LowerTrigger);
                        if(result == RFIDResults.RFID_API_SUCCESS){

                           // Log.d("getKeylayoutType","Keymap val = "+mConnectedReader.Config.getKeylayoutType() );
                            Toast.makeText(getContext(), "Trigger Selection applied successfully " , Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getContext(), "Trigger selection settings not allowed " , Toast.LENGTH_SHORT).show();
                        }

                    } catch (InvalidUsageException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        Toast.makeText(getContext(),"Invalid Usage" ,Toast.LENGTH_SHORT).show();
                    } catch (OperationFailureException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        Toast.makeText(getContext(),"Remapping not set for the device" ,Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getContext(),"Inventory inprogress TriggerMapping not allowed" ,Toast.LENGTH_SHORT).show();
                }


            }
        });




        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        // RFIDBaseActivity.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        // RFIDBaseActivity.removeReaderDeviceFoundHandler(this);
    }

    @Override
    public void onDestroy() {
        RFIDBaseActivity.getInstance().resetReaderstatuscallback();

        super.onDestroy();
    }
}
   /* ListView listView;
    ArrayAdapter<String> adapter;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private static final String TAG = "KeyRemapFragment";

    ActiveDeviceActivity mActivity;

    String[] itemsPremiumPlus = {"Upper(RFID)Lower(Host Scan)",
            "Upper(Host Scan)Lower(RFID)",
            "Upper(RFID)Lower(Sled Scan)",
            "Upper(Sled Scan) & Lower(RFID)"};

    String[] itemsStandard = {"Upper(RFID)Lower(Host Scan)",
            "Upper(Host Scan)Lower(RFID)"};

    String[] items = null;

    public static KeyRemapFragment newInstance() {
        KeyRemapFragment fragment = new KeyRemapFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootview = inflater.inflate(R.layout.activity_key_remap, container, false);
        //getSupportActionBar().setTitle(R.string.KeyMapping);
        listView = rootview.findViewById(R.id.keymaplist);
        if (Application.RFD_DEVICE_MODE == DEVICE_STD_MODE)
            items = itemsStandard;
        else
            items = itemsPremiumPlus;

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, items);
        listView.setAdapter(adapter);
        ENUM_KEYLAYOUT_TYPE keylayoutType = null;
        int currentkeymapping;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                try {
                    if (mConnectedReader == null) return;

                    if(!RFIDController.mIsInventoryRunning) {
                        switch (position) {
                            case 0:
                                Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_SHORT).show();
                                mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_RFID);
                                break;
                            case 1:
                                Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_SHORT).show();
                                mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SCAN);
                                break;
                            case 2:
                                Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_SHORT).show();
                                mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.LOWER_TRIGGER_FOR_SLED_SCAN);
                                break;
                            case 3:
                                Toast.makeText(parent.getContext(), "TriggerTrigger Selected: " + item, Toast.LENGTH_SHORT).show();
                                mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SLED_SCAN);
                                break;
                        }
                        Application.keyLayoutType = position;
                    } else {
                        Toast.makeText(parent.getContext(),"Inventory inprogress TriggerMapping not allowed" ,Toast.LENGTH_SHORT).show();

                        position = Application.keyLayoutType;
                    }
                    listView.setItemChecked(position, true);

                } catch (InvalidUsageException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                } catch (OperationFailureException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }

            }
        });

        try {
            if(!RFIDController.mIsInventoryRunning) {
                keylayoutType = mConnectedReader.Config.getKeylayoutType();
                currentkeymapping = keylayoutType.getEnumValue();
                Application.keyLayoutType = currentkeymapping;
            } else {
                currentkeymapping = Application.keyLayoutType;
            }
            listView.setSelection(currentkeymapping);
            if (currentkeymapping == -1) {
                listView.setItemChecked(0, true);
            } else {
                listView.setItemChecked(currentkeymapping, true);
            }

        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        // RFIDBaseActivity.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        // RFIDBaseActivity.removeReaderDeviceFoundHandler(this);
    }

    @Override
    public void onDestroy() {
        RFIDBaseActivity.getInstance().resetReaderstatuscallback();

        super.onDestroy();
    }
*//*
    @Override
    public void ReaderDeviceConnected(ReaderDevice device) {

    }

    @Override
    public void ReaderDeviceDisConnected(ReaderDevice device) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
       /* if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).RFIDReaderAppeared(device);
        }
       if (RFIDController.NOTIFY_READER_AVAILABLE) {
            if(!device.getName().equalsIgnoreCase("null"))
                sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is available.");
        }* /

    }

    @Override
    public void ReaderDeviceConnFailed(ReaderDevice device) {

    }
*//*
}

*/