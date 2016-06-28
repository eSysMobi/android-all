package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.londatiga.android.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;
import mobi.esys.downloaders.InstagramPhotoDownloader;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.filesystem.files.FilesHelper;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.view.PhotoElement;
import mobi.esys.tasks.GetTagPhotoIGTask;
import mobi.esys.twitter.model.TwitterHelper;


public class MainSliderActivity extends Activity {
    private static final String URL = "url";
    private transient List<PhotoElement> photoElements;
    private transient int[] nextElementsState = {0, 1, 2};

    private transient RelativeLayout relativeLayout;
    private transient TextView tvTagView;
    private transient UNHApp mApp;
    private transient SharedPreferences preferences;

    private transient String[] photoFiles;

    private transient JSONObject igObject;

    private transient Instagram instagram;

    private transient String igHashTag;
    private transient String twHashTag;

    private transient boolean isTwAllow;

    private transient final String TAG = "unTagMainSlider";

    private transient List<InstagramPhoto> igPhotos;

    private transient int musicIndex;
    private transient MediaPlayer mediaPlayer;

    private transient List<Integer> soundIds;
    private transient List<Integer> rawIds;
    private transient List<Integer> imageIds;

    private transient int heightElement = 100;

    private transient final Handler changeImagesHandler = new Handler();
    private transient Runnable changeImagesRunnable;
    private transient final Handler twitterFeedHandler = new Handler();
    private transient Runnable twitterFeedRunnable;

    private transient ImageView logoView;
    private FilesHelper logoFileHelper;

