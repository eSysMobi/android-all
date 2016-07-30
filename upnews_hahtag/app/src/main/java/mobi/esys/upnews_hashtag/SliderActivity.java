package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;
import mobi.esys.downloaders.InstagramPhotoDownloaderWeb;
import mobi.esys.eventbus.EventIgCheckingComplete;
import mobi.esys.eventbus.EventIgLoadingComplete;
import mobi.esys.eventbus.SongStopEvent;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.tasks.CheckInstaTagTaskWeb;
import mobi.esys.twitter.model.TwitterHelper;
import mobi.esys.view.PhotoElement;


public class SliderActivity extends Activity {
    private EventBus bus = EventBus.getDefault();
    private transient MediaPlayer mediaPlayer;
    private transient TextView textView;
    private transient RelativeLayout relativeLayout;
    private transient List<PhotoElement> photoElements;
    private transient int[] nextElementsState = {0, 1, 2};

    private transient UNHApp mApp;

    private transient SharedPreferences preferences;

    private transient List<Integer> rawIds;
    private transient List<Integer> imageIds;
    private transient List<Integer> soundIds;
    private transient int musicIndex = 0;
    private transient int musicPosition = 0;

    private static final String URL = "url";

//    private transient Instagram instagram;

    private transient String igHashTag;
    private transient String twHashTag;
    private transient int heightElement = 100;

    private transient boolean isTwAllow;

    private transient static final String TAG = "unTagEmptySlide";

    private transient final Handler changeImagesHandler = new Handler();
    private transient Runnable changeImagesRunnable;
    private transient final Handler twitterFeedHandler = new Handler();
    private transient Runnable twitterFeedRunnable;

    private transient List<InstagramPhoto> igPhotos;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        DirectoryHelper directoryHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        if (directoryHelper.getDirFileList(TAG).length > 0) {
            startActivity(new Intent(SliderActivity.this, MainSliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            init();
        }
    }

    public void init() {
        setContentView(R.layout.activity_main_slider);

        mApp = (UNHApp) getApplicationContext();

//        instagram = new Instagram(SliderActivity.this,
//                ISConsts.instagramconsts.instagram_client_id,
//                ISConsts.instagramconsts.instagram_client_secret,
//                ISConsts.instagramconsts.instagram_redirect_uri);

        rawIds = new ArrayList<>();
        rawIds.addAll(getAllResourceIDs(R.raw.class));

        imageIds = new ArrayList<>();
        soundIds = new ArrayList<>();

        for (int i = 0; i < rawIds.size(); i++) {
            String resName = getResources().getResourceName(rawIds.get(i));
            Log.d("raw id's", resName);
            if (resName.contains("img")) {
                imageIds.add(rawIds.get(i));
            } else if (resName.contains("snd")) {
                soundIds.add(rawIds.get(i));
            }
        }

        textView = new TextView(SliderActivity.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.layoutTwitter);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        heightElement = metrics.widthPixels / 3 - 40;

        LinearLayout llInfo = (LinearLayout) findViewById(R.id.llInfo);
        llInfo.setVisibility(View.GONE);

        photoElements = new ArrayList<>();
        RelativeLayout rlIg1 = (RelativeLayout) findViewById(R.id.rlIg1);
        ImageView ivIg1 = (ImageView) findViewById(R.id.ivIg1);
        TextView tvIg1 = (TextView) findViewById(R.id.tvIg1);
        tvIg1.setVisibility(View.GONE);
        photoElements.add(new PhotoElement(rlIg1, ivIg1, tvIg1));
        RelativeLayout rlIg2 = (RelativeLayout) findViewById(R.id.rlIg2);
        ImageView ivIg2 = (ImageView) findViewById(R.id.ivIg2);
        TextView tvIg2 = (TextView) findViewById(R.id.tvIg2);
        tvIg2.setVisibility(View.GONE);
        photoElements.add(new PhotoElement(rlIg2, ivIg2, tvIg2));
        RelativeLayout rlIg3 = (RelativeLayout) findViewById(R.id.rlIg3);
        ImageView ivIg3 = (ImageView) findViewById(R.id.ivIg3);
        TextView tvIg3 = (TextView) findViewById(R.id.tvIg3);
        tvIg3.setVisibility(View.GONE);
        photoElements.add(new PhotoElement(rlIg3, ivIg3, tvIg3));

        for (int i = 0; i < photoElements.size(); i++) {
            photoElements.get(i).getRelativeLayout().setLayoutParams(new FrameLayout.LayoutParams(heightElement, heightElement));
        }

        igHashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, ISConsts.globals.default_hashtag);
        twHashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, ISConsts.globals.default_hashtag);
        isTwAllow = preferences.getBoolean(ISConsts.prefstags.twitter_allow, false);

