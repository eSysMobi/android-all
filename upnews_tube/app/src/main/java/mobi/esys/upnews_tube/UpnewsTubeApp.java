package mobi.esys.upnews_tube;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import java.io.File;

import mobi.esys.upnews_tube.constants.DevelopersKeys;
import mobi.esys.upnews_tube.constants.Folders;
import mobi.esys.upnews_tube.filesystem.FolderHelper;


public class UpnewsTubeApp extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = DevelopersKeys.TWITTER_KEY;
    private static final String TWITTER_SECRET = DevelopersKeys.TWITTER_SECRET;

    private static final String[] folders = {
            Folders.BASE_FOLDER,
            Folders.BASE_FOLDER.
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER)};

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        createFolders();
    }

    public void createFolders() {
        for (String folder : folders) {
            FolderHelper.createFolderIfNoExisted(folder);
        }
    }
}