    private transient int musicPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_slider);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        igHashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, ISConsts.globals.default_hashtag);
        twHashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, ISConsts.globals.default_hashtag);
        isTwAllow = preferences.getBoolean(ISConsts.prefstags.twitter_allow, false);


        igPhotos = new ArrayList<>();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        heightElement = metrics.widthPixels / 3 - 40;

        photoElements = new ArrayList<>();
        RelativeLayout rlIg1 = (RelativeLayout) findViewById(R.id.rlIg1);
        ImageView ivIg1 = (ImageView) findViewById(R.id.ivIg1);
        TextView tvIg1 = (TextView) findViewById(R.id.tvIg1);
        photoElements.add(new PhotoElement(rlIg1, ivIg1, tvIg1));
        RelativeLayout rlIg2 = (RelativeLayout) findViewById(R.id.rlIg2);
        ImageView ivIg2 = (ImageView) findViewById(R.id.ivIg2);
        TextView tvIg2 = (TextView) findViewById(R.id.tvIg2);
        photoElements.add(new PhotoElement(rlIg2, ivIg2, tvIg2));
        RelativeLayout rlIg3 = (RelativeLayout) findViewById(R.id.rlIg3);
        ImageView ivIg3 = (ImageView) findViewById(R.id.ivIg3);
        TextView tvIg3 = (TextView) findViewById(R.id.tvIg3);
        photoElements.add(new PhotoElement(rlIg3, ivIg3, tvIg3));

        for (int i = 0; i < photoElements.size(); i++) {
            photoElements.get(i).getRelativeLayout().setLayoutParams(new FrameLayout.LayoutParams(heightElement, heightElement));
        }

        relativeLayout = (RelativeLayout) findViewById(R.id.layoutTwitter);
        logoView = (ImageView) findViewById(R.id.logoMainSlider);
        tvTagView = (TextView) findViewById(R.id.tvTagView);
        tvTagView.setText(igHashTag);

        changeImagesRunnable = new Runnable() {
            @Override
            public void run() {
                loadSlide();
                changeImagesHandler.postDelayed(this, 14 * 1000);
            }
        };

        instagram = new Instagram(MainSliderActivity.this,
                ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret,
                ISConsts.instagramconsts.instagram_redirect_uri);

        mApp = (UNHApp) getApplicationContext();

        DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        photoFiles = photoDirHelper.getDirFileList(TAG);

        loadRes();
        loadSlide();
        initTwitter();
        playMP3();
        updateIGPhotos(igHashTag);

        String logoFile = Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name)
                .concat(ISConsts.globals.dir_changeable_logo_name)
                .concat(ISConsts.globals.changeable_logo_name);
        logoFileHelper = new FilesHelper(logoFile, getApplicationContext());
    }

    private void updateIGPhotos(final String tag) {
        final GetTagPhotoIGTask getTagPhotoIGTask = new GetTagPhotoIGTask(
                MainSliderActivity.this,
                "default", tag, false, mApp);
        getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());

        Log.d("aT main", instagram.getSession().getAccessToken());
        try {
            igObject = new JSONObject(getTagPhotoIGTask.get());
            Log.d("object", igObject.toString());
            getIGPhotos(igObject);
        } catch (JSONException e) {
            Log.d("error", "json error");
        } catch (InterruptedException e) {
            Log.d("error", "interrupted error");
        } catch (ExecutionException e) {
            Log.d("error", "execution error");
        }

    }

    private void getIGPhotos(JSONObject igObject) {
        igPhotos = new ArrayList<>();
        try {
            Log.d("object main", igObject.toString());
            final JSONArray igData = igObject.getJSONArray("data");

            for (int i = 0; i < igData.length(); i++) {

                final JSONObject currLikeObj = igData.getJSONObject(i)
                        .getJSONObject("likes");

                final JSONObject currObj = igData.getJSONObject(i)
                        .getJSONObject("images");
                Log.d("images main", currObj.toString());
                String origURL = currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type)
                        .getString(URL);
                int tmpIndex = origURL.indexOf("?");
                if (tmpIndex > 0) {
                    origURL = origURL.substring(0, tmpIndex);
                }
                Log.d("images url main", origURL);

                final String idDown = igData.getJSONObject(i).getString("id");
                String thumbDown = currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type).getString(URL);
                tmpIndex = thumbDown.indexOf("?");
                if (tmpIndex > 0) {
                    thumbDown = thumbDown.substring(0, tmpIndex);
                }
                final int likesDown = currLikeObj.getInt("count");

                igPhotos.add(new InstagramPhoto(idDown, thumbDown, origURL, likesDown));
                Log.d("ig photos main", igPhotos.get(i).toString());

            }

            InstagramPhotoDownloader instagramPhotoDownloader = new InstagramPhotoDownloader(MainSliderActivity.this, true);
            instagramPhotoDownloader.download(igPhotos);

        } catch (JSONException e) {
            Log.d("json", "json_error");
        }
    }

    public void loadSlide() {
        Log.d(TAG.concat("_photo"), igPhotos.toString());
        Log.d(TAG, "change slides");
        DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        photoFiles = photoDirHelper.getDirFileList(TAG);

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
                    if (nextElementsState[0] >= photoFiles.length) {
                        nextElementsState[0] = 0;
                    }
                    File imgFile = new File(photoFiles[nextElementsState[0]]);
                    boolean finded = false;
                    for (int j = nextElementsState[0]; j < photoFiles.length; j++) {
                        imgFile = new File(photoFiles[j]);
                        if (imgFile.exists()) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        photoElements.get(0).getImageView().setImageBitmap(bitmap);
                        nextElementsState[0]++;
                        String target = imgFile.getName().substring(0, imgFile.getName().lastIndexOf("."));
                        int likes = 0;
                        for (int k = 0; k < igPhotos.size(); k++) {
                            String searchable = igPhotos.get(k).getIgPhotoID();
                            if (searchable.equals(target)) {
                                likes = igPhotos.get(k).getIgLikes();
                                break;
                            }
                        }
                        photoElements.get(0).getTextView().setText("\u2764 " + likes);
                    } else {
                        Log.w(TAG, "All files is corrupted!");
                    }
                    break;
                case 1:
                    nextElementsState[1] = nextElementsState[0];
                    if (nextElementsState[1] >= photoFiles.length) {
                        nextElementsState[1] = 0;
                    }
                    imgFile = new File(photoFiles[nextElementsState[1]]);
                    finded = false;
                    for (int j = nextElementsState[1]; j < photoFiles.length; j++) {
                        imgFile = new File(photoFiles[j]);
                        if (imgFile.exists()) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        photoElements.get(1).getImageView().setImageBitmap(bitmap);
                        nextElementsState[1]++;
                        String target = imgFile.getName().substring(0, imgFile.getName().lastIndexOf("."));
                        int likes = 0;
                        for (int k = 0; k < igPhotos.size(); k++) {
                            String searchable = igPhotos.get(k).getIgPhotoID();
                            if (searchable.equals(target)) {
                                likes = igPhotos.get(k).getIgLikes();
                                break;
                            }
                        }
                        photoElements.get(1).getTextView().setText("\u2764 " + likes);
                    } else {
                        Log.w(TAG, "All files is corrupted!");
                    }
                    break;
                case 2:
                    nextElementsState[2] = nextElementsState[1];
                    if (nextElementsState[2] >= photoFiles.length) {
                        nextElementsState[2] = 0;
                    }
                    imgFile = new File(photoFiles[nextElementsState[2]]);
                    finded = false;
                    for (int j = nextElementsState[2]; j < photoFiles.length; j++) {
                        imgFile = new File(photoFiles[j]);
                        if (imgFile.exists()) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        photoElements.get(2).getImageView().setImageBitmap(bitmap);
                        nextElementsState[2]++;
                        String target = imgFile.getName().substring(0,imgFile.getName().lastIndexOf("."));
                        int likes = 0;
                        for (int k = 0; k < igPhotos.size(); k++) {
                            String searchable = igPhotos.get(k).getIgPhotoID();
                            if (searchable.equals(target)) {
                                likes = igPhotos.get(k).getIgLikes();
                                break;
                            }
                        }
                        photoElements.get(2).getTextView().setText("\u2764 " + likes);
                    } else {
                        Log.w(TAG, "All files is corrupted!");
                    }
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


    private void playMP3() {
        mediaPlayer = MediaPlayer.create(MainSliderActivity.this, soundIds.get(0));
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playEmbedded();
                updateIGPhotos(igHashTag);
            }
        });

        mediaPlayer.start();
    }

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
                Log.d(TAG, e.getLocalizedMessage());
            }

        } else {
            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                afd.close();
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

        }
    }


    public List<Integer> getAllResourceIDs(Class<?> aClass) throws IllegalArgumentException {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        stopSlidesHandlersRefresh();
        stopTwitterHandlersRefresh();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        stopSlidesHandlersRefresh();
        stopTwitterHandlersRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(musicPosition);
            mediaPlayer.start();
        }
        restartSlidesHandlersRefresh();
        restartTwitterHandlersRefresh();
        checkLogo();
    }

    //Check logo.
    private void checkLogo() {
        Log.d("TAG1", "Check logo in onResume.");
        logoFileHelper.createLogoInExternalStorage();
        Bitmap logoFromFile = logoFileHelper.getLogoFromExternalStorage();
        logoView.setImageBitmap(logoFromFile);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        stopSlidesHandlersRefresh();
        stopTwitterHandlersRefresh();
        trimCache(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    public void loadRes() {
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

    public void initTwitter() {
        if (isTwAllow) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
            Fabric.with(this, new Twitter(authConfig));
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
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                Log.d("unTagMainSlider","Start deleting cache");
                deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


}