        changeImagesRunnable = new Runnable() {
            @Override
            public void run() {
                loadSlide();
                changeImagesHandler.postDelayed(this, 14 * 1000);
            }
        };

        igPhotos = new ArrayList<>();

        playMP3();

        if (isTwAllow) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
            Fabric.with(SliderActivity.this, new Twitter(authConfig));
            twitterFeedRunnable = new Runnable() {
                @Override
                public void run() {
                    Twitter.getInstance();
                    Twitter.getInstance();
                    TwitterHelper.startLoadTweets(Twitter.getApiClient(), twHashTag, relativeLayout, getApplicationContext());
                    twitterFeedHandler.postDelayed(this, 900000);
                }
            };
        }

        updateIGPhotos(igHashTag);

    }

    private void playMP3() {
        mediaPlayer = MediaPlayer.create(SliderActivity.this, soundIds.get(musicIndex));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {


                                                    DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
                                                    String[] photoFileList = photoDirHelper.getDirFileList(TAG);

                                                    if (photoFileList.length == 0) {
                                                        playEmbedded();
                                                        bus.post(new SongStopEvent());
                                                        // restartPhotoDownload();
                                                        //restartMusicDownload();
                                                    } else {
                                                        if (photoFileList.length > 0 && !preferences.getBoolean("isDel", false)) {
                                                            //stopMusicDownload();
                                                            startActivity(new Intent(SliderActivity.this, MainSliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                            finish();

                                                        } else {
                                                            playEmbedded();
                                                            bus.post(new SongStopEvent());
                                                            //restartPhotoDownload();
                                                            //restartMusicDownload();
                                                        }
                                                    }
                                                }
                                            }
        );

        textView.setSelected(true);
        textView.invalidate();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopTwitterHandlersRefresh();
        stopSlidesHandlersRefresh();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        stopTwitterHandlersRefresh();
        stopSlidesHandlersRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(musicPosition);
            mediaPlayer.start();
        }
        restartTwitterHandlersRefresh();
        restartSlidesHandlersRefresh();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopTwitterHandlersRefresh();
        stopSlidesHandlersRefresh();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
    }


    private List<Integer> getAllResourceIDs(Class<?> aClass) throws IllegalArgumentException {
        Field[] IDFields = aClass.getFields();

        List<Integer> ids = new ArrayList<>();

        try {
            for (int i = 0; i < IDFields.length; i++) {
                ids.add(IDFields[i].getInt(null));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        return ids;
    }

    private void loadSlide() {
        Log.w(TAG, "Change slides :" + nextElementsState[0] + "," + nextElementsState[1] + "," + nextElementsState[2]);

        Animation fade_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_1.setFillAfter(true);
        fade_1.setAnimationListener(new AnimList(0));
        photoElements.get(0).getRelativeLayout().startAnimation(fade_1);

        Animation fade_3 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_3.setStartOffset(500);
        fade_3.setFillAfter(true);
        fade_3.setAnimationListener(new AnimList(1));
        photoElements.get(1).getRelativeLayout().startAnimation(fade_3);

        Animation fade_5 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_5.setStartOffset(1000);
        fade_5.setFillAfter(true);
        fade_5.setAnimationListener(new AnimList(2));
        photoElements.get(2).getRelativeLayout().startAnimation(fade_5);
    }

    final class AnimList implements Animation.AnimationListener {
        private int stage;

        public AnimList(int incStage) {
            this.stage = incStage;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            switch (stage) {
                case 0:
                    if (nextElementsState[0] >= imageIds.size()) {
                        nextElementsState[0] = 0;
                    }
                    photoElements.get(0).getImageView().setImageDrawable(getResources().getDrawable(imageIds.get(nextElementsState[0])));
                    nextElementsState[0]++;
                    Log.d(TAG, "nextElementsState[0] =" + nextElementsState[0]);
                    break;
                case 1:
                    nextElementsState[1] = nextElementsState[0];
                    if (nextElementsState[1] >= imageIds.size()) {
                        nextElementsState[1] = 0;
                    }
                    photoElements.get(1).getImageView().setImageDrawable(getResources().getDrawable(imageIds.get(nextElementsState[1])));
                    nextElementsState[1]++;
                    Log.d(TAG, "nextElementsState[1] =" + nextElementsState[1]);
                    break;
                case 2:
                    nextElementsState[2] = nextElementsState[1];
                    if (nextElementsState[2] >= imageIds.size()) {
                        nextElementsState[2] = 0;
                    }
                    photoElements.get(2).getImageView().setImageDrawable(getResources().getDrawable(imageIds.get(nextElementsState[2])));
                    nextElementsState[2]++;
                    Log.d(TAG, "nextElementsState[2] =" + nextElementsState[2]);
                    break;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }
    }

    private void updateIGPhotos(final String tag) {
        final CheckInstaTagTaskWeb checkInstaTagTaskWeb = new CheckInstaTagTaskWeb(tag, false, preferences);
        checkInstaTagTaskWeb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onEvent(EventIgCheckingComplete event) {
        List<InstagramPhoto> igPhotos = event.getIgPhotos();
        if (igPhotos.size() > 0) {
            InstagramPhotoDownloaderWeb downloader = new InstagramPhotoDownloaderWeb(this, mApp.getPhotoDir(), igHashTag);
            downloader.download(igPhotos);
        }
    }

    @Subscribe
    public void onEvent(EventIgLoadingComplete event) {
        Log.d(TAG, "Load IG photo is complete");
        //loadSlide();
    }

//    private void updateIGPhotos(final String tag) {
//        final GetTagPhotoIGTask getTagPhotoIGTask = new GetTagPhotoIGTask(
//                SliderActivity.this,
//                "default", tag, false, mApp);
//        getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());
//
//        Log.d("aT", instagram.getSession().getAccessToken());
//        try {
//            igObject = new JSONObject(getTagPhotoIGTask.get());
//            getIGPhotos();
//        } catch (JSONException e) {
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
//    }
//
//    private void getIGPhotos() {
//        igPhotos = new ArrayList<>();
//        try {
//            final JSONArray igData = igObject.getJSONArray("data");
//            for (int i = 0; i < igData.length(); i++) {
//
//                final JSONObject currLikeObj = igData.getJSONObject(i)
//                        .getJSONObject("likes");
//
//                final JSONObject currObj = igData.getJSONObject(i)
//                        .getJSONObject("images");
//                Log.d("images main", currObj.toString());
//                String origURL = currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type)
//                        .getString(URL);
//                int tmpIndex = origURL.indexOf("?");
//                if (tmpIndex > 0) {
//                    origURL = origURL.substring(0, tmpIndex);
//                }
//                Log.d("images url main", origURL);
//
//                final String idDown = igData.getJSONObject(i).getString("id");
//                String thumbDown = currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type).getString(URL);
//                tmpIndex = thumbDown.indexOf("?");
//                if (tmpIndex > 0) {
//                    thumbDown = thumbDown.substring(0, tmpIndex);
//                }
//                final int likesDown = currLikeObj.getInt("count");
//
//                igPhotos.add(new InstagramPhoto(idDown, thumbDown, origURL, likesDown));
//                Log.d("ig photos main", igPhotos.get(i).toString());
//
//            }
//        } catch (JSONException e) {
//        }
//        InstagramPhotoDownloader instagramPhotoDownloader = new InstagramPhotoDownloader(SliderActivity.this, false);
//        instagramPhotoDownloader.download(igPhotos);
//    }

    public void playEmbedded() {
        musicIndex++;
        if (musicIndex == soundIds.size()) {
            musicIndex = 0;

            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
                afd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
                afd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopSlidesHandlersRefresh() {
        if (changeImagesHandler != null && changeImagesRunnable != null) {
            changeImagesHandler.removeCallbacks(changeImagesRunnable);
        }
    }

    public void restartSlidesHandlersRefresh() {
        if (changeImagesHandler != null && changeImagesRunnable != null) {
            changeImagesHandler.postDelayed(changeImagesRunnable, 1000);
        }
    }

    public void stopTwitterHandlersRefresh() {
        if (twitterFeedHandler != null && twitterFeedRunnable != null) {
            twitterFeedHandler.removeCallbacks(twitterFeedRunnable);
        }
    }

    public void restartTwitterHandlersRefresh() {
        if (isTwAllow && twitterFeedHandler != null && twitterFeedRunnable != null) {
            twitterFeedHandler.postDelayed(twitterFeedRunnable, ISConsts.times.twitter_get_feed_delay);
        }
    }

    @Subscribe
    public void onEvent(SongStopEvent songStop) {
        updateIGPhotos(igHashTag);
    }
}
