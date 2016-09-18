package mobi.esys.upnews_lite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mobi.esys.UNLConsts;
import mobi.esys.events.EventCameraShot;
import mobi.esys.events.EventLogoLoadingComplete;
import mobi.esys.events.EventStartRSS;
import mobi.esys.events.EventToast;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.playback.Playback;
import mobi.esys.system.MarqueeTextView;
import mobi.esys.taskmanager.TaskManager;
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.GetMACsTask;

public class MainActivity extends Activity {
    private static final String TAG = "unTag_MainActivity";
    private static transient int DELAY_NAV_HIDE = 2000;
    private Playback playback;
    private MarqueeTextView textView;
    private boolean isFirstRSS;
    private UNLApp mApp;
    private ImageView mLogo;
    private SurfaceHolder holder;
    private Uri defaultVideoURI;

    //Get MACs
    private transient Handler handlerGetMACs = null;
    private transient Runnable runnableGetMACs = null;

    private transient Handler handlerHideUI;
    private transient View decorView = null;

    private transient TaskManager tm;

    private final EventBus bus = EventBus.getDefault();


    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_videofullscreen);
        Log.d(TAG, "Start onCreate MainActivity");

        //do not off screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //surface for camera
        SurfaceView surfaceView = new SurfaceView(MainActivity.this);
        FrameLayout surfaceHodler = (FrameLayout) findViewById(R.id.surfaceHolder);
        surfaceHodler.addView(surfaceView);
        holder = surfaceView.getHolder();

        //find views
        textView = (MarqueeTextView) findViewById(R.id.creepingLine);
        textView.setSelected(true);
        mLogo = (ImageView) findViewById(R.id.logo);
        VideoView videoView = (VideoView) findViewById(R.id.video);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        String uriPath = "android.resource://" + getPackageName() + "/assets/" + R.raw.emb;
        defaultVideoURI = Uri.parse(uriPath);

        //prepare handler for hide Android status bar
        if (Build.VERSION.SDK_INT >= 14) {
            decorView = getWindow().getDecorView();
            setUISmall();
            handlerHideUI = new mHandler(this);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility != View.SYSTEM_UI_FLAG_LOW_PROFILE) {
                        //Log.d(TAG, "onSystemUiVisibilityChange " + visibility + " need hide nav. Post delay handler");
                        handlerHideUI.sendEmptyMessageDelayed(32, DELAY_NAV_HIDE);
                    }
                }
            });
        }

        //prepare Logo task handler
        checkAndSetLogoFromExStorage();

        //gather surround MACs
        if (UNLConsts.ALLOW_NET_SCAN) {
            handlerGetMACs = new Handler();
            runnableGetMACs = new Runnable() {
                @Override
                public void run() {
                    WifiManager wifiMan = (WifiManager) getSystemService(WIFI_SERVICE);
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    int ipAddress = wifiInf.getIpAddress();
                    String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                    Log.d(TAG, "Start scan network. IP: " + ip);

                    GetMACsTask gmt = new GetMACsTask(ip);
                    Thread gmtThread = new Thread(gmt);
                    gmtThread.start();

                    handlerGetMACs.postDelayed(this, UNLConsts.MACS_CYCLE_DELAY);
                }
            };
            handlerGetMACs.postDelayed(runnableGetMACs, UNLConsts.MACS_START_DELAY);
        }

        tm = new TaskManager(mApp);
        playback = new Playback(this, mApp, videoView, defaultVideoURI, tm);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleLogoLoadingComplete(EventLogoLoadingComplete event) {
        if (event.isSuccessful()) {
            Log.d(TAG, "Receive logo is success");
            checkAndSetLogoFromExStorage();
        } else {
            Log.d(TAG, "Receive logo is fail");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleStartRSS(EventStartRSS event) {
        startRSS(event.getFeed());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCameraShot(EventCameraShot event) {
        String nameCurrentPlayedFile = event.getFileName();
        if (!nameCurrentPlayedFile.isEmpty()) {
            Log.d(TAG, "Start face counting after ending file " + nameCurrentPlayedFile);
            CameraShotTask csTask = new CameraShotTask(holder, MainActivity.this, nameCurrentPlayedFile, mApp);
            csTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.d(TAG, "Can't start face counting after ending file because nameCurrentPlayedFile is empty");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleToast(EventToast event) {
        String message = event.getMessage();
        if (UNLConsts.ALLOW_TOAST && message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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
    protected void onResume() {
        super.onResume();
        bus.register(this);
        playback.playFolder();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Post getMACs runnable in onRestart()");
        if (UNLConsts.ALLOW_NET_SCAN && handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.postDelayed(runnableGetMACs, UNLConsts.MACS_START_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Unregister all in onPause()");
        bus.unregister(this);
        if (handlerGetMACs != null && runnableGetMACs != null) {
            handlerGetMACs.removeCallbacks(runnableGetMACs);
        }
        if (handlerHideUI != null) {
            handlerHideUI.removeMessages(32);
        }
    }

    public void startRSS(String feed) {
        Log.d(TAG, "Start RSS line");
        if (feed != null) {
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
    }

    private static class mHandler extends Handler {
        MainActivity wrActivity;

        public mHandler(MainActivity activity) {
            wrActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            wrActivity.setUISmall();
        }
    }

    private void setUISmall() {
        //not need check SDK version because checking in onCreate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Log.d(TAG, "UiVisibility before " + decorView.getSystemUiVisibility());
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            Log.d(TAG, "UiVisibility after " + decorView.getSystemUiVisibility());
        }
    }

    public void forceSetUISmall() {
        setUISmall();
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (Build.VERSION.SDK_INT >= 14) {
//            setUISmall();
//        }
//    }

}
