package mobi.esys.upnews_lite;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.system.MarqueeTextView;
import mobi.esys.taskmanager.TaskManager;
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.SendStatisticsToGD;

public class FirstVideoActivity extends Activity {
    private transient VideoView video;
    private transient String uriPath = "";
    private transient MediaController controller;

    private transient SharedPreferences prefs;

    private transient boolean isFirstRSS;
    private transient MarqueeTextView textView;

    private transient ImageView mLogo;

    private transient UNLApp mApp;
    private transient TaskManager tm = null;

    private transient BroadcastReceiver brFirst;
    private transient IntentFilter intFilt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        //textView = new TextView(FirstVideoActivity.this);

        prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);

//        CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(FirstVideoActivity.this, false, mApp, false);
//        createDriveFolderTask.execute();

        setContentView(R.layout.activity_firstvideo);
        mLogo = (ImageView) findViewById(R.id.logo_first);
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

                String localNames = prefs.getString("localNames", "");

                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME +
                                UNLConsts.GD_STORAGE_DIR_NAME +
                                "/");
                String[] files = directoryWorks.getDirFileList("first");
                boolean haveVideoFile = false;
                for (int i = 0; i < files.length; i++) {
                    FileWorks fileWorks = new FileWorks(files[i]);
                    if (localNames.contains(files[i]) && Arrays.asList(UNLConsts.UNL_ACCEPTED_FILE_EXTS).contains(fileWorks.getFileExtension())) {
                        startActivity(new Intent(FirstVideoActivity.this,
                                FullscreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                        haveVideoFile = true;
                        break;
                    }
                    //stopDownload();
                }

                if (!haveVideoFile) {
                    play();
                    restartTasks();
                }

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
            }
        });

        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("mp error", String.valueOf(what) + ":" + String.valueOf(extra));
                return false;
            }
        });


        //prepare Logo task handler
        checkAndSetLogoFromExStorage();

        brFirst = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("unTag_FirstScreenAct", "Receive signal from BroadcastReceiver " + intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK));
                switch (intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK)) {
                    case UNLConsts.GET_LOGO_STATUS_NOT_OK:
                        Log.d("unTag_FirstScreenAct", "Receive logo is fail");
                        break;
                    case UNLConsts.GET_LOGO_STATUS_OK:
                        Log.d("unTag_FirstScreenAct", "Receive logo is success");
                        checkAndSetLogoFromExStorage();
                        break;
                    case UNLConsts.SIGNAL_TOAST:
                        if (UNLConsts.ALLOW_TOAST) {
                            Toast.makeText(FirstVideoActivity.this, intent.getStringExtra("toastText"), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case UNLConsts.SIGNAL_START_RSS:
                        startRSS(intent.getStringExtra("rssToShow"));
                        break;
                    case UNLConsts.SIGNAL_REC_TO_MP:
                        recToMP(intent.getStringExtra("recToMP_tag"), intent.getStringExtra("recToMP_message"));
                        break;
                }
            }
        };
        // Create intent-filter for BroadcastReceiver
        intFilt = new IntentFilter(UNLConsts.BROADCAST_ACTION_FIRST);

        tm = TaskManager.getInstance();
        tm.init(mApp, "first");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("unTag_FirstScreenAct", "Register Receiver");
        registerReceiver(brFirst, intFilt);
        tm.setNeedLogo(true);
        tm.setNeedRss(true);
        tm.needRssNOW();
        tm.setNeedDown(true);
        tm.setNeedSendStat(false);
        tm.startAllTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!video.isPlaying()) {
            video.resume();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!video.isPlaying()) {
            video.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        video.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        video.pause();
        Log.d("unTag_FirstScreenAct", "Unregister Receiver in onStop()");
        unregisterReceiver(brFirst);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //check logo in device, if we have it - set, if not have - use standard
    private void checkAndSetLogoFromExStorage() {
        String logoFilePath = UNLApp.getAppExtCachePath()
                + UNLConsts.VIDEO_DIR_NAME
                + UNLConsts.GD_LOGO_DIR_NAME
                + "/"
                + UNLConsts.GD_LOGO_FILE_TITLE;
        FileWorks fw = new FileWorks(logoFilePath);
        Bitmap logoFromFile = fw.getLogoFromExternalStorage();
        if (logoFromFile != null) {
            mLogo.setImageBitmap(logoFromFile);
        }
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

    public void restartTasks() {
        tm.setNeedLogo(false);
        tm.startAllTask();
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
