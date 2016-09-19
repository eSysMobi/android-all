package mobi.esys.taskmanager;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;

import mobi.esys.data.GDFile;
import mobi.esys.server.UNLServer;
import mobi.esys.tasks.CheckAndGetLogoFromGDriveTask;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.RSSTask;
import mobi.esys.tasks.SendStatisticsToGD;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 17.03.2016.
 * Class singleton for managing tasks:
 * 0 - getting logo             //first because this task starts only 1 time
 * 1 - check faces              //starts every time when starts new video
 * 2 - getting rss
 * 3 - download
 * 4 - sending statistics in GD
 */
public class TaskManager extends Handler {
    private static final String TAG = "unTag_TaskManager";
    private static volatile TaskManager instance;
    private boolean[] tasks = new boolean[5];
    private int currentTask;

    private boolean isRunning = false;

    private UNLApp mApp;
    private transient UNLServer server = null;
    private transient List<GDFile> gdFiles = null;
    private transient String serverMD5 = null;

    private byte rssCurrCount = 0;
    private byte rssMaxCount = 20;

    private byte statCurrCount = 2;
    private byte statMaxCount = 7;

    //constructor
    public TaskManager(UNLApp incomingApp) {
        this.mApp = incomingApp;
        init();
    }

    public void startAllTask() {
        if (mApp != null) {
            Log.d(TAG, "Start executing all tasks");
            if (!isRunning) {
                isRunning = true;
                GetServer gs = new GetServer();
                gs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Log.d(TAG, "Can't execute all task again. Previous executing in progress...");
            }
        } else {
            Log.d(TAG, "Need initializing. Use init(UNLApp incomingApp)");
        }
    }

    private void startTask(int numOfTask) {
        if (tasks[numOfTask]) {
            switch (numOfTask) {
                case 0:
                    Log.d(TAG, "Start task 0 (LOGO)");
                    CheckAndGetLogoFromGDriveTask task = new CheckAndGetLogoFromGDriveTask(mApp, this, server.getGdLogo());
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    tasks[0] = false;
                    break;
                case 1:
                    Log.d(TAG, "Start task 1 (CHECK FACES)");
                    //TODO start
                    nextTask();
                    break;
                case 2:
                    if (rssCurrCount <= 0) {
                        Log.d(TAG, "Start task 2 (RSS)");
                        rssCurrCount = rssMaxCount;
                        RSSTask rssTask = new RSSTask(mApp, this, server.getGdRSS());
                        rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        Log.d(TAG, "RSS task 2 started after " + rssCurrCount + " videos");
                        rssCurrCount--;
                        nextTask();
                    }
                    break;
                case 3:
                    Log.d(TAG, "Start task 3 (DOWNLOAD)");
                    DownloadVideoTask downloadVideoTask = new DownloadVideoTask(mApp, this, gdFiles, serverMD5);
                    downloadVideoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case 4:
                    if (statCurrCount <= 0) {
                        Log.d(TAG, "Start task 4 (SEND STATISTICS)");
                        statCurrCount = statMaxCount;
                        SendStatisticsToGD sstGD = new SendStatisticsToGD(mApp, this);
                        sstGD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        Log.d(TAG, "Send statistic task 4 started after " + statCurrCount + " videos");
                        statCurrCount--;
                        nextTask();
                    }
                    break;
            }
        } else {
            Log.d(TAG, "Not need execute task " + currentTask);
            currentTask++;
            if (currentTask >= tasks.length) {
                //tasks cycle is complete
                currentTask = 0;
                isRunning = false;
                Log.d(TAG, "All tasks is ended");
            } else {
                startTask(currentTask);
            }
        }
    }

    private void nextTask() {
        Log.d(TAG, "Task " + currentTask + " is ended");
        currentTask++;
        if (currentTask >= tasks.length) {
            //tasks cycle is complete
            currentTask = 0;
            isRunning = false;
            Log.d(TAG, "All tasks is ended");
        } else {
            startTask(currentTask);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 42) {  //all ok
            nextTask();
        }
        if (msg.what == 43) {  //clear all
            init();
        }
        super.handleMessage(msg);
    }

    private void init() {
        currentTask = 0;
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = true;
        }
    }

    private class GetServer extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            server = new UNLServer(mApp);
            serverMD5 = server.getMD5FromServer();
            gdFiles = server.getGdFiles();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            startTask(currentTask);
        }
    }

}
