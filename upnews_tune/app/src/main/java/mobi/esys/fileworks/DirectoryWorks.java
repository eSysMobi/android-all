package mobi.esys.fileworks;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.upnews_tune.R;
import mobi.esys.upnews_tune.UNLApp;

public class DirectoryWorks {
    private transient String directoryPath;
    private static final String DIR_WORKS_TAG = "unTag_DirectoryWorks";

    public DirectoryWorks(String directoryPath) {
        this.directoryPath = directoryPath;
        checkDirs();
    }

    public boolean checkDirs() {
        boolean res = true;
        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + UNLConsts.DIR_NAME);
        if (!audioDir.exists()) {
            res = audioDir.mkdir();
        }
        return res;
    }


    public String[] getDirFileList(boolean onlyAudio) {
        checkDirs();
        File videoDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(this.directoryPath));
        List<String> filePaths = new ArrayList<>();
        //get only video files from directory
        File[] files = videoDir.listFiles();
        for (File file : files) {
            if (file.exists()) {
                if (onlyAudio) {
                    FileWorks fw = new FileWorks(file);
                    if(Arrays.asList(UNLConsts.UNL_ACCEPTED_FILE_EXTS).contains(fw.getFileExtension())){
                        filePaths.add(file.getPath());
                    }
                } else {
                    filePaths.add(file.getPath());
                }
            }
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public void deleteFilesFromDir(List<String> maskList) {
        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, "deleteFilesFromDir " + Environment.getExternalStorageDirectory().getAbsolutePath().concat(this.directoryPath) + " Deleting " + maskList.size() + " files");
        Log.d(DIR_WORKS_TAG, "mask list task" + maskList.toString());
        if (audioDir.exists()) {
            File[] files = audioDir.listFiles();

            //check maskList
            if (maskList.size() == 1 && maskList.get(0).equals("unTuneDelAll")) {
                //delete all files in video directory excluded user files (with prefix dd)
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].getName().startsWith(UNLConsts.PREFIX_USER_VIDEOFILES)) {
                        files[i].delete();
                    }
                }
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (maskList.contains(files[i].getPath())) {

                        Date modDate = new Date(files[i].lastModified());
                        Calendar today = Calendar.getInstance();

                        long diff = today.getTimeInMillis() - modDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        if ((files[i].exists() && !files[i].getPath().equals(UNLApp.getCurPlayFile()))
                                ||
                                files[i].exists() && (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14)
                                ) {
                            files[i].delete();
                        }
                    }
                }
            }
        } else {
            Log.d(DIR_WORKS_TAG, "Folder don't exists");
            checkDirs();
        }
        UNLApp.setIsDeleting(false);
    }

    public List<String> getMD5Sums(boolean onlyAudio) {
        String[] files = getDirFileList(onlyAudio);
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            dirMD5s.add(fileWorks.getFileMD5());
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
