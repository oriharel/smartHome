package niyo.nfc.com.nfcori.fragments;

import android.net.Uri;

import niyo.nfc.com.nfcori.CameraStateListener;
import niyo.nfc.com.nfcori.LampStateListener;
import niyo.nfc.com.nfcori.PresenceStateListener;
import niyo.nfc.com.nfcori.SensorsStateListener;

/**
 * Created by oriharel on 19/11/2016.
 */

public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
    void registerForLampsStateChange(LampStateListener listener);
    void registerForPresenceChange(PresenceStateListener listener);
    void registerForCameraChange(CameraStateListener listener);
    void registerForSensorsChange(SensorsStateListener listener);
}


