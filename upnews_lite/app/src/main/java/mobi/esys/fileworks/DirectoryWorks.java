package mobi.esys.fileworks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.upnews_lite.UNLApp;

public class DirectoryWorks {
    private transient String directoryPath;
    private static final String DIR_WORKS_TAG = "unTag_DirectoryWorks";

    public DirectoryWorks(String directoryPath) {
        this.directoryPath = directoryPath;
        checkDirs();
    }

    public void checkDirs() {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + UNLConsts.VIDEO_DIR_NAME);
        if (!videoDir.exists()) {
            videoDir.mkdir();
        }
        File storageDir = new File(videoDir.getAbsolutePath() + UNLConsts.STORAGE_DIR_NAME);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File logoDir = new File(videoDir.getAbsolutePath() + UNLConsts.LOGO_DIR_NAME);
        if (!logoDir.exists()) {
            logoDir.mkdir();
        }
    }

    public String[] getDirFileList(String mess) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, videoDir.getAbsolutePath());
        List<String> filePaths = new ArrayList<>();
        if (videoDir.exists()) {
            //get all files from directory
            File[] files = videoDir.listFiles();
            for (File file : files) {
                if (file.exists()) {
                    filePaths.add(file.getPath());
                }
            }
            Log.d(DIR_WORKS_TAG + mess, filePaths.toString());
        } else {
            Log.d(DIR_WORKS_TAG, "folder don't exist");
            checkDirs();
        }

        return filePaths.toArray(new String[filePaths.size()]);
    }

    public void deleteFilesFromDir(List<String> maskList, Context context) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, "deleteFilesFromDir Deleting " + maskList.size() + " files");
        Log.d(DIR_WORKS_TAG, Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));

        Log.d(DIR_WORKS_TAG, "mask list task" + maskList.toString());
        if (videoDir.exists()) {
            File[] files = videoDir.listFiles();

            //check maskList
            if (maskList.size() == 1 && maskList.get(0).equals("unLiteDelAll")) {

                for (int i = 0; i < files.length; i++) {
                    Date modDate = new Date(files[i].lastModified());
                    Calendar today = Calendar.getInstance();


                    long diff = today.getTimeInMillis() - modDate.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);
                    if (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14) {
                        files[i].delete();
                    } else {
                        files[i].delete();
                    }
                }
            } else {

                for (int i = 0; i < files.length; i++) {
                    if (maskList.contains(files[i].getAbsolutePath())) {

                        Date modDate = new Date(files[i].lastModified());
                        Calendar today = Calendar.getInstance();

                        long diff = today.getTimeInMillis() - modDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        if ((files[i].exists() && !files[i].getAbsolutePath().equals(UNLApp.getCurPlayFile()))
                                ||
                             (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14)
                            ) {
                            files[i].delete();
                        }
                    }
                }
            }
            UNLApp.setIsDeleting(false);
        } else {
            Log.d(DIR_WORKS_TAG, "Folder don't exists");
            checkDirs();
        }
    }

    public List<String> getMD5Sums() {
        String[] files = getDirFileList("getMD5SUM");
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            File file = new File(files[i]);
            if (file.exists()) {
                dirMD5s.add(fileWorks.getFileMD5());
            }
        }
        return dirMD5s;
    }

    public String getFileExtension(String fileName) {
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1);
        }
        Log.d(DIR_WORKS_TAG, "Getting ext is " + ext);
        return ext;
    }
}
