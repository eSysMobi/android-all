package mobi.esys.upnews_lite;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.system.MarqueeTextView;
import mobi.esys.tasks.CreateDriveFolderTask;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.RSSTask;

public class FirstVideoActivity extends Activity {
    private transient VideoView video;
    private transient String uriPath;
    private transient MediaController controller;
    private transient SharedPreferences prefs;
//    private transient boolean isDown;
    private transient Set<String> md5sApp;
    private transient DownloadVideoTask downloadVideoTask;
    private transient Handler handler;
    private transient Runnable runnable;
    private transient boolean isFirstRSS;
    private transient MarqueeTextView textView;
    private transient UNLApp mApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        //textView = new TextView(FirstVideoActivity.this);

        prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);

//        CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(FirstVideoActivity.this, false, mApp, false);
//        createDriveFolderTask.execute();

//        isDown = prefs.getBoolean("isDownload", true);
        uriPath = "";
        Set<String> defSet = new HashSet<>();
        md5sApp = prefs.getStringSet("md5sApp", defSet);

        setContentView(R.layout.activity_firstvideo);
        textView = (MarqueeTextView) findViewById(R.id.creepingLine_first);
        textView.setSelected(true);

        JSONObject props = new JSONObject();
        try {
            props.put("embedded_video_play", "Playing embedded video");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        controller = new MediaController(FirstVideoActivity.this);

        video = (VideoView) findViewById(R.id.video_first);
        video.setMediaController(controller);

        controller.setAnchorView(video);


        uriPath = "android.resource://" + getPackageName() + "/assets/"
                + R.raw.emb;
        Log.d("video", uriPath);
        play();


        video.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
                Set<String> defSet = new HashSet<>();
                md5sApp = prefs.getStringSet("md5sApp", defSet);
                Log.d("unTag_FirstScreenAct", "md5sApp: " + md5sApp.toString());

                String[] files = directoryWorks.getDirFileList("first");
                boolean haveVideoFile = false;
                for (int i = 0; i < files.length; i++) {
                    for (int j = 0; j < UNLConsts.UNL_ACCEPTED_FILE_EXTS.length; j++) {
                        if (files[i].contains(UNLConsts.UNL_ACCEPTED_FILE_EXTS[j])) {
                            FileWorks fileWorks = new FileWorks(files[i]);
                            if (md5sApp.contains(fileWorks.getFileMD5())) {
                                startActivity(new Intent(FirstVideoActivity.this,
                                        FullscreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            }
                            //stopDownload();
                            haveVideoFile = true;
                            break;
                        }
                    }
                }
                if(!haveVideoFile){
                    play();
                    restartDownload();
                }


//                if (directoryWorks.getDirFileList("first").length == 0
//                        && md5sApp.size() == 0) {
//                    play();
//                    restartDownload();
//                } else {
//                    if (directoryWorks.getDirFileList("first").length > 0) {
//                        FileWorks fileWorks = new FileWorks(directoryWorks
//                                .getDirFileList("first")[0]);
//                        stopDownload();
//                        if (md5sApp.contains(fileWorks.getFileMD5())) {
//                            startActivity(new Intent(FirstVideoActivity.this,
//                                    FullscreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                            finish();
//                        } else {
//                            play();
//                            restartDownload();
//                        }
//                    } else {
//                        play();
//                        restartDownload();
//
//                    }
//                    //textView.requestFocus();
//                }

            }
        });

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                controller.show();
                LinearLayout ll = (LinearLayout) controller.getChildAt(0);


                for (int i = 0; i < ll.getChildCount(); i++) {

                    if (ll.getChildAt(i) instanceof LinearLayout) {
                        LinearLayout llC = (LinearLayout) ll.getChildAt(i);
                        for (int j = 0; j < llC.getChildCount(); j++) {
                            if (llC.getChildAt(j) instanceof SeekBar) {
                                SeekBar seekBar = (SeekBar) llC.getChildAt(j);
                                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.seekbartheme_scrubber_progress_horizontal_holo_dark));
                                seekBar.setThumb(getResources().getDrawable(R.drawable.seekbartheme_scrubber_control_selector_holo_dark));
                            }
                        }
                    }

                }
                //textView.requestFocus();
            }
        });

        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("mp error", String.valueOf(what) + ":" + String.valueOf(extra));
                return false;
            }
        });

        downloadVideoTask = new DownloadVideoTask(mApp, FirstVideoActivity.this, "first");
        downloadVideoTask.execute();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                RSSTask rssTask = new RSSTask(FirstVideoActivity.this, "first", mApp);
                rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                handler.postDelayed(this, UNLConsts.RSS_REFRESH_INTERVAL);
            }
        };

        //handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);


    }


    @Override
    protected void onStop() {
        super.onStop();
        video.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        video.pause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!video.isPlaying()) {
            video.resume();
        }
        if (handler != null && runnable != null) {
            Log.d("unTag_FirstScreenAct", "Start RSS handler in onRestart");
            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!video.isPlaying()) {
            video.resume();
        }
        if (handler != null && runnable != null) {
            Log.d("unTag_FirstScreenAct", "Start RSS handler in onResume");
            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public void startRSS(String feed) {
        if (isFirstRSS) {
            Log.d("feed", feed);
            textView.setVisibility(View.VISIBLE);
            textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            textView.setSelected(true);
            textView.requestFocus();
            isFirstRSS = false;
        } else {
            textView.setText("");
            textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            textView.requestFocus();
        }
    }

    public void play() {
        Uri uri = Uri.parse(uriPath);
        video.setVideoURI(uri);
        video.start();
    }

    public void restartDownload() {
//        if (!isDown) {
        if(!UNLApp.getIsDownloadTaskRunning()){
            downloadVideoTask.cancel(true);
            downloadVideoTask = new DownloadVideoTask(mApp, FirstVideoActivity.this, "first");
            downloadVideoTask.execute();
        }
    }

    public void stopDownload() {
        downloadVideoTask.cancel(true);
    }

    public void recToMP(String tag, String message) {
        JSONObject props = new JSONObject();
        try {
            props.put(tag, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
