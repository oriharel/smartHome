package niyo.nfc.com.nfcori;

/**
 * Created by oriharel on 20/11/2016.
 */

public abstract class PresenceStateListener {
    public abstract void onChange(Boolean oriState,
                                  String oriSince,
                                  Boolean yifatState,
                                  String yifatSince,
                                  Boolean itchukState,
                                  String itchukSince,
                                  String lastUpdateTime);
}
