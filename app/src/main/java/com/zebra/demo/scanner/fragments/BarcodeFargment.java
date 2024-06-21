package com.zebra.demo.scanner.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
//import com.zebra.demo.scanner.activities.ActiveScannerActivity;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.scanner.adapters.BarcodeListAdapter;
import com.zebra.demo.scanner.helpers.Barcode;

import java.util.ArrayList;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.LOCATE_TAG_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_ADVANCED_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SETTINGS_TAB;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeFargment extends Fragment{
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdvancedFragment.
     */
    BarcodeListAdapter barcodeAdapter;
    ListView barcodesList;
    ArrayList<Barcode> barcodes;
    private int scannerID;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            toggle(view, position);
            String tagid=new String(barcodeAdapter.getItem(position).getBarcodeData());
            RFIDController.accessControlTag = tagid;
            Application.locateTag = tagid;
            Application.PreFilterTag = tagid;
        }

    };

    private void toggle(View view, final int position) {
        Barcode barcodeItem = barcodeAdapter.getItem(position);
        Application.mSelectedItem = position;
       /* if (!barcodeItem.isVisible()) {
            barcodeItem.setVisible(true);
            view.setBackgroundColor(0x66444444);
        } else {
            barcodeItem.setVisible(false);
            view.setBackgroundColor(Color.WHITE);
        }*/
        barcodeAdapter.notifyDataSetChanged();
    }

    public static BarcodeFargment newInstance() {
        return new BarcodeFargment();
    }

    public BarcodeFargment() {
        // Required empty public constructor
    }

    /*    public static Fragment newInstance(int position) {
        BarcodeFargment fragment = new BarcodeFargment();
        Bundle args = new Bundle();
        //args.putInt(ARG_COUNT, counter);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        //((ActiveDeviceActivity) requireActivity()).initBatchRequest();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_scan, menu);
        menu.findItem( R.id.action_scan_setting).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(RFIDController.mConnectedReader == null ){
                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ((ActiveDeviceActivity)getActivity()).setCurrentTabFocus(SETTINGS_TAB, SCAN_SETTINGS_TAB);
                //((ActiveDeviceActivity) getActivity()).loadNextFragment(SCAN_SETTINGS_TAB);
                return true;
            }
        });

//        menu.findItem( R.id.action_adv_settings).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if(RFIDController.mConnectedReader == null ){
//                    Toast.makeText(getContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                ((ActiveDeviceActivity)getActivity()).setCurrentTabFocus(SETTINGS_TAB);
//                ((ActiveDeviceActivity) getActivity()).loadNextFragment(SCAN_ADVANCED_TAB);
//                return true;
//            }
//        });
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_barcode_fargment, container, false);
        //barcodes=new ArrayList<Barcode>();
        barcodes = ((ActiveDeviceActivity) requireActivity()).getBarcodeData(barcodes, ((ActiveDeviceActivity) requireActivity()).getScannerID());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                barcodes = ((ActiveDeviceActivity) requireActivity()).getBarcodeData(barcodes, ((ActiveDeviceActivity) requireActivity()).getScannerID());
//            }
//        }).start();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //if(getActivity() instanceof ActiveDeviceActivity) {
            //barcodes = ((ActiveDeviceActivity) requireActivity()).getBarcodeData(((ActiveDeviceActivity) requireActivity()).getScannerID());
        /*} else if(getActivity() instanceof ActiveScannerActivity) {
            barcodes = ((ActiveScannerActivity) requireActivity()).getBarcodeData(((ActiveScannerActivity) requireActivity()).getScannerID());
        }*/
        barcodesList = (ListView) getActivity().findViewById(R.id.barcodesList);
        if(barcodeAdapter == null) {
            barcodeAdapter = new BarcodeListAdapter(getActivity(), barcodes);
        }

        barcodesList.setAdapter(barcodeAdapter);
        barcodesList.setOnItemClickListener(onItemClickListener);
        barcodeAdapter.notifyDataSetChanged();

        Button btnClear = (Button) getActivity().findViewById(R.id.btnClearList);

        if(barcodes==null || barcodes.size()<=0) {
            btnClear.setEnabled(false);

        }
        if(barcodes.size()>0){
            btnClear.setEnabled(true);
        }

        barcodesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //if(getActivity() instanceof ActiveDeviceActivity) {
            ((ActiveDeviceActivity) getActivity()).updateBarcodeCount();
        /*} else if(getActivity() instanceof ActiveScannerActivity) {
            ((ActiveScannerActivity) getActivity()).updateBarcodeCount();
        }*/

    }


    public void showBarCode() {

        //barcodes.add(barcode);
        View mView = requireActivity().findViewById(R.id.btnScanTrigger);
        if(mView != null ) {
            mView.setEnabled(true);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null)
                        ((ActiveDeviceActivity)getActivity()).scanTrigger(mView);
                }
            });
        }

        // barcodeAdapter.add(barcode);
        barcodeAdapter.notifyDataSetChanged();

    }

    public void clearList(){
        barcodes.clear();
        barcodeAdapter.clear();
        Application.mSelectedItem = -1;
        barcodesList.setAdapter(barcodeAdapter);
        ((ActiveDeviceActivity) requireActivity()).clearBarcodeData();
    }

    public void scanTrigger() {


    }

}
