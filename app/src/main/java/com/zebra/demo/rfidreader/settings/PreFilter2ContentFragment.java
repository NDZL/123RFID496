package com.zebra.demo.rfidreader.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.InputFilterMax;
import com.zebra.demo.rfidreader.common.PreFilters;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import static com.zebra.demo.rfidreader.home.RFIDBaseActivity.filter;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_PREFILTERS_TAB;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Use the {@link PreFilter2ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to show the pre-filter 2 UI.
 */
public class PreFilter2ContentFragment extends Fragment {
    private EditText preFilterOffset;
    private CheckBox preFilter2EnableFilter;

    public PreFilter2ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreFilter2ContentFragment.
     */
    public static PreFilter2ContentFragment newInstance() {
        return new PreFilter2ContentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pre_filter2_content, container, false);
        Spinner spinner = view.findViewById(R.id.prefilter_type);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();

                if(selectedType.equals("Basic") ) {
                    Application.showAdvancedOptions = false;
                    SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(Constants.PREFILTER_ADV_OPTIONS, Application.showAdvancedOptions);
                    editor.commit();
                    ((ActiveDeviceActivity)getActivity()).loadNextFragment(RFID_PREFILTERS_TAB);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        initializeSpinner();
        AutoCompleteTextView tagIDField = ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID));
        RFIDController.getInstance().updateTagIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Application.tagIDs);
        tagIDField.setAdapter(adapter);
        tagIDField.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});
        preFilterOffset = ((EditText) getActivity().findViewById(R.id.preFilter2Offset));
        //preFilterOffset.setFilters(new InputFilter[]{new InputFilterMax(Long.valueOf(Constants.MAX_OFFSET))});
        preFilter2EnableFilter = (CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter);
        //Load the saved states
        loadPreFilterStates();

    }

    /**
     * method to initialize memory bank spinner
     */
    private void initializeSpinner() {

        Spinner memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_memory_bank_array, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        memoryBankSpinner.setAdapter(memoryBankAdapter);

        Spinner actionSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2Action);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> actionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_action_array, R.layout.spinner_small_font);
        // Specify the layout to use when the list of choices appears
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        actionSpinner.setAdapter(actionAdapter);

        Spinner targetSpinner = (Spinner) getActivity().findViewById(R.id.preFilter2Target);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_target_options, R.layout.custom_spinner_layout);
        // Specify the layout to use when the list of choices appears
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        targetSpinner.setAdapter(targetAdapter);
    }

    /**
     * Method to load the pre-filter states
     */
    private void loadPreFilterStates() {
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pre_filter_memory_bank_array, R.layout.custom_spinner_layout);

        if (RFIDController.preFilters != null && RFIDController.preFilters[1] != null) {
            PreFilters preFilter = RFIDController.preFilters[1];
            ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).setText(preFilter.getTag());
            preFilterOffset.setText("" + preFilter.getOffset());
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).setChecked(preFilter.isFilterEnabled());
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).jumpDrawablesToCurrentState();
            ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).setSelection(memoryBankAdapter.getPosition(preFilter.getMemoryBank().trim().toUpperCase()));
            ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).setSelection(preFilter.getAction());
            ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).setSelection(preFilter.getTarget());
            ((EditText) getActivity().findViewById(R.id.preFilter2Length)).setText(String.valueOf(preFilter.getBitCount()));

        } else {
            ((AutoCompleteTextView) getActivity().findViewById(R.id.preFilter2TagID)).setText("");
            preFilterOffset.setText("0");
            ((CheckBox) getActivity().findViewById(R.id.preFilter2EnableFilter)).setChecked(false);
            ((Spinner) getActivity().findViewById(R.id.preFilter2MemoryBank)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilter2Action)).setSelection(0);
            ((Spinner) getActivity().findViewById(R.id.preFilter2Target)).setSelection(0);
            ((EditText) getActivity().findViewById(R.id.preFilter2Length)).setText(String.valueOf(Constants.NO_OF_BITS));
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
