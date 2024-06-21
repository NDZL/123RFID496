package com.zebra.demo.rfidreader.locate_tag.multitag_locate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.hextoascii;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import static android.app.Activity.RESULT_OK;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;

/**
 * Created by PKF847 on 7/31/2017.
 */

public class MultiTagLocateFragment extends Fragment implements ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,
        View.OnClickListener {
    private static final int LOCATE_TAG_CSV_IMPORT = 0;
    private MultiTagLocateInventoryAdapter tagListAdapter;
    private static final String TAG = "MultiTagLocateFragment";

    private LinearLayout tagItemDataLayout;
    private AutoCompleteTextView tagItemView;
    private Button addItemButton;
    private Button deleteItemButton;
    private FloatingActionButton locateButton;
    private FloatingActionButton resetButton;
    private FloatingActionButton btnImportTagList;
    private RecyclerView listView;
    private ArrayAdapter<String> searchTagListAdapter;
    File cacheLocateTagfile = null;

    private MultiTagLocateInventoryAdapter.OnItemClickListner onItemClickListener = new MultiTagLocateInventoryAdapter.OnItemClickListner() {
        @Override
        public void onItemClick(int position) {
            if (!Application.mIsMultiTagLocatingRunning) {
                tagItemView.setText(tagListAdapter.getItem(position).getTagID());

            }
        }
    };


    public MultiTagLocateFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InventoryFragment.
     */
    public static MultiTagLocateFragment newInstance() {
        return new MultiTagLocateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multitag_locate, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (tagItemView.getText().toString() != null && Application.multiTagLocateTagListMap.containsKey(tagItemView.getText().toString()))
            Application.locateTag = tagItemView.getText().toString();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tagItemDataLayout = (LinearLayout) getActivity().findViewById(R.id.multiTagLocateDataLayout);
        tagItemView = (AutoCompleteTextView) getActivity().findViewById(R.id.multiTagLocate_epc);
        addItemButton = (Button) getActivity().findViewById(R.id.multiTagLocateAddItemButton);
        deleteItemButton = (Button) getActivity().findViewById(R.id.multiTagLocateDeleteItemButton);
        locateButton = (FloatingActionButton) getActivity().findViewById(R.id.multiTagLocateButton);
        resetButton = (FloatingActionButton) getActivity().findViewById(R.id.multiTagLocateResetButton);
        btnImportTagList = (FloatingActionButton) getActivity().findViewById(R.id.multi_tag_locate_import);
        btnImportTagList.setOnClickListener(this);
        listView = (RecyclerView) getActivity().findViewById(R.id.inventoryList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchTagListAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.multiTagLocateTagIDs);
        tagItemView.setAdapter(searchTagListAdapter);

        if (Application.locateTag != null && Application.multiTagLocateTagListMap.containsKey(Application.locateTag)){
            if (RFIDController.asciiMode == true) {
                tagItemView.setText(hextoascii.convert(Application.locateTag));
            } else {
                tagItemView.setText(Application.locateTag);
            }
        }

        if (Application.mIsMultiTagLocatingRunning) {
            locateButton.setImageResource(R.drawable.ic_play_stop);
            enableGUIComponents(false);
        } else {
            locateButton.setImageResource(android.R.drawable.ic_media_play);
            enableGUIComponents(true);
        }
        cacheLocateTagfile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_LOCATE_TAG_FILE);
        if(Application.MultiTagInventoryMultiSelect == true) {
            multiLocateTagImportFromInventory();
        }else {
            if (cacheLocateTagfile.exists()) {
                multiTagLocatPreImportList(cacheLocateTagfile);
            } else {
                updateTagItemList();
            }
        }
    }

    public void enableGUIComponents(boolean flag) {
        //tagItemDataLayout.setEnabled(flag);
        //tagItemView.setEnabled(flag);
        addItemButton.setEnabled(flag);
        deleteItemButton.setEnabled(flag);
        resetButton.setEnabled(flag);
        btnImportTagList.setEnabled(flag);
    }

    /**
     * method to reset multitag locationing status to default on reader disconnection or resetbutton click event
     */
    public void resetMultiTagLocateDetail(boolean isDeviceDisconnected) {
        if (getActivity() != null) {
            Application.mIsMultiTagLocatingRunning = false;
            locateButton.setImageResource(android.R.drawable.ic_media_play);
            //enableGUIComponents(true);

            if (!isDeviceDisconnected) { //called because of RESET button event
                if (Application.multiTagLocateTagListExist || Application.multiTagLocatelastTag ) {
                    Application.multiTagLocateActiveTagItemList.clear();
                    //Application.multiTagLocateTagIDs.clear();

                    for (String tagID : Application.multiTagLocateTagListMap.keySet()) {
                        Application.multiTagLocateTagListMap.get(tagID).setReadCount(0);
                        Application.multiTagLocateTagListMap.get(tagID).setProximityPercent((short) 0);
                    }

                    Application.multiTagLocateActiveTagItemList = new ArrayList<MultiTagLocateListItem>(Application.multiTagLocateTagListMap.values());
                    try {
                        RFIDController.mConnectedReader.Actions.MultiTagLocate.purgeItemList();
                        RFIDController.mConnectedReader.Actions.MultiTagLocate.importItemList(Application.multiTagLocateTagMap);
                        if(Application.multiTagLocatelastTag == true) {
                            Application.multiTagLocateTagListExist =true;
                            Application.multiTagLocatelastTag =false;

                        }
                    } catch (InvalidUsageException e) {
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getInfo());
                    } catch (OperationFailureException e) {
                        ((ActiveDeviceActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, e.getVendorMessage());
                    }
                    tagItemView.setText("");
                    listView.setAdapter(null);
                    updateTagItemList();
                }
            }else{

                if (Application.multiTagLocateTagListExist || Application.multiTagLocatelastTag ) {
                    Application.multiTagLocateActiveTagItemList.clear();
                    //Application.multiTagLocateTagIDs.clear();

                    for (String tagID : Application.multiTagLocateTagListMap.keySet()) {
                        Application.multiTagLocateTagListMap.get(tagID).setReadCount(0);
                        Application.multiTagLocateTagListMap.get(tagID).setProximityPercent((short) 0);
                        try {
                            RFIDController.mConnectedReader.Actions.MultiTagLocate.deleteItem(tagID);
                        } catch (InvalidUsageException e) {

                        } catch (OperationFailureException e) {

                        }
                    }
                }

                tagItemView.setText("");
                listView.setAdapter(null);
                Application.multiTagLocateActiveTagItemList.clear();
                updateTagItemList();
            }
        }
    }

    @Override
    public void triggerPressEventRecieved() {
        if (!Application.mIsMultiTagLocatingRunning) {
           getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                        ((ActiveDeviceActivity) getActivity()).multiTagLocateStartOrStop(locateButton);
                }
            });
        }
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (Application.mIsMultiTagLocatingRunning ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   //locateButton.setImageResource(R.drawable.ic_play_stop);
                     ((ActiveDeviceActivity) getActivity()).multiTagLocateStartOrStop(locateButton);
                }
            });
        }
    }

    @Override
    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    Application.mIsMultiTagLocatingRunning = false;
                    if (locateButton != null) {
                        locateButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                    //enableGUIComponents(true);
                }
            }
        });
    }

    public void updateTagItemList() {
        if(listView.getAdapter() == null) {
            tagListAdapter = new MultiTagLocateInventoryAdapter(onItemClickListener);
            listView.setAdapter(tagListAdapter);
        }
        if (Application.MULTI_TAG_LOCATE_SORT) {
            tagListAdapter.sortItemList();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tagListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void multiTagLocatPreImportList(File uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            FutureTask importTask = new FutureTask(new MultiTagLocateTagListDataImportTask(uri), "Import task complete");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(importTask);
            while (true) {
                if (importTask.isDone()) {
                    Application.MultiTagLocateTagListImport=true;
                    Application.MultiTagInventoryMultiSelect=false;
                    listView.setAdapter(null);
                    updateTagItemList();
                    executorService.shutdown();
                    break;
                }
            }
        }
    }
    private void multiLocateTagImportFromInventory(){

        if(Application.multiTagLocateTagListMap.size() > 0)
        {
            Application.multiTagLocateActiveTagItemList = new ArrayList<MultiTagLocateListItem>(Application.multiTagLocateTagListMap.values());
            Application.multiTagLocateTagIDs = new ArrayList<String>(Application.multiTagLocateTagListMap.keySet());
            Application.multiTagLocateTagListExist = true;
            if (mConnectedReader != null && mConnectedReader.isConnected()) {
                if(Application.multiTagLocateTagListExist) {
                    try {
                        mConnectedReader.Actions.MultiTagLocate.purgeItemList();
                        mConnectedReader.Actions.MultiTagLocate.importItemList(Application.multiTagLocateTagMap);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }
                Application.MultiTagLocateTagListImport=false;
                listView.setAdapter(null);
                updateTagItemList();
            }
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multi_tag_locate_import:
                if(!Application.mIsMultiTagLocatingRunning) {
                  /*  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    startActivityForResult(Intent.createChooser(intent, "ChooseFile to upload"), LOCATE_TAG_CSV_IMPORT);*/

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
                    intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activityResultLauncher.launch(intent);


                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.multiTag_locate_error_operation_running), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (resultCode == RESULT_OK && requestCode == LOCATE_TAG_CSV_IMPORT) {
            if(data == null) return;
            Uri uri = data.getData();
            importLocateTagList(String.valueOf(uri));
        }
    }


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    File cacheMatchModeTagFile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_TAGLIST_MATCH_MODE_FILE);
                    if (cacheMatchModeTagFile.exists()) {
                        cacheMatchModeTagFile.delete();
                    }

                    if (result.getResultCode() == RESULT_OK ) {
                        //    Uri uri = data.getData();

                        Intent data = result.getData();
                        Uri uri =  data.getData();
                        if( data == null) {
                            Toast.makeText(getActivity(),"No File selected ,using old CSV file for TAGLIST_MATCH_MODE ",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (data != null) {
                            if (data.getData().toString().contains("content://com.android.providers")) {
                                getActivity().runOnUiThread(this::ShowPlugInPathChangeDialog);
                            }

                            else{
                                importLocateTagList(String.valueOf(uri));
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

    private void importLocateTagList(String locateCsvFile) {
        Uri locateCsvUri = Uri.parse(locateCsvFile);
        try {
            if(cacheLocateTagfile.exists()) {
                cacheLocateTagfile.delete();
            }
            InputStream in = getActivity().getContentResolver().openInputStream(locateCsvUri);
            OutputStream out = new FileOutputStream(cacheLocateTagfile);
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
        if(cacheLocateTagfile.exists()) {
            multiTagLocatPreImportList(cacheLocateTagfile);
            Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.status_failure_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleReaderDisapeared() {
        Application.mIsMultiTagLocatingRunning = false;
        resetMultiTagLocateDetail(true);
        if (locateButton != null) {
            locateButton.setImageResource(android.R.drawable.ic_media_play);
        }

    }
}
