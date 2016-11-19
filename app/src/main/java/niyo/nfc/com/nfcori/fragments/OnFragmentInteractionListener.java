package niyo.nfc.com.nfcori.fragments;

import android.net.Uri;

import niyo.nfc.com.nfcori.LampStateListener;

/**
 * Created by oriharel on 19/11/2016.
 */

public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
    void registerForLampsStateChange(LampStateListener listener);
}


