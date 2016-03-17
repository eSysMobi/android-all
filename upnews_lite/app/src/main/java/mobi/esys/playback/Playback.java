package mobi.esys.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
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
    private transient String nameCurrentPlayedFile = "";
    private transient int videofileIndex = 0;
    private transient SharedPreferences prefs;
    private transient Set<String> md5sApp;
    private transient UNLApp mApp;
    private transient DownloadVideoTask downloadVideoTask;
    private transient boolean isFirstSession = true;

    private transient DirectoryWorks directoryWorks;

    //127578844442-9qab0sqd5p13fhhs671lg1joqetcvj7k debug
    //127578844442-h41s9f3md1ni2soa7e3t3rpuqrukkd1u release

    public Playback(Context context, UNLApp app) {
        super();
        Log.d(TAG, "New playback");
        mController = new MediaController(context);
        //mController.setPadding(0, 0, 0, 50);  //if need up controls
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
//            mController.setFocusable(false);
//            mController.setSelected(false);
//            mController.setFocusableInTouchMode(false);

            mVideo.setVideoURI(Uri.parse(filePath));
            mVideo.start();
        } else {
            nextTrack();
        }
    }

    public void playFolder() {
        downloadVideoTask = new DownloadVideoTask(mApp, mContext, "full");
        downloadVideoTask.execute();
        directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
        files = directoryWorks.getDirFileList("play folder");
        boolean haveVideoFile = false;
        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
                if (files[i].contains(UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
                    haveVideoFile = true;
                    break;
                }
            }
        }
        if (haveVideoFile) {
            //playFile(files[0]);
            nextTrack();
            mVideo.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    signalDetectFaces();

                    nextTrack();
                    restartDownload();
                }

            });

            mVideo.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "MediaPlayer error " + String.valueOf(what) + ":" + String.valueOf(extra));
                    nextTrack();
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
                }
            });
        } else {
            Log.d(TAG, "File list is empty");
            mContext.startActivity(new Intent(mContext, FirstVideoActivity.class));
            ((Activity) mContext).finish();
        }

        ((FullscreenActivity) mContext).recToMP("playlist_video_play", "Start playing playlist video");
    }

    //if need check logo on GoogleDrive
    private void signalCheckNewLogo() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.STATUS_NEED_CHECK_LOGO);
        mApp.sendBroadcast(intentOut);
    }

    //if need do a face detect from cameras
    private void signalDetectFaces() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_CAMERASHOT);
        intentOut.putExtra("nameCurrentPlayedFile", nameCurrentPlayedFile);
        mApp.sendBroadcast(intentOut);
    }

    private void nextTrack() {
        files = directoryWorks.getDirFileList("play folder");
        //get only videofiles
        List<String> videoFiles = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
                if (files[i].contains(UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
                    videoFiles.add(files[i]);
                    //break;    //is this ended both cycles?
                }
            }
        }

        Log.d(TAG, "Video files on device (" + videoFiles.size() + "): " + videoFiles.toString());
        if (videoFiles.size() > 0) {

            //check and restored lastPlayedFileIndex
            if (isFirstSession) {
                int lastPlayedFileIndex = 0;
                String lastPlayedFileName = prefs.getString("lastPlayedFileName", "");
                for (int i = 0; i < videoFiles.size(); i++) {
                    if (videoFiles.get(i).contains(lastPlayedFileName)) {
                        lastPlayedFileIndex = i;
                        break;
                    }
                }
                isFirstSession = false;
                videofileIndex = lastPlayedFileIndex;
            }
            if (videofileIndex >= videoFiles.size()) {
                videofileIndex = 0;
            }

            File fs = new File(videoFiles.get(videofileIndex));
            Log.d(TAG, "next file: " + fs.getName());
            if(fs.exists()){
                playFile(videoFiles.get(videofileIndex));
                videofileIndex++;
                if (videofileIndex >= videoFiles.size()) {
                    videofileIndex = 0;
                }
                //save lastPlayedFileName
                nameCurrentPlayedFile = fs.getName();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastPlayedFileName", nameCurrentPlayedFile);
                editor.apply();
            }else{
                Log.d(TAG, "File not exists!");
                videofileIndex++;
                nextTrack();
            }
        } else {
            mContext.startActivity(new Intent(mContext, FirstVideoActivity.class));
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
        videofileIndex = 0;
    }

}