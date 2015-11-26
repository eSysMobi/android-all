package mobi.esys.upnews_hashtag;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mobi.esys.consts.ISConsts;
import mobi.esys.filesystem.files.FilesHelper;


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

    private void createFoldersIfNotExist() {
        //TODO checking free space
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name));
        File photoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name));
        File logoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.dir_changeable_logo_name));

        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        if (!logoDir.exists()) {
            logoDir.mkdir();
        }

    }
}
