package niyo.nfc.com.nfcori;

/**
 * Created by oriharel on 19/11/2016.
 */

public abstract class LampStateListener {
    public abstract void onChange(Boolean tallState,
                                  Boolean sofaState,
                                  Boolean windowState,
                                  String temp);
}
