package mobi.esys.upnews_tube.instagram;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import mobi.esys.upnews_tube.eventbus.EventIgLoadingComplete;
import okio.BufferedSink;
import okio.Okio;

public class InstagramDownloader {
    private final String TAG = "unTag_InstagramDown";
    private transient String photoDownDir;
    private transient Context context;
    private transient int downloaded;
    private transient int needDownload;
    private transient String tag;
    private EventBus bus = EventBus.getDefault();


    public InstagramDownloader(final Context context,
                               final String photoDownDir,
                               final String tag) {
        this.context = context;
        this.photoDownDir = photoDownDir;
        this.tag = tag;
    }

    public void download(String urls) {
        String[] urlArray = urls.split(",");

        needDownload = urlArray.length;
        for (int i = 0; i < urlArray.length; i++) {
            String currFileName = getNameFromArray(urlArray[i]);
            File picFile = new File(photoDownDir, currFileName);
            if(!picFile.exists()){
                downloadFileAsync(urlArray[i], currFileName);
            } else{
                Log.d(TAG, "IG file exists, not need download");
                downloaded++;
            }
        }
    }

    private String getNameFromArray(String url) {
        String name = url;
        int srt = name.lastIndexOf("/") + 1;
        int end = name.length();
        int end2 = name.indexOf("?");
        if (end2 != -1) {
            end = end2;
        }
        name = name.substring(srt, end);

        return name;
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
                    } else{
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