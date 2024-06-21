package com.zebra.demo.scanner.fragments;

import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextView tv_name,tv_serialno,tv_model, tv_mac;
    TextView tv_ipaddress, tv_ipaddress2;
    TextView tv_wifi, tv_rfid, tv_scan;
    Bundle readerdetailsBundle;
    ReaderDevice mReaderDevice;
    ImageView iconRenameReader;

    public ReaderDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReaderDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReaderDetailsFragment newInstance() {
        return new ReaderDetailsFragment();
    }
    public static ReaderDetailsFragment newInstance(String param1, String param2) {
        ReaderDetailsFragment fragment = new ReaderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.activity_reader_details, container, false);
        InitializeView(view);

        iconRenameReader.setVisibility(View.GONE);
        iconRenameReader.setOnClickListener(viewRenameReader -> renameReader());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setReaderDetails();
    }

    private void setReaderDetails() {
        if(getActivity() instanceof ActiveDeviceActivity)
            mReaderDevice = ((ActiveDeviceActivity)getActivity()).connectedReaderDetails();
        else if(getActivity() instanceof DeviceDiscoverActivity)
            mReaderDevice = ((DeviceDiscoverActivity)getActivity()).connectedReaderDetails();
        tv_name.setText(mReaderDevice.getName().toString());
        String readerName;
      /*  if(mReaderDevice.getRFIDReader().ReaderCapabilities != null && !mReaderDevice.getName().startsWith("RFD8500")) {
            readerName = mReaderDevice.getRFIDReader().ReaderCapabilities.getModelName();
        }
        else*/
            readerName = mReaderDevice.getName().toString();
       // Log.d("serialno","no-"+mReaderDevice.getRFIDReader().ReaderCapabilities.getSerialNumber());
        if(RFIDController.mConnectedReader != null &&
                RFIDController.mConnectedReader.getHostName().equalsIgnoreCase(readerName)){
            iconRenameReader.setVisibility(View.VISIBLE);
        }

        if (readerName != null) {
            if (readerName.startsWith("MC33")) {
                tv_model.setText("MC330XR");
                tv_wifi.setText("Not Available");
                tv_scan.setText("Not Available");
                String[] splitStr = readerName.split("R");

                if (mReaderDevice.getRFIDReader().ReaderCapabilities != null){
                    tv_serialno.setText(mReaderDevice.getRFIDReader().ReaderCapabilities.getSerialNumber());
                }
                else
                    tv_serialno.setText(splitStr[1]);

            } else if (readerName.startsWith("RFD40")) {

                String[] splitStr = readerName.split("-");

                tv_serialno.setText(mReaderDevice.getSerialNumber());
              /*  if (mReaderDevice.getRFIDReader().ReaderCapabilities != null) {

              //      tv_serialno.setText(mReaderDevice.getRFIDReader().ReaderCapabilities.getSerialNumber());
                }*/
                if (splitStr[0].equals("RFD4030")) {
                    if (splitStr[1].contains("G0")) {
                        tv_model.setText("Standard");
                        tv_wifi.setText("Not Available");
                        tv_scan.setText("Not Available");
                    }

                } else if (splitStr[0].equals("RFD4031")) {
                    if (splitStr[1].contains("G0")) {
                        tv_model.setText("Premium (WiFi)");
                        tv_wifi.setText("Available");
                        tv_scan.setText("Not Available");
                    } else if (splitStr[1].contains("G1")) {
                        tv_model.setText("Premium Plus (WiFi & Scan)");
                        tv_wifi.setText("Available");
                        tv_scan.setText("Available");
                    }

                } else if (splitStr[0].startsWith("RFD40+")) {
                    String serialno[] = readerName.split("_");
                    int length = serialno.length;
                    tv_serialno.setText(serialno[length-1]);
                    tv_model.setText("Premium Plus (WiFi & Scan)");
                    tv_wifi.setText("Available");
                    tv_scan.setText("Available");
                }else if (splitStr[0].startsWith("RFD40P")) {
                    String serialno[] = readerName.split("_");
                    int length = serialno.length;
                    tv_serialno.setText(serialno[length-1]);
                    tv_model.setText("Premium (WiFi)");
                    tv_wifi.setText("Available");
                    tv_scan.setText("Not Available");
                }

            }  else if(readerName.startsWith("RFD90+")) {
                String serialno[] = readerName.split("_");
                int length = serialno.length;
                tv_serialno.setText(serialno[length-1]);
                tv_model.setText("Premium Plus (WiFi & Scan)");
                tv_wifi.setText("Available");
                tv_scan.setText("Available");
            }  else if(readerName.startsWith("RFD90")) {
                readerName = mReaderDevice.getSerialNumber();
                String serialno[] = readerName.split("S/N:");
                tv_serialno.setText(serialno[1]);
                tv_model.setText("Premium Plus (WiFi & Scan)");
                tv_wifi.setText("Available");
                tv_scan.setText("Available");
            } else if (readerName.startsWith("RFD8500")) {
                String[] splitStr = readerName.split("RFD8500");
                tv_serialno.setText(splitStr[1]);
                tv_model.setText("RFD8500");
                tv_wifi.setText("Not Available");
                if(Application.currentScannerId == -1)
                    tv_scan.setText("Not Available");
                else
                    tv_scan.setText("Available");
            }
        }

        if(tv_serialno.getText().toString().contains("S/N:")){
            String sn = tv_serialno.getText().toString();
            String serialno[] = sn.split("S/N:");
            tv_serialno.setText(serialno[1]);
        }
    }

    public void renameReader( ){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rename_reader, null);
        EditText etRenameReader = view.findViewById(R.id.edit_ReaderName);
        try {
            etRenameReader.setText(mConnectedReader.Config.getFriendlyName());
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, e.getStackTrace()[0].toString());
        }
        builder.setView(view)
                .setTitle("Rename Reader")
                .setNegativeButton("cancel", (dialogInterface, i) -> {

                })
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    String newName = etRenameReader.getText().toString();
                    try {
                        RFIDResults rfidResults = mConnectedReader.Config.setFriendlyName(newName);

                        if(rfidResults == RFIDResults.RFID_API_SUCCESS){
                            Toast.makeText(getActivity(),"Rename Success. To see changes" +
                                    "\nUSB connection: Deattach and attach the reader " +
                                    "\nBluetooth: Unpair and pair the device",Toast.LENGTH_LONG).show();
                            requireActivity().onBackPressed();
                        }
                        else if(rfidResults == RFIDResults.RFID_COMMAND_OPTION_WITHOUT_DELIMITER){
                            Toast.makeText(getActivity(),"Renaming failed. Don't include Space in between words", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity(),"Rename fail. Unknown error",Toast.LENGTH_SHORT).show();
                        }
                    } catch (InvalidUsageException e) {
                        if(e.getStackTrace().length>0) {
                            Log.e(TAG, e.getStackTrace()[0].toString());
                            Log.e(TAG, e.getInfo());
                            Toast.makeText(getActivity(),e.getInfo(),Toast.LENGTH_SHORT).show();
                        }
                    } catch (OperationFailureException e) {
                        if(e.getStackTrace().length>0)
                        { Log.e(TAG, e.getStackTrace()[0].toString()); }
                    }
                });
        builder.show();
    }

    private void InitializeView(View view) {
        tv_name = view.findViewById(R.id.readername_value);
        tv_serialno = view.findViewById(R.id.serialno_value);
        tv_model = view.findViewById(R.id.model_value);
        tv_wifi = view.findViewById(R.id.wifi_available);
        tv_rfid = view.findViewById(R.id.rfid_availabe);
        tv_scan = view.findViewById(R.id.scan_available);
        iconRenameReader = view.findViewById(R.id.icon_rename_reader);

    }

}