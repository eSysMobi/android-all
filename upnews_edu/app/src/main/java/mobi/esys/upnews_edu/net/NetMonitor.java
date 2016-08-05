package mobi.esys.upnews_edu.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import mobi.esys.upnews_edu.UpnewsApp;


public class NetMonitor {
    public static boolean isNetworkAvailable(UpnewsApp app) {

        Context context = app.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo.State mobile = NetworkInfo.State.DISCONNECTED;
        if (mobileInfo != null) {
            mobile = mobileInfo.getState();
        }

        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State wifi = NetworkInfo.State.DISCONNECTED;
        if (wifiInfo != null) {
            wifi = wifiInfo.getState();
        }

        return (mobile.equals(NetworkInfo.State.CONNECTED) || wifi.equals(NetworkInfo.State.CONNECTED));
    }
}
