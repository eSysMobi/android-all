package mobi.esys.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

public class Playback {
    private transient MediaController mController;
    private transient Context mContext;
    private transient VideoView mVideo;
    private static final String TAG = "unTag_Playback";
    private transient String[] files;
    private transient String[] ulrs = {""};
    private transient String nameCurrentPlayedFile = "";
    private transient int serverIndex = 0;
    private transient SharedPreferences prefs;
    private transient Set<String> md5sApp;
    private transient UNLApp mApp;
    private transient DownloadVideoTask downloadVideoTask;


    //127578844442-9qab0sqd5p13fhhs671lg1joqetcvj7k debug
    //127578844442-h41s9f3md1ni2soa7e3t3rpuqrukkd1u release

    public Playback(Context context, UNLApp app) {
        super();
        mController = new MediaController(context);
        mVideo = ((FullscreenActivity) context).getVideoView();
        mVideo.setMediaController(mController);
        mVideo.requestFocus();
        mContext = context;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        Set<String> defaultSet = new HashSet<String>();
        md5sApp = prefs.getStringSet("md5sApp", defaultSet);
    }


    public void playFile(String filePath) {
        Set<String> defaultSet = new HashSet<String>();
        md5sApp = prefs.getStringSet("md5sApp", defaultSet);  //why here, but not in constructor?
        File file = new File(filePath);
        FileWorks fileWorks = new FileWorks(filePath);
        if (file.exists() && md5sApp.contains(fileWorks.getFileMD5())) {
            UNLApp.setCurPlayFile(filePath);
            mVideo.setVideoURI(Uri.parse(filePath));
            mVideo.start();
        } else {
            nextTrack(files);
        }
    }

