package mobi.esys.upnews_tv.download;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.upnews_tv.constants.Folders;
import mobi.esys.upnews_tv.facebook.FacebookVideoItem;

public class FacebookVideoDownloadHelper {
    private transient DownloadManager downloadManager;
    private transient String downloadDir;
    private transient int currentDownloadIndex;
    private transient List<FacebookVideoItem> fbVideos;
    private transient List<File> filesMask;
    private transient List<Long> enqueues;
    private transient File currentFile;
    private transient File doneFile;
    private transient long enqueue;
    private transient String TAG = "unTag_FVideoDownHelper";
    private transient boolean isInited = false;

    private transient DownloadState currentState = DownloadState.DOWNLOAD_END;

    public static class FacebookVideoDownloadHelperHolder {
        public static final FacebookVideoDownloadHelper HOLDER_INSTANCE = new FacebookVideoDownloadHelper();
    }

    public static FacebookVideoDownloadHelper getInstance() {
        return FacebookVideoDownloadHelperHolder.HOLDER_INSTANCE;
    }

    public void init(String downFolder, List<FacebookVideoItem> fbVideos, Context context) {
        this.downloadDir = downFolder;
        this.fbVideos = fbVideos;
        currentDownloadIndex = 0;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        enqueue = 0l;
        isInited = true;
        registerReceiverInFacebookVDHelper(context);
        //context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)); //manual register reciever
        this.filesMask = new ArrayList<>();
        enqueues = new ArrayList<>();
        Log.d(TAG, "fbVideos: " + fbVideos.toString());
        Log.d(TAG, "fbVideos num: " + fbVideos.size());
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "receiver alert, action: " + action);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Log.d(TAG, "tmp file: " + currentFile.getAbsolutePath());
                Log.d(TAG, "done file: " + doneFile.getAbsolutePath());

                //write from tmp file
                int  attemptNum = 0;
                try {
                    if (currentFile.length() > 0) {
                        FileUtils.moveFile(currentFile, doneFile);
                        attemptNum++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        Log.d(TAG, "error down: " + e.getMessage());
                        Log.d(TAG, "cause: " + e.getCause());
                    }
                }
                //repeat if not moved
                if (attemptNum==0) {
                    try {
                        if (currentFile.length() > 0) {
                            FileUtils.moveFile(currentFile, doneFile);
                            attemptNum++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e.getMessage() != null) {
                            Log.d(TAG, "error down: " + e.getMessage());
                            Log.d(TAG, "cause: " + e.getCause());
                        }
                    }
                }
                if (attemptNum>0) {
                    Log.d(TAG, "File successfully writed. Attempt " + attemptNum);
                    filesMask.add(doneFile);
                    //clear tmp file
                    if (currentFile.exists()) {
                        boolean del = currentFile.delete();
                        Log.d(TAG, "File " + currentFile.getName() + " deleted = " + del);
                    }
                } else {
                    Log.d(TAG, "File NOT writed");
                }

                //check and change index
                currentDownloadIndex++;
                if (currentDownloadIndex < fbVideos.size() - 1) {
                    getVidFromUrl(fbVideos.get(currentDownloadIndex));
                }
                if (currentDownloadIndex == fbVideos.size() - 1) {
                    setCurrentState(DownloadState.DOWNLOAD_END);
                    currentDownloadIndex = 0;
                    Log.d(TAG, "facebook mask " + filesMask.toString());
                    String path = Folders.SD_CARD.concat(File.separator).concat(downloadDir);
                    File[] downDirList = new File(path).listFiles();
                    for (File file : downDirList) {
                        if (!filesMask.contains(file)) {
                            Log.d(TAG, "facebook delete file: " + file.getAbsolutePath());
                            file.delete();
                        }
                    }
                }
            }
        }
    };


    public void download() {
        setCurrentState(DownloadState.DOWNLOAD_IN_PROGRESS);
        getVidFromUrl(fbVideos.get(0));
    }

    public void getVidFromUrl(FacebookVideoItem fbVideo) {
        String path = Folders.SD_CARD.concat(File.separator).concat(downloadDir);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(fbVideo.getSource()));
        String fileName = fbVideo.getId().concat(".tmp");
        String currentFileName = fbVideo.getId().concat(".mp4");
        currentFile = new File(path, fileName);
        doneFile = new File(path, currentFileName);

        if (!doneFile.exists()) {
            if (currentFile.exists()) {
                currentFile.delete();
                try {
                    currentFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "download fb video: " + fileName);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    | DownloadManager.Request.NETWORK_MOBILE).
                    setVisibleInDownloadsUi(false).
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN).
                    setAllowedOverRoaming(false).setDestinationInExternalPublicDir(downloadDir, fileName);
            enqueue = downloadManager.enqueue(request);
            enqueues.add(enqueue);
        } else {
            Log.d(TAG, "We have file " + doneFile.getName() + " Not need download.");
            if(currentFile.exists()){
                currentFile.delete();
                Log.d(TAG, "Tmp file deleted");
            }
            currentDownloadIndex++;
            if (currentDownloadIndex < fbVideos.size() - 1) {
                getVidFromUrl(fbVideos.get(currentDownloadIndex));
            } else {
                Log.d(TAG, "We have all videos!");
                //clear all tmp-files
                File[] downDirList = new File(path).listFiles();
                for (File file : downDirList) {
                    if (file.getName().contains(".tmp")) {
                        Log.d(TAG, "Delete tmp file: " + file.getAbsolutePath());
                        file.delete();
                    }
                }
                setCurrentState(DownloadState.DOWNLOAD_END);
                currentDownloadIndex = 0;
            }
        }
    }

    public void registerReceiverInFacebookVDHelper(Context incomingContext) { //TODO use it in PlayerActivity
        if (isInited) {
            incomingContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            //incomingContext.registerReceiver(receiver, new IntentFilter(String.valueOf(DownloadManager.ERROR_FILE_ERROR)));
            Log.d(TAG, "Register receiver");
        } else {
            Log.d(TAG, "Receiver not registered because FacebookVideoDownloadHelper not initialized");
        }
    }

    public void unRegisterReceiverInFacebookVDHelper(Context incomingContext) { //TODO use it in PlayerActivity
        incomingContext.unregisterReceiver(receiver);
        Log.d(TAG, "Unregister receiver");
    }

    public DownloadState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DownloadState currentState) {
        this.currentState = currentState;
    }

    public int getCurrentDownloadIndex() {
        return currentDownloadIndex;
    }

    public void setCurrentDownloadIndex(int currentDownloadIndex) {
        this.currentDownloadIndex = currentDownloadIndex;
    }
}
