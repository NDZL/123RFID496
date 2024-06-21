package com.zebra.demo.rfidreader.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.zebra.rfid.api3.ENUM_BLUETOOTH_MODE;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.sgtinMode;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link ApplicationSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the Connection Settings UI
 */
public class ApplicationSettingsFragment extends Fragment {

    public static String TAG = "ApplicationSettingsFragment";
    private static final int TAGLIST_MATCH_MODE_IMPORT = 111;
    private CheckBox autoReconnectReaders;
    private CheckBox readerConnection;
    private CheckBox readerBattery;
    private CheckBox exportData;
    public CheckBox tagListMatchMode;
    private CheckBox tagListMatchTagNames;
    private CheckBox asciiMode;
    private CheckBox sgtin96Mode;
    private SharedPreferences settings;
    File cacheMatchModeTagFile = null;
    private static TextView KeyMapTextView;
    private TextView bluetoothModeTextView;
    private Spinner bluetoothMode;
    private int bluetoothModePosition=0;
    private boolean TaglistmatchmodeChecked = false;
    public ApplicationSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConnectionSettingsFragment.
     */
    public static ApplicationSettingsFragment newInstance() {
        return new ApplicationSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheMatchModeTagFile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_TAGLIST_MATCH_MODE_FILE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  =  inflater.inflate(R.layout.fragment_connection_settings, container, false);
        return view;

    }

    public static void SetSpinnerText(String value){
        if(KeyMapTextView!=null && value !=null) {
            KeyMapTextView.setText(value);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onResume() {
        super.onResume();
        loadCheckBoxStates();
        if(getActivity() != null )
            ((ActiveDeviceActivity)getActivity()).disableScanner();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeViews();
    }

    private void initializeViews() {
        autoReconnectReaders = ((CheckBox) getActivity().findViewById(R.id.autoReconnectReaders));
        readerConnection = ((CheckBox) getActivity().findViewById(R.id.readerConnection));
        readerBattery = ((CheckBox) getActivity().findViewById(R.id.readerBattery));
        exportData = ((CheckBox) getActivity().findViewById(R.id.exportData));
        tagListMatchMode = ((CheckBox) getActivity().findViewById(R.id.tagListMatchMode));
        tagListMatchTagNames = ((CheckBox) getActivity().findViewById(R.id.tagListMatchTagNames));
        asciiMode = ((CheckBox) getActivity().findViewById(R.id.asciiMode));
        sgtin96Mode = ((CheckBox) getActivity().findViewById(R.id.sgtinMode));
        bluetoothMode = ((Spinner) getActivity().findViewById(R.id.spinnerbluetoothmode));
        bluetoothModeTextView = ((TextView) getActivity().findViewById(R.id.BluetoothmodeText));


        if (RFIDController.mIsInventoryRunning || RFIDController.isLocatingTag) {
            //tagListMatchMode.setEnabled(false);
            tagListMatchTagNames.setEnabled(false);
        }
        loadCheckBoxStates();

        tagListMatchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (RFIDController.mIsInventoryRunning || RFIDController.isLocatingTag) {
                    if(getActivity() != null )
                        Toast.makeText(getActivity(), "Operation in Progress", Toast.LENGTH_SHORT).show();

                    tagListMatchMode.setChecked(!tagListMatchMode.isChecked());
                    return;
                }
                if (isChecked) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TaglistmatchmodeChecked = true;
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("text/*");
                            Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
                            intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activityResultLauncher.launch(intent);


/*
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("text/*");
                             startActivityForResult(Intent.createChooser(intent, "ChooseFile to upload"), TAGLIST_MATCH_MODE_IMPORT);*/
                        }
                    });


                } else {
                    // clear friendly names
//                    tagListMatchTagNames.setChecked(false);
                    TaglistmatchmodeChecked = false;
                    tagListMatchTagNames.setEnabled(false);
                }
                RFIDController.getInstance().clearInventoryData();
            }
        });
        tagListMatchTagNames.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //if(isChecked){
