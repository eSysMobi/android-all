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
import java.util.Arrays;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.taskmanager.TaskManager;
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
    private transient int videofileLocalIndex = 0;
    private transient SharedPreferences prefs;
    private transient UNLApp mApp;
    private transient boolean isFirstSession = true;

    private transient DirectoryWorks directoryWorks;

    private transient TaskManager tm;

    //127578844442-9qab0sqd5p13fhhs671lg1joqetcvj7k debug
    //127578844442-h41s9f3md1ni2soa7e3t3rpuqrukkd1u release

    public Playback(Context context, UNLApp app, TaskManager incTM) {
        super();
        Log.d(TAG, "New playback");
        tm = incTM;
        mController = new MediaController(context);
        //mController.setPadding(0, 0, 0, 50);  //if need up controls
        mVideo = ((FullscreenActivity) context).getVideoView();
        mVideo.setMediaController(mController);
        mVideo.requestFocus();
        mContext = context;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
    }

    public void playFolder() {
        directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
        files = directoryWorks.getDirFileList("play folder");
        boolean haveVideoFile = false;
        String localNames = prefs.getString("localNames", "");
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            if (localNames.contains(files[i]) && Arrays.asList(UNLConsts.UNL_ACCEPTED_FILE_EXTS).contains(fileWorks.getFileExtension())) {
                haveVideoFile = true;
                break;
            }
        }
//        for (int i = 0; i < files.length; i++) {
//            for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
//                if (files[i].contains(UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
//                    haveVideoFile = true;
//                    break;
//                }
//            }
//        }
        if (haveVideoFile) {
            nextTrack();
            mVideo.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    signalDetectFaces();

                    nextTrack();
                    restartTasks();
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
                    //hide elements
                    Log.d(TAG, "Force hide UiVisibility in mVideo.setOnPreparedListener");
                    ((FullscreenActivity) mContext).forceSetUISmall();
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
        String[] md5sAppArray = prefs.getString("md5sApp", "").split(",");
        String[] localMD5Array = prefs.getString("localMD5", "").split(",");
        String localNames = prefs.getString("localNames", "");
        String[] localNamesArray = localNames.split(",");

        if (videofileIndex >= md5sAppArray.length) {
            videofileIndex = 0;
        }

        files = directoryWorks.getDirFileList("play folder");

        //get only video files      TODO is this need with md5?
        List<String> videoFiles = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
                if (files[i].endsWith("." + UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
                    videoFiles.add(files[i]);
                    //break;    //is this ended both cycles?
                }
            }
        }
        Log.d(TAG, "Video files on device (" + videoFiles.size() + "): " + videoFiles.toString());
        if (videoFiles.size() > 0) {
            //check and restored lastPlayedFileIndex
            if (isFirstSession) {
                String lastPlayed = prefs.getString("lastPlayedFile", "");
                if (!lastPlayed.isEmpty()) {
                    int lastPlayedFileIndex = 0;
                    for (int i = 0; i < videoFiles.size(); i++) {
                        if (videoFiles.get(i).equals(lastPlayed)) {
                            lastPlayedFileIndex = i;
                            break;
                        }
                    }
                    videofileLocalIndex = lastPlayedFileIndex;
                    //get lastPlayedMD5
                    String lastPlayedMD5 = "";
                    for (int i = 0; i < localNamesArray.length; i++) {
                        if (localNamesArray[i].equals(videoFiles.get(videofileLocalIndex))) {
                            if (i<localMD5Array.length){
                                lastPlayedMD5 = localMD5Array[i];
                            }
                            break;
                        }
                    }
                    //get videofileIndex from hash
                    if (!lastPlayedMD5.isEmpty()) {
                        for (int i = 0; i < md5sAppArray.length; i++) {
                            if (md5sAppArray[i].equals(lastPlayedMD5)) {
                                videofileIndex = i;
                                break;
                            }
                        }
                    }

                }
                isFirstSession = false;
            } else{
                String tmpFileName = "";
                for (int i = 0; i < localMD5Array.length; i++) {
                    if(localMD5Array[i].equals(md5sAppArray[videofileIndex])){
                        if (i < localNamesArray.length){
                            tmpFileName = localNamesArray[i];
                        }
                        break;
                    }
                }

                for (int i = 0; i < videoFiles.size(); i++) {
                    if(videoFiles.get(i).equals(tmpFileName)){
                        videofileLocalIndex = i;
                        break;
                    }
                }
            }

            if (videofileLocalIndex >= videoFiles.size()) {
                videofileLocalIndex = 0;
            }

            File fs  = new File(videoFiles.get(videofileLocalIndex));

            //find result finding file with current md5
            if (fs != null) {
                Log.d(TAG, "next file: " + fs.getName());
                if (fs.exists()) {
                    //playFile(fs);

                    //mController.setFocusable(false);
                    //mController.setSelected(false);
                    //mController.setFocusableInTouchMode(false);
                    mVideo.setVideoURI(Uri.parse(fs.getPath()));
                    mVideo.start();

                    //save name of current played file
                    UNLApp.setCurPlayFile(fs.getPath());
                    //save lastPlayedFile
                    nameCurrentPlayedFile = fs.getName();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastPlayedFile", fs.getPath());
                    editor.apply();

                    videofileIndex++;
                    videofileLocalIndex++;
                } else {
                    Log.d(TAG, "File not exists!");
                    videofileIndex++;
                    videofileLocalIndex++;
                    nextTrack();
                }
            } else {
                Log.d(TAG, "We have no files associated with this videofileIndex");
                videofileIndex++;
                videofileLocalIndex++;
                nextTrack();
            }
        } else {
            Log.d(TAG, "We have no video files");
            mContext.startActivity(new Intent(mContext, FirstVideoActivity.class));
            ((Activity) mContext).finish();
        }
    }

    public void playFile(File file) {
        if (file.exists()) {
            UNLApp.setCurPlayFile(file.getPath());
            mVideo.setVideoURI(Uri.parse(file.getPath()));
            mVideo.start();
        } else {
            nextTrack();
        }
    }

    public void restartTasks() {
        tm.setNeedLogo(false);
        tm.setNeedRss(true);
        tm.setNeedDown(true);
        tm.setNeedSendStat(true);
        tm.startAllTask();
    }

}