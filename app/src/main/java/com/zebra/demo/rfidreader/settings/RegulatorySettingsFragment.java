package com.zebra.demo.rfidreader.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.application.Application;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderCapabilities;
import com.zebra.rfid.api3.RegionInfo;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import java.util.ArrayList;
import java.util.Collections;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_RFID_SETTINGS_TAB;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link RegulatorySettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle Regulatory Settings operations and UI.
 */
public class RegulatorySettingsFragment extends BackPressedFragment implements Spinner.OnItemSelectedListener,
        View.OnClickListener {
    public static String TAG = "RegulatorySettingsFragment";
    private ArrayAdapter<String> currentRegionAdapter;
    private LinearLayout scrollView;
    private Spinner currentRegionSpinner;
    private boolean isselectedChannelsChanged = false;

    private String selectedChannels;
    private boolean hoppingConfigurable;
    private ArrayList<String> supportedRegions = new ArrayList<>();
    private ArrayList<String> supportedRegionDetails = new ArrayList<>();
    private RegionInfo selectedRegionInfo = null;

    public RegulatorySettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RegulatorySettingsFragment.
     */
    public static RegulatorySettingsFragment newInstance() {
        return new RegulatorySettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_regulatory_settings, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scrollView = ((LinearLayout) getActivity().findViewById(R.id.regChannelCheckBoxes));
//        actionBar.setTitle(R.string.title_activity_regulatory_settings);
        currentRegionSpinner = (Spinner) getActivity().findViewById(R.id.currentRegionSpinner);
        UpdateSupportedRegions();

        ReaderCapabilities readerCapabilities = null;
        if (RFIDController.mConnectedReader != null)
            readerCapabilities = RFIDController.mConnectedReader.ReaderCapabilities;
        // Create an ArrayAdapter using the string array and a default spinner layout
        currentRegionAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, supportedRegionDetails);
        // Specify the layout to use when the list of choices appears
        currentRegionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        currentRegionSpinner.setAdapter(currentRegionAdapter);
        if (RFIDController.mConnectedReader != null && RFIDController.regulatory == null && !RFIDController.mIsInventoryRunning) {
            try {
                RFIDController.regulatory = RFIDController.mConnectedReader.Config.getRegulatoryConfig();
            } catch (InvalidUsageException e) {
               Log.d(TAG,  "Returned SDK Exception");
            } catch (OperationFailureException e) {
               Log.d(TAG,  "Returned SDK Exception");
            }
        }
        //To handle unwanted call back, when we visit the fragment from elsewhere
        //
        if (readerCapabilities != null && RFIDController.regulatory != null && RFIDController.regulatory.getRegion() != null /*&& !RFIDController.regulatorySettings.getregion().isEmpty()*/) {
            currentRegionSpinner.setSelection(supportedRegions.indexOf(RFIDController.regulatory.getRegion()), false);
            if (RFIDController.regulatory.getRegion() != null && !RFIDController.regulatory.getRegion().equalsIgnoreCase("NA")/*&& RFIDController.regulatoryConfigResponse.RegionCode != null && RFIDController.regulatoryConfigResponse.RegionCode.equalsIgnoreCase(RFIDController.regulatorySettings.getregion())*/) {
                selectedChannels = "";
                //selectedRegionInfo = RFIDController.mConnectedReader.ReaderCapabilities.SupportedRegions.getRegionInfo(regionNotSet.indexOf(RFIDController.regulatory.getRegion()));
                // get default channel list
                RegionInfo regionInfo = RFIDController.mConnectedReader.ReaderCapabilities.SupportedRegions.getRegionInfo(supportedRegions.indexOf(RFIDController.regulatory.getRegion()));
                try {
                    selectedRegionInfo = RFIDController.mConnectedReader.Config.getRegionInfo(regionInfo);
                    setSelectedChannels();
                } catch (InvalidUsageException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
            } else if (getActivity() != null && currentRegionSpinner.getSelectedItem() != null) {
                //((SettingsDetailActivity) getActivity()).getRegionDetails(currentRegionSpinner.getSelectedItem().toString());
                selectedChannels = "";
                try {
                    selectedRegionInfo = RFIDController.mConnectedReader.Config.getRegionInfo(RFIDController.mConnectedReader.ReaderCapabilities.SupportedRegions.getRegionInfo(0));
                    setSelectedChannels();
                } catch (InvalidUsageException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
            }
        }
        currentRegionSpinner.setOnItemSelectedListener(this);
        if(SettingsDetailActivity.mSettingOnFactory == false) {
            Button button = getActivity().findViewById(R.id.regulatoryButton);
            button.setVisibility(View.GONE);
            TextView tv = getActivity().findViewById(R.id.selectCountryWarningText);
            tv.setGravity(Gravity.CENTER);

        }
    }

    private void UpdateSupportedRegions() {
        ReaderCapabilities readerCapabilities = null;
        if (RFIDController.mConnectedReader != null)
            readerCapabilities = RFIDController.mConnectedReader.ReaderCapabilities;
        if (readerCapabilities != null && (RFIDController.mConnectedReader.isCapabilitiesReceived() || RFIDController.regionNotSet == true)) {
            supportedRegions.clear();
            supportedRegionDetails.clear();
            for (int idx = 0; idx < readerCapabilities.SupportedRegions.length(); idx++) {
                supportedRegionDetails.add(readerCapabilities.SupportedRegions.getRegionInfo(idx).getName() + " (" + readerCapabilities.SupportedRegions.getRegionInfo(idx).getRegionCode() + ")");
                supportedRegions.add(readerCapabilities.SupportedRegions.getRegionInfo(idx).getRegionCode());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /* @Override
     public void handleStatusResponse(final Response_Status statusData) {
         getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 if (statusData.command.trim().equalsIgnoreCase(Constants.COMMAND_REGULATORY)) {
                     if (statusData.Status.trim().equalsIgnoreCase("OK")) {
                         ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
                     } else
                         ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + statusData.Status);

                     ((SettingsDetailActivity) getActivity()).callBackPressed();
                 }
             }
         });
     }
 */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

        if(RFIDController.mIsInventoryRunning == true )
        {
            Toast.makeText(getContext(), "operation in progress..Cannot change regulatory settings", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentRegionSpinner != null && currentRegionSpinner.getSelectedItem() != null) {
            scrollView.removeAllViews();

            //  ((SettingsDetailActivity) getActivity()).getRegionDetails(currentRegionSpinner.getSelectedItem().toString());
            RegionInfo regionInfo = RFIDController.mConnectedReader.ReaderCapabilities.SupportedRegions.getRegionInfo(pos);
            try {
                selectedRegionInfo = RFIDController.mConnectedReader.Config.getRegionInfo(regionInfo);
            } catch (InvalidUsageException e) {
               Log.d(TAG,  "Returned SDK Exception");
            } catch (OperationFailureException e) {
               Log.d(TAG,  "Returned SDK Exception");
            }
            setSelectedChannels();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //DO Nothing
    }
    //@Override
/*    public void handleRegulatoryConfigResponse(final Response_RegulatoryConfig statusData) {
        if (statusData != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setSelectedChannels();
                    } catch (Exception e) {
                        Constants.logAsMessage(Constants.TYPE_ERROR, "RegulatorySettings", e.getMessage());
                    }
                }
            });
        }
    }*/

    @Override
    public void onBackPressed() {

        if(SettingsDetailActivity.mSettingOnFactory == false)
            setRegulatory();

    }



    public void setRegulatory() {

        selectedChannels = "";
        if (currentRegionSpinner.getSelectedItem() != null) {
            if (scrollView.getChildCount() > 0) {
                for (int i = 0; i < scrollView.getChildCount(); i++) {
                    if (scrollView.getChildAt(i) instanceof CheckBox) {
                        CheckBox c = (CheckBox) scrollView.getChildAt(i);
                        if (c.isChecked())
                            selectedChannels = selectedChannels + c.getText() + " ";
                    }
                }
                selectedChannels = selectedChannels.trim();
                selectedChannels = selectedChannels.replaceAll(" ", ",");
                if (RFIDController.regulatory == null)
                    isselectedChannelsChanged = true;
                else if (!supportedRegions.get(currentRegionSpinner.getSelectedItemPosition()).toString().equalsIgnoreCase(RFIDController.regulatory.getRegion()))
                    isselectedChannelsChanged = true;
                else if (RFIDController.regulatory.getEnabledchannels() != null) {
                    if (RFIDController.regulatory.getEnabledchannels().length != selectedChannels.split(",").length)
                        isselectedChannelsChanged = true;
                    else {
                        ArrayList<String> enabledChannels = new ArrayList<>();
                        Collections.addAll(enabledChannels, RFIDController.regulatory.getEnabledchannels());
                        for (String s : selectedChannels.split(",")) {
                            if (!enabledChannels.contains(s)) {
                                isselectedChannelsChanged = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (currentRegionSpinner.getSelectedItem() != null && isselectedChannelsChanged) {
            if(currentRegionSpinner.getSelectedItem().toString().contains("Ukraine-License")) {
                AlertDialog builder = new AlertDialog.Builder(getContext()).setCancelable(false)
                        .setTitle(R.string.warning_title_ukrain_l).setMessage(R.string.warning_text_ukrain_l)
                        .setPositiveButton("Have License", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Task_SaveRegionConfiguration().execute();
                                dialog.cancel();
                            }
                        }).setNegativeButton("No License", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), getResources().getString(R.string.status_failure_message), Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                //((SettingsDetailActivity) getActivity()).callBackPressed();
                                if(!getActivity().isFinishing())
                                    getActivity().finish();
                            }
                        }).create();
                builder.show();
            } else {
                new Task_SaveRegionConfiguration().execute();
            }
        } else {
            if(getActivity() instanceof ActiveDeviceActivity)
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
            else if(getActivity() instanceof SettingsDetailActivity) {
                ((SettingsDetailActivity) getActivity()).callBackPressed();
                if (!getActivity().isFinishing())
                    getActivity().finish();
            }


        }
    }

    @Override
    public void onClick(View view) {
    }

    /**
     * method to set selected Channels for the region on the screen
     */
    public void setSelectedChannels() {
        if (selectedRegionInfo != null) {
            scrollView.removeAllViews();
            hoppingConfigurable = selectedRegionInfo.isHoppingConfigurable();
            String[] channels = selectedRegionInfo.getSupportedChannels();
            if (hoppingConfigurable) {
                ArrayList<String> enabledChannels = new ArrayList<>();
                if (RFIDController.regulatory != null && RFIDController.regulatory.getEnabledchannels() != null
                        && RFIDController.regulatory.getRegion().equalsIgnoreCase(selectedRegionInfo.getRegionCode())) {
                    Collections.addAll(enabledChannels, RFIDController.regulatory.getEnabledchannels());
                }

                if (channels.length > 0) {
                    for (String s : channels) {
                        CheckBox checkBox = new CheckBox(getActivity());
                        checkBox.setText(s);
                        checkBox.setEnabled(hoppingConfigurable);
                        if (enabledChannels != null && enabledChannels.contains(s)) {
                            checkBox.setChecked(true);
                        }
                        checkBox.setOnClickListener(RegulatorySettingsFragment.this);
                        scrollView.addView(checkBox);
                    }
                }
            } else {
                if (channels.length > 0) {
                    for (String s : channels) {
                        CheckBox checkBox = new CheckBox(getActivity());
                        checkBox.setText(s);
                        checkBox.setEnabled(hoppingConfigurable);
                        checkBox.setChecked(true);
                        checkBox.setOnClickListener(RegulatorySettingsFragment.this);
                        scrollView.addView(checkBox);
                    }
                }
            }
        }
    }

    private class Task_SaveRegionConfiguration extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progressDialog;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private final RegulatoryConfig regulatoryConfig;

        public Task_SaveRegionConfiguration() {
            regulatoryConfig = new RegulatoryConfig();
            regulatoryConfig.setRegion(selectedRegionInfo.getRegionCode());
            regulatoryConfig.setEnabledChannels(selectedChannels.split(","));
            if (selectedRegionInfo.isHoppingConfigurable())
                regulatoryConfig.setIsHoppingOn(true);
        }

        @Override
        protected void onPreExecute() {
            if (getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing()) {
                return;
            }
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.regulatory_progress_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                RFIDController.mConnectedReader.Config.setRegulatoryConfig(regulatoryConfig);
                RFIDController.regulatory = regulatoryConfig;
                return true;
            } catch (InvalidUsageException e) {
               Log.d(TAG,  "Returned SDK Exception");
                invalidUsageException = e;
            } catch (OperationFailureException e) {
               Log.d(TAG,  "Returned SDK Exception");
                operationFailureException = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //if (getActivity() == null) return;
            //if (getActivity().isDestroyed() || getActivity().isFinishing()) return;
            if(progressDialog != null){
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }


            if (result) {
                if(getActivity() instanceof SettingsDetailActivity)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
                else if(getActivity() instanceof ActiveDeviceActivity)
                    ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
                try {
                    // update trigger in case of no region set scenario
                    if (RFIDController.settings_startTrigger == null) {
                        RFIDController.getInstance().updateReaderConnection(true);
                    } else
                        RFIDController.getInstance().updateReaderConnection(false);
                } catch (InvalidUsageException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
            } else {
                if (invalidUsageException != null) {
                    if(getActivity() instanceof SettingsDetailActivity)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                    else if(getActivity() instanceof ActiveDeviceActivity)
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                }
                if (operationFailureException != null) {
                    if(getActivity() instanceof SettingsDetailActivity)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
                    else if(getActivity() instanceof ActiveDeviceActivity)
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(getActivity() instanceof SettingsDetailActivity)
                            Toast.makeText(((SettingsDetailActivity)getActivity()), R.string.status_failure_message, Toast.LENGTH_SHORT).show();
                        else if(getActivity() instanceof ActiveDeviceActivity)
                            Toast.makeText(((ActiveDeviceActivity)getActivity()), R.string.status_failure_message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if (invalidUsageException == null && operationFailureException == null) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
                    }
                });

                if(getActivity() instanceof SettingsDetailActivity) {
                    if (SettingsDetailActivity.mSettingOnFactory == true) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.SETTING_ITEM_ID, R.id.profiles);
                        ((SettingsDetailActivity) getActivity()).startFragment(intent);
                    }
                }else if(getActivity() instanceof ActiveDeviceActivity) {
                    //((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
                }

            }


            if(getActivity() instanceof ActiveDeviceActivity) {
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
            }
            //((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }


    /**
     * method to get channels for the selected region after device got connected in case reconnection
     */
    public void deviceConnected() {
/*        if (getActivity() != null && currentRegionSpinner.getSelectedItem() != null)
            ((SettingsDetailActivity) getActivity()).getRegionDetails(currentRegionSpinner.getSelectedItem().toString());*/
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UpdateSupportedRegions();
                // Create an ArrayAdapter using the string array and a default spinner layout
                currentRegionAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, supportedRegionDetails);
                // Specify the layout to use when the list of choices appears
                currentRegionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                currentRegionSpinner.setAdapter(currentRegionAdapter);
                if (RFIDController.mConnectedReader != null) {
                    try {
                        RFIDController.regulatory = RFIDController.mConnectedReader.Config.getRegulatoryConfig();
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }
                if (RFIDController.regulatory.getRegion() != null /*&& !RFIDController.regulatorySettings.getregion().isEmpty()*/)
                    currentRegionSpinner.setSelection(supportedRegions.indexOf(RFIDController.regulatory.getRegion()), false);
                currentRegionAdapter.notifyDataSetChanged();
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //if (RFIDController.regulatoryConfigResponse.SupportedChannels != null)
                {
                    scrollView.removeAllViews();
                }
                currentRegionSpinner.setAdapter(null);
                if(getActivity() instanceof ActiveDeviceActivity)
                    ((ActiveDeviceActivity)getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("","");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("","");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("","");
    }
}
