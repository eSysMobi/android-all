package mobi.esys.upnews_tube.filesystem;


import android.util.Log;

import java.io.File;

import mobi.esys.upnews_tube.constants.Folders;

public class FolderHelper {
    public static boolean createFolderIfNoExisted(final String folderPath) {
        boolean ret = true;

        File file = new File(Folders.SD_CARD, folderPath);
        Log.d("create folder - ", file.getAbsolutePath());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("Folder creating error", "Problem while creating folder " + file.getAbsolutePath());
                ret = false;
            }
        }
        return ret;
    }
}
