package mobi.esys.downloaders;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import mobi.esys.eventbus.EventIgLoadingComplete;
import mobi.esys.instagram.model.InstagramPhoto;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by ZeyUzh on 28.07.2016.
 */
public class InstagramPhotoDownloaderWeb {
    private final String TAG = "unTag_InstagramDown";
    private transient String photoDownDir;
    private transient Context context;
    private transient int downloaded;
    private transient int needDownload;
    private transient String tag;
    private EventBus bus = EventBus.getDefault();


    public InstagramPhotoDownloaderWeb(final Context context,
                                       final String photoDownDir,
                                       final String tag) {
        this.context = context;
        this.photoDownDir = photoDownDir;
        this.tag = tag;
    }

    public void download(List<InstagramPhoto> igPhotos) {

        needDownload = igPhotos.size();
        for (int i = 0; i < igPhotos.size(); i++) {
            String url = igPhotos.get(i).getIgThumbURL();
            String currFileName = igPhotos.get(i).getIgPhotoID() + getExtension(url);
            File picFile = new File(photoDownDir, currFileName);
            if (!picFile.exists()) {
                downloadFileAsync(url, currFileName);
            } else {
                Log.d(TAG, "IG file exists, not need download");
                downloaded++;
                if (downloaded == needDownload) {
                    bus.post(new EventIgLoadingComplete(tag));
                }
            }
        }
    }

    private String getExtension(String url) {
        String extension = url;
        int srt = extension.lastIndexOf(".");
        extension = extension.substring(srt, extension.length());
        return extension;
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
                    bus.post(new EventIgLoadingComplete(tag));
                }
            }
        };

        Glide.with(context).load(url).asBitmap().toBytes().into(target);
    }
}
