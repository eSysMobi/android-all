package mobi.esys.filesystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.instagram.InstagramItem;
import mobi.esys.upnews_hashtag.R;

public class IOHelper {
    private static final String TAG = "unTag_Dirhelper";
    private static final String baseDir = Environment
            .getExternalStorageDirectory()
            .getAbsolutePath()
            .concat(File.separator)
            .concat(ISConsts.globals.base_dir);
    private static final String photoDir = baseDir
            .concat(File.separator)
            .concat(ISConsts.globals.photo_dir);
    private static final String logoDir = baseDir
            .concat(File.separator)
            .concat(ISConsts.globals.logo_dir);

    public static String getPhotoDir() {
        return photoDir;
    }

    public static String getLogoDir() {
        return logoDir;
    }

    public static String[] getDirFileList() {
        File videoDir = new File(photoDir);
        List<String> filePaths = new ArrayList<>();
        if (videoDir.exists()) {
            File[] files = videoDir.listFiles();
            for (File file : files) {
                if (file.exists()) {
                    filePaths.add(file.getPath());
                } else {
                    continue;
                }
            }
        } else {
            Log.d(TAG, "Base folder don't exist");
            checkDirs();
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public static void clearPhotoDir() {
        File[] igPhotosFileList = new File(photoDir).listFiles();
        int deletedFiles = 0;
        for (File photoFile : igPhotosFileList) {
            if (photoFile.delete()) {
                deletedFiles++;
            }
        }
        Log.d(TAG, "Deleted " + deletedFiles + "photos");
    }

    public static Bitmap getLogoFromExternalStorage() {
        Bitmap result = null;
        File logoFile = new File(logoDir, ISConsts.globals.logo_name);
        if (logoFile.exists()) {
            result = BitmapFactory.decodeFile(logoFile.getAbsolutePath());
            Log.d(TAG, "Logo from file decoded");
        }
        return result;
    }

    public static boolean createLogoInExternalStorage(Context context) {
        File logoFile = new File(logoDir, ISConsts.globals.logo_name);
        if (logoFile.exists()) {
            Log.d(TAG, "Logo file exists. Not need to create.");
            return true;
        } else {
            //TODO check free space
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(logoFile);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.upnews_logo_w2);
                logo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bos.flush();
                bos.close();
                Log.d(TAG, "Success saving logo file in external storage.");
                return true;
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Error saving logo file in external storage: " + e.getMessage());
                return false;
            } catch (IOException e) {
                Log.d(TAG, "Error saving logo file in external storage: " + e.getMessage());
                return false;
            }
        }
    }

    public static String getExtension(String fileName) {
        String extension = "";
        int srt = fileName.lastIndexOf('.');
        if (srt > 0 && srt < fileName.length() - 1) {
            extension = fileName.substring(srt);
        }
        return extension;
    }

    public static boolean checkDirs() {
        boolean result1;
        boolean result2;
        boolean result3;

        File checkingBaseDir = new File(baseDir);
        File checkingPhotoDir = new File(photoDir);
        File checkingLogoDir = new File(logoDir);

        if (!checkingBaseDir.exists()) {
            result1 = checkingBaseDir.mkdir();
        } else {
            result1 = true;
        }
        if (!checkingPhotoDir.exists()) {
            result2 = checkingPhotoDir.mkdir();
        } else {
            result2 = true;
        }
        if (!checkingLogoDir.exists()) {
            result3 = checkingLogoDir.mkdir();
        } else {
            result3 = true;
        }
        return result1 && result2 && result3;
    }

    public static void distinctFiles(List<InstagramItem> igPhotos) {
        File videoDir = new File(photoDir);
        if (videoDir.exists()) {
            File[] files = videoDir.listFiles();
            int statisticsDeletedFiles = 0;
            for (File file : files) {
                if (file.exists()) {
                    boolean needDelete = true;
                    for (InstagramItem item : igPhotos) {
                        if (file.getName().contains(item.getIgPhotoID())) {
                            needDelete = false;
                            break;
                        }
                    }
                    if(needDelete){
                        if(file.delete()){
                        statisticsDeletedFiles++;}
                    }
                } else {
                    continue;
                }
            }
            Log.w(TAG, "Files deleted " + statisticsDeletedFiles);
        } else {
            Log.d(TAG, "Base folder don't exist");
            checkDirs();
        }

    }
}
