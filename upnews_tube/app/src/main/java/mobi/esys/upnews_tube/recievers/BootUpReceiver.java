package mobi.esys.upnews_tube.recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Timer;
import java.util.TimerTask;

import mobi.esys.upnews_tube.YouTubeSelectActivity;
import mobi.esys.upnews_tube.constants.OtherConst;
import mobi.esys.upnews_tube.constants.TimeConsts;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SharedPreferences prefs = context.getSharedPreferences(OtherConst.APP_PREF, Context.MODE_PRIVATE);
                    String instHashtag = prefs.getString("instHashTag", "");
                    String ytPlaylistID = prefs.getString(OtherConst.APP_PREF_PLAYLIST, "");
                    if (!instHashtag.isEmpty() && !ytPlaylistID.isEmpty()) {
                        context.startActivity(new Intent(context, YouTubeSelectActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
