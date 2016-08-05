package mobi.esys.upnews_edu.recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Timer;
import java.util.TimerTask;

import mobi.esys.upnews_edu.LoginActivity;
import mobi.esys.upnews_edu.constants.TimeConsts;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SharedPreferences prefs = context.getSharedPreferences("unoPref", Context.MODE_PRIVATE);
                    String instHashtag = prefs.getString("instHashTag", "");
                    String fbGroupID = prefs.getString("fbGroupID", "");
                    if (instHashtag != null && fbGroupID != null) {
                        if (!instHashtag.equals("") && !fbGroupID.equals("")) {
                            context.startActivity(new Intent(context, LoginActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }
                    }
                }
            }, TimeConsts.APP_START_DELAY);
            try {
                Thread.sleep(TimeConsts.APP_START_DELAY);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
