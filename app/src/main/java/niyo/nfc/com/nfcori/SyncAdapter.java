package niyo.nfc.com.nfcori;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by oriharel on 24/10/2016.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    Context mContext;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "SyncAdapter constructor 1 called");
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.d(LOG_TAG, "SyncAdapter constructor 2 called");
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
    }

    private void startService() {
        Intent serviceIntent = new Intent(mContext, HomeStateFetchService.class);
        serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                HomeStateFetchService.LAST_STATE_EVENT_NAME);
        mContext.startService(serviceIntent);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {


        Log.d(LOG_TAG, "onPerformSync started");
        startService();

    }
}
