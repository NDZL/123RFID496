package com.zebra.demo.rfidreader.settings;


import androidx.fragment.app.Fragment;

/**
 * Base Fragment class to handle {@link android.app.Activity#onBackPressed()} method for saving operations when the user presses back button
 */
public abstract class BackPressedFragment extends Fragment {

    /**
     * Method to be called when back button is pressed by the user
     */
    public abstract void onBackPressed();
}
