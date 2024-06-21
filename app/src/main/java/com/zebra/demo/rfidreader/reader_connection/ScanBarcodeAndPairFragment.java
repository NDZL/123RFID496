package com.zebra.demo.rfidreader.reader_connection;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.discover_connect.nfc.PairOperationsFragment;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanBarcodeAndPairFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanBarcodeAndPairFragment extends Fragment {

    private static final String TAG = "ScanBarcodeAndPairFragment";
    static String btAddress;
    BarCodeView barCodeView;
    //DCSSDKDefs.DCSSDK_BT_PROTOCOL protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST ;
    DCSSDKDefs.DCSSDK_BT_PROTOCOL protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.LEGACY_B;
    DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG config = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT;
    Dialog dialogBTAddress;
    static String userEnteredBluetoothAddress;
    private static final int MAX_BLUETOOTH_ADDRESS_CHARACTERS = 17;
    public static final String BLUETOOTH_ADDRESS_VALIDATOR = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private static final String DEFAULT_EMPTY_STRING = "";
    private static final String COLON_CHARACTER = ":";
    private static final int MAX_ALPHANUMERIC_CHARACTERS = 12;

    FrameLayout flBarcodeView;
    TextView barcodeType, btProtocol;
    TextView cancelContinueButton;

    public ScanBarcodeAndPairFragment() {
        // Required empty public constructor
    }

    public static ScanBarcodeAndPairFragment newInstance() {
        return new ScanBarcodeAndPairFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_barcode_and_pair, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flBarcodeView = view.findViewById(R.id.scan_barcode_to_connect);
        barcodeType = view.findViewById(R.id.barcode_scan_type);
        btProtocol = view.findViewById(R.id.bt_protocol);
        barcodeType.setText("STC Barcode");
        btProtocol.setText("SSI Legacy");
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        if(PairOperationsFragment.currentFragment instanceof ScanBarcodeAndPairFragment)
            btAddress = getDeviceBTAddress(settings);
        if(btAddress != null)
            barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(protocol,config,btAddress);
        if(barCodeView != null) {
            updateBarcodeView(layoutParams, barCodeView);

        } else {
        }

    }

    private void updateBarcodeView(LinearLayout.LayoutParams layoutParams, com.zebra.scannercontrol.BarCodeView barCodeView) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int orientation =this.getResources().getConfiguration().orientation;
        int x = width * 9 / 10;
        int y = x / 3;
        if(getDeviceScreenSize()>6){ // TODO: Check 6 is ok or not
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                x =  width /2;
                y = x/3;
            }else {
                x =  width *2/3;
                y = x/3;
            }
        }
        barCodeView.setSize(x, y);
        flBarcodeView.addView(barCodeView, layoutParams);
    }
    private double getDeviceScreenSize() {
        double screenInches = 0;
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        int mWidthPixels;
        int mHeightPixels;

        try {
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            mWidthPixels = realSize.x;
            mHeightPixels = realSize.y;
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(mWidthPixels/dm.xdpi,2);
            double y = Math.pow(mHeightPixels/dm.ydpi,2);
            screenInches = Math.sqrt(x+y);
        } catch (Exception ignored) {
        }
        return screenInches;
    }

    private String getDeviceBTAddress(SharedPreferences settings) {
        String bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
        if (bluetoothMAC.equals("")) {

            if (dialogBTAddress == null) {

                dialogBTAddress = new Dialog(getActivity());
                dialogBTAddress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogBTAddress.setContentView(R.layout.dialog_bt_address);

                cancelContinueButton = (TextView) dialogBTAddress.findViewById(R.id.cancel_continue);
                final TextView abtPhoneButton = (TextView) dialogBTAddress.findViewById(R.id.abt_phone);
                abtPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent statusSettings = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                        startActivity(statusSettings);
                    }

                });
                cancelContinueButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (cancelContinueButton.getText().equals(getContext().getResources().getString(R.string.cancel))) {
                            if (dialogBTAddress != null) {
                                dialogBTAddress.dismiss();
                                dialogBTAddress = null;
                            }

                        } else {
                            Application.sdkHandler.dcssdkSetSTCEnabledState(true);
                            SharedPreferences.Editor settingsEditor = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                            settingsEditor.putString(Constants.PREF_BT_ADDRESS, userEnteredBluetoothAddress).commit();// Commit is required here. So suppressing warning.
                            if (dialogBTAddress != null) {
                                dialogBTAddress.dismiss();
                                dialogBTAddress = null;
                            }
                            flBarcodeView.removeAllViews();
                            barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(protocol,config,userEnteredBluetoothAddress);
                            if(barCodeView != null) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                                updateBarcodeView(layoutParams, barCodeView);

                            }
                        }


                    }
                });

                final EditText editTextBluetoothAddress = (EditText) dialogBTAddress.findViewById(R.id.text_bt_address);
                editTextBluetoothAddress.addTextChangedListener(new TextWatcher() {
                    String previousMac = null;
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String enteredMacAddress = editTextBluetoothAddress.getText().toString().toUpperCase();
                        String cleanMacAddress = clearNonMacCharacters(enteredMacAddress);
                        String formattedMacAddress = formatMacAddress(cleanMacAddress);

                        int selectionStart = editTextBluetoothAddress.getSelectionStart();
                        formattedMacAddress = handleColonDeletion(enteredMacAddress, formattedMacAddress, selectionStart);
                        int lengthDiff = formattedMacAddress.length() - enteredMacAddress.length();

                        setMacEdit(cleanMacAddress, formattedMacAddress, selectionStart, lengthDiff);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        userEnteredBluetoothAddress = s.toString();
                        if(userEnteredBluetoothAddress.length() > MAX_BLUETOOTH_ADDRESS_CHARACTERS)
                            return;

                        if (isValidBTAddress(userEnteredBluetoothAddress)) {

                            Drawable dr = getResources().getDrawable(R.drawable.tick);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            editTextBluetoothAddress.setCompoundDrawables(null, null, dr, null);
                            cancelContinueButton.setText(getResources().getString(R.string.continue_txt));

                        } else {
                            editTextBluetoothAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            cancelContinueButton.setText(getResources().getString(R.string.cancel));

                        }
                    }

                    /**
                     * Strips all characters from a string except A-F and 0-9
                     * (Keep Bluetooth address allowed characters only).
                     *
                     * @param inputMacString User input string.
                     * @return String containing bluetooth MAC-allowed characters.
                     */
                    private String clearNonMacCharacters(String inputMacString) {
                        return inputMacString.toString().replaceAll("[^A-Fa-f0-9]", DEFAULT_EMPTY_STRING);
                    }

                    /**
                     * Adds a colon character to an unformatted bluetooth MAC address after
                     * every second character (strips full MAC trailing colon)
                     *
                     * @param cleanMacAddress Unformatted MAC address.
                     * @return Properly formatted MAC address.
                     */
                    private String formatMacAddress(String cleanMacAddress) {
                        int groupedCharacters = 0;
                        String formattedMacAddress = DEFAULT_EMPTY_STRING;

                        for (int i = 0; i < cleanMacAddress.length(); ++i) {
                            formattedMacAddress += cleanMacAddress.charAt(i);
                            ++groupedCharacters;

                            if (groupedCharacters == 2) {
                                formattedMacAddress += COLON_CHARACTER;
                                groupedCharacters = 0;
                            }
                        }

                        // Removes trailing colon for complete MAC address
                        if (cleanMacAddress.length() == MAX_ALPHANUMERIC_CHARACTERS)
                            formattedMacAddress = formattedMacAddress.substring(0, formattedMacAddress.length() - 1);

                        return formattedMacAddress;
                    }

                    /**
                     * Upon users colon deletion, deletes bluetooth MAC character preceding deleted colon as well.
                     *
                     * @param enteredMacAddress     User input MAC.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @return Formatted MAC address.
                     */
                    private String handleColonDeletion(String enteredMacAddress, String formattedMacAddress, int selectionStartPosition) {
                        if (previousMac != null && previousMac.length() > 1) {
                            int previousColonCount = colonCount(previousMac);
                            int currentColonCount = colonCount(enteredMacAddress);

                            if (currentColonCount < previousColonCount) {
                                try {
                                    formattedMacAddress = formattedMacAddress.substring(0, selectionStartPosition - 1) + formattedMacAddress.substring(selectionStartPosition);
                                }catch (Exception e){
                                   Log.d(TAG,  "Returned SDK Exception");
                                }
                                String cleanMacAddress = clearNonMacCharacters(formattedMacAddress);
                                formattedMacAddress = formatMacAddress(cleanMacAddress);
                            }
                        }
                        return formattedMacAddress;
                    }

                    /**
                     * Gets bluetooth MAC address current colon count.
                     *
                     * @param formattedMacAddress Formatted MAC address.
                     * @return Current number of colons in MAC address.
                     */
                    private int colonCount(String formattedMacAddress) {
                        return formattedMacAddress.replaceAll("[^:]", DEFAULT_EMPTY_STRING).length();
                    }

                    /**
                     * Removes TextChange listener, sets MAC EditText field value,
                     * sets new cursor position and re-initiates the listener.
                     *
                     * @param cleanMacAddress       Clean MAC address.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @param characterDifferenceLength     Formatted/Entered MAC number of characters difference.
                     */
                    private void setMacEdit(String cleanMacAddress, String formattedMacAddress, int selectionStartPosition, int characterDifferenceLength) {
                        editTextBluetoothAddress.removeTextChangedListener(this);
                        if (cleanMacAddress.length() <= MAX_ALPHANUMERIC_CHARACTERS) {
                            editTextBluetoothAddress.setText(formattedMacAddress);

                            editTextBluetoothAddress.setSelection(selectionStartPosition + characterDifferenceLength);
                            previousMac = formattedMacAddress;
                        } else {
                            if (previousMac != null) {
                                editTextBluetoothAddress.setText(previousMac);
                                editTextBluetoothAddress.setSelection(previousMac.length());
                            }
                        }
                        editTextBluetoothAddress.addTextChangedListener(this);
                    }

                });

                dialogBTAddress.setCancelable(false);
                dialogBTAddress.setCanceledOnTouchOutside(false);
                if(isVisible() && !dialogBTAddress.isShowing() ) {
                    dialogBTAddress.show();
                }
                Window window = dialogBTAddress.getWindow();
                if(window!=null) window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
            } else {
                dialogBTAddress.show();
            }
        }
        return bluetoothMAC;
    }

    private boolean isValidBTAddress(String text) {

        return text != null && text.length() > 0 && text.matches(BLUETOOTH_ADDRESS_VALIDATOR);

    }
    @Override
    public void onStop() {
        super.onStop();
        if (dialogBTAddress != null && dialogBTAddress.isShowing()){
            dialogBTAddress.dismiss();
            dialogBTAddress = null;
        }
    }

}
