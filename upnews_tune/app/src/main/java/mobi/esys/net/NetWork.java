package mobi.esys.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import mobi.esys.upnews_tune.UNLApp;

/**
 * Created by Артем on 02.03.2015.
 */
public class NetWork {

    public static final boolean isNetworkAvailable(UNLApp app) {
        Context context = app.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobileInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo.State mobile = NetworkInfo.State.DISCONNECTED;
        if (mobileInfo != null) {
            mobile = mobileInfo.getState();
        }

        NetworkInfo wifiInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State wifi = NetworkInfo.State.DISCONNECTED;
        if (wifiInfo != null) {
            wifi = wifiInfo.getState();
        }

        if (mobile.equals(NetworkInfo.State.CONNECTED) || wifi.equals(NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }
}
