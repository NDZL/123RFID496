package com.zebra.demo.scanner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.SCAN_BATCH_MODE;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_DATAVIEW_TAB;
//import com.zebra.demo.scanner.activities.ActiveScannerActivity;



/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    /* Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
            * @return A new instance of fragment AdvancedFragment.
            */
    private View settingsFragmentView;
    private static String TAG = "SettingsFragment";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_scan_settings, menu);
//        menu.findItem( R.id.action_scan).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                ((ActiveDeviceActivity) getActivity()).loadNextFragment(SCAN_DATAVIEW_TAB);
//                return true;
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settingsFragmentView  = inflater.inflate(R.layout.fragment_settings, container, false);
        SwitchCompat picklistMode = (SwitchCompat)settingsFragmentView.findViewById(R.id.switch_picklist_mode);
        final TextView txtPicklistMode = (TextView)settingsFragmentView.findViewById(R.id.txt_picklist_mode);
        if(picklistMode!=null) {

            int picklistInt = ((ActiveDeviceActivity) requireActivity()).getPickListMode();
            boolean picklistBool = false;
            if (picklistInt == 2) {
                picklistBool = true;
            }
            Log.i("PickListMode", "Setting "+picklistBool +" int value = "+picklistInt);
            picklistMode.setChecked(picklistBool);
            if(picklistBool){
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
            }else{
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
            }
            picklistMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int picklistInt = 0;
                    if(isChecked){
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
                        picklistInt = 2;
                    }else{
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
                    }
                    ((ActiveDeviceActivity) getActivity()).setPickListMode(picklistInt);
                }
            });
        }

        final TextView txtVibration = (TextView)settingsFragmentView.findViewById(R.id.vibration_feedback_text);
        TableRow tblRowFW = (TableRow)settingsFragmentView.findViewById(R.id.vibration_feedback_tbl_row);

        if(txtVibration!=null && tblRowFW!=null) {
            boolean isPagerMotorAvailable= ((ActiveDeviceActivity) requireActivity()).isPagerMotorAvailable();
            if(isPagerMotorAvailable) {
                tblRowFW.setClickable(true);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
            }else{
                tblRowFW.setClickable(false);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
            }
        }

        Spinner scanBatchModeSpinner = (Spinner) settingsFragmentView.findViewById(R.id.scan_batch_mode);
        ArrayAdapter<CharSequence> scanBatchModeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.scan_batch_mode, R.layout.custom_spinner_layout);
        scanBatchModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scanBatchModeSpinner.setAdapter(scanBatchModeAdapter);

        scanBatchModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (RFIDController.mConnectedReader != null && RFIDController.scanBatchMode != position) {
                    try {
                        RFIDResults rfidResults = RFIDController.mConnectedReader.Config.setScanBatchMode((SCAN_BATCH_MODE) SCAN_BATCH_MODE.GetBatchModeCodeValue(position));
                        if(rfidResults == RFIDResults.RFID_API_SUCCESS){
                            Toast.makeText(getActivity(), "Scan Batch Mode Applied", Toast.LENGTH_SHORT).show();
                            RFIDController.scanBatchMode = RFIDController.mConnectedReader.Config.getScanBatchModeConfig().getValue();
                        }else{
                            Toast.makeText(getActivity(), "Failed to Apply Scan Batch Mode", Toast.LENGTH_SHORT).show();
                        }
                    } catch (InvalidUsageException | OperationFailureException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (RFIDController.scanBatchMode != -1) {
            scanBatchModeSpinner.setSelection(RFIDController.scanBatchMode);
        }
//        SwitchCompat scanControl = (SwitchCompat)settingsFragmentView.findViewById(R.id.switch_scanning);
//        final TextView txtScanningControl = (TextView)settingsFragmentView.findViewById(R.id.txt_scanning_control);
//        if(scanControl!=null){
//            scanControl.setChecked(true);
//            scanControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if(isChecked) {
//                        ((ActiveDeviceActivity) getActivity()).enableScanning(settingsFragmentView);
//                        txtScanningControl.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
//                    }else{
//                        ((ActiveDeviceActivity) getActivity()).disableScanning(settingsFragmentView);
//                        txtScanningControl.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
//                    }
//                }
//            });
//        }
        return settingsFragmentView;
    }

    @Override
    public void onResume (){
        super.onResume();
    }

}
