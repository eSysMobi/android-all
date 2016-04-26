package mobi.esys.upnews_tube;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.io.File;

import mobi.esys.upnews_tube.constants.Folders;
import mobi.esys.upnews_tube.filesystem.FolderHelper;


public class UpnewsTubeApp extends Application {
    private transient Activity currentActivityInstance;
    private static final String[] folders = {
            Folders.BASE_FOLDER,
            Folders.BASE_FOLDER.
                    concat(File.separator).
                    concat(Folders.VIDEO_FOLDER),
            Folders.BASE_FOLDER.
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER)};

    @Override
    public void onCreate() {
        super.onCreate();
        createFolders();
    }

    public Activity getCurrentActivityInstance() {
        return currentActivityInstance;
    }

    public void setCurrentActivityInstance(Activity currentActivityInstance) {
        this.currentActivityInstance = currentActivityInstance;
    }

    public void createFolders() {
        for (String folder : folders) {
            FolderHelper.createFolderIfNoExisted(folder);
        }
    }
}
