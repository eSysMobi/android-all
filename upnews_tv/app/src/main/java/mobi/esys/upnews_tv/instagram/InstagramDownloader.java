package mobi.esys.upnews_tv.instagram;


import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import mobi.esys.upnews_tv.PlayerActivity;
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


    public void download(List<InstagramItem> instagramPhotos) {
        Log.d("new download", instagramPhotos.toString());
        lastFileName = "photo".concat(String.valueOf(instagramPhotos.size()).concat(".").concat(FilenameUtils.getExtension(instagramPhotos.get(0).getIgOriginURL())));
        for (int i = 0; i < instagramPhotos.size(); i++) {

            String currFileName = "photo".concat(String.valueOf(i + 1).concat(".").concat(FilenameUtils.getExtension(instagramPhotos.get(0).getIgOriginURL())));
            Log.d("file name", currFileName);

            String url = instagramPhotos.get(i).getIgOriginURL();
            downloadFileAsync(url, currFileName);


        }


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
                                                                        } catch (
                                                                                IOException e
                                                                                )

                                                                        {
                                                                            e.printStackTrace();
                                                                        }
                                                                        if (fileName.equals(lastFileName)) {
                                                                            ((PlayerActivity) context).loadSlide(tag);

                                                                        }
                                                                    }
                                                                }

        );

    }


}
