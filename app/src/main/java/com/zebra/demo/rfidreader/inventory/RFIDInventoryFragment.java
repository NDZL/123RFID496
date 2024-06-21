package com.zebra.demo.rfidreader.inventory;


import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.common.asciitohex;
import com.zebra.demo.rfidreader.locate_tag.multitag_locate.MultiTagLocateListItem;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.settings.ISettingsUtil;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.TAG_FIELD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import static android.app.Activity.RESULT_OK;

import static com.zebra.demo.rfidreader.common.Constants.TYPE_DEBUG;
import static com.zebra.demo.rfidreader.common.Constants.logAsMessage;
import static com.zebra.demo.rfidreader.rfid.RFIDController.ActiveProfile;
import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isInventoryAborted;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.LOCATE_TAG_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RAPID_READ_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_ACCESS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_PREFILTERS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SETTINGS_TAB;

/**
 *
 * <p/>
 * Use the {@link RFIDInventoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to handle inventory operations and UI.
 */
public class RFIDInventoryFragment extends Fragment implements Spinner.OnItemSelectedListener, ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,
        View.OnClickListener {
    private static final int TAGLIST_MATCH_MODE_IMPORT = 0;
	private String TAG = "RFIDInventoryFragment";
    private static RFIDInventoryFragment mInvemtoryFragmentHandle;
    TextView totalNoOfTags;
    TextView uniqueTags;
    TextView uniqueTagsTitle;
    TextView totalReads;
    LinearLayout inventoryHeaderRow;
    TextView rssiColumnHeader;
    private ListView listView;
    private ModifiedInventoryAdapter adapter;
    private ArrayAdapter<CharSequence> invAdapter;

    //ID to maintain the memory bank selected
    private String memoryBankID = "none";
    private ExtendedFloatingActionButton inventoryButton;
    private FloatingActionButton fabMatchMode;
    private FloatingActionButton fabReset;

    private long prevTime = 0;
    private TextView timeText;
    private Spinner invSpinner;
    private TextView batchModeInventoryList;
    private ISettingsUtil settingsUtil;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            if (!RFIDController.mIsInventoryRunning) {
                toggle(view, position);

                if(adapter.getItem(position).getMemoryBank() != null){
                    RFIDController.accessControlTag = adapter.getItem(position).getMemoryBankData();
                    Application.locateTag = adapter.getItem(position).getTagID();
                    Application.PreFilterTag = adapter.getItem(position).getMemoryBankData();
                }else{
                    RFIDController.accessControlTag = adapter.getItem(position).getTagID();
                    Application.locateTag = adapter.getItem(position).getTagID();
                    Application.PreFilterTag = adapter.getItem(position).getTagID();
                }

                if(adapter.getItem(position).isVisible()){
                    MultiTagLocateListItem inv = new MultiTagLocateListItem(Application.locateTag, adapter.getItem(position).getRSSI(), 0, (short) 0);
                    if((Application.locateTag != null ) && !Application.multiTagLocateTagListMap.containsKey(Application.locateTag)) {
                        Application.multiTagLocateTagListMap.put(Application.locateTag, inv);
                        Application.multiTagLocateTagMap.put(asciitohex.convert(Application.locateTag).toUpperCase(), inv.getRssiValue());
                    }
                }else{
                    if((Application.locateTag != null) && Application.multiTagLocateTagListMap.containsKey(Application.locateTag)) {
                        Application.multiTagLocateTagListMap.remove(Application.locateTag);
                        Application.multiTagLocateTagMap.remove(asciitohex.convert(Application.locateTag).toUpperCase());
                    }
                }

                if(Application.multiTagLocateTagListMap.size() > 1)
                    Application.MultiTagInventoryMultiSelect =true;
            }
        }
    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InventoryFragment.
     */
    public static RFIDInventoryFragment newInstance() {
       // if(mInvemtoryFragmentHandle == null )
       mInvemtoryFragmentHandle = new RFIDInventoryFragment();
       return  mInvemtoryFragmentHandle;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate( R.menu.menu_inventory, menu);
        //
        menu.findItem( R.id.action_locate).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

              //  if (RFIDController.isLocatingTag) {
              //      Toast.makeText(getContext(), "Locationing is already in progress", Toast.LENGTH_SHORT).show();
              //  } else
              //  if (Application.locateTag != null && !Application.locateTag.equals(""))
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                {
                    Log.d(TAG, "locate " + Application.locateTag);
                    ((ActiveDeviceActivity)getActivity()).loadNextFragment(LOCATE_TAG_TAB);
                    return true;
                }
                // return false;
            }
        });
        menu.findItem( R.id.action_locate_menu).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //  if (RFIDController.isLocatingTag) {
                //      Toast.makeText(getContext(), "Locationing is already in progress", Toast.LENGTH_SHORT).show();
                //  } else
                //  if (Application.locateTag != null && !Application.locateTag.equals(""))
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                {
                    Log.d(TAG, "locate " + Application.locateTag);
                    ((ActiveDeviceActivity)getActivity()).loadNextFragment(LOCATE_TAG_TAB);
                    return true;
                }
                // return false;
            }
        });
        menu.findItem( R.id.action_rapidread).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                    ((ActiveDeviceActivity)getActivity()).loadNextFragment(RAPID_READ_TAB);
                    return true;
            }
        });

        menu.findItem( R.id.action_prefilter).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(RFID_PREFILTERS_TAB);
                return true;
            }
        });

        menu.findItem( R.id.action_tagwrite).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(RFID_ACCESS_TAB);
                return true;
            }
        });
        menu.findItem( R.id.action_settings).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }

                ((ActiveDeviceActivity)getActivity()).setCurrentTabFocus(SETTINGS_TAB, RFID_SETTINGS_TAB);
