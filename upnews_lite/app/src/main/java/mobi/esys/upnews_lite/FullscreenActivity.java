package mobi.esys.upnews_lite;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import org.json.JSONException;
import org.json.JSONObject;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.playback.Playback;
import mobi.esys.system.MarqueeTextView;
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.CheckAndGetLogoFromGDriveTask;
import mobi.esys.tasks.RSSTask;
import mobi.esys.tasks.SendStatisticsToGD;

public class FullscreenActivity extends Activity implements View.OnSystemUiVisibilityChangeListener {
    private static transient int DELAY_NAV_HIDE = 2000;
    private transient VideoView videoView;
    private transient Playback playback = null;
    private transient MarqueeTextView textView;
    private transient boolean isFirstRSS;
    private transient Handler handler;
    private transient Runnable runnable;
    private transient UNLApp mApp;
    private transient BroadcastReceiver br;
    private transient IntentFilter intFilt;
    private transient ImageView mLogo;
    private transient SurfaceHolder holder;

    private transient Handler handler2;
    private transient View decorView = null;


    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_videofullscreen);
        Log.d("unTag_FullscreenAct", "Start onCreate FullscreenActivity");

        //do not off screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SurfaceView surfaceView = new SurfaceView(FullscreenActivity.this);
        FrameLayout surfaceHodler = (FrameLayout) findViewById(R.id.surfaceHolder);
        surfaceHodler.addView(surfaceView);
        holder = surfaceView.getHolder();

        textView = (MarqueeTextView) findViewById(R.id.creepingLine);
        textView.setSelected(true);
        mLogo = (ImageView) findViewById(R.id.logo);
        videoView = (VideoView) findViewById(R.id.video);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        //prepare RSS task handler
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                RSSTask rssTask = new RSSTask(FullscreenActivity.this, "full", mApp);
                rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                handler.postDelayed(this, UNLConsts.RSS_REFRESH_INTERVAL);
            }
        };

        //prepare handler for hide Android status bar
        if (Build.VERSION.SDK_INT >= 14) {
                decorView = getWindow().getDecorView();
                setUISmall();
                handler2 = new mHandler(this);
                decorView.setOnSystemUiVisibilityChangeListener(this);
        }


        //prepare Logo task handler
        checkAndSetLogoFromExStorage();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("unTag_FullscreenAct", "Receive signal from BroadcastReceiver " + intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK));
                switch (intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK)) {
                    case UNLConsts.GET_LOGO_STATUS_NOT_OK:
                        Log.d("unTag_FullscreenAct", "Receive logo is fail");
                        //mLogo.setImageDrawable(getDrawable(R.drawable.upnews_logo_w2));
                        break;
                    case UNLConsts.GET_LOGO_STATUS_OK:
                        Log.d("unTag_FullscreenAct", "Receive logo is success");
                        checkAndSetLogoFromExStorage();
                        break;
                    case UNLConsts.STATUS_NEED_CHECK_LOGO:
                        renewLogo();
                        break;
                    case UNLConsts.SIGNAL_TOAST:
                        if (UNLConsts.ALLOW_TOAST) {
                            Toast.makeText(FullscreenActivity.this, intent.getStringExtra("toastText"), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case UNLConsts.SIGNAL_CAMERASHOT:
                        String nameCurrentPlayedFile = intent.getStringExtra("nameCurrentPlayedFile");
                        if (!nameCurrentPlayedFile.isEmpty()) {
                            Log.d("unTag_FullscreenAct", "Start face counting after ending file " + nameCurrentPlayedFile);
                            CameraShotTask csTask = new CameraShotTask(holder, FullscreenActivity.this, nameCurrentPlayedFile, mApp);
                            Thread thread = new Thread(csTask, "CameraShotTask");
                            thread.start();
                            //or
                            //CameraShotTask csTask = new CameraShotTask(holder, FullscreenActivity.this, nameCurrentPlayedFile, mApp);
                            //Thread thread = new Thread(null, csTask, "CameraShotTask");
                            //thread.start();
                        } else {
                            Log.d("unTag_FullscreenAct", "Can't start face counting after ending file because nameCurrentPlayedFile is empty");
                        }
                        break;
                    case UNLConsts.SIGNAL_SEND_STATDATA_TO_GD:
                        if (!UNLApp.getIsDownloadTaskRunning()) {
                            Log.d("unTag_FullscreenAct", "Start sending statistics to GD.");
                            SendStatisticsToGD sstGD = new SendStatisticsToGD(mApp);
                            Thread thread = new Thread(sstGD, "SendStatisticsToGD");
                            thread.start();
                        }
                        break;
                }
            }
        };
        // Create intent-filter for BroadcastReceiver
        intFilt = new IntentFilter(UNLConsts.BROADCAST_ACTION);
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

    private void renewLogo() {
        //Start task for check or download logo from Google Disk
        CheckAndGetLogoFromGDriveTask task = new CheckAndGetLogoFromGDriveTask(mApp);
        task.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("unTag_FullscreenAct", "Unregister Receiver in onStop()");
        unregisterReceiver(br);
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
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
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
        if (directoryWorks.getDirFileList("fullscreen").length == 0) {
            startActivity(new Intent(FullscreenActivity.this,
                    FirstVideoActivity.class));
            finish();
        } else {
            startPlayback();
        }
        Log.d("unTag_FullscreenAct", "Start RSS handler in onRestart");
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
        if (directoryWorks.getDirFileList("fullscreen").length == 0) {
            startActivity(new Intent(FullscreenActivity.this,
                    FirstVideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            startPlayback();
            Log.d("unTag_FullscreenAct", "Start RSS handler in onResume");
            if (handler != null && runnable != null) {
                handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
            }
        }
    }

    public void startPlayback() {
        playback = new Playback(FullscreenActivity.this, mApp);
        playback.playFolder();
    }

    public VideoView getVideoView() {
        return this.videoView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("unTag_FullscreenAct", "Register Receiver");
        registerReceiver(br, intFilt);
        Log.d("unTag_FullscreenAct", "Check remote logo from FullscreenActivity");
        renewLogo();
    }

    public void startRSS(String feed) {
        Log.d("unTag_FullscreenAct", "Start RSS line");
        if (isFirstRSS) {
            Log.d("feed", feed);
            textView.setVisibility(View.VISIBLE);
            //textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            textView.setText(Html.fromHtml(feed));
            textView.setSelected(true);
            textView.requestFocus();
            isFirstRSS = false;
        } else {
            textView.setText("");
            textView.setText(Html.fromHtml(feed));
            textView.requestFocus();
        }
    }

//    public void restartCreepingLine() {
//        textView.requestFocus();
//    }

    public void recToMP(String tag, String message) {
        JSONObject props = new JSONObject();
        try {
            props.put(tag, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.d("unTag_FullscreenAct", "onSystemUiVisibilityChange " + visibility);
        if (visibility != View.SYSTEM_UI_FLAG_LOW_PROFILE) {
            handler2.sendEmptyMessageDelayed(32, DELAY_NAV_HIDE);
        }
    }

    private static class mHandler extends Handler {
        //need check this! may be memory leak

//        WeakReference<FullscreenActivity> wrActivity;
//
//        public mHandler(FullscreenActivity activity) {
//            wrActivity = new WeakReference<FullscreenActivity>(activity);
//        }

        FullscreenActivity wrActivity;
        public mHandler(FullscreenActivity activity) {
            wrActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            FullscreenActivity activity = wrActivity.get();
//            if (activity != null)
//                activity.setUISmall();
            wrActivity.setUISmall();
        }
    }

    private void setUISmall() {
        //not need check SDK version because checking in onCreate
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= 14) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

}
