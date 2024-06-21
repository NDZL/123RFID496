
package com.zebra.demo.rfidreader.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
//import com.zebra.demo.scanner.activities.ActiveScannerActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_HOME_SETTINGS_TAB;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManagerFragment extends Fragment {
    /* Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdvancedFragment.
     */
    private View ManagerFragmentView;

    public static ManagerFragment newInstance() {
        return new ManagerFragment();
    }

    private TableRow ShareFile;
    private static final int CHOOSE_FILE_FROM_DEVICE = 1001;
    private Uri filePath;
    private ArrayList<Uri> multipleFiles;

    public ManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

      /*  inflater.inflate(R.menu.disactivity_home_drawer, menu);
        menu.findItem(android.R.id.home).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                return true;
            }
        });*/
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //ManagerFragmentView = inflater.inflate(R.layout.fragment_manage, container, false);
        ManagerFragmentView = inflater.inflate(R.layout.fragment_manage, container, false);
       // ((ActiveDeviceActivity)getActivity()).getSupportActionBar().setTitle("General Settings");

        SwitchCompat picklistMode = (SwitchCompat) ManagerFragmentView.findViewById(R.id.switch_picklist_mode);
        final TextView txtPicklistMode = (TextView) ManagerFragmentView.findViewById(R.id.txt_picklist_mode);
        if (picklistMode != null) {
            /*int picklistInt = -1;
            if(getActivity() instanceof ActiveDeviceActivity) {*/
            int picklistInt = ((ActiveDeviceActivity) requireActivity()).getPickListMode();
            /*} else if(getActivity() instanceof ActiveScannerActivity) {
                picklistInt = ((ActiveScannerActivity) requireActivity()).getPickListMode();
            }*/
            boolean picklistBool = false;
            if (picklistInt == 2) {
                picklistBool = true;
            }
            Log.i("PickListMode", "Setting " + picklistBool + " int value = " + picklistInt);
            picklistMode.setChecked(picklistBool);
            if (picklistBool) {
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
            } else {
                txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
            }
            picklistMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int picklistInt = 0;
                    if (isChecked) {
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
                        picklistInt = 2;
                    } else {
                        txtPicklistMode.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
                    }
                    ((ActiveDeviceActivity) getActivity()).setPickListMode(picklistInt);
                }
            });
        }

        final TextView txtVibration = (TextView) ManagerFragmentView.findViewById(R.id.vibration_feedback_text);
        TableRow tblRowFW = (TableRow) ManagerFragmentView.findViewById(R.id.vibration_feedback_tbl_row);

        if (txtVibration != null && tblRowFW != null) {
            boolean isPagerMotorAvailable = ((ActiveDeviceActivity) requireActivity()).isPagerMotorAvailable();
            if (isPagerMotorAvailable) {
                tblRowFW.setClickable(true);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.font_color));
            } else {
                tblRowFW.setClickable(false);
                txtVibration.setTextColor(ContextCompat.getColor(((ActiveDeviceActivity) getActivity()), R.color.inactive_text));
            }
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
        return ManagerFragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();

    }

    private void initialize() {
        multipleFiles = new ArrayList<>();
        ShareFile = (TableRow) getActivity().findViewById(R.id.ShareFiles);
        ShareFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callChooseFileFromDevice();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("Share", "onActivityResult called");

        if (requestCode == CHOOSE_FILE_FROM_DEVICE && resultCode == RESULT_OK) {
            Log.d("Share", "requestCode == CHOOSE_PDF_FROM_DEVICE && RESULT_OK");

            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        filePath = data.getClipData().getItemAt(i).getUri();
                        Log.d("Share", "filePath = " + filePath);
                        multipleFiles.add(filePath);
                        Log.d("Share", "file added = " + i);
                    }
                    shareFile();
                } else {
                    filePath = data.getData();
                    multipleFiles.add(filePath);
                    Log.d("Share", "filePath = " + filePath);
                    shareFile();

                }
            }
        }
    }




    public void callChooseFileFromDevice(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setType("*/*");
        //  startActivityForResult(Intent.createChooser(intent,"Selecting multiple files"),CHOOSE_PDF_FROM_DEVICE);
        startActivityForResult(Intent.createChooser(intent,"Selecting multiple files"),CHOOSE_FILE_FROM_DEVICE);
        //  Log.d("Share"," selecting ");
    }

    public void shareFile(){

        Log.d("Share","before sharing");
        Intent intentShare = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intentShare.setType("*/*");
        intentShare.putParcelableArrayListExtra(Intent.EXTRA_STREAM, multipleFiles);
        startActivity(Intent.createChooser(intentShare,"Share the file ..."));
        Log.d("Share","after sharing");
        multipleFiles.clear();
    }

}
