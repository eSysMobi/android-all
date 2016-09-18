package mobi.esys.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import mobi.esys.UNLConsts;
import mobi.esys.upnews_lite.DriveAuthActivity;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(UNLConsts.APP_START_DELAY);
            } catch (InterruptedException e) {
            }
            SharedPreferences prefs = context.getSharedPreferences(UNLConsts.APP_PREF, 0);
            String accName = prefs.getString("accName", "");
            if (!accName.isEmpty()) {
                context.startActivity(new Intent(context, DriveAuthActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        }
    }
}
