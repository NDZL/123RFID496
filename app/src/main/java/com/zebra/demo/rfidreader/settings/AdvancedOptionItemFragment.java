package com.zebra.demo.rfidreader.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import static com.zebra.demo.rfidreader.settings.AdvancedOptionsContent.DPO_ITEM_INDEX;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnAdvancedListFragmentInteractionListener}
 * interface.
 */
public class AdvancedOptionItemFragment extends BackPressedFragment {

    // TODO: Customize parameters
    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private AdvancedOptionItemAdapter advancedOptionItemAdapter;

    private OnAdvancedListFragmentInteractionListener mListener;

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdvancedOptionItemFragment newInstance() {
        AdvancedOptionItemFragment fragment = new AdvancedOptionItemFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdvancedOptionItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advancedoptionitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            advancedOptionItemAdapter = new AdvancedOptionItemAdapter(AdvancedOptionsContent.ITEMS, mListener);
            recyclerView.setAdapter(advancedOptionItemAdapter);
            recyclerView.addItemDecoration(new
                    DividerItemDecoration(getActivity(),
                    DividerItemDecoration.VERTICAL));
        }
        getActivity().setTitle(SettingsContent.ITEM_MAP.get(R.id.advanced_options + "").content);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAdvancedListFragmentInteractionListener) {
            mListener = (OnAdvancedListFragmentInteractionListener) context;
        } else if(context instanceof ActiveDeviceActivity) {
            mListener = (OnAdvancedListFragmentInteractionListener) context;
        }else{
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAdvancedListFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnAdvancedListFragmentInteractionListener(AdvancedOptionsContent.SettingItem item);
    }

    @Override
    public void onBackPressed() {
        if(getActivity() instanceof SettingsDetailActivity)
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        else if (getActivity() instanceof  ActiveDeviceActivity)
            ((ActiveDeviceActivity) getActivity()).callBackPressed();

    }

    @Override
    public void onResume() {
        super.onResume();
        settingsListUpdated();

    }

    public void settingsListUpdated() {
        //
        if (RFIDController.dynamicPowerSettings != null && RFIDController.dynamicPowerSettings.getValue() == 1)
            AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_enabled;
        else {
            AdvancedOptionsContent.ITEMS.get(DPO_ITEM_INDEX).icon = R.drawable.title_dpo_disabled;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (advancedOptionItemAdapter != null)
                    advancedOptionItemAdapter.notifyDataSetChanged();
            }
        });
    }


}
