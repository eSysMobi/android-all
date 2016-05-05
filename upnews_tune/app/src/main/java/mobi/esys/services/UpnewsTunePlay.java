package mobi.esys.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.tasks.DownloadAudioTask;
import mobi.esys.upnews_tune.R;
import mobi.esys.upnews_tune.StartPlayerActivity;
import mobi.esys.upnews_tune.UNLApp;

public class UpnewsTunePlay extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final String TAG = "unTag_TunePlay";
    private boolean isFirstSession = true;
    private int audiofileLocalIndex;

    private UNLApp mApp;
    private SharedPreferences preferences;
    private Uri defaultURI;

    private MediaPlayer mMediaPlayer = null;
    private WifiManager.WifiLock wifiLock = null;
    private Notification.Builder builder;


    public UpnewsTunePlay() {
        mApp = UNLApp.getmApp();
        defaultURI = UNLApp.getDefaultAudio();
        preferences = UNLApp.getPreferences();
        builder = new Notification.Builder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case UNLConsts.ACTION_PLAY:
                UNLApp.setIsPlaying(true);
                wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "UpnewsTuneWiFiLock");

                sendNotif(0, "Playing default file");

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

                nextTrack();
                break;
            case UNLConsts.ACTION_STOP:
                UNLApp.setIsPlaying(false);
                stopForeground(true);
                stopSelf();
                break;
        }

        return START_NOT_STICKY;
    }

    private void sendNotif(int count, String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), StartPlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setAutoCancel(false);
        builder.setTicker("upnews | TUNE");
        builder.setContentTitle("upnews | TUNE");
        //builder.setContentText("You have a new message");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pi);
        builder.setOngoing(true);
        builder.setSubText(text);   //API level 16
        builder.setNumber(count);
        builder.build();

        Notification notification = builder.getNotification();

        startForeground(42, notification);
    }

    void nextTrack() {
        mMediaPlayer.reset();

        DownloadAudioTask downloadVideoTask = new DownloadAudioTask(mApp);
        downloadVideoTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        Log.d(TAG, "MediaPlayer nextTrack");


        DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.DIR_NAME);
        String[] files = directoryWorks.getDirFileList(true);
        List<String> audiofiles = Arrays.asList(files);

        if (audiofiles.size() > 0) {
            if (preferences.getBoolean("RandomPlaylist",false)) {
                //yes, random
                Log.d(TAG, "Set Random");
                Collections.shuffle(audiofiles);
            } else {
                Log.d(TAG, "Set Alphabetic");
                //not, not random. Alphabetic
                Collections.sort(audiofiles);
            }
            //------------------------------------
            //looking for index of last played file
            if (isFirstSession) {
                String lastPlayed = preferences.getString("lastPlayedFile", "");
                if (!lastPlayed.isEmpty()) {
                    int lastPlayedFileIndex = 0;
                    for (int i = 0; i < audiofiles.size(); i++) {
                        if (audiofiles.get(i).equals(lastPlayed)) {
                            lastPlayedFileIndex = i;
                            break;
                        }
                    }
                    audiofileLocalIndex = lastPlayedFileIndex;
                }
                isFirstSession = false;
            }

            //checking
            if (audiofileLocalIndex >= audiofiles.size()) {
                audiofileLocalIndex = 0;
            }

            File fs = new File(audiofiles.get(audiofileLocalIndex));
            if (fs.exists()) {
                try {
                    //save name of current played file
                    UNLApp.setCurPlayFile(fs.getPath());
                    //save lastPlayedFile
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lastPlayedFile", fs.getPath());
                    editor.apply();

                    Log.w(TAG, "Play file " + fs.getName());
                    mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(fs.getPath()));
                    mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                    audiofileLocalIndex++;

                    sendNotif(audiofileLocalIndex, fs.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "MediaPlayer error prepareAsync: " + e.getMessage());
                    audiofileLocalIndex++;
                    nextTrack();
                }
            } else {
                Log.d(TAG, "We have no files associated with this audiofileLocalIndex");
                audiofileLocalIndex++;
                nextTrack();
            }
            //------------------------------------

        } else {
            Log.d(TAG, "We have no audio files");
            try {
                mMediaPlayer.setDataSource(getApplicationContext(), defaultURI);
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "MediaPlayer error prepareAsync: " + e.getMessage());
                nextTrack();
            }
        }
    }


    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        if (wifiLock != null) {
            wifiLock.acquire();
        }
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "MediaPlayer error " + String.valueOf(what) + ":" + String.valueOf(extra));
        nextTrack();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextTrack();
    }


    @Override
    public void onDestroy() {
        if (wifiLock != null) {
            wifiLock.release();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}