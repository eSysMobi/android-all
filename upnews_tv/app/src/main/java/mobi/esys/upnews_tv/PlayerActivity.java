package mobi.esys.upnews_tv;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.londatiga.android.instagram.Instagram;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import mobi.esys.upnews_tv.cbr.CurrenciesList;
import mobi.esys.upnews_tv.cbr.GetCurrencies;
import mobi.esys.upnews_tv.constants.DevelopersKeys;
import mobi.esys.upnews_tv.constants.Folders;
import mobi.esys.upnews_tv.constants.TimeConsts;
import mobi.esys.upnews_tv.download.DownloadState;
import mobi.esys.upnews_tv.download.FacebookVideoDownloadHelper;
import mobi.esys.upnews_tv.eventbus.EventCurrency;
import mobi.esys.upnews_tv.eventbus.EventGetLocComplete;
import mobi.esys.upnews_tv.eventbus.EventIgCheckingComplete;
import mobi.esys.upnews_tv.eventbus.EventIgLoadingComplete;
import mobi.esys.upnews_tv.facebook.FacebookVideoItem;
import mobi.esys.upnews_tv.filesystem.FileSystemHelper;
import mobi.esys.upnews_tv.instagram.CheckInstaTagTask;
import mobi.esys.upnews_tv.instagram.InstagramDownloader;
import mobi.esys.upnews_tv.instagram.InstagramItem;
import mobi.esys.upnews_tv.net.NetMonitor;
import mobi.esys.upnews_tv.tasks.GetLocationTask;
import mobi.esys.upnews_tv.twitter.TwitterHelper;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherExceptionListener;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

