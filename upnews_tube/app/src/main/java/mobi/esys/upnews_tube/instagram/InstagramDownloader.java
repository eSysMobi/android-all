package mobi.esys.upnews_tube.instagram;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.IOException;

import mobi.esys.upnews_tube.PlayerActivityYouTube;
import okio.BufferedSink;
import okio.Okio;


public class InstagramDownloader {
    private transient String photoDownDir;
    private transient Context context;
    private transient String lastFileName;
    private transient String tag;


    public InstagramDownloader(final Context context,
                               final String photoDownDir,
                               final String tag) {
        this.context = context;
        this.photoDownDir = photoDownDir;
        this.tag = tag;
    }


    public void download(String urls) {
        String[] urlArray = urls.split(",");

        lastFileName = getNameFromArray(urlArray[urlArray.length - 1]);
        for (int i = 0; i < urlArray.length; i++) {
            String currFileName = getNameFromArray(urlArray[i]);
            Log.d("file name", currFileName);

            downloadFileAsync(urlArray[i], currFileName);
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

    public void downloadFileAsync(String url, final String fileName) {

        Glide.with(context).load(url).asBitmap().toBytes().into(new SimpleTarget<byte[]>() {
                                                                    @Override
                                                                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                                                        File picFile = new File(photoDownDir, fileName);
                                                                        Log.d("pic file", picFile.getAbsolutePath());
                                                                        try {
                                                                            if (!picFile.exists()) {
                                                                                picFile.createNewFile();
                                                                            }


                                                                            BufferedSink sink = Okio.buffer(Okio.sink(picFile));
                                                                            sink.write(resource);
                                                                            sink.close();
                                                                        } catch (IOException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        if (fileName.equals(lastFileName)) {
                                                                            ((PlayerActivityYouTube) context).loadSlide(tag);
                                                                        }
                                                                    }
                                                                }
        );
    }
}