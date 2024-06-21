package com.zebra.demo.wifi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.zebra.demo.R;

public class WifiPasswordDialog  extends AppCompatDialogFragment {

    public static String TAG = "WifiPasswordDialog";
    private EditText etWifiPassword;
    private String wifiName;
    Context mContext;

    public WifiPasswordDialog(Context mContext, String wifiName) {
        this.wifiName = wifiName;
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_wifipassword,null,false);
        etWifiPassword = view.findViewById(R.id.wifi_password);
        
        builder.setView(view)
                .setTitle(wifiName)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setPositiveButton("Continue", (dialog, which) ->{

                });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (etWifiPassword.getText().toString().length() >= 8) {
                ReaderWifiSettingsFragment.getInstance()
                        .addProfile(etWifiPassword.getText().toString(), wifiName);
                dialog.dismiss();
            } else {
                etWifiPassword.setError("Min 8 characters!");
            }

        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
