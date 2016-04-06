package mobi.esys.upnews_lite;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.playback.Playback;
import mobi.esys.system.MarqueeTextView;
import mobi.esys.taskmanager.TaskManager;
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.GetMACsTask;
import mobi.esys.tasks.SendStatisticsToGD;

public class FullscreenActivity extends Activity implements View.OnSystemUiVisibilityChangeListener {
    private static transient int DELAY_NAV_HIDE = 2000;
    private transient VideoView videoView;
    private transient Playback playback = null;
    private transient MarqueeTextView textView;
    private transient boolean isFirstRSS;
    private transient UNLApp mApp;
    private transient BroadcastReceiver br;
    private transient IntentFilter intFilt;
    private transient ImageView mLogo;
    private transient SurfaceHolder holder;

    //Get MACs
    private transient Handler handlerGetMACs = null;
    private transient Runnable runnableGetMACs = null;

    private transient Handler handlerHideUI;
    private transient View decorView = null;

    private transient TaskManager tm;


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

        //prepare handler for hide Android status bar
        if (Build.VERSION.SDK_INT >= 14) {
            decorView = getWindow().getDecorView();
            setUISmall();
            handlerHideUI = new mHandler(this);
            decorView.setOnSystemUiVisibilityChangeListener(this);
        }

        //prepare Logo task handler
        checkAndSetLogoFromExStorage();

        if (UNLConsts.ALLOW_NET_SCAN) {
            handlerGetMACs = new Handler();
            runnableGetMACs = new Runnable() {
                @Override
                public void run() {
                    WifiManager wifiMan = (WifiManager) getSystemService(WIFI_SERVICE);
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    int ipAddress = wifiInf.getIpAddress();
                    String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                    Log.d("unTag_FullscreenAct", "Start scan network. IP: " + ip);

                    GetMACsTask gmt = new GetMACsTask(ip);
                    Thread gmtThread = new Thread(gmt);
                    gmtThread.start();

                    handlerGetMACs.postDelayed(this, UNLConsts.MACS_CYCLE_DELAY);
                }
            };
            handlerGetMACs.postDelayed(runnableGetMACs, UNLConsts.MACS_START_DELAY);
        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("unTag_FullscreenAct", "Receive signal from BroadcastReceiver " + intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK));
                switch (intent.getByteExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK)) {
                    case UNLConsts.GET_LOGO_STATUS_NOT_OK:
                        Log.d("unTag_FullscreenAct", "Receive logo is fail");
                        break;
                    case UNLConsts.GET_LOGO_STATUS_OK:
                        Log.d("unTag_FullscreenAct", "Receive logo is success");
                        checkAndSetLogoFromExStorage();
                        break;
                    case UNLConsts.SIGNAL_TOAST:
                        if (UNLConsts.ALLOW_TOAST) {
                            Toast.makeText(FullscreenActivity.this, intent.getStringExtra("toastText"), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case UNLConsts.SIGNAL_START_RSS:
                        startRSS(intent.getStringExtra("rssToShow"));
                        break;
                    case UNLConsts.SIGNAL_CAMERASHOT:
                        String nameCurrentPlayedFile = intent.getStringExtra("nameCurrentPlayedFile");
                        if (!nameCurrentPlayedFile.isEmpty()) {
                            Log.d("unTag_FullscreenAct", "Start face counting after ending file " + nameCurrentPlayedFile);
                            CameraShotTask csTask = new CameraShotTask(holder, FullscreenActivity.this, nameCurrentPlayedFile, mApp);
                            csTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            //or
                            //CameraShotTask csTask = new CameraShotTask(holder, FullscreenActivity.this, nameCurrentPlayedFile, mApp);
                            //Thread thread = new Thread(null, csTask, "CameraShotTask");
                            //thread.start();
                        } else {
                            Log.d("unTag_FullscreenAct", "Can't start face counting after ending file because nameCurrentPlayedFile is empty");
                        }
                        break;
                    case UNLConsts.SIGNAL_REC_TO_MP:
                        recToMP(intent.getStringExtra("recToMP_tag"), intent.getStringExtra("recToMP_message"));
                        break;
                }
            }
        };
        // Create intent-filter for BroadcastReceiver
        intFilt = new IntentFilter(UNLConsts.BROADCAST_ACTION);

        tm = TaskManager.getInstance();
        tm.init(mApp, "full");
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("unTag_FullscreenAct", "Register Receiver");
        registerReceiver(br, intFilt);
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
        }
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
        }
        if (UNLConsts.ALLOW_NET_SCAN && handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.postDelayed(runnableGetMACs, UNLConsts.MACS_START_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.removeCallbacks(runnableGetMACs);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("unTag_FullscreenAct", "Unregister Receiver in onStop()");
        unregisterReceiver(br);
        if (handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.removeCallbacks(runnableGetMACs);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.removeCallbacks(runnableGetMACs);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.removeCallbacks(runnableGetMACs);
        }
        finish();
    }

    public void startPlayback() {
        playback = new Playback(FullscreenActivity.this, mApp, tm);
        playback.playFolder();
    }

    public VideoView getVideoView() {
        return this.videoView;
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
        recToMP("rss_start", "Start rss feed");
    }

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
            handlerHideUI.sendEmptyMessageDelayed(32, DELAY_NAV_HIDE);
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

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (Build.VERSION.SDK_INT >= 14) {
//            setUISmall();
//        }
//    }

}
