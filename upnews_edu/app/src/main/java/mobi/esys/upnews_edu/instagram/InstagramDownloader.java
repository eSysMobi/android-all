package mobi.esys.upnews_edu.instagram;


import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import mobi.esys.upnews_edu.eventbus.EventIgLoadingComplete;
import okio.BufferedSink;
import okio.Okio;


public class InstagramDownloader {
    private final String TAG = "unTag_InstagramDown";
    private transient String photoDownDir;
    private transient Context context;
    private transient int downloaded;
    private transient int needDownload;
    private EventBus bus = EventBus.getDefault();


    public InstagramDownloader(final Context context,
                               final String photoDownDir) {
        this.context = context;
        this.photoDownDir = photoDownDir;
    }

    public void download(List<InstagramItem> igPhotos) {
        needDownload = igPhotos.size();
        for (int i = 0; i < igPhotos.size(); i++) {
            String currFileName = igPhotos.get(i).getIgPhotoID() + getExtensionFromURL(igPhotos.get(i).getIgThumbURL());
            File picFile = new File(photoDownDir, currFileName);
            if (!picFile.exists()) {
                downloadFileAsync(igPhotos.get(i).getIgThumbURL(), currFileName);
            } else {
                Log.d(TAG, "IG file exists, not need download");
                downloaded++;
                if (downloaded == needDownload) {
                    bus.post(new EventIgLoadingComplete());
                }
            }
        }
    }

    private String getExtensionFromURL(String url) {
        String ext = url;
        int srt = ext.lastIndexOf(".");
        ext = ext.substring(srt);
        return ext;
    }

    private void downloadFileAsync(String url, final String fileName) {
        SimpleTarget target = new SimpleTarget<byte[]>() {
            @Override
            public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                File picFile = new File(photoDownDir, fileName);
                try {
                    boolean fileIsOk;
                    if (!picFile.exists()) {
                        fileIsOk = picFile.createNewFile();
                    } else {
                        fileIsOk = true;
                    }
                    if (fileIsOk) {
                        BufferedSink sink = Okio.buffer(Okio.sink(picFile));
                        sink.write(resource);
                        sink.close();
                    } else {
                        Log.e(TAG, "Can't save image. Problem with creating file " + picFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                downloaded++;
                if (downloaded == needDownload) {
                    bus.post(new EventIgLoadingComplete());
                }
            }
        };

        Glide.with(context).load(url).asBitmap().toBytes().into(target);
    }
}
