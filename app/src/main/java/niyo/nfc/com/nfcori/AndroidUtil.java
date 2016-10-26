package niyo.nfc.com.nfcori;

/**
 * Created by oriharel on 24/10/2016.
 */

public class AndroidUtil {

    public static String getArrayAsString(Object[] array)
    {
        String result = "";
        if (array != null) {
            for (Object object : array) {
                result += object.toString() + ", ";
            }
        }
        return result;
    }
}
