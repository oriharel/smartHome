package niyo.nfc.com.nfcori;

/**
 * Created by oriharel on 11/12/2016.
 */

public abstract class CameraStateListener {

    public abstract void onChange(byte[] homeImage64);
}
