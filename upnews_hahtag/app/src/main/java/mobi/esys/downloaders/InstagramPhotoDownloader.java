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
import mobi.esys.filesystem.IOHelper;
import mobi.esys.instagram.InstagramItem;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by ZeyUzh on 28.07.2016.
 */
public class InstagramPhotoDownloader {
    private final String TAG = "unTag_InstagramDown";

    private Context context;
    private int downloaded;
    private int needDownload;

    private int loadedFromGlideFiles;

    private EventBus bus = EventBus.getDefault();


    public InstagramPhotoDownloader(final Context context) {
        this.context = context;
    }

    public void download(List<InstagramItem> igPhotos) {

        needDownload = igPhotos.size();
        for (int i = 0; i < igPhotos.size(); i++) {
            String url = igPhotos.get(i).getIgOriginURL();
            String currFileName = igPhotos.get(i).getIgPhotoID() + IOHelper.getExtension(url);
            File picFile = new File(IOHelper.getPhotoDir(), currFileName);
            if (!picFile.exists()) {
                downloadFileAsync(url, currFileName);
            } else {
                downloaded++;
                if (downloaded == needDownload) {
                    bus.post(new EventIgLoadingComplete(loadedFromGlideFiles));
                    Log.w(TAG, "Downloaded " + loadedFromGlideFiles + " files from " + needDownload);
                }
            }
        }
    }

    private void downloadFileAsync(String url, final String fileName) {
        SimpleTarget target = new SimpleTarget<byte[]>() {
            @Override
            public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                File picFile = new File(IOHelper.getPhotoDir(), fileName);
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
                        loadedFromGlideFiles++;
                    } else {
                        Log.e(TAG, "Can't save image. Problem with creating file " + picFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                downloaded++;
                if (downloaded == needDownload) {
                    bus.post(new EventIgLoadingComplete(loadedFromGlideFiles));
                    Log.w(TAG, "Downloaded " + loadedFromGlideFiles + " files from " + needDownload);
                }
            }
        };

        Glide.with(context).load(url).asBitmap().toBytes().into(target);
    }
}