//                ((ActiveDeviceActivity)getActivity()).showRFIDSettings(null);
                return true;

            }
        });

    }

    public ModifiedInventoryAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_inventory, container, false);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.getFilter().filter(newText);
                    }
                });
                return false;
            }
        });


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActiveDeviceActivity activity = (ActiveDeviceActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onRFIDFragmentSelected();

    }

    public void RFIDReaderDisappeared(final ReaderDevice readerDevice) {
        Intent intent;
        intent = new Intent(getActivity(), DeviceDiscoverActivity.class);
        intent.putExtra("enable_toolbar", false);
        //startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.matchModeButton:
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getActivity(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
                    intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activityResultLauncher.launch(intent);


                   /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    startActivityForResult(Intent.createChooser(intent, "ChooseFile to upload"), TAGLIST_MATCH_MODE_IMPORT);*/
                }
                break;
            case R.id.resetButton:
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getActivity(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {
                    Application.cycleCountProfileData = null;
                    RFIDController.getInstance().clearAllInventoryData();
                    resetTagsInfoDetails();
                }
                break;
            default:
                break;
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
                                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                                } catch (Exception e) {
                                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                                }
                                if (!cacheMatchModeTagFile.exists()) {
                                    Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
                                } else {
                                    settingsUtil.LoadTagListCSV();
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if( data == null) {
           Toast.makeText(getActivity(),"No File selected ,using old CSV file for TAGLIST_MATCH_MODE ",Toast.LENGTH_SHORT).show();
           return;
       }
        File cacheMatchModeTagFile = new File(getActivity().getCacheDir().getAbsolutePath(), Application.CACHE_TAGLIST_MATCH_MODE_FILE);
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
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            } catch (Exception e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
            if (!cacheMatchModeTagFile.exists()) {
                Toast.makeText(getActivity(), getString(R.string.REQUIRES_TAGLIST_CSV), Toast.LENGTH_SHORT).show();
            } else {
                settingsUtil.LoadTagListCSV();
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void ClearListviewSelection(){
        for(int i=0;i< adapter.getCount();i++) {
            InventoryListItem listItem = adapter.getItem(i);
            listItem.setVisible(false);
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshSelectedTags() {

        ArrayList<MultiTagLocateListItem> multiTagLocateActiveList = new ArrayList<>(Application.multiTagLocateTagListMap.values());
        for (MultiTagLocateListItem list : multiTagLocateActiveList){
            if(!Application.multiTagLocateActiveTagItemList.contains(list)){
                Application.multiTagLocateTagListMap.remove(list.getTagID());
            }
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            InventoryListItem listItem = adapter.getItem(i);
            if (listItem.isVisible()) {
                listItem.setVisible(Application.multiTagLocateActiveTagItemList.contains(Application.multiTagLocateTagListMap.get(listItem.getTagID())));
            }
        }
    }

    private void toggle(View view, final int position) {
        InventoryListItem listItem = adapter.getItem(position);
        if (!listItem.isVisible()) {
            listItem.setVisible(true);
            if (Application.TAG_LIST_MATCH_MODE)
                view.findViewById( R.id.tagDetailsCSV).setVisibility(View.VISIBLE);
            view.setBackgroundColor(0x66444444);
        } else {
            listItem.setVisible(false);
            view.setBackgroundColor(Color.WHITE);
        }
        //if(!RFIDController.mIsInventoryRunning)
        adapter.notifyDataSetChanged();
    }

    boolean batchModeEventReceived = false;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        memoryBankID = adapterView.getSelectedItem().toString();
        Application.memoryBankId = invAdapter.getPosition(memoryBankID);
        memoryBankID = memoryBankID.toLowerCase();
        Application.tagsReadForSearch.clear();

        ArrayList<InventoryListItem> tempList = new ArrayList<>();

        if (Application.TAG_LIST_MATCH_MODE ) {
            if (Application.memoryBankId == 0) {
                adapter.searchItemsList = Application.tagsReadInventory;
                Application.tagsReadForSearch.addAll( Application.tagsReadInventory);
                adapter.notifyDataSetChanged();
            } else if (Application.memoryBankId == 1) {  //matching tags
                for (int k = 0; k < Application.tagsReadInventory.size(); k++) {
                    if ((Application.tagsReadInventory.get(k).getCount() > 0) && Application.tagListMap.containsKey( Application.tagsReadInventory.get(k).getTagID())) {
                        tempList.add( Application.tagsReadInventory.get(k));
                        Application.tagsReadForSearch.add( Application.tagsReadInventory.get(k));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();
            } else if (Application.memoryBankId == 2) {  //missed tags
                for (int j = 0; j < Application.tagsReadInventory.size(); j++) {
                    //adapter.searchItemsList.get(i).getCount()
                    if (Application.tagsReadInventory.get(j).getCount() == 0) {
                        tempList.add( Application.tagsReadInventory.get(j));
                        Application.tagsReadForSearch.add( Application.tagsReadInventory.get(j));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();

            } else if (Application.memoryBankId == 3) {   //unknown tags
                for (int k = 0; k < Application.tagsReadInventory.size(); k++) {
                    if ((Application.tagsReadInventory.get(k).getCount() > 0) && !Application.tagListMap.containsKey( Application.tagsReadInventory.get(k).getTagID())) {
                        tempList.add( Application.tagsReadInventory.get(k));
                        Application.tagsReadForSearch.add( Application.tagsReadInventory.get(k));
                    }
                }
                adapter.searchItemsList = tempList;
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Method to access the memory bank set
     *
     * @return - Memory bank set
     */
    public String getMemoryBankID() {
        return memoryBankID;
    }

    /**
     * method to reset the tag info
     */
    public void resetTagsInfo() {
        // Moved code to  resetTagsInfoDetails() method for testing purpose
        if (!ActiveProfile.id.equals("1"))
            resetTagsInfoDetails();
    }

    public void resetTagsInfoDetails() {
        if (Application.inventoryList != null && Application.inventoryList.size() > 0)
            Application.inventoryList.clear();
        if (totalNoOfTags != null)
            totalNoOfTags.setText(String.valueOf( Application.TOTAL_TAGS));
        if (uniqueTags != null)
            uniqueTags.setText(String.valueOf( Application.UNIQUE_TAGS));
        if (timeText != null) {
            String min = String.format("%d", TimeUnit.MILLISECONDS.toMinutes( RFIDController.mRRStartedTime));
            String sec = String.format("%d", TimeUnit.MILLISECONDS.toSeconds( RFIDController.mRRStartedTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes( RFIDController.mRRStartedTime)));
            if (min.length() == 1) {
                min = "0" + min;
            }
            if (sec.length() == 1) {
                sec = "0" + sec;
            }
            timeText.setText(min + ":" + sec);
        }
        if (listView.getAdapter() != null) {
            ((ModifiedInventoryAdapter) listView.getAdapter()).clear();
            ((ModifiedInventoryAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
        if (Application.TAG_LIST_MATCH_MODE) {
            totalNoOfTags.setText(String.valueOf( Application.missedTags));
            uniqueTags.setText(String.valueOf( Application.matchingTags));
        }


    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (listView.getAdapter() == null) {
                        listView.setAdapter(adapter);
                        batchModeInventoryList.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }
                    if (Application.TAG_LIST_MATCH_MODE) {
                        totalNoOfTags.setText(String.valueOf( Application.missedTags));
                        uniqueTags.setText(String.valueOf( Application.matchingTags));
                    } else {
                        totalNoOfTags.setText(String.valueOf( Application.TOTAL_TAGS));
                        if (uniqueTags != null)
                            uniqueTags.setText(String.valueOf( Application.UNIQUE_TAGS));
                    }
                    if (isAddedToList) {
                        if (!Application.TAG_LIST_MATCH_MODE) {
                            adapter.add(inventoryListItem);

                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    //  @Override
    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (results.equals( RFIDResults.RFID_BATCHMODE_IN_PROGRESS)) {
                    if (batchModeInventoryList != null) {
                        //  adapter.clear();
                        //  adapter.notifyDataSetChanged();
                        batchModeInventoryList.setText( R.string.batch_mode_inventory_title);
                        batchModeInventoryList.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                } else if (!results.equals( RFIDResults.RFID_API_SUCCESS)) {
                    RFIDController.isBatchModeInventoryRunning = false;
                    RFIDController.mIsInventoryRunning = false;
                    if (inventoryButton != null)
                        inventoryButton.setIconResource( android.R.drawable.ic_media_play );
                    if (invSpinner != null)
                        invSpinner.setEnabled(true);

                    //TODO: need of clearing
                    if (results.equals( RFIDResults.RFID_OPERATION_IN_PROGRESS)) {
                        if (Application.TAG_LIST_MATCH_MODE) {
                            logAsMessage(TYPE_DEBUG, "Inventory Fragment", "handleStatusResponse");
                            if (Application.tagsListCSV.size() == Application.tagsReadInventory.size()) {
                                Application.tagsReadInventory.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }else{
                            if (inventoryButton != null) {
                                inventoryButton.setIconResource(R.drawable.ic_play_stop);
                                inventoryButton.setText(R.string.stop);
                            }
                            mIsInventoryRunning = true;

                        }
                    }
                }
            }
        });
    }

    //   @Override
    public void triggerPressEventRecieved() {
        if (!RFIDController.mIsInventoryRunning && getActivity() != null) {

                //RFIDController.mInventoryStartPending = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inventoryButton != null) {
                        inventoryButton.setIconResource( R.drawable.ic_play_stop);
                    }
                    ActiveDeviceActivity activity = (ActiveDeviceActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop(null);
                    }
                }
            });
        }
    }

    //   @Override
    public void triggerReleaseEventRecieved() {
        if ((RFIDController.mIsInventoryRunning && getActivity() != null)) {
            //RFIDController.mInventoryStartPending = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inventoryButton != null) {
                        inventoryButton.setIconResource( android.R.drawable.ic_media_play );
                    }
                    ActiveDeviceActivity activity = (ActiveDeviceActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop(null);
                    }
                }
            });
        }
    }

    /**
     * method to set inventory status to stopped on reader disconnection
     */
    public void resetInventoryDetail() {

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //if (!ActiveProfile.id.equals("1"))
                    {
                        if (getActivity() != null) {
                            if (inventoryButton != null && !RFIDController.mIsInventoryRunning &&
                                    (RFIDController.isBatchModeInventoryRunning == null || !RFIDController.isBatchModeInventoryRunning)) {
                                inventoryButton.setIconResource(android.R.drawable.ic_media_play);
                            }
                            if (invSpinner != null)
                                invSpinner.setEnabled(true);
                            if (batchModeInventoryList != null && batchModeInventoryList.getVisibility() == View.VISIBLE) {
                                listView.setAdapter(adapter);
                                batchModeInventoryList.setText("");
                                batchModeInventoryList.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });


    }

    @Override
    public void batchModeEventReceived() {

        batchModeEventReceived = true;
        if (inventoryButton != null) {
            inventoryButton.setIconResource( R.drawable.ic_play_stop);
        }
//        if (invSpinner != null) {
//            invSpinner.setSelection(0);
//            invSpinner.setEnabled(false);
//        }

//        if (listView != null && batchModeInventoryList != null) {
//            adapter.clear();
//            adapter.notifyDataSetChanged();
//            listView.setEmptyView(batchModeInventoryList);
//            batchModeInventoryList.setVisibility(View.VISIBLE);

//            batchModeInventoryList.setText( R.string.batch_mode_inventory_title);
//        }
    }


    public void onRFIDFragmentSelected() {
        totalNoOfTags = (TextView) getActivity().findViewById( R.id.inventoryCountText);
        uniqueTags = (TextView) getActivity().findViewById( R.id.inventoryUniqueText);
        uniqueTagsTitle = (TextView) getActivity().findViewById( R.id.uniqueTags);
        totalReads = (TextView) getActivity().findViewById( R.id.totalReads);
        inventoryHeaderRow = (LinearLayout) getActivity().findViewById( R.id.inventoryHeaderRow);
        rssiColumnHeader = (TextView) getActivity().findViewById( R.id.rssiColumnHeader);
        rssiColumnHeader.setVisibility(View.GONE);
        if (RFIDController.tagStorageSettings != null) {
            for (TAG_FIELD tag_field : RFIDController.tagStorageSettings.getTagFields()) {
                if (tag_field == TAG_FIELD.PEAK_RSSI)
                    rssiColumnHeader.setVisibility(View.VISIBLE);
            }
        }
        if (Application.TAG_LIST_MATCH_MODE) {
            totalNoOfTags.setText(String.valueOf( Application.missedTags));
            uniqueTags.setText(String.valueOf( Application.matchingTags));
        } else {
            if (totalNoOfTags != null)
                totalNoOfTags.setText(String.valueOf( Application.TOTAL_TAGS));
            if (uniqueTags != null)
                uniqueTags.setText(String.valueOf( Application.UNIQUE_TAGS));
        }
        invSpinner = (Spinner) getActivity().findViewById( R.id.inventoryOptions);
        if (Application.TAG_LIST_MATCH_MODE && Application.TAG_LIST_FILE_EXISTS) {
            uniqueTagsTitle.setText("MATCHING TAGS");
            totalReads.setText("MISSED TAGS");
            ((TextView) getActivity().findViewById( R.id.inventorySpinnerText)).setText("TAG LIST");
            invAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inv_menu_items_for_matching_tags, R.layout.spinner_small_font);

        } else {
            // Create an ArrayAdapter using the string array and a default spinner layout
            invAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inv_menu_items, R.layout.spinner_small_font);
        }
        // Specify the layout to use when the list of choices appears
        invAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        invSpinner.setAdapter(invAdapter);
        if (Application.memoryBankId != -1 && Application.memoryBankId < invAdapter.getCount())
            invSpinner.setSelection( Application.memoryBankId);
        invSpinner.setOnItemSelectedListener(this);
        if (RFIDController.mIsInventoryRunning) {
            invSpinner.setEnabled(false);
        }
        inventoryButton = (ExtendedFloatingActionButton) getActivity().findViewById( R.id.inventoryButton);
        if (inventoryButton != null) {
            if (RFIDController.mIsInventoryRunning) {
                inventoryButton.setIconResource( R.drawable.ic_play_stop);
                if (Application.TAG_LIST_MATCH_MODE)
                    totalNoOfTags.setText("0");
            }
        }
        //Set the font size in constants
        Constants.INVENTORY_LIST_FONT_SIZE = (int) getResources().getDimension( R.dimen.inventory_list_font_size);
        batchModeInventoryList = (TextView) getActivity().findViewById( R.id.batchModeInventoryList);
        listView = (ListView) getActivity().findViewById( R.id.inventoryList);
        adapter = new ModifiedInventoryAdapter(getActivity(), R.layout.inventory_list_item);
        //enables filtering for the contents of the given ListView
        listView.setTextFilterEnabled(true);
        if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
            listView.setEmptyView(batchModeInventoryList);
            batchModeInventoryList.setVisibility(View.VISIBLE);
        } else {
            listView.setAdapter(adapter);
            batchModeInventoryList.setVisibility(View.GONE);
        }
        listView.setOnItemClickListener(onItemClickListener);
        adapter.notifyDataSetChanged();

        //  getActivity().findViewById(R.id.fab_prefilter).setVisibility(isPreFilterSimpleEnabled ? View.VISIBLE : View.GONE);
        getActivity().findViewById( R.id.tv_prefilter_enabled).setVisibility(
                RFIDController.getInstance().isPrefilterEnabled() ? View.VISIBLE : View.INVISIBLE);

        fabReset = (FloatingActionButton) getActivity().findViewById(R.id.resetButton);
        fabReset.setOnClickListener(this);
        if(ActiveProfile.id.equals("1")) {
            fabReset.show();
        } else {
            fabReset.hide();
        }

        fabMatchMode = (FloatingActionButton) getActivity().findViewById(R.id.matchModeButton);
        fabMatchMode.setOnClickListener(this);
        if(Application.TAG_LIST_MATCH_MODE) {
            fabMatchMode.show();
        } else {
            fabMatchMode.hide();
        }

        settingsUtil = (ActiveDeviceActivity)getActivity();
       /* if(Application.TAG_LIST_MATCH_MODE) {
           settingsUtil.LoadTagListCSV();
        }*/
        if(Application.MultiTagLocateTagListImport== true){
            Application.multiTagLocateTagListMap.clear();
            Application.multiTagLocateActiveTagItemList.clear();
            Application.multiTagLocateTagIDs.clear();
            Application.multiTagLocateTagMap.clear();
            Application.multiTagLocateTagListExist = false;
            ClearListviewSelection();
        }

        refreshSelectedTags();

    }
}
