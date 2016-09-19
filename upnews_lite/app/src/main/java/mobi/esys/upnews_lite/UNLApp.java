package mobi.esys.upnews_lite;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.api.services.drive.Drive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.fabric.sdk.android.Fabric;
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
    private static AtomicBoolean isStatFileWriting;
    private static AtomicBoolean isStatNetFileWriting;
    private static String curPlayFile;
    private static String fullDeviceIdForStatistic = "";
    private static String appExtCachePath;
    private static int[] camerasID;
    private static List<HashCache> hashCaches;

    public static String getFullDeviceIdForStatistic() {
        if (fullDeviceIdForStatistic.isEmpty()) {
            fullDeviceIdForStatistic = getDeviceId();
        }
        return fullDeviceIdForStatistic;
    }

    private static String getDeviceId() {
        String result = "";
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String serial = Build.SERIAL;
            if (model.startsWith(manufacturer)) {
                return model + "-" + serial;
            }
            result = manufacturer + " " + model + "-" + serial;
        } catch (Exception e) {
            result = "UNKNOWN DEVICE";
        }
        return result;
    }

    public static boolean getIsCamerasWorking() {
        return isCamerasWorking.get();
    }

    public static void setIsCamerasWorking(boolean state) {
        UNLApp.isCamerasWorking.set(state);
    }

    public static boolean getIsCreatingDriveFolder() {
        return isCreatingDriveFolder.get();
    }

    public static void setIsCreatingDriveFolder(boolean state) {
        UNLApp.isCreatingDriveFolder.set(state);
    }

    public static int[] getCamerasID() {
        return camerasID;
    }

    public static void setCamerasID(int[] camerasID) {
        UNLApp.camerasID = camerasID;
    }

    public static String getAppExtCachePath() {
        return appExtCachePath;
    }

    public static void setAppExtCachePath(String newPath) {
        appExtCachePath = newPath;
    }

    public static boolean getIsDownloadTaskRunning() {
        return isDownloadTaskRunning.get();
    }

    public static void setIsDownloadTaskRunning(boolean state) {
        Log.d("unTag_UNLApp", "Set isDownloadTaskRunning " + state);
        isDownloadTaskRunning.set(state);
    }

    public static boolean getIsDeleting() {
        return isDeleting.get();
    }

    public static void setIsDeleting(boolean state) {
        Log.d("unTag_UNLApp", "Set isDeleting " + state);
        isDeleting.set(state);
    }

    public static boolean getIsStatFileWriting() {
        return isStatFileWriting.get();
    }

    public static void setIsStatFileWriting(boolean state) {
        Log.d("unTag_UNLApp", "Set isStatFileWriting " + state);
        isStatFileWriting.set(state);
    }

    public static boolean getIsStatNetFileWriting() {
        return isStatNetFileWriting.get();
    }

    public static void setIsStatNetFileWriting(boolean state) {
        Log.d("unTag_UNLApp", "Set isStatNetFileWriting " + state);
        isStatNetFileWriting.set(state);
    }

    public static String getCurPlayFile() {
        return curPlayFile;
    }

    public static void setCurPlayFile(String incCrPlayFile) {
        curPlayFile = incCrPlayFile;
    }

    public static Drive getDriveService() {
        return driveService;
    }

    public static synchronized List<HashCache> getHashCaches() {
        return hashCaches;
    }

    public static synchronized void setHashCaches(List<HashCache> incHashCaches) {
        hashCaches = incHashCaches;
    }

    public void registerGoogle(Drive drive) {
        driveService = drive;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        isDownloadTaskRunning = new AtomicBoolean(false);
        isDeleting = new AtomicBoolean(false);
        isCreatingDriveFolder = new AtomicBoolean(false);
        isCamerasWorking = new AtomicBoolean(false);
        isStatFileWriting = new AtomicBoolean(false);
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
