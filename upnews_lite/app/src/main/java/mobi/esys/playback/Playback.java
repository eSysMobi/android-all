package mobi.esys.playback;

import android.content.Context;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.UNLConsts;
import mobi.esys.events.EventCameraShot;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.taskmanager.TaskManager;
import mobi.esys.upnews_lite.MainActivity;
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

    private Uri defaultVideoURI;

    private EventBus bus = EventBus.getDefault();

    //127578844442-9qab0sqd5p13fhhs671lg1joqetcvj7k debug
    //127578844442-h41s9f3md1ni2soa7e3t3rpuqrukkd1u release

    public Playback(Context context, UNLApp app, VideoView videoView, Uri defaultVideoURI, TaskManager incTM) {
        super();
        Log.d(TAG, "New playback");
        tm = incTM;
        mController = new MediaController(context);
        //mController.setPadding(0, 0, 0, 50);  //if need up controls
        mVideo = videoView;
        mVideo.setMediaController(mController);
        mVideo.requestFocus();
        mContext = context;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        this.defaultVideoURI = defaultVideoURI;
    }

    public void playFolder() {
        mVideo.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                bus.post(new EventCameraShot(nameCurrentPlayedFile));
                nextTrack();
                tm.startAllTask();
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
                //change controller theme
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
                ((MainActivity) mContext).forceSetUISmall();
            }
        });

        nextTrack();
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
        for (String file : files) {
            for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
                if (file.endsWith("." + UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
                    videoFiles.add(file);
                    break;
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
                            if (i < localMD5Array.length) {
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
            } else {
                String tmpFileName = "";
                for (int i = 0; i < localMD5Array.length; i++) {
                    if (localMD5Array[i].equals(md5sAppArray[videofileIndex])) {
                        if (i < localNamesArray.length) {
                            tmpFileName = localNamesArray[i];
                        }
                        break;
                    }
                }

                for (int i = 0; i < videoFiles.size(); i++) {
                    if (videoFiles.get(i).equals(tmpFileName)) {
                        videofileLocalIndex = i;
                        break;
                    }
                }
            }

            if (videofileLocalIndex >= videoFiles.size()) {
                videofileLocalIndex = 0;
            }

            File fs = new File(videoFiles.get(videofileLocalIndex));

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
            mVideo.setVideoURI(defaultVideoURI);
            mVideo.start();
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
}