    public void playFolder() {
        downloadVideoTask = new DownloadVideoTask(mApp, mContext, "full");
        downloadVideoTask.execute();
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME +
                UNLConsts.GD_STORAGE_DIR_NAME +
                "/");
        files = directoryWorks.getDirFileList("play folder");
        this.mVideo.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                nextTrack(files);
                return true;
            }
        });
        if (files.length > 0) {
            //playFile(files[0]);
            nextTrack(files);
            mVideo.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    //TODO using cameras
                    Log.d(TAG, "Start face counting after ending file " + nameCurrentPlayedFile);
                    CameraShotTask csTask = new CameraShotTask(mContext,nameCurrentPlayedFile);
                    csTask.start();

                    nextTrack(files);
                    restartDownload();
                    ((FullscreenActivity) mContext).restartCreepingLine();
                }

            });

            mVideo.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG,"MediaPlayer error " + String.valueOf(what) + ":" + String.valueOf(extra));
                    nextTrack(files);
                    return false;
                }
            });

            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mController.show();
                    LinearLayout ll = (LinearLayout) mController.getChildAt(0);
                    for (int i = 0; i < ll.getChildCount(); i++) {
                        if (ll.getChildAt(i) instanceof LinearLayout) {
                            LinearLayout llC = (LinearLayout) ll.getChildAt(i);
                            for (int j = 0; j < llC.getChildCount(); j++) {
                                if (llC.getChildAt(j) instanceof SeekBar) {
                                    SeekBar seekBar = (SeekBar) llC.getChildAt(j);
                                    seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbartheme_scrubber_progress_horizontal_holo_dark));
                                    seekBar.setThumb(mContext.getResources().getDrawable(R.drawable.seekbartheme_scrubber_control_selector_holo_dark));
                                }
                            }
                        }

                    }

                    ((FullscreenActivity) mContext).restartCreepingLine();
                }
            });
        } else {
            Log.d(TAG, "File list is empty");
            mContext.startActivity(new Intent(mContext,
                    FirstVideoActivity.class));
            ((Activity) mContext).finish();
        }

        ((FullscreenActivity) mContext).recToMP("playlist_video_play", "Start playing playlist video");
    }

    //if need check logo on GoogleDrive
    private void signalCheckNewLogo() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.STATUS_GET_LOGO, UNLConsts.STATUS_NEED_CHECK_LOGO);
        mApp.sendBroadcast(intentOut);
    }

    private void nextTrack(final String[] files) {

        String[] listFiles = {files[0]};
//        Log.d("log_tag", "Send signal checking logo");
//        signalCheckNewLogo();

        if (!prefs.getString("urls", "").replace("[", "").replace("]", "").equals("")) {
            Log.d(TAG,"urls string " +
                    prefs.getString("urls", "")
                            .replace("[", "").replace("]", ""));

            ulrs = prefs.getString("urls", "")
                    .replace("[", "").replace("]", "").split(",");

            if (files.length > 0) {
                listFiles = new String[ulrs.length];
                for (int i = 0; i < listFiles.length; i++) {
                    ulrs[i] = ulrs[i].trim();
                    listFiles[i] = UNLApp.getAppExtCachePath()
                            + UNLConsts.VIDEO_DIR_NAME
                            + UNLConsts.GD_STORAGE_DIR_NAME
                            + "/"
                            + ulrs[i]
                            .substring(ulrs[i].lastIndexOf('/') + 1,
                                    ulrs[i].length()).replace("[", "")
                            .replace("]", "");
                }
                if (serverIndex >= listFiles.length) {
                    serverIndex=0;
                }

                Log.d(TAG,"urls next " + Arrays.asList(listFiles).toString());
                File fs = new File(listFiles[serverIndex]);
                Log.d(TAG,"next file^ " + fs.getAbsolutePath());
                String currDownFile = prefs.getString("currDownFile", "");
                Log.d(TAG, "current download " + currDownFile);
                if (fs.exists()) {
                    if (!currDownFile.equals(fs.getAbsolutePath())) {
                        FileWorks fileWorks = new FileWorks(fs.getAbsolutePath());

                        DirectoryWorks directoryWorks = new DirectoryWorks(
                                UNLConsts.VIDEO_DIR_NAME +
                                UNLConsts.GD_STORAGE_DIR_NAME +
                                "/");
                        String[] refreshFiles = directoryWorks.getDirFileList("folder");
                        Log.d(TAG,"files " + Arrays.asList(refreshFiles).toString());
                        if (!UNLConsts.TEMP_FILE_EXT.equals(fileWorks.getFileExtension())) {
                            if (md5sApp.contains(fileWorks.getFileMD5()) && Arrays.asList(refreshFiles).contains(fs.getAbsolutePath())) {

                                if (serverIndex >= listFiles.length - 1) {
                                    Log.d(TAG,"File index " + String.valueOf(serverIndex));
                                    Log.d(TAG,"Played files counts is " + String.valueOf(listFiles.length));
                                    nameCurrentPlayedFile = fs.getName();
                                    playFile(listFiles[serverIndex]);
                                    serverIndex = 0;
                                } else {
                                    nameCurrentPlayedFile = fs.getName();
                                    playFile(listFiles[serverIndex]);
                                    Log.d(TAG,"Played files counts is " + String.valueOf(listFiles.length));
                                    serverIndex++;
                                }
                            } else {
                                if (serverIndex >= listFiles.length - 1) {
                                    serverIndex=0;
                                } else {
                                    serverIndex++;
                                }
                                nextTrack(refreshFiles);
                            }
                        } else {
                            if (serverIndex >= listFiles.length - 1) {
                                serverIndex=0;
                            } else {
                                serverIndex++;
                            }
                            nextTrack(refreshFiles);
                        }
                    } else {
                        if (serverIndex >= listFiles.length - 1) {
                            serverIndex=0;
                        } else {
                            serverIndex++;
                        }
                        nextTrack(files);
                    }

                } else {
                    if (serverIndex >= listFiles.length - 1) {
                        serverIndex=0;
                    } else {
                        serverIndex++;
                    }
                    nextTrack(files);
                }
            }
        }else {
            mContext.startActivity(new Intent(mContext,FirstVideoActivity.class));
            ((Activity) mContext).finish();
        }

    }


    public void restartDownload() {
        if (!UNLApp.getIsDownloadTaskRunning()) {
            downloadVideoTask.cancel(true);
            downloadVideoTask = new DownloadVideoTask(mApp, mContext, "full");
            downloadVideoTask.execute();
        }
    }

    public void stopDownload() {
        downloadVideoTask.cancel(true);
    }


    public void restartPlayback() {
        playFile(files[0]);
        serverIndex = 0;
    }

}