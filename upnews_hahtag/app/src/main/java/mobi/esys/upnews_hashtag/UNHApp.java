package mobi.esys.upnews_hashtag;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;

import java.io.File;

import mobi.esys.consts.ISConsts;


public class UNHApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createFoldersIfNotExist();
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

    public boolean createFoldersIfNotExist() {
        boolean result1;
        boolean result2;
        boolean result3;
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name));
        File photoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name));
        File logoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.dir_changeable_logo_name));

        if (!dir.exists()) {
            result1 = dir.mkdir();
        } else {
            result1 = true;
        }
        if (!photoDir.exists()) {
            result2 = photoDir.mkdir();
        } else {
            result2 = true;
        }
        if (!logoDir.exists()) {
            result3 = logoDir.mkdir();
        } else {
            result3 = true;
        }
        return result1 && result2 && result3;
    }

    public String getPhotoDir(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name);
    }

    public String getLogoDir(){
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.dir_changeable_logo_name);
    }

}
