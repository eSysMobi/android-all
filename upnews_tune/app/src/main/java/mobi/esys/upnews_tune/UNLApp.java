package mobi.esys.upnews_tune;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;

import com.google.api.services.drive.Drive;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import mobi.esys.constants.UNLConsts;


/**
 * Created by Артем on 02.03.2015.
 */
public class UNLApp extends Application {
    private static Drive driveService;
    private static AtomicBoolean isDownloadTaskRunning;
    private static AtomicBoolean isCreatingDriveFolder;
    private static AtomicBoolean isDeleting;
    private static String curPlayFile;
    private static String lastPlayedFile;
    private static boolean isPlaying = false;
    private static boolean firstStart = true;
    private static Uri defaultAudio = null;
    private static SharedPreferences preferences;
    private static UNLApp mApp;

    public static boolean isFirstStart() {
        boolean result = firstStart;
        firstStart = false;
        return result;
    }

    public static void setmApp(UNLApp mApp) {
        UNLApp.mApp = mApp;
    }

    public static UNLApp getmApp() {
        return UNLApp.mApp;
    }

    public static void setIsPlaying(boolean isPlaying) {
        UNLApp.isPlaying = isPlaying;
    }

    public static boolean getIsPlaying() {
        return isPlaying;
    }

    public static void setIsCreatingDriveFolder(boolean state) {
        UNLApp.isCreatingDriveFolder.set(state);
    }

    public static boolean getIsCreatingDriveFolder() {
        return isCreatingDriveFolder.get();
    }

    public static void setIsDownloadTaskRunning(boolean state) {
        Log.d("unTag_UNLApp", "Set isDownloadTaskRunning " + state);
        isDownloadTaskRunning.set(state);
    }

    public static boolean getIsDownloadTaskRunning() {
        return isDownloadTaskRunning.get();
    }

    public static void setIsDeleting(boolean state) {
        Log.d("unTag_UNLApp", "Set isDeleting " + state);
        isDeleting.set(state);
    }

    public static boolean getIsDeleting() {
        return isDeleting.get();
    }

    public static void setCurPlayFile(String incCrPlayFile) {
        curPlayFile = incCrPlayFile;
    }

    public static String getCurPlayFile() {
        return curPlayFile;
    }

    public void registerGoogle(Drive drive) {
        driveService = drive;
    }

    public static Drive getDriveService() {
        return driveService;
    }

    public static Uri getDefaultAudio() {
        return defaultAudio;
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isDownloadTaskRunning = new AtomicBoolean(false);
        isDeleting = new AtomicBoolean(false);
        isCreatingDriveFolder = new AtomicBoolean(false);
        String uriPath = "android.resource://" + getPackageName() + "/assets/"
                + R.raw.emb;
        defaultAudio = Uri.parse(uriPath);
        preferences = getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
