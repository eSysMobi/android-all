package mobi.esys.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.UNLApp;

public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
    private transient Set<String> md5set;
    private transient UNLApp mApp;
    private transient String mActName;
    private transient Context mContext;
    private static final String TAG = "unTag_DeleteBrokeFiles";


    public DeleteBrokeFilesTask(UNLApp app, Context context, String actName) {
        mApp = app;
        mActName = actName;
        mContext = context;
    }

    public DeleteBrokeFilesTask(UNLApp app, Context context, Set<String> serverMD5, String actName) {
        mApp = app;
        md5set = serverMD5;
        mActName = actName;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetWork.isNetworkAvailable(mApp)) {
            if (!UNLApp.getIsDownloadTaskRunning()) {
                Log.d(TAG, "Start deleting broken files");
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
                String[] folderFiles = directoryWorks.getDirFileList("del");
                List<String> folderMD5s = directoryWorks.getMD5Sums();
                List<String> maskList = new ArrayList<>();  //masklist with names
                if (md5set==null) {
                    UNLServer server = new UNLServer(mApp);
                    md5set = server.getMD5FromServer();
                }
                List<String> md5sList = new ArrayList<String>();
                md5sList.addAll(md5set);
                Log.d(TAG, "md5 list " + md5sList.toString());
                Log.d(TAG, "md5 folder list " + folderMD5s.toString());
                if (md5sList.size() == 0 && folderFiles.length > 0) {
                    //delete all local video-files
                    //maskList.add("unLiteDelAll");
                } else {
                    for (int i = 0; i < folderMD5s.size(); i++) {
                        if (!md5sList.contains(folderMD5s.get(i))) {
                            maskList.add(folderFiles[i]);
                        }
                    }
                }
                Log.d(TAG, "mask list task " + maskList.toString());
                UNLApp.setIsDeleting(true);
                directoryWorks.deleteFilesFromDir(maskList, mApp.getApplicationContext());
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
            ((FirstVideoActivity) mContext).recToMP("video_deleting", "Video delete has been ended");
        } else {
            ((FullscreenActivity) mContext).recToMP("video_deleting", "Video delete has been ended");

        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if ("first".equals(mActName)) {
            ((FirstVideoActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");
        } else {
            ((FullscreenActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");

        }
    }
}
