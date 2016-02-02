package mobi.esys.upnews_lite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import mobi.esys.tasks.CameraShotTask;
import mobi.esys.tasks.CheckAndGetLogoFromGDriveTask;
import mobi.esys.tasks.CreateDriveFolderTask;
import mobi.esys.tasks.RSSTask;
import mobi.esys.tasks.SendStatisticsToGD;

@SuppressLint({"NewApi", "SimpleDateFormat"})
public class FullscreenActivity extends Activity {
    private transient VideoView videoView;
    private transient Playback playback;
    private transient TextView textView;
    private transient boolean isFirstRSS;
    private transient RelativeLayout relativeLayout;
    private transient Handler handler;
    private transient Runnable runnable;
    private transient UNLApp mApp;
    private transient BroadcastReceiver br;
    private transient IntentFilter intFilt;
    private transient ImageView mLogo;
    private transient SurfaceHolder holder;


    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_videofullscreen);

        SurfaceView surfaceView = new SurfaceView(FullscreenActivity.this);
        FrameLayout surfaceHodler = (FrameLayout) findViewById(R.id.surfaceHolder);
        surfaceHodler.addView(surfaceView);
        holder = surfaceView.getHolder();

        relativeLayout = (RelativeLayout) findViewById(R.id.fullscreenLayout);
        textView = new TextView(FullscreenActivity.this);
        mLogo = (ImageView) findViewById(R.id.logo);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                RSSTask rssTask = new RSSTask(FullscreenActivity.this, "full", mApp);
                rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                handler.postDelayed(this, UNLConsts.RSS_REFRESH_INTERVAL);
            }
        };

        handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);

        videoView = (VideoView) findViewById(R.id.video);

//        CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(FullscreenActivity.this, false, mApp, false);
//        createDriveFolderTask.execute();

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
                            Thread thread = new Thread(csTask,"CameraShotTask");
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

        startPlayback();
    }

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
        Log.d("unTag_FullscreenAct", "Unregister Receiver in onDestroy()");
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
        if (isFirstRSS) {
            Log.d("feed", feed);
            textView.setBackgroundColor(getResources().getColor(R.color.rss_line));
            textView.setTextColor(Color.WHITE);
            RelativeLayout.LayoutParams tslp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 80);
            tslp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            tslp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tslp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(tslp);
            textView.setTextSize(30);
            textView.setPadding(20, 0, 20, 0);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.setMarqueeRepeatLimit(-1);
            textView.setHorizontallyScrolling(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
            textView.setFreezesText(true);
            textView.requestFocus();

            textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            relativeLayout.addView(textView);
            isFirstRSS = false;

        } else {
            textView.setText("");
            textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            textView.requestFocus();
        }
    }

    public void restartCreepingLine() {
        textView.requestFocus();
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
