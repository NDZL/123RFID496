package com.zebra.demo.rfidreader.access_operations;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.TagData;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.InputFilterMax;
import com.zebra.demo.rfidreader.common.hextoascii;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import java.util.Timer;
import java.util.TimerTask;

import static com.zebra.demo.rfidreader.home.RFIDBaseActivity.filter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link AccessOperationsReadWriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle the Access Read/Write Operations.
 */
public class AccessOperationsReadWriteFragment extends Fragment implements AccessOperationsFragment.OnRefreshListener {

    public static Timer tLED;
    public Timer tbeep;
    TextView textRWData;
    private EditText offsetEditText;
    private EditText lengthEditText;
    private AutoCompleteTextView tagIDField;
    private ArrayAdapter<String> adapter;
    private boolean beepON = false;
    private boolean LEDON = false;
    private int LED_STOP_TIME = 500;
    private NotificationManager notificationManager;
    private long BEEP_STOP_TIME = 20;
    private CheckBox accessEnableAdvanceOptions;
    private boolean showAdvancedOptions;
    Spinner rw_typespinner;

    public AccessOperationsReadWriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccessOperationsReadWriteFragment.
     */
    public static AccessOperationsReadWriteFragment newInstance() {
        return new AccessOperationsReadWriteFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.dw_action));
        filter.addCategory(getResources().getString(R.string.dw_category));
        getActivity().registerReceiver(scanResultBroadcast, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_access_operations_read_write, container, false);
        rw_typespinner = view.findViewById(R.id.readwrite_type);
        rw_typespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if(selectedType.equals("Advanced")) {
                    Application.rwAdvancedOptions = true;
                } else {
                    Application.rwAdvancedOptions = false;
                }


                SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.ACCESS_ADV_OPTIONS, Application.rwAdvancedOptions);
                editor.commit();
                UpdateViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeSpinner();
        offsetEditText = (EditText) getActivity().findViewById(R.id.accessRWOffsetValue);
        lengthEditText = (EditText) getActivity().findViewById(R.id.accessRWLengthValue);
        tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.accessRWTagID));
        textRWData = (TextView) getActivity().findViewById(R.id.accessRWData);
        offsetEditText.setHorizontallyScrolling(false);
        lengthEditText.setHorizontallyScrolling(false);

        //handle Seek Operations
        handleSeekOperations();
        RFIDController.getInstance().updateTagIDs();
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);
        if (RFIDController.asciiMode == true) {
            tagIDField.setFilters(new InputFilter[]{filter});
            textRWData.setFilters(new InputFilter[]{filter});
        } else {
            tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});
            textRWData.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});

        }
        if (RFIDController.accessControlTag != null) {
            if (RFIDController.asciiMode == true)
                tagIDField.setText(hextoascii.convert(RFIDController.accessControlTag));
            else
                tagIDField.setText((RFIDController.accessControlTag));
            offsetEditText.setText("2");
        } else {
            offsetEditText.setText("0");
        }

        //
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        showAdvancedOptions = settings.getBoolean(Constants.ACCESS_ADV_OPTIONS, false);
        if(showAdvancedOptions) {
            rw_typespinner.setSelection(1);

        }
        UpdateViews();
    }

    private void UpdateViews() {
        LinearLayout advancedOptions = (LinearLayout) getActivity().findViewById(R.id.accessRWAdvanceOption);
        if (advancedOptions != null) {
            if (Application.rwAdvancedOptions ) {
                advancedOptions.setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.seperaterData).setVisibility(View.GONE);
            } else {
                advancedOptions.setVisibility(View.INVISIBLE);
                //getActivity().findViewById(R.id.seperaterData).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Method to initialize the seekbars
     */
    private void handleSeekOperations() {
        //offsetEditText.setFilters(new InputFilter[]{new InputFilterMax(Long.valueOf(Constants.MAX_OFFSET))});
        lengthEditText.setFilters(new InputFilter[]{new InputFilterMax(Long.valueOf(Constants.MAX_LEGTH))});
    }

    private void initializeSpinner() {
        Spinner memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.accessRWMemoryBank);
        if (memoryBankSpinner != null) {
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.acess_read_write_memory_bank_array, R.layout.custom_spinner_layout);
            // Specify the layout to use when the list of choices appears
            memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            memoryBankSpinner.setAdapter(memoryBankAdapter);
            //
            memoryBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            offsetEditText.setText("2"); // EPC
                            lengthEditText.setText("0");
                            break;
                        case 1:
                        case 2:
                            offsetEditText.setText("0"); // TID USER
                            lengthEditText.setText("0");
                            break;
                        case 4:
                            offsetEditText.setText("0"); // kill password
                            lengthEditText.setText("2");
                            break;
                        case 3: // access password
                            offsetEditText.setText("2");
                            lengthEditText.setText("2");
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(scanResultBroadcast);
    }

    public void handleTagResponse(final TagData response_tagData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (response_tagData != null) {
                    ACCESS_OPERATION_CODE readAccessOperation = response_tagData.getOpCode();
                    if (readAccessOperation != null) {
                        if (response_tagData.getOpStatus() != null && !response_tagData.getOpStatus().equals(ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)) {
                            String strErr = response_tagData.getOpStatus().toString().replaceAll("_", " ");
                            Toast.makeText(getActivity(), strErr.toLowerCase(), Toast.LENGTH_SHORT).show();
                        } else {
                            if (response_tagData.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ) {
                                TextView text = (TextView) getActivity().findViewById(R.id.accessRWData);
                                if (text != null) {
                                    text.setText(RFIDController.asciiMode == true ? hextoascii.convert(response_tagData.getMemoryBankData()) : response_tagData.getMemoryBankData());
                                }
                                Toast.makeText(getActivity(), R.string.msg_read_succeed, Toast.LENGTH_SHORT).show();
                                startbeepingTimer();
                            } else {
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.err_read_access_op_failed, Toast.LENGTH_SHORT).show();
                        Constants.logAsMessage(Constants.TYPE_DEBUG, "ACCESS READ", "memoryBankData is null");
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.err_access_op_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onUpdate() {
        if (isVisible() && tagIDField != null) {
            RFIDController.accessControlTag = tagIDField.getText().toString();
        }
    }

    @Override
    public void onRefresh() {
        if (RFIDController.accessControlTag != null && tagIDField != null) {
            tagIDField.setText(RFIDController.accessControlTag);
        }
    }

    public void startbeepingTimer() {
        if (RFIDController.beeperVolume != BEEPER_VOLUME.QUIET_BEEP) {
            if (!beepON) {
                beepON = true;
                beep();
                if (tbeep == null) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            stopbeepingTimer();
                            beepON = false;
                        }
                    };
                    tbeep = new Timer();
                    tbeep.schedule(task, BEEP_STOP_TIME);
                }
            }
        }
    }

    /**
     * method to stop timer
     */
    public void stopbeepingTimer() {
        if (tbeep != null) {
            if (RFIDController.toneGenerator != null)
                RFIDController.toneGenerator.stopTone();
            tbeep.cancel();
            tbeep.purge();
        }
        tbeep = null;
    }

    public void beep() {
        if (RFIDController.toneGenerator != null) {
            int toneType = ToneGenerator.TONE_PROP_BEEP;
            RFIDController.toneGenerator.startTone(toneType);
        }
    }

    private BroadcastReceiver scanResultBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action!=null && action.equals(getResources().getString(R.string.dw_action))) {
                displayScanResult(intent);
            }

        }
    };

    private void displayScanResult(Intent initiatingIntent) {
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        if (decodedData != null && tagIDField!=null) {
            tagIDField.setText(decodedData);
            tagIDField.setSelection(decodedData.length());
        }
    }
}
