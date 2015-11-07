package mobi.esys.upnews_play.playback;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import mobi.esys.upnews_play.App;
import mobi.esys.upnews_play.R;
import mobi.esys.upnews_play.consts.Consts;
import mobi.esys.upnews_play.filesystem.FileSystemHelper;
import mobi.esys.upnews_play.slideshow.Slideshow;

public class VideoPlayback {
    private VideoView video;
    private ImageView slider;
    private File folderInst;
    private int playbackIndex = 0;
    private Context context;
    private int stopPosition = 0;
    private int stopIndex = 0;

    private transient App app;

    private transient boolean isSlideshow=false;


    private transient List<File> picFiles;
    private transient Slideshow slideshow;
    private transient MediaPlayer musicPlayer;


    public VideoPlayback(VideoView video, File folderInst, Context context, ImageView slider) {
        this.video = video;
        this.folderInst = folderInst;
        this.context = context;
        this.slider = slider;
        this.app=(App)((Activity)context).getApplication();
    }

    public void init() {
        playbackIndex = 0;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1) {
            video.buildLayer();
        }



        Log.d("folder play",app.getAppFolder().getAbsolutePath());
        picFiles=FileSystemHelper.getFileListByExts(app.getAppFolder(),Consts.PICTURES_EXTS);


        MediaController mediaController = new MediaController(context);
        mediaController.setVisibility(View.GONE);
        mediaController.setAnchorView(video);

        video.setMediaController(mediaController);

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });


        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(mp.isPlaying()) {
                    mp.stop();
                }
                playNext();
                return false;
            }
        });
    }

    public void stop() {
        if (video.isPlaying()) {
            video.stopPlayback();
        }
        stopPosition = video.getCurrentPosition();
        stopIndex = playbackIndex;
    }

    public void resume() {
        if(folderInst.listFiles().length>0) {
            video.setVideoPath(folderInst.listFiles()[stopIndex].getAbsolutePath());
            video.seekTo(stopPosition);
            video.start();
        }
    }

    public void start() {
        playNext();
    }

    private boolean isPlayBackFolderEmpty() {
        Log.d("folder", folderInst.getAbsolutePath());
        return folderInst.listFiles().length == 0;
    }

    public void playNext() {
        video.setVisibility(View.VISIBLE);
        if (!isPlayBackFolderEmpty()) {
            List<File> fs = FileSystemHelper.getFileListByExts(folderInst, Consts.MEDIA_EXTS);
            if (playbackIndex > fs.size() - 1) {
                playbackIndex = 0;
            }
            video.refreshDrawableState();
            video.setDrawingCacheEnabled(true);
            video.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            Log.d("file #".concat(String.valueOf(playbackIndex)), FilenameUtils.
                    getExtension(fs.get(playbackIndex).getName()));

            if (Arrays.asList(Consts.MUSIC_EXTS).
                    contains(FilenameUtils.getExtension(fs.get(playbackIndex).getName()))) {
                musicPlayer=new MediaPlayer();


                List<File> animPicFiles=FileSystemHelper.getFileListByName(picFiles,
                        FilenameUtils.getBaseName(fs.get(playbackIndex).getName()).concat("_"));
                slideshow = new Slideshow(Consts.ANIM_DELAY, context, animPicFiles, slider);

                if(animPicFiles.size()>1) {
                    isSlideshow=true;
                    slideshow.startAnimation();
                }
                else if(animPicFiles.size()==1){
                    isSlideshow=false;
                    Bitmap bitmap=null;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    try {
                        bitmap = BitmapFactory.decodeStream(new FileInputStream(animPicFiles.get(0))
                                , null, options);
                    } catch (FileNotFoundException ignored) {
                    }

                    slider.setImageBitmap(bitmap);
                }
                else{
                    isSlideshow=false;
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.upnews_logo_w2);
                    slider.setImageBitmap(bitmap);
                }
                playMusicFile(fs.get(playbackIndex));
            } else {
                if (Arrays.asList(Consts.VIDEOS_EXTS).contains(FilenameUtils.
                        getExtension(fs.get(playbackIndex).getName()))) {
                    video.setVideoPath(fs.get(playbackIndex).getAbsolutePath());
                    video.start();
                    playbackIndex++;
                } else {
                    playNext();
                }
            }

        } else {
            video.stopPlayback();
        }
    }

    public void playMusicFile(File musicFile) {
        video.setVisibility(View.GONE);
        slider.setVisibility(View.VISIBLE);

        if (playbackIndex > folderInst.listFiles().length - 1) {
            playbackIndex = 0;
        }

        try {
            musicPlayer.setDataSource(musicFile.getAbsolutePath());
        } catch (IllegalArgumentException | IllegalStateException | IOException ignored) {
        }
        if(!musicPlayer.isPlaying()) {
            try {
                musicPlayer.prepare();
            } catch (IllegalStateException | IOException ignored) {
            }
        }


        musicPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(mp.isPlaying()) {
                    mp.stop();
                }
                slider.setVisibility(View.GONE);
                playNext();
                return false;
            }
        });

        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isSlideshow) {
                    slideshow.stopAnimation();
                }
                slider.setVisibility(View.GONE);
                mp.release();
                playNext();
            }
        });
        musicPlayer.start();
        playbackIndex++;
    }

}

