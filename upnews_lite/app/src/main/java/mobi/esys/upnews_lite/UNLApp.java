package mobi.esys.upnews_lite;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.google.api.services.drive.Drive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mobi.esys.system.HashCache;


/**
 * Created by Артем on 02.03.2015.
 */
public class UNLApp extends Application {
    private static Drive driveService;
    private static AtomicBoolean isDownloadTaskRunning;
    private static AtomicBoolean isCreatingDriveFolder;
    private static AtomicBoolean isDeleting;
    private static AtomicBoolean isCamerasWorking;
    private static AtomicBoolean isStatNetFileWriting;
    private static String curPlayFile;
    private static String fullDeviceIdForStatistic;
    private static String appExtCachePath;
    private static Integer[] camerasID = null;
    private static List<HashCache> hashCaches;

    public static void setFullDeviceIdForStatistic(String incDeviceId) {
        Log.d("unTag_UNLApp", "Set fullDeviceIdForStatistic " + incDeviceId);
        fullDeviceIdForStatistic = incDeviceId;
    }

    public static String getFullDeviceIdForStatistic() {
        return fullDeviceIdForStatistic;
    }

    public static void setIsCamerasWorking(boolean state) {
        UNLApp.isCamerasWorking.set(state);
    }

    public static boolean getIsCamerasWorking() {
        return isCamerasWorking.get();
    }

    public static void setIsCreatingDriveFolder(boolean state) {
        UNLApp.isCreatingDriveFolder.set(state);
    }

    public static boolean getIsCreatingDriveFolder() {
        return isCreatingDriveFolder.get();
    }

    public static void setCamerasID(Integer[] camerasID) {
        UNLApp.camerasID = camerasID;
    }

    public static Integer[] getCamerasID() {
        return camerasID;
    }

    public static String getAppExtCachePath() {
        return appExtCachePath;
    }

    public static void setAppExtCachePath(String newPath) {
        appExtCachePath = newPath;
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

    public static void setIsStatNetFileWriting(boolean state) {
        Log.d("unTag_UNLApp", "Set isStatNetFileWriting " + state);
        isStatNetFileWriting.set(state);
    }

    public static boolean getIsStatNetFileWriting() {
        return isStatNetFileWriting.get();
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


    @Override
    public void onCreate() {
        super.onCreate();
        isDownloadTaskRunning = new AtomicBoolean(false);
        isDeleting = new AtomicBoolean(false);
        isCreatingDriveFolder = new AtomicBoolean(false);
        isCamerasWorking = new AtomicBoolean(false);
        isStatNetFileWriting = new AtomicBoolean(false);
        hashCaches = new ArrayList<>();
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
