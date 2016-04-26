package mobi.esys.upnews_tube.download;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mobi.esys.upnews_tube.PlayerActivityYouTube;
import mobi.esys.upnews_tube.constants.Folders;
import mobi.esys.upnews_tube.instagram.InstagramItem;

public class InstagramPhotoDownloader extends AsyncTask<Void, Void, Void> {
    private transient List<InstagramItem> instagramItems;
    private transient String downloadDir;
    private transient Context context;
    private transient int downloadIndex = 0;
    private transient String tag;
    private transient List<File> filesMask;
    private transient SharedPreferences preferences;


    private transient DownloadState currentState;

    @Override
    protected Void doInBackground(Void... params) {
        download();
        return null;
    }


    public InstagramPhotoDownloader(String downFolder, List<InstagramItem> instagramItems, Context context, String tag) {
        this.downloadDir = downFolder;
        this.instagramItems = instagramItems;
        this.context = context;
        this.tag = tag;
        this.filesMask = new ArrayList<>();
        this.preferences = context.getSharedPreferences("unoPref", Context.MODE_PRIVATE);
        clearFolder();
    }


    public void download() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isDown", true);
        editor.apply();
        for (InstagramItem instagramItem : instagramItems) {

            try {
                String url = instagramItem.getIgOriginURL();
                String fileName = FilenameUtils.getBaseName(url).concat(".")
                        .concat(FilenameUtils.getExtension(url));
                Log.d("ig url", url);
                Log.d("ig file name", fileName);
                File file = new File(downloadDir, fileName);
                filesMask.add(file);
                if (!file.exists()) {
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                    Glide.with(context).load(url).asBitmap().into(100, 100).get().compress(Bitmap.CompressFormat.JPEG, 50, os);
                    os.close();
                    os.flush();

                } else {
                    break;
                }

            } catch (IOException | InterruptedException | ExecutionException ignored) {
                if (ignored.getMessage() != null) {
                    Log.d("ig error", ignored.getMessage());
                }
            }



        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        ((PlayerActivityYouTube) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((PlayerActivityYouTube) context).loadSlide(tag);
            }
        });
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isDown", false);
        editor.apply();

    }

    public DownloadState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DownloadState currentState) {
        this.currentState = currentState;
    }

    public void clearFolder() {
        File[] igPhotosFileList = new File(Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.PHOTO_FOLDER)).listFiles();
        Log.d("photo folder", Arrays.asList(igPhotosFileList).toString());

        for (File photoFile : igPhotosFileList) {
            photoFile.delete();
        }
    }
}