//                Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
            tagListMatchTagNames.setChecked(isChecked);
            //}
        });

        //   asciiMode.setOnCheckedChangeListener((buttonView, isChecked) -> RFIDController.asciiMode = isChecked);
        //  sgtin96Mode.setOnCheckedChangeListener((buttonView, isChecked) -> RFIDController.sgtinMode = isChecked);
        asciiMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                RFIDController.asciiMode = b;
                if(asciiMode.isChecked() && sgtin96Mode.isChecked())
                    RFIDController.sgtinMode = false;
                sgtin96Mode.setChecked(RFIDController.sgtinMode);
            }
        });
        sgtin96Mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                RFIDController.sgtinMode = b;
                if(asciiMode.isChecked() && sgtin96Mode.isChecked())
                    RFIDController.asciiMode = false;
                asciiMode.setChecked(RFIDController.asciiMode);
            }
        });
        boolean readerType = (RFIDController.mConnectedReader != null )?RFIDController.mConnectedReader.getHostName().startsWith("RFD40"):false;

        /*Adding spinner for bluetooth mode selection*/
        List<String> categories = new ArrayList<String>();
        categories.add("CDC Mode");
        categories.add("HID Mode");

        // Creating adapter for spinner
        ArrayAdapter<String> BluetoothModeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
        BluetoothModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothMode.setAdapter(BluetoothModeAdapter);
        /*TODO set the spinner position based on sled configuration*/

        bluetoothMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                try {
                    if (mConnectedReader == null) return;
                    switch (position) {
                        case 0:
                            mConnectedReader.Config.setBluetoothMode(ENUM_BLUETOOTH_MODE.BLUETOOTH_CDCMODE);
                            break;
                        case 1:
                            mConnectedReader.Config.setBluetoothMode(ENUM_BLUETOOTH_MODE.BLUETOOTH_HIDMODE);
                            break;
                    }
                } catch (InvalidUsageException e) {
                    Log.d(TAG,  "Returned SDK Exception");
                } catch (OperationFailureException e) {
                    Log.d(TAG,  "Returned SDK Exception");
                }
                bluetoothModePosition=position;
                bluetoothModeTextView.setText(bluetoothMode.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }

        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        storeCheckBoxesStatus();
    }

  /*  public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO Auto-generated method stub
        if (cacheMatchModeTagFile.exists()) {
            cacheMatchModeTagFile.delete();
        }
        if (resultCode == RESULT_OK && requestCode == TAGLIST_MATCH_MODE_IMPORT) {
            Uri uri = data.getData();
            if (data == null) {
                return;
            }
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(cacheMatchModeTagFile);
                Log.d("size", in.toString());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG,  "Returned SDK Exception");
            } catch (Exception e) {
                Log.d(TAG,  "Returned SDK Exception");
            }
            if (!cacheMatchModeTagFile.exists()) {
                Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
                tagListMatchMode.setChecked(false);
            } else {
                tagListMatchTagNames.setEnabled(true);
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            }
        }
        // if( getActivity().isFinishing() == false ){
        loadCheckBoxStates();
        storeCheckBoxesStatus();
        //  getActivity().finish();
        // }
    }*/


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (cacheMatchModeTagFile.exists()) {
                        cacheMatchModeTagFile.delete();
                    }
                    if(result.getResultCode() == Activity.RESULT_CANCELED){
                        TaglistmatchmodeChecked = false;
                    }

                    if (result.getResultCode() == RESULT_OK ) {
                        Intent data = result.getData();
                        Uri documentUri =  data.getData();
                        if( data == null) {
                            //   Toast.makeText(getActivity(),"No File selected  ",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (data != null) {
                            if (data.getData().toString().contains("content://com.android.providers")) {
                                getActivity().runOnUiThread(this::ShowPlugInPathChangeDialog);
                            }

                            else{
                                try {
                                    InputStream in = getActivity().getContentResolver().openInputStream(documentUri);
                                    OutputStream out = new FileOutputStream(cacheMatchModeTagFile);
                                    Log.d("size", in.toString());
                                    byte[] buf = new byte[1024];
                                    int len;
                                    while ((len = in.read(buf)) > 0) {
                                        out.write(buf, 0, len);
                                    }
                                    out.close();
                                    in.close();
                                } catch (FileNotFoundException e) {
                                    Log.d(TAG, "Returned SDK Exception");
                                } catch (Exception e) {
                                    Log.d(TAG, "Returned SDK Exception");
                                }
                                if (!cacheMatchModeTagFile.exists()) {
                                    Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
                                    tagListMatchMode.setChecked(false);
                                } else {
                                    //  tagListMatchMode.setChecked(true);
                                    tagListMatchTagNames.setEnabled(true);
                                    Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                private void ShowPlugInPathChangeDialog() {
                    if (!getActivity().isFinishing()) {
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_taglistmatchmode_path);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
                        declineButton.setOnClickListener(v -> dialog.dismiss());
                    }
                }

            });


    /**
     * Method to load the checkbox states
     */
    private void loadCheckBoxStates() {
        settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        autoReconnectReaders.setChecked(settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true));
        readerConnection.setChecked(settings.getBoolean(Constants.NOTIFY_READER_CONNECTION, false));
        readerBattery.setChecked(settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true));
