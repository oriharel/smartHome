package niyo.nfc.com.nfcori;

/**
 * Created by oriharel on 26/05/2017.
 */

public abstract class SensorsStateListener {
    public abstract void onChange(Boolean doorStatus, Long doorTime, Boolean ginaStatus, Long ginaTime);
}
