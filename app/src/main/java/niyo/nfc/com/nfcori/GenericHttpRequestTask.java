package niyo.nfc.com.nfcori;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GenericHttpRequestTask extends AsyncTask<String, Void, Integer> {
	
	private static final String LOG_TAG = GenericHttpRequestTask.class.getSimpleName();
	private ServiceCaller mCaller;
	private String mMsg;
    public static final String BASE_URL = "http://niyoapi.appspot.com";
	
	public GenericHttpRequestTask(ServiceCaller caller) {
		
		mCaller = caller;
	}

	@Override
	protected Integer doInBackground(String... params) {
		
        try {
        	
        	URL url = new URL(params[0]);
        	Log.d(LOG_TAG, "starting http request with "+url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (params.length > 1) {
                con.setDoOutput(Boolean.valueOf(params[1]));
            }
            int sc = con.getResponseCode();
            Log.d(LOG_TAG, url.getHost()+" returned "+sc);
            if (sc == 200) {
              InputStream is = con.getInputStream();
              mMsg = readResponse(is);
              is.close();
              return sc;
            } else if (sc == 401) {
                Log.e(LOG_TAG, "Server auth error, please try again.");
                mMsg = "Server auth error: " + readResponse(con.getErrorStream());
                Log.i(LOG_TAG, mMsg);
                return sc;
            } else {
            	mMsg = "Server returned the following error code: " + sc;
                Log.e(LOG_TAG, mMsg);
            	return sc;
            }
        } catch (Exception ex) {
        	mMsg = "Error:" + ex.getMessage();
            ex.printStackTrace();
        }
        return -1;
	}
	
	private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }
	
	@Override
	protected void onPostExecute(Integer result) {
         if (result != 200) {
             Log.e(LOG_TAG, "ERROR!! "+mMsg);
        	 mCaller.failure(result, mMsg);
         }
         else {
//        	 ClientLog.d(LOG_TAG, "Http request succeeded with "+mMsg);
        	 mCaller.success(mMsg);
         }
	}

}
