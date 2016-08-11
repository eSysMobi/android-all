package mobi.esys.upnews_hashtag;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;

import java.io.File;

import mobi.esys.consts.ISConsts;
import mobi.esys.filesystem.IOHelper;


public class UpnewsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        IOHelper.checkDirs();
    }

}
