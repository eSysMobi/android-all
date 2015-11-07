package mobi.esys.upnews_play;

import android.app.Application;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import mobi.esys.upnews_play.consts.Consts;
import mobi.esys.upnews_play.filesystem.FileSystemHelper;


public class App extends Application {
    private  File appFolder;

    public  File getAppFolder() {
        Log.d("root folder", appFolder.getAbsolutePath());
        return appFolder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("folder", Arrays.toString(Consts.MOUNT_FOLDER.list()));
        if(!isFindFolder()) {
            FileSystemHelper.createFolder(Consts.APP_FOLDER);
            appFolder=Consts.APP_FOLDER;
        }
    }

    private boolean isFindFolder(){
        boolean isFolder=false;
        if(Consts.STORAGE_FOLDER.listFiles()!=null) {
            if(Consts.STORAGE_FOLDER.listFiles().length>0) {
                Log.d("folder",Consts.STORAGE_FOLDER.getAbsolutePath());
                for (File storage : Consts.STORAGE_FOLDER.listFiles()) {
                    List<String> subfolders = FileSystemHelper.getFolderSubFoldersNames(storage);
                    if (subfolders != null && subfolders.size() > 0) {
                        Log.d("folders storage", Arrays.toString(Consts.STORAGE_FOLDER.listFiles()));
                        Log.d("folders storage", FilenameUtils.getBaseName(storage.getName()));
                        if (subfolders.contains("ROMBICA_PLAY")) {
                            isFolder = true;
                            appFolder = new File(storage.getAbsolutePath()
                                    .concat(File.separator).concat("ROMBICA_PLAY"));
                            Log.d("folder storage",Consts.STORAGE_FOLDER.getAbsolutePath());
                            Log.d("folder storage",storage.getAbsolutePath());
                        }
                    }
                }
            }
            else if(Consts.MOUNT_FOLDER.listFiles()!=null){
                Log.d("folder mnt",Consts.MOUNT_FOLDER.getAbsolutePath());
                if(Consts.MOUNT_FOLDER.listFiles().length>0) {
                    for (File storage : Consts.MOUNT_FOLDER.listFiles()) {
                        List<String> subfolders = FileSystemHelper.getFolderSubFoldersNames(storage);
                        if (subfolders != null && subfolders.size() > 0) {
                            Log.d("folders mnt", Arrays.toString(Consts.MOUNT_FOLDER.listFiles()));
                            Log.d("folders mnt", FilenameUtils.getBaseName(storage.getName()));
                            if (subfolders.contains("ROMBICA_PLAY")) {
                                isFolder = true;
                                appFolder = new File(storage.getAbsolutePath()
                                        .concat(File.separator).concat("ROMBICA_PLAY"));

                                Log.d("folder mnt",storage.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
        else if(Consts.MOUNT_FOLDER.listFiles()!=null){
            Log.d("folder mnt",Consts.MOUNT_FOLDER.getAbsolutePath());
            if(Consts.MOUNT_FOLDER.listFiles().length>0) {
                for (File storage : Consts.MOUNT_FOLDER.listFiles()) {
                    List<String> subfolders = FileSystemHelper.getFolderSubFoldersNames(storage);
                    if (subfolders != null && subfolders.size() > 0) {
                        Log.d("folders mnt", Arrays.toString(Consts.MOUNT_FOLDER.listFiles()));
                        Log.d("folders mnt", FilenameUtils.getBaseName(storage.getName()));
                        if (subfolders.contains("ROMBICA_PLAY")) {
                            isFolder = true;
                            appFolder = new File(storage.getAbsolutePath()
                                    .concat(File.separator).concat("ROMBICA_PLAY"));

                            Log.d("folder mnt",storage.getAbsolutePath());
                        }
                    }
                }
            }
        }

        return  isFolder;
    }

}
