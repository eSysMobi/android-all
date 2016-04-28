package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.upnews_lite.UNLApp;

public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
    private transient List<String> md5set;
    private transient UNLApp mApp;
    private transient String mActName;
    private transient SharedPreferences prefs;
    private static final String TAG = "unTag_DeleteBrokeFiles";


    public DeleteBrokeFilesTask(UNLApp app, String actName) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        mActName = actName;
    }

    public DeleteBrokeFilesTask(UNLApp app, List<String> serverMD5, String actName) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        md5set = serverMD5;
        mActName = actName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetWork.isNetworkAvailable(mApp)) {
            if (!UNLApp.getIsDownloadTaskRunning()) {
                Log.d(TAG, "Start deleting broken files");

                String[] localMD5 = prefs.getString("localMD5","").split(",");
                String[] localNames = prefs.getString("localNames","").split(",");

                List<String> maskList = new ArrayList<>();  //masklist with names
                Log.d(TAG, "md5 list " + md5set.toString());
                Log.d(TAG, "md5 folder list " + Arrays.toString(localMD5));
                if (md5set.size() == 0 && localNames.length > 0) {
                    //delete all local video-files
                    //maskList.add("unLiteDelAll");
                } else {
                    for (int i = 0; i < localMD5.length; i++) {
                        if (!md5set.contains(localMD5[i])) {
                            maskList.add(localNames[i]);
                        }
                    }
                }
                Log.d(TAG, "mask list task " + maskList.toString());
                UNLApp.setIsDeleting(true);
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME +
                                UNLConsts.GD_STORAGE_DIR_NAME +
                                "/");
                directoryWorks.deleteFilesFromDir(maskList);
            } else {
                cancel(true);
            }
        } else {
            cancel(true);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if ("first".equals(mActName)) {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "video_deleting");
            intentOut.putExtra("recToMP_message", "Video delete has been ended");
            mApp.sendBroadcast(intentOut);
        } else {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "video_deleting");
            intentOut.putExtra("recToMP_message", "Video delete has been ended");
            mApp.sendBroadcast(intentOut);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if ("first".equals(mActName)) {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "video_deleting");
            intentOut.putExtra("recToMP_message", "Video delete has been canceled");
            mApp.sendBroadcast(intentOut);
        } else {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "video_deleting");
            intentOut.putExtra("recToMP_message", "Video delete has been canceled");
            mApp.sendBroadcast(intentOut);
        }
    }
}
