package mobi.esys.upnews_tv.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import mobi.esys.upnews_tv.UpnewsOnlineApp;


public class NetMonitor {
    public static boolean isNetworkAvailable(UpnewsOnlineApp app) {

        Context context = app.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

//        boolean state = false;
//        NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
//
//
//        for (int i = 0; i < info.length; i++) {
//            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                state = true;
//                break;
//            }
//        }
//        return state;

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

        //for emulator
//        return true;
    }
}
