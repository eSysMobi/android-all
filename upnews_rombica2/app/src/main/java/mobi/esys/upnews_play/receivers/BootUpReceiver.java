package mobi.esys.upnews_play.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

import mobi.esys.upnews_play.MainActivity_;
import mobi.esys.upnews_play.consts.Consts;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                    context.startActivity(new Intent(context, MainActivity_.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }, Consts.APP_DELAY);
    }






}
