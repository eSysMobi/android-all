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
    private transient File currentFile;
    private transient File doneFile;
    private transient long enqueue;


    private transient DownloadState currentState;

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
        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        this.filesMask = new ArrayList<>();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Log.d("tmp file", currentFile.getAbsolutePath());
                Log.d("done file", doneFile.getAbsolutePath());
                filesMask.add(doneFile);
                try {
                    if (currentFile.length() > 0) {
                        FileUtils.moveFile(currentFile, doneFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        Log.d("down", e.getMessage());
                    }
                }

                currentDownloadIndex++;
                if (currentDownloadIndex < fbVideos.size() - 1) {
                    getVidFromUrl(fbVideos.get(currentDownloadIndex));
                }
                if (currentDownloadIndex == fbVideos.size() - 1) {
                    setCurrentState(DownloadState.DOWNLOAD_END);
                    Log.d("facebook mask", filesMask.toString());
                    String path = Folders.SD_CARD.concat(File.separator).concat(downloadDir);
                    File[] downDirList = new File(path).listFiles();
                    for (File file : downDirList) {
                        if (!filesMask.contains(file)) {
                            Log.d("facebook delete file", file.getAbsolutePath());
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
            Log.d("download fb video", fileName);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    | DownloadManager.Request.NETWORK_MOBILE).
                    setVisibleInDownloadsUi(false).
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN).
                    setAllowedOverRoaming(false).setDestinationInExternalPublicDir(downloadDir, fileName);
            enqueue = downloadManager.enqueue(request);
        } else {
            currentDownloadIndex++;
            if(currentDownloadIndex<fbVideos.size()-1) {
                getVidFromUrl(fbVideos.get(currentDownloadIndex));
            }
        }
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
