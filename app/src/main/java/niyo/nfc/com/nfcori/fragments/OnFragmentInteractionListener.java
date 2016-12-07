package niyo.nfc.com.nfcori.fragments;

import android.net.Uri;

import niyo.nfc.com.nfcori.LampStateListener;
import niyo.nfc.com.nfcori.PresenceStateListener;

/**
 * Created by oriharel on 19/11/2016.
 */

public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
    void registerForLampsStateChange(LampStateListener listener);
    void registerForPresenceChange(PresenceStateListener listener);
}


