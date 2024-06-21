package com.zebra.demo.rfidreader.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.USB_BATCH_MODE;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_ADVANCED_OPTIONS_TAB;

/**
 * A simple {@link androidx.core.app.Fragment} subclass.
 * <p/>
 * Use the {@link TagReportingFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle tag reporting operations and UI
 */
public class TagReportingFragment extends BackPressedFragment implements View.OnClickListener {
    private boolean pcChecked, pcfield;
    private boolean rssiChecked, rssifield;
    private boolean phaseChecked, phasefield;
    private boolean channelChecked, channelfield;
    private boolean seenCountChecked, seencountfield;
    private Spinner batchModeSpinner, usbbatchModeSpinner;
    private CheckBox beepOnUniqueTag, cb_ignorechk;
    private boolean uniqueTagChecked, uniqueTagReportfield;
    public static String TAG = "TagReportingFragment";

    private EditText brandidET, epcLenET;

    private boolean isBrandIdSettingsChanged;


    public TagReportingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagReportingFragment.
     */
    public static TagReportingFragment newInstance() {
        return new TagReportingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_reporting, container, false);
        CheckBox pc = (CheckBox) view.findViewById(R.id.incPC);
        pc.setOnClickListener(this);
        CheckBox rssi = (CheckBox) view.findViewById(R.id.incRSSI);
        rssi.setOnClickListener(this);
        CheckBox phase = (CheckBox) view.findViewById(R.id.incPhase);
        phase.setOnClickListener(this);
        CheckBox channelIndex = (CheckBox) view.findViewById(R.id.incChannel);
        channelIndex.setOnClickListener(this);
        CheckBox tagSeen = (CheckBox) view.findViewById(R.id.incTagSeen);
        tagSeen.setOnClickListener(this);
        beepOnUniqueTag = (CheckBox) view.findViewById(R.id.beepOnUniqueTag);
        beepOnUniqueTag.setOnClickListener(this);
        brandidET = view.findViewById(R.id.brandidET);
        epcLenET = view.findViewById(R.id.epcET);
        cb_ignorechk = view.findViewById(R.id.brandIDCheck);
        // loadBrandIdValues();
        brandidET.setText(Application.strBrandID);
        epcLenET.setText(Application.iBrandIDLen + "");
        brandidET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                //   setBrandIdValues();
                isBrandIdSettingsChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        epcLenET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // setBrandIdValues();
                isBrandIdSettingsChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        cb_ignorechk.setChecked(RFIDController.brandidcheckenabled);
        cb_ignorechk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //setBrandIdValues();
                isBrandIdSettingsChanged = true;
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadCheckBoxStates();
        batchModeSpinner = (Spinner) getActivity().findViewById(R.id.batchMode);
        usbbatchModeSpinner = (Spinner) getActivity().findViewById(R.id.batchModeusb);
        ArrayAdapter<CharSequence> batchModeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.batch_modes_array, R.layout.custom_spinner_layout);
        batchModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchModeSpinner.setAdapter(batchModeAdapter);
        ArrayAdapter<CharSequence> usbbatchModeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.usb_batch_modes_array, R.layout.custom_spinner_layout);
        if (RFIDController.mConnectedReader != null) {
            if (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD40P")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD40+")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD90+")) {


               batchModeSpinner.setEnabled(true);
               batchModeSpinner.setVisibility(View.VISIBLE);
            }// else
                if(!RFIDController.mConnectedReader.getHostName().startsWith("RFD8500") && !RFIDController.mConnectedReader.getHostName().startsWith("MC33")){
               // batchModeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.usb_batch_modes_array, R.layout.custom_spinner_layout);
               // batchModeSpinner.setEnabled(false);
               // batchModeSpinner.setVisibility(View.INVISIBLE);
                    usbbatchModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    usbbatchModeSpinner.setAdapter(usbbatchModeAdapter);


            } else {
                    usbbatchModeSpinner.setEnabled(false);
                    usbbatchModeSpinner.setVisibility(View.INVISIBLE);
                    RFIDController.usbBatchMode = -1;
            }
        } else {
            batchModeSpinner.setEnabled(false);
            batchModeSpinner.setVisibility(View.INVISIBLE);
            usbbatchModeSpinner.setEnabled(false);
            usbbatchModeSpinner.setVisibility(View.INVISIBLE);

        }
        if (RFIDController.batchMode != -1 ) {
            batchModeSpinner.setSelection(RFIDController.batchMode);
        }
        if (RFIDController.usbBatchMode != -1) {
            switch(RFIDController.usbBatchMode) {
                case 0:
                    usbbatchModeSpinner.setSelection(RFIDController.usbBatchMode);
                    break;
                case 1:
                    usbbatchModeSpinner.setSelection(RFIDController.usbBatchMode);
                    break;
                case 2:
                    usbbatchModeSpinner.setSelection(1);
                    break;
            }
        }
//        if (RFIDController.scanBatchMode != -1) {
//            //usbbatchModeSpinner.setSelection(RFIDController.scanBatchMode);
//            switch(RFIDController.scanBatchMode) {
//                case 0:
//                    usbbatchModeSpinner.setSelection(RFIDController.scanBatchMode);
//                    break;
//                case 1:
//                    usbbatchModeSpinner.setSelection(RFIDController.scanBatchMode);
//                    break;
//                case 2:
//                    usbbatchModeSpinner.setSelection(1);
//                    break;
//            }
//        }
        if (RFIDController.reportUniquetags != null) {
            if (RFIDController.reportUniquetags.getValue() == 1)
                beepOnUniqueTag.setChecked(true);
            else if (RFIDController.reportUniquetags.getValue() == 0)
                beepOnUniqueTag.setChecked(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Method to load the checkbox states
     */
    private void loadCheckBoxStates() {
        rssifield = phasefield = pcfield = channelfield = seencountfield = uniqueTagReportfield = false;
        if (RFIDController.tagStorageSettings != null) {
            TAG_FIELD[] tag_field = RFIDController.tagStorageSettings.getTagFields();
            for (int idx = 0; idx < tag_field.length; idx++) {
                if (tag_field[idx] == TAG_FIELD.PEAK_RSSI)
                    rssifield = true;
                if (tag_field[idx] == TAG_FIELD.PHASE_INFO)
                    phasefield = true;
                if (tag_field[idx] == TAG_FIELD.PC)
                    pcfield = true;
                if (tag_field[idx] == TAG_FIELD.CHANNEL_INDEX)
                    channelfield = true;
                if (tag_field[idx] == TAG_FIELD.TAG_SEEN_COUNT)
                    seencountfield = true;
            }
            ((CheckBox) getActivity().findViewById(R.id.incRSSI)).setChecked(rssifield);
            ((CheckBox) getActivity().findViewById(R.id.incPhase)).setChecked(phasefield);
            ((CheckBox) getActivity().findViewById(R.id.incPC)).setChecked(pcfield);
            ((CheckBox) getActivity().findViewById(R.id.incChannel)).setChecked(channelfield);
            ((CheckBox) getActivity().findViewById(R.id.incTagSeen)).setChecked(seencountfield);
        }
        if (RFIDController.reportUniquetags != null) {
            if (RFIDController.reportUniquetags != null && RFIDController.reportUniquetags.getValue() == 1)
                uniqueTagReportfield = true;
            ((CheckBox) getActivity().findViewById(R.id.beepOnUniqueTag)).setChecked(uniqueTagReportfield);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isSettingsChanged()) {
            //((SettingsDetailActivity) getActivity()).callBackPressed();
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
            if(getActivity() instanceof ActiveDeviceActivity)
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(RFID_ADVANCED_OPTIONS_TAB);

        }
    }

    private boolean isSettingsChanged() {
        boolean isSettingsChanged = false;
        TagStorageSettings tmpTagStorageSettings = null;
        Integer batchmode = null;
        Integer usbbatchmode = null;
        Boolean uniqueTagCheck = null;
        if (RFIDController.tagStorageSettings != null && (rssiChecked || phaseChecked || pcChecked || channelChecked || seenCountChecked)) {
            isSettingsChanged = true;
            try {
                tmpTagStorageSettings = RFIDController.mConnectedReader.Config.getTagStorageSettings();
            } catch (InvalidUsageException e) {
               Log.d(TAG,  "Returned SDK Exception");
            } catch (OperationFailureException e) {
               Log.d(TAG,  "Returned SDK Exception");
                if(getActivity() instanceof SettingsDetailActivity)
                    ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());
                if(getActivity() instanceof ActiveDeviceActivity)
                    ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + e.getVendorMessage());

                return false;
            }
            TAG_FIELD[] tag_fields = new TAG_FIELD[5];
            int index = 0;
            boolean incPC = ((CheckBox) getActivity().findViewById(R.id.incPC)).isChecked();
            boolean incRSSI = ((CheckBox)
                    getActivity().findViewById(R.id.incRSSI)).isChecked();
            boolean incPhase = ((CheckBox) getActivity().findViewById(R.id.incPhase)).isChecked();
            boolean incChannel = ((CheckBox) getActivity().findViewById(R.id.incChannel)).isChecked();
            boolean incTagSeen = ((CheckBox) getActivity().findViewById(R.id.incTagSeen)).isChecked();
            if (incRSSI)
                tag_fields[index++] = TAG_FIELD.PEAK_RSSI;
            if (incPhase)
                tag_fields[index++] = TAG_FIELD.PHASE_INFO;
            if (incPC)
                tag_fields[index++] = TAG_FIELD.PC;
            if (incChannel)
                tag_fields[index++] = TAG_FIELD.CHANNEL_INDEX;
            if (incTagSeen)
                tag_fields[index] = TAG_FIELD.TAG_SEEN_COUNT;
            tmpTagStorageSettings.setTagFields(tag_fields);
        }
        if (RFIDController.batchMode != -1 && RFIDController.batchMode != batchModeSpinner.getSelectedItemPosition()) {
            if (RFIDController.mConnectedReader != null && (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD90")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD40"))) {
                isSettingsChanged = true;
                batchmode = batchModeSpinner.getSelectedItemPosition();
            }
        }
        if(RFIDController.usbBatchMode != -1 && RFIDController.usbBatchMode != usbbatchModeSpinner.getSelectedItemPosition()) {
            if (RFIDController.mConnectedReader != null && (RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")
                    ||RFIDController.mConnectedReader.getHostName().startsWith("RFD90")
                    || RFIDController.mConnectedReader.getHostName().startsWith("RFD40"))) {
                isSettingsChanged = true;
                usbbatchmode = usbbatchModeSpinner.getSelectedItemPosition();
            }
        }
        if (RFIDController.reportUniquetags != null && uniqueTagChecked) {
            isSettingsChanged = true;
            uniqueTagCheck = beepOnUniqueTag.isChecked();
        }
        if (isSettingsChanged || isBrandIdSettingsChanged)
            new Task_SaveTagReportingConfiguration(tmpTagStorageSettings, batchmode, uniqueTagCheck, usbbatchmode).execute();
        return isSettingsChanged || isBrandIdSettingsChanged;
    }

    @Override
    public void onClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        if (RFIDController.tagStorageSettings != null) {
            if (checkBox.getId() == R.id.incRSSI) {
                rssiChecked = !(rssifield && checkBox.isChecked() || (!rssifield && !checkBox.isChecked()));
            } else if (checkBox.getId() == R.id.incPhase) {
                phaseChecked = !(phasefield && checkBox.isChecked() || (!phasefield && !checkBox.isChecked()));
            } else if (checkBox.getId() == R.id.incPC) {
                pcChecked = !(pcfield && checkBox.isChecked() || (!pcfield && !checkBox.isChecked()));
            } else if (checkBox.getId() == R.id.incChannel) {
                channelChecked = !(channelfield && checkBox.isChecked() || (!channelfield && !checkBox.isChecked()));
            } else if (checkBox.getId() == R.id.incTagSeen) {
                seenCountChecked = !(seencountfield && checkBox.isChecked() || (!seencountfield && !checkBox.isChecked()));
            }
        }
        if (RFIDController.reportUniquetags != null) {
            if (checkBox.getId() == R.id.beepOnUniqueTag) {
                uniqueTagChecked = !(uniqueTagReportfield && checkBox.isChecked() || (!uniqueTagReportfield && !checkBox.isChecked()));
            }
        }
    }

    private class Task_SaveTagReportingConfiguration extends AsyncTask<Void, Void, Boolean> {
        private final TagStorageSettings fnTagStorageSettings;
        private final Integer fnbatchmodepos;
        private final Boolean uniqueTagReport;
        private CustomProgressDialog progressDialog;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private final Integer usbfnbatchmodepos;

        public Task_SaveTagReportingConfiguration(TagStorageSettings tagStorageSettings, Integer batchmodepos, Boolean uniqueTagReport, Integer usbfnbatchmodepos) {
            this.fnTagStorageSettings = tagStorageSettings;
            this.fnbatchmodepos = batchmodepos;
            this.uniqueTagReport = uniqueTagReport;
            this.usbfnbatchmodepos = usbfnbatchmodepos;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.tag_reporting_progress_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean bResult = true;
            try {
                if (fnTagStorageSettings != null) {
                    RFIDController.mConnectedReader.Config.setTagStorageSettings(fnTagStorageSettings);
                    RFIDController.tagStorageSettings = fnTagStorageSettings;
                }
                if (fnbatchmodepos != null && RFIDController.mConnectedReader != null) {
                    RFIDController.mConnectedReader.Config.setBatchMode((BATCH_MODE) BATCH_MODE.GetBatchModeCodeValue(fnbatchmodepos));
                    RFIDController.batchMode = RFIDController.mConnectedReader.Config.getBatchModeConfig().getValue();
                }
                if(usbfnbatchmodepos != null && RFIDController.mConnectedReader != null) {
                    RFIDController.mConnectedReader.Config.setUsbBatchMode((USB_BATCH_MODE) USB_BATCH_MODE.GetBatchModeCodeValue(usbfnbatchmodepos));
                    RFIDController.usbBatchMode = RFIDController.mConnectedReader.Config.getUsbBatchModeConfig().getValue();
                }
                if (uniqueTagReport != null) {
                    RFIDController.mConnectedReader.Config.setUniqueTagReport(uniqueTagReport);
                    RFIDController.reportUniquetags = RFIDController.mConnectedReader.Config.getUniqueTagReport();
                }
                if (isBrandIdSettingsChanged)
                    bResult = setBrandIdValues();

            } catch (InvalidUsageException e) {
               Log.d(TAG,  "Returned SDK Exception");
                invalidUsageException = e;
                bResult = false;
            } catch (OperationFailureException e) {
                Log.d(TAG,  "Returned SDK Exception");
                operationFailureException = e;
                bResult = false;
            }
            return bResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            if (!result) {
                if (invalidUsageException != null) {
                    if(getActivity() instanceof SettingsDetailActivity)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                    if(getActivity() instanceof ActiveDeviceActivity)
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());

                }
                else if (operationFailureException != null) {
                    if(getActivity() instanceof SettingsDetailActivity)
                        ((SettingsDetailActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
                    if(getActivity() instanceof ActiveDeviceActivity)
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());

                }
                else
                    Toast.makeText(getContext(), getContext().
                            getString(R.string.failed_settings), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
            AdvancedOptionItemFragment fragment = AdvancedOptionItemFragment.newInstance();
            replaceFragment(getFragmentManager(), fragment, R.id.settings_content_frame);
            if(getActivity() instanceof ActiveDeviceActivity)
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(RFID_ADVANCED_OPTIONS_TAB);
        }
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadCheckBoxStates();
                if (RFIDController.batchMode != -1) {
                    batchModeSpinner.setSelection(RFIDController.batchMode);
                }
                if(RFIDController.usbBatchMode != -1) {
                    usbbatchModeSpinner.setSelection(RFIDController.usbBatchMode);
                }
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((CheckBox) getActivity().findViewById(R.id.incRSSI)).setChecked(false);
                ((CheckBox) getActivity().findViewById(R.id.incPhase)).setChecked(false);
                ((CheckBox) getActivity().findViewById(R.id.incPC)).setChecked(false);
                ((CheckBox) getActivity().findViewById(R.id.incChannel)).setChecked(false);
                ((CheckBox) getActivity().findViewById(R.id.incTagSeen)).setChecked(false);
                ((CheckBox) getActivity().findViewById(R.id.beepOnUniqueTag)).setChecked(false);
                batchModeSpinner.setSelection(1);
                usbbatchModeSpinner.setSelection(0);
            }
        });
    }

    public static void replaceFragment(@NonNull FragmentManager fragmentManager,
                                       @NonNull Fragment fragment, int frameId) {
       // FragmentTransaction transaction = fragmentManager.beginTransaction();
       // transaction.replace(frameId, fragment);
       // transaction.disallowAddToBackStack();
       // transaction.commit();
    }

    public boolean setBrandIdValues() {
        String brandiD = brandidET.getText().toString();
        String epcLen = epcLenET.getText().toString();
        int epcLength = epcLen.length() != 0 ? Integer.parseInt(epcLen) : 0;
        if (brandiD.length() == 0) {
            return false;
        } else if (epcLength > 255) {
            return false;

        } else {
            Application.strBrandID = brandiD;
            Application.iBrandIDLen = epcLength;
            RFIDController.brandidcheckenabled = cb_ignorechk.isChecked();
            Application.strBrandIDLogo = -1;
            RFIDController.bFound = false;
            Application.iUpdateLogo = 0;
            saveValue(Application.strBrandID, Application.iBrandIDLen, RFIDController.brandidcheckenabled);
            return true;
        }
        //Toast.makeText(getContext(), brandiD + "   " + epcLen, Toast.LENGTH_SHORT).show();
    }


    public void saveValue(String brandID, int epcLen, boolean isBrandcheck) {
        SharedPreferences pref = getContext().getSharedPreferences("BrandIdValues", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(ActiveDeviceActivity.BRAND_ID, brandID);
        editor.putInt(ActiveDeviceActivity.EPC_LEN, epcLen);
        editor.putBoolean(ActiveDeviceActivity.IS_BRANDID_CHECK, isBrandcheck);
        editor.commit();

    }

   /* public void loadBrandIdValues(){
        SharedPreferences pref = getContext().getSharedPreferences("BrandIdValues", 0);

        RFIDController.strBrandID = pref.getString(MainActivity.BRAND_ID, "AAAA"); // getting String
        RFIDController.iBrandIDLen =  pref.getInt(MainActivity.EPC_LEN, 12); // getting Integer
        RFIDController.brandidcheckenabled =pref.getBoolean(MainActivity.IS_BRANDID_CHECK, false);
    }*/

}