//        if (Build.MODEL.contains("MC33"))
//            readerBattery.setChecked(settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, false));
//        else
//            readerBattery.setChecked(settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true));
        exportData.setChecked(settings.getBoolean(Constants.EXPORT_DATA, false));
        tagListMatchMode.setChecked(settings.getBoolean(Constants.TAG_LIST_MATCH_MODE, false));

        if (tagListMatchMode.isChecked()) {
            if (!cacheMatchModeTagFile.exists()) {
                tagListMatchMode.setChecked(false);
            }
        }

        if (!tagListMatchMode.isChecked()) {
            tagListMatchTagNames.setEnabled(false);
        } else
            tagListMatchTagNames.setChecked(settings.getBoolean(Constants.SHOW_CSV_TAG_NAMES, false));

        RFIDController.asciiMode = settings.getBoolean(Constants.ASCII_MODE, false);
        asciiMode.setChecked(RFIDController.asciiMode);
        RFIDController.sgtinMode = settings.getBoolean(Constants.SGTIN_MODE, false);
        sgtin96Mode.setChecked(RFIDController.sgtinMode);
    }

    /**
     * Method to store the checkbox states
     */
    private void storeCheckBoxesStatus() {

        boolean AUTO_RECONNECT_READERS = ((CheckBox) getActivity().findViewById(R.id.autoReconnectReaders)).isChecked();
        boolean NOTIFY_READER_CONNECTION = ((CheckBox) getActivity().findViewById(R.id.readerConnection)).isChecked();
        boolean NOTIFY_BATTERY_STATUS = ((CheckBox) getActivity().findViewById(R.id.readerBattery)).isChecked();
        boolean EXPORT_DATA = ((CheckBox) getActivity().findViewById(R.id.exportData)).isChecked();
        boolean TAG_LIST_MATCH_MODE = ((CheckBox) getActivity().findViewById(R.id.tagListMatchMode)).isChecked();
        boolean SHOW_CSV_TAG_NAMES = ((CheckBox) getActivity().findViewById(R.id.tagListMatchTagNames)).isChecked();
        boolean ASCCI_MODE = asciiMode.isChecked();
        boolean SGTIN_MODE = sgtin96Mode.isChecked();
        Log.d("Matchmode","Tagmatchmode="+TaglistmatchmodeChecked);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();

        boolean isChanged = false;

        if (settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true) != AUTO_RECONNECT_READERS) {
            editor.putBoolean(Constants.AUTO_RECONNECT_READERS, AUTO_RECONNECT_READERS);
            isChanged = true;
        }

        if (settings.getBoolean(Constants.NOTIFY_READER_CONNECTION, false) != NOTIFY_READER_CONNECTION) {
            editor.putBoolean(Constants.NOTIFY_READER_CONNECTION, NOTIFY_READER_CONNECTION);
            isChanged = true;
        }

        if (settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true) != NOTIFY_BATTERY_STATUS) {
            editor.putBoolean(Constants.NOTIFY_BATTERY_STATUS, NOTIFY_BATTERY_STATUS);
            isChanged = true;
        }
        if (settings.getBoolean(Constants.EXPORT_DATA, false) != EXPORT_DATA) {
            editor.putBoolean(Constants.EXPORT_DATA, EXPORT_DATA);
            isChanged = true;
        }
        if (settings.getBoolean(Constants.TAG_LIST_MATCH_MODE, false) != TAG_LIST_MATCH_MODE) {
            editor.putBoolean(Constants.TAG_LIST_MATCH_MODE, TAG_LIST_MATCH_MODE);
            if(!TaglistmatchmodeChecked)
                isChanged = true;
        }
        if (settings.getBoolean(Constants.SHOW_CSV_TAG_NAMES, false) != SHOW_CSV_TAG_NAMES) {
            editor.putBoolean(Constants.SHOW_CSV_TAG_NAMES, SHOW_CSV_TAG_NAMES);
            isChanged = true;
        }

        if (settings.getBoolean(Constants.ASCII_MODE, false) != ASCCI_MODE) {
            editor.putBoolean(Constants.ASCII_MODE, ASCCI_MODE);
            isChanged = true;
        }
        if (settings.getBoolean(Constants.SGTIN_MODE, false) != SGTIN_MODE) {
            editor.putBoolean(Constants.SGTIN_MODE, SGTIN_MODE);
            isChanged = true;
        }
        // Commit the edits!
        editor.commit();

        //Update the preferences in the RFIDController
        RFIDController.AUTO_RECONNECT_READERS = AUTO_RECONNECT_READERS;
        RFIDController.NOTIFY_READER_CONNECTION = NOTIFY_READER_CONNECTION;
        RFIDController.NOTIFY_BATTERY_STATUS = NOTIFY_BATTERY_STATUS;
        RFIDController.EXPORT_DATA = EXPORT_DATA;
        Application.TAG_LIST_MATCH_MODE = TAG_LIST_MATCH_MODE;
        RFIDController.SHOW_CSV_TAG_NAMES = SHOW_CSV_TAG_NAMES;
        RFIDController.asciiMode = ASCCI_MODE;
        RFIDController.sgtinMode = SGTIN_MODE;

        if (isChanged)
            Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
    }


}