public class PlayerActivity extends Activity implements YahooWeatherInfoListener,
        YahooWeatherExceptionListener {

    private transient UpnewsOnlineApp mApp;

    private transient RelativeLayout relativeLayout;
    private transient Instagram instagram;
    private transient SliderLayout mSlider;
    //private transient List<InstagramItem> igPhotos;
    private transient List<FacebookVideoItem> videoItemsTmp;
    private transient List<FacebookVideoItem> videoItems;
    private transient int videoIndex = 0;

    public static final String[] VIDEOS_EXTS = {"avi", "mp4"};

    private transient VideoView playerView;

    private transient ProgressDialog dialog;

    private YahooWeather mYahooWeather = YahooWeather.getInstance(
            TimeConsts.WEATHER_REQUEST_TIMEOUT,
            TimeConsts.WEATHER_REQUEST_TIMEOUT, true);

    boolean isEuroUp = true;
    boolean isDollarUp = true;
    boolean isPoundUp = true;
    boolean isRubUp = true;
    boolean isYenaUp = true;
    boolean isYanUp = true;

    private transient LinearLayout dashLayout;
    private transient LinearLayout weatherLayout;

    private transient boolean isFirst = true;

    private transient SharedPreferences preferences;
    private boolean isStartVideo = true;
    private transient String nextVideo = "";

    private transient ImageView groupIconImageView;
    private transient EasyTracker easyTracker;

    private transient Uri embUri = Uri.parse("android.resource://mobi.esys.upnews_online/raw/emb");
    private transient Handler locationHandler;
    private transient Runnable locationRunnable;

    private transient Handler facebookRefreshHandler;
    private transient Runnable facebookRefreshRunnable;

    private final Handler currHandler = new Handler();
    private Runnable currRunnable;

    private final Handler instagramHandler = new Handler();
    private Runnable instagramRunnable;

    private final Handler twitterFeedHandler = new Handler();
    private Runnable twitterFeedRunnable;

    private final static int PERMISSION_REQUEST_CODE = 334;
    private transient boolean allowflag = false;

    private transient Handler handlerHideUI;
    private transient View decorView = null;
    private static transient int DELAY_NAV_HIDE = 2000;

    private final static String TAG = "unTag_PlayerActivity";
    private transient boolean needShowInstagram = false;
    private transient boolean needShowTwitter = false;
    private transient boolean readyShowWeather = false;
    private transient String hashTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        embUri = Uri.parse("android.resource://" + getPackageName() + "/assets/" + R.raw.emb);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setFormat(PixelFormat.TRANSPARENT);

        setContentView(R.layout.activity_main);

        easyTracker = EasyTracker.getInstance(PlayerActivity.this);

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);
        needShowInstagram = preferences.getBoolean("needShowInstagram", false);
        needShowTwitter = preferences.getBoolean("needShowTwitter", false);
        hashTag = preferences.getString("instHashTag", "");

        Log.w(TAG, "needShowInstagram = " + needShowInstagram);
        Log.w(TAG, "needShowTwitter = " + needShowTwitter);

        mApp = (UpnewsOnlineApp) getApplicationContext();

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mSlider = (SliderLayout) findViewById(R.id.photoSlider);
        mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        mSlider.setDuration(TimeConsts.SLIDER_DURATION);

        mYahooWeather.setExceptionListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.playerID);
        LinearLayout clockLayout = (LinearLayout) findViewById(R.id.clockLayout);

        groupIconImageView = new ImageView(PlayerActivity.this);
        LinearLayout.LayoutParams giiLP = new LinearLayout.LayoutParams(
                90,
                90);
        giiLP.setMargins(10, 0, 0, 0);
        giiLP.gravity = Gravity.CENTER_VERTICAL;
        groupIconImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        groupIconImageView.setLayoutParams(giiLP);
        clockLayout.addView(groupIconImageView);

        getGroupIconURL();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextClock digitalClock = new TextClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams dgLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(10, 0, 0, 0);
            dgLP.gravity = Gravity.CENTER_VERTICAL;
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            digitalClock.setId(R.id.clock);
            digitalClock.setFormat24Hour("k:mm");
            clockLayout.addView(digitalClock);
        } else {
            DigitalClock digitalClock = new DigitalClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams dgLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(10, 0, 0, 0);
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            dgLP.gravity = Gravity.CENTER_VERTICAL;
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setId(R.id.clock);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            clockLayout.addView(digitalClock);
        }


        //igPhotos = new ArrayList<>();
        videoItems = new ArrayList<>();
        videoItemsTmp = new ArrayList<>();


        dashLayout = new LinearLayout(this);
        weatherLayout = new LinearLayout(this);

        relativeLayout.addView(dashLayout);
        relativeLayout.addView(weatherLayout);


        dialog = new ProgressDialog(this);
        dialog.setTitle("");
        dialog.setMessage("Buffering...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);


        playerView = (VideoView) findViewById(R.id.video);

        MediaController mediaController = new MediaController(this);
        mediaController.setVisibility(View.GONE);
        mediaController.setAnchorView(playerView);

        // Init Video
        playerView.setMediaController(mediaController);

        instagram = new Instagram(PlayerActivity.this, DevelopersKeys.INSTAGRAM_CLIENT_ID, DevelopersKeys.INSTAGRAM_CLIENT_SECRET, DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                allowflag = true;
            }
        } else {
            allowflag = true;
        }

        locationHandler = new Handler();
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (allowflag) {
                    GetLocationTask glt = new GetLocationTask(mApp);
                    glt.execute();
                    //getLocation();
                }
                locationHandler.postDelayed(this, TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };


        //start facebook loading every 5 minutes
        facebookRefreshHandler = new Handler();
        facebookRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Attempt start facebook download. Status = " + FacebookVideoDownloadHelper.getInstance().getCurrentState());
                if (FacebookVideoDownloadHelper.getInstance().getCurrentState() != DownloadState.DOWNLOAD_IN_PROGRESS) {
                    Log.d(TAG, "Start new facebook download");
                    loadfbGroupVideos();
                } else {
                    Log.d(TAG, "New facebook download not started because old has state DOWNLOAD_IN_PROGRESS");
                }
                facebookRefreshHandler.postDelayed(this, TimeConsts.FACEBOOK_REFRESH_INTERVAL);
            }
        };

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            playerView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (!dialog.isShowing()) {
                                dialog.show();
                            }
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            break;
                    }
                    return false;
                }
            });
        }

        playerView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                easyTracker.send(MapBuilder.createEvent("playback",
                        "video_playback_error", "Error while video playback, go to next video", null).build());

                nextFile();
                return true;
            }
        });

        String path = Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.VIDEO_FOLDER);
        File[] folderList = new File(path).listFiles();
        Log.d(TAG, "all files in video folder" + Arrays.toString(folderList));
        List<File> mp4Files = new ArrayList<>();

        for (File file : folderList) {
            if ("mp4".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                mp4Files.add(file);
            }
        }
        Log.d(TAG, "mp4 files in video folder" + mp4Files.toString());

        if (mp4Files.size() > 0) {
            nextFile();
        } else {
            playerView.setVideoURI(embUri);
            Log.d(TAG, "We have NO mp4 files in video folder, start default video");
        }

        playerView.start();

        playerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextFile();
            }
        });

        TwitterAuthConfig authConfig = new TwitterAuthConfig(DevelopersKeys.TWITTER_KEY, DevelopersKeys.TWITTER_SECRET);
        Fabric.with(PlayerActivity.this, new Twitter(authConfig));
        twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                if (NetMonitor.isNetworkAvailable(mApp)) {
                    Twitter.getInstance();
                    TwitterHelper.startLoadTweets(Twitter.getApiClient(), relativeLayout, PlayerActivity.this, isFirst);
                    isFirst = false;
                    twitterFeedHandler.postDelayed(this, TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
                } else {
                    Toast.makeText(PlayerActivity.this, "Twitter is unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        };

        instagramRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(hashTag, true);
                    checkInstaTagTask.execute(instagram.getSession().getAccessToken());
                } finally {
                    instagramHandler.postDelayed(this, TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
                }
            }
        };

        currRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrencies();
                currHandler.postDelayed(this, TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };

        //prepare handler for hide Android status bar
        if (Build.VERSION.SDK_INT >= 14) {
            decorView = getWindow().getDecorView();
            setUISmall();
            handlerHideUI = new mHandler(this);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility != View.SYSTEM_UI_FLAG_LOW_PROFILE) {
                        //Log.d("unTag_FullscreenAct", "onSystemUiVisibilityChange " + visibility + " need hide nav. Post delay handler");
                        handlerHideUI.sendEmptyMessageDelayed(32, DELAY_NAV_HIDE);
                    }
                }
            });
        }
    }

    @Subscribe
    public void onEvent(EventIgLoadingComplete event) {
        startSlides();
    }

    private void startSlides() {
        String path = Folders.SD_CARD
                .concat(File.separator)
                .concat(Folders.BASE_FOLDER)
                .concat(File.separator)
                .concat(Folders.PHOTO_FOLDER);
        File[] folderList = new File(path).listFiles();

        if (folderList.length > 0) {
            Log.w(TAG, "Start autocycle " + folderList.length + " photos");

            mSlider.stopAutoCycle();
            mSlider.removeAllSliders();
            //mSlider.setPresetTransformer(SliderLayout.Transformer.Fade);

            for (int i = 0; i < folderList.length; i++) {
                final TextSliderView textSliderView = new TextSliderView(PlayerActivity.this);
                textSliderView.description("#".concat(hashTag)).
                        image(folderList[i]).setScaleType(DefaultSliderView.ScaleType.Fit);
                mSlider.addSlider(textSliderView);
            }
            mSlider.startAutoCycle();
        }
    }

    @Subscribe
    public void onEvent(EventIgCheckingComplete event) {
        List<InstagramItem> result = event.getIgPhotos();
        if (result.size() > 0) {
            String folder = Folders.SD_CARD.concat(File.separator).
                    concat(Folders.BASE_FOLDER).
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER);
            InstagramDownloader instagramDownloader = new InstagramDownloader(PlayerActivity.this, folder);
            instagramDownloader.download(result);
        } else {
            Log.w(TAG, "Can't load IG images. Urls is empty");
        }
    }

    private void loadfbGroupVideos() {
        if (NetMonitor.isNetworkAvailable(mApp)) {
            String fbGroupID = preferences.getString("fbGroupID", "");
            videoItemsTmp = new ArrayList<>();
            GraphRequest request = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + fbGroupID + "/videos",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.d("resp", response.toString());
                            try {
                                JsonObject o = new JsonParser().parse(response.getJSONObject().toString()).getAsJsonObject();
                                JsonArray array = o.get("data").getAsJsonArray();
                                if (array.size() > 0) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<FacebookVideoItem>>() {
                                    }.getType();


                                    videoItems = gson.fromJson(array.toString(), listType);

                                    String folderPath = Folders.BASE_FOLDER.
                                            concat(File.separator).
                                            concat(Folders.VIDEO_FOLDER);
                                    Log.d("download folder", folderPath);
                                    if (videoItems.size() > 0 && FacebookVideoDownloadHelper.getInstance().getCurrentState()
                                            != DownloadState.DOWNLOAD_IN_PROGRESS) {
                                        FacebookVideoDownloadHelper.getInstance().init(folderPath,
                                                videoItems,
                                                PlayerActivity.this);
                                        FacebookVideoDownloadHelper.getInstance().download();
                                    }


                                    Log.d("video", videoItems.toString());

                                    if (isStartVideo) {
//                                        playerView.setVideoURI(Uri.parse(videoItems.get(videoIndex).getSource()));
//                                        playerView.start();
//                                        isStartVideo = false;
                                    } else {
                                        videoItemsTmp = gson.fromJson(array.toString(), listType);

                                        if (!videoItems.equals(videoItemsTmp)) {
                                            videoItems.clear();
                                            videoItems.addAll(videoItemsTmp);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error with facebook GraphRequest: " + e.getCause());
                            }

                        }

                    }
            );
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,description,source");

            request.setParameters(parameters);
            request.executeAsync();
        } else {
            Toast.makeText(PlayerActivity.this,
                    "Can't update facebook videos", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFailConnection(Exception e) {

    }

    @Override
    public void onFailParsing(Exception e) {

    }

    @Override
    public void onFailFindLocation(Exception e) {

    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        boolean flagProceed = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            flagProceed = !isDestroyed();
        }
        if (readyShowWeather && flagProceed && NetMonitor.isNetworkAvailable(mApp)) {
            if (weatherInfo != null) {
                weatherLayout.removeAllViews();
                weatherLayout.setOrientation(LinearLayout.HORIZONTAL);
                weatherLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                weatherLayout.setBackgroundColor(getResources().getColor(R.color.rss_line));

                RelativeLayout.LayoutParams wlLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                wlLP.addRule(RelativeLayout.ABOVE, R.id.slider);
                wlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                wlLP.addRule(RelativeLayout.ALIGN_LEFT, R.id.slider);

                weatherLayout.setLayoutParams(wlLP);

                ImageView conditionImage = new ImageView(this);
                conditionImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                conditionImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                LinearLayout.LayoutParams ciLP = new LinearLayout.LayoutParams(40, 40);
                conditionImage.setLayoutParams(ciLP);

                TextView tempText = new TextView(this);
                tempText.setTextColor(Color.WHITE);
                tempText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                tempText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                String tempInC = "";
                if (mYahooWeather.getUnit() == 'c') {
                    tempInC = String.valueOf((weatherInfo.getCurrentTemp() - 32) * 5 / 9) + " " + "\u2103";
                } else {
                    tempInC = String.valueOf(weatherInfo.getCurrentTemp()) + " " + "\u2109";
                }

                tempText.setText(tempInC);

                weatherLayout.addView(conditionImage);
                weatherLayout.addView(tempText);


                Glide.with(this).load(Uri.parse(weatherInfo.getCurrentConditionIconURL())).into(conditionImage);
            } else {
                Toast.makeText(PlayerActivity.this, "Weather information is unavailable", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Weather information is unavailable. weatherInfo = null");
            }
        } else {
            Toast.makeText(PlayerActivity.this, "Weather information is unavailable. No inet.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Weather information is unavailable. No inet.");
        }
    }

    // Currencies
    @Subscribe
    public void onEvent(EventCurrency event) {
        CurrenciesList list = event.getToday();
        CurrenciesList yesterdayList = event.getYesterday();
        if (list.currencies.size() > 0 && yesterdayList.currencies.size() > 0 && list.currencies.size() == yesterdayList.currencies.size()) {
            String euro = "\u20ac" + " 0,0";
            String dollar = "\u0024" + " 0,0";
            String pound = "\u00a3" + " 0,0";
            String rub = "\u20bd" + " 0,0";
            String yena = "\u00a5" + " 0,0";    //or 04b0
            String yan = "\u5143" + " 0,0";

            DecimalFormat df = new DecimalFormat("0.0000");

            // всегда к доллару: евро, британский фунт, японская ена, китайский юань
            isEuroUp = true;
            isDollarUp = true;
            isPoundUp = true;
            isRubUp = true;
            isYenaUp = true;
            isYanUp = true;

            double yesterdayUSD = 0;
            double todayUSD = 0;

            if (list != null && yesterdayList != null && list.currencies.size() > 0 && yesterdayList.currencies.size() > 0 && list.currencies.size() == yesterdayList.currencies.size()) {
                for (int i = 0; i < list.currencies.size(); i++) {
                    if (list.currencies.get(i).getCurrCharCode().equals("USD")) {
                        String usdValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                        String usdNominal = list.currencies.get(i).getNominal();
                        double usdValueDouble = Double.valueOf(usdValue);
                        int usdNominalInt = Integer.parseInt(usdNominal);
                        todayUSD = usdValueDouble / usdNominalInt;
                        //convert to usd
                        //todayUSD = 1 / todayUSD;

                        usdValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                        usdNominal = yesterdayList.currencies.get(i).getNominal();
                        usdValueDouble = Double.valueOf(usdValue);
                        usdNominalInt = Integer.parseInt(usdNominal);
                        yesterdayUSD = usdValueDouble / usdNominalInt;
                        //convert to usd
                        //yesterdayUSD = 1 / yesterdayUSD;

                        double tVal = todayUSD / usdNominalInt;
                        double yVal = yesterdayUSD / usdNominalInt;
                        tVal = 1 / tVal;
                        yVal = 1 / yVal;

                        isRubUp = tVal > yVal;

                        //rub = "\u20bd" + " " + df.format(tVal);
                        rub = "Р " + df.format(tVal);
                        rub = "RUB " + df.format(tVal);
                    }
                }

                for (int i = 0; i < list.currencies.size(); i++) {
                    if (i < list.currencies.size()) {
                        if (list.currencies.get(i).getCurrCharCode().equals("GBP")) {
                            String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                            String nominal = list.currencies.get(i).getNominal();
                            double poundValueDouble = Double.valueOf(poundValue);
                            int nominalInt = Integer.parseInt(nominal);

                            String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                            String ynominal = yesterdayList.currencies.get(i).getNominal();
                            double ypoundValueDouble = Double.valueOf(ypoundValue);
                            int ynominalInt = Integer.parseInt(ynominal);

                            double tVal = poundValueDouble / nominalInt;
                            double yVal = ypoundValueDouble / ynominalInt;
                            tVal = tVal / todayUSD;
                            yVal = yVal / yesterdayUSD;

                            isPoundUp = tVal > yVal;

                            pound = "\u00a3" + " " + df.format(tVal);
                            pound = "GBP " + df.format(tVal);
                        } else if (list.currencies.get(i).getCurrCharCode().equals("CNY")) {
                            String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                            String nominal = list.currencies.get(i).getNominal();
                            double poundValueDouble = Double.valueOf(poundValue);
                            int nominalInt = Integer.parseInt(nominal);

                            String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                            String ynominal = yesterdayList.currencies.get(i).getNominal();
                            double ypoundValueDouble = Double.valueOf(ypoundValue);
                            int ynominalInt = Integer.parseInt(ynominal);

                            double tVal = poundValueDouble / nominalInt;
                            double yVal = ypoundValueDouble / ynominalInt;
                            tVal = tVal / todayUSD;
                            yVal = yVal / yesterdayUSD;

                            isYanUp = tVal >= yVal;

                            yan = "\u5143" + " " + df.format(tVal);
                            yan = "CNY " + df.format(tVal);
                        } else if (list.currencies.get(i).getCurrCharCode().equals("JPY")) {
                            String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                            String nominal = list.currencies.get(i).getNominal();
                            double poundValueDouble = Double.valueOf(poundValue);
                            int nominalInt = Integer.parseInt(nominal);

                            String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                            String ynominal = yesterdayList.currencies.get(i).getNominal();
                            double ypoundValueDouble = Double.valueOf(ypoundValue);
                            int ynominalInt = Integer.parseInt(ynominal);

                            double tVal = poundValueDouble / nominalInt;
                            double yVal = ypoundValueDouble / ynominalInt;
                            tVal = tVal / todayUSD;
                            yVal = yVal / yesterdayUSD;

                            isYenaUp = tVal >= yVal;

                            yena = "\u00a5" + " " + df.format(tVal);
                            yena = "JPY " + df.format(tVal);
                        } else if (list.currencies.get(i).getCurrCharCode().equals("EUR")) {

                            String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                            String nominal = list.currencies.get(i).getNominal();
                            double poundValueDouble = Double.valueOf(poundValue);
                            int nominalInt = Integer.parseInt(nominal);

                            String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                            String ynominal = yesterdayList.currencies.get(i).getNominal();
                            double ypoundValueDouble = Double.valueOf(ypoundValue);
                            int ynominalInt = Integer.parseInt(ynominal);

                            double tVal = poundValueDouble / nominalInt;
                            double yVal = ypoundValueDouble / ynominalInt;
                            tVal = tVal / todayUSD;
                            yVal = yVal / yesterdayUSD;

                            isEuroUp = tVal >= yVal;

                            euro = "\u20ac" + " " + df.format(tVal);
                            euro = "EUR " + df.format(tVal);
                        }
                    }
                    if (i == list.currencies.size() - 1) {
                        dashLayout.removeAllViews();
                        dashLayout.setOrientation(LinearLayout.VERTICAL);
                        dashLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        dashLayout.setBackgroundColor(getResources().getColor(R.color.rss_line));
                        RelativeLayout.LayoutParams dlLP = new RelativeLayout.
                                LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);

                        dlLP.addRule(RelativeLayout.ALIGN_LEFT, R.id.slider);
                        dlLP.addRule(RelativeLayout.BELOW, R.id.slider);
                        dlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


                        dashLayout.setLayoutParams(dlLP);

                        TextView eurText = new TextView(this);
                        eurText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView gbpText = new TextView(this);
                        gbpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView rubText = new TextView(this);
                        rubText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView cnyText = new TextView(this);
                        cnyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView jpnText = new TextView(this);
                        jpnText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                        eurText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        gbpText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        rubText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        cnyText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        jpnText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                        dashLayout.addView(eurText);
                        dashLayout.addView(gbpText);
                        dashLayout.addView(rubText);
                        dashLayout.addView(cnyText);
                        dashLayout.addView(jpnText);

                        if (isEuroUp) {
                            eurText.setTextColor(Color.GREEN);
                        } else {
                            eurText.setTextColor(Color.RED);
                        }
                        eurText.setText(euro);

                        if (isRubUp) {
                            rubText.setTextColor(Color.GREEN);
                        } else {
                            rubText.setTextColor(Color.RED);
                        }
                        rubText.setText(rub);

                        if (isPoundUp) {
                            gbpText.setTextColor(Color.GREEN);
                        } else {
                            gbpText.setTextColor(Color.RED);
                        }
                        gbpText.setText(pound);

                        if (isYanUp) {
                            cnyText.setTextColor(Color.GREEN);
                        } else {
                            cnyText.setTextColor(Color.RED);
                        }
                        cnyText.setText(yan);

                        if (isYenaUp) {
                            jpnText.setTextColor(Color.GREEN);
                        } else {
                            jpnText.setTextColor(Color.RED);
                        }
                        jpnText.setText(yena);
                    }
                }

            } else {
                Toast.makeText(this, "Currencies data unavailable", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Currencies data unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onEvent(EventGetLocComplete event) {
        String cityName = event.getCity();
        if (cityName != null) {
            if (cityName.equals("Not Found")) {
                Toast.makeText(this, "Can't define current location name", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "City: " + cityName);
                mYahooWeather.setNeedDownloadIcons(true);
                mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
                mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
                mYahooWeather.queryYahooWeatherByPlaceName(this, cityName, this);
            }
        } else {
            Toast.makeText(this, "Can't define current location name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allowflag = true;
//                    getLocation();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        EventBus.getDefault().register(this);
        if (playerView != null) {
            playerView.resume();  // <-- this will cause re-buffer.
        }
        //FacebookVideoDownloadHelper.getInstance().registerReceiverInFacebookVDHelper(PlayerActivity.this);

        facebookRefreshHandler.postDelayed(facebookRefreshRunnable, 1000);
        //loadfbGroupVideos();  //manual start facebook loading

        readyShowWeather = true;
        locationHandler.postDelayed(locationRunnable, TimeConsts.WEATHER_LOAD_DELAY);
        currHandler.postDelayed(currRunnable, TimeConsts.CURRENCIES_LOAD_DELAY);
        if (needShowInstagram) {
            instagramHandler.postDelayed(instagramRunnable, TimeConsts.INSTAGRAM_LOAD_DELAY);
            mSlider.setVisibility(View.VISIBLE);
            startSlides();
        } else {
            mSlider.setVisibility(View.GONE);
        }
        if (needShowTwitter)
            twitterFeedHandler.postDelayed(twitterFeedRunnable, TimeConsts.TWITTER_LOAD_DELAY);

        super.onResume();
    }


    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        if (playerView != null) {
            playerView.suspend();
        }
        //FacebookVideoDownloadHelper.getInstance().unRegisterReceiverInFacebookVDHelper(PlayerActivity.this);

        if (facebookRefreshRunnable != null) {
            facebookRefreshHandler.removeCallbacks(facebookRefreshRunnable);
        }
        readyShowWeather = false;
        if (locationRunnable != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
        if (currRunnable != null) {
            currHandler.removeCallbacks(currRunnable);
        }
        if (instagramRunnable != null) {
            instagramHandler.removeCallbacks(instagramRunnable);
        }
        if (twitterFeedRunnable != null) {
            twitterFeedHandler.removeCallbacks(twitterFeedRunnable);
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("unTag_FullscreenAct", "Remove messages from handlerHideUI in onStop()");
        trimCache(this);
        if (handlerHideUI != null) {
            handlerHideUI.removeMessages(32);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHandler != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
    }

    public void getCurrencies() {
        if (NetMonitor.isNetworkAvailable(mApp)) {
            GetCurrencies getCurrencies = new GetCurrencies();
            getCurrencies.execute(new Date());
        }
    }

    public void getGroupIconURL() {
        String fbGroupID = preferences.getString("fbGroupID", "");
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/".concat(fbGroupID),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            JSONObject resp = null;
                            try {
                                resp = response.getJSONObject();
                                Log.d("icon url", resp.toString());
                                Glide.with(PlayerActivity.this).load(Uri.parse(resp.getJSONObject("picture").getJSONObject("data").getString("url"))).override(90, 90).placeholder(R.drawable.facebook).error(R.drawable.facebook)
                                        .into(groupIconImageView);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Glide.with(PlayerActivity.this).load(R.drawable.facebook).override(90, 90).placeholder(R.drawable.facebook).error(R.drawable.facebook)
                                        .into(groupIconImageView);
                            }

//                            try {
//                                Glide.with(PlayerActivity.this).load(Uri.parse(resp.getJSONObject("picture").getJSONObject("data").getString("url"))).override(90, 90).placeholder(R.drawable.facebook).error(R.drawable.facebook)
//                                        .into(groupIconImageView);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PlayerActivity.this, "Can't get group icon", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
        );

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture");

        request.setParameters(parameters);
        request.executeAsync();
    }


    public void nextFile() {
        String path = Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.VIDEO_FOLDER);
        File[] folderList = new File(path).listFiles();

        File videoDir = new File(path);
        boolean dirOK = true;
        if (!videoDir.exists()) {
            dirOK = videoDir.mkdir();
        }

        if (dirOK) {
            Log.d("files", Arrays.toString(folderList));
            List<File> mp4Files = FileSystemHelper.getFileListByExts(videoDir, VIDEOS_EXTS);
            Collections.sort(mp4Files);
            Collections.reverse(mp4Files);
            Log.d(TAG, "mp4 files in video folder" + mp4Files.toString());

//        for (File file : folderList) {
//            if ("mp4".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
//                mp4Files.add(file);
//            }
//        }

            for (int i = 0; i < mp4Files.size(); i++) {
                if (mp4Files.get(i).getName().equals(nextVideo)) {
                    videoIndex = i;
                }
            }

            if (mp4Files.size() > 0) {
                Log.d(TAG, "Play file " + videoIndex + " name " + mp4Files.get(videoIndex).getName());

                if (videoIndex < mp4Files.size()) {
                    playerView.setVideoURI(Uri.parse(mp4Files.get(videoIndex).getAbsolutePath()));
                    playerView.start();

                    videoIndex++;
                    if (videoIndex < mp4Files.size()) {
                        nextVideo = mp4Files.get(videoIndex).getName();
                    } else {
                        nextVideo = mp4Files.get(0).getName();
                    }

                    easyTracker.send(MapBuilder.createEvent("playback",
                            "video_playback", "go to next video", null).build());
                } else {
                    videoIndex = 0;
                    playerView.setVideoURI(Uri.parse(mp4Files.get(videoIndex).getAbsolutePath()));
                    playerView.start();

                    if (mp4Files.size() > videoIndex + 1) {
                        nextVideo = mp4Files.get(videoIndex + 1).getName();
                    }

                    easyTracker.send(MapBuilder.createEvent("playback",
                            "video_playback", "go to next video", null).build());
                }


            } else {
                playerView.setVideoURI(embUri);
                playerView.start();
                easyTracker.send(MapBuilder.createEvent("playback",
                        "video_playback", "go to next video", null).build());
            }
        } else {
            Toast.makeText(this, "Problem with filesystem on SD. Can't create video dir.", Toast.LENGTH_SHORT).show();
        }
    }

    private static class mHandler extends Handler {
        //need check this! may be memory leak
        PlayerActivity wrActivity;

        public mHandler(PlayerActivity activity) {
            wrActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            wrActivity.setUISmall();
        }
    }

    private void setUISmall() {
        //not need check SDK version because checking in onCreate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Log.d(TAG, "UiVisibility before " + decorView.getSystemUiVisibility());
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            Log.d(TAG, "UiVisibility after " + decorView.getSystemUiVisibility());
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                Log.d(TAG, "Start deleting cache");
                deleteDir(dir);
            }
        } catch (Exception e) {
            Log.d(TAG, "Can't delete cache");
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
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}