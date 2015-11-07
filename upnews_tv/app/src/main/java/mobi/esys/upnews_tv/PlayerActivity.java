package mobi.esys.upnews_tv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import mobi.esys.upnews_tv.cbr.CurrenciesList;
import mobi.esys.upnews_tv.cbr.GetCurrencies;
import mobi.esys.upnews_tv.constants.DevelopersKeys;
import mobi.esys.upnews_tv.constants.Folders;
import mobi.esys.upnews_tv.constants.TimeConsts;
import mobi.esys.upnews_tv.download.DownloadState;
import mobi.esys.upnews_tv.download.FacebookVideoDownloadHelper;
import mobi.esys.upnews_tv.facebook.FacebookVideoItem;
import mobi.esys.upnews_tv.filesystem.FileSystemHelper;
import mobi.esys.upnews_tv.instagram.GetIGPhotosTask;
import mobi.esys.upnews_tv.instagram.InstagramDownloader;
import mobi.esys.upnews_tv.instagram.InstagramItem;
import mobi.esys.upnews_tv.net.NetMonitor;
import mobi.esys.upnews_tv.twitter.TwitterHelper;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherExceptionListener;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

public class PlayerActivity extends Activity implements LocationListener, YahooWeatherInfoListener,
        YahooWeatherExceptionListener {
    private transient RelativeLayout relativeLayout;
    private transient Instagram instagram;
    private transient SliderLayout mSlider;
    private transient List<InstagramItem> igPhotos;
    private transient List<FacebookVideoItem> videoItemsTmp;
    private static final String URL = "url";
    private transient List<FacebookVideoItem> videoItems;
    private transient int videoIndex = 0;

    public static final String[] VIDEOS_EXTS={"avi","mp4"};

    private transient VideoView playerView;

    private transient Location location;

    private transient ProgressDialog dialog;


    private YahooWeather mYahooWeather = YahooWeather.getInstance(
            TimeConsts.WEATHER_REQUEST_TIMEOUT,
            TimeConsts.WEATHER_REQUEST_TIMEOUT, true);

    boolean isEuroUp = true;
    boolean isDollarUp = true;
    boolean isPoundUp = true;
    boolean isYenaUp = true;


    private transient LinearLayout dashLayout;
    private transient LinearLayout weatherLayout;

    private transient boolean isFirst = true;

    private transient SharedPreferences preferences;
    private boolean isStartVideo = true;

    private transient ImageView groupIconImageView;
    private transient EasyTracker easyTracker;

    private transient Uri embUri = Uri.parse("android.resource://mobi.esys.upnews_online/raw/emb");
    private transient Handler locationHandler;
    private transient Runnable locationRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        getWindow().setFormat(PixelFormat.TRANSPARENT);

        setContentView(R.layout.activity_main);

        easyTracker = EasyTracker.getInstance(PlayerActivity.this);

//        IjkMediaPlayer.loadLibrariesOnce(null);
//        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        UpnewsOnlineApp app = (UpnewsOnlineApp) getApplicationContext();
        app.setCurrentActivityInstance(this);

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


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


        igPhotos = new ArrayList<>();
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


        locationHandler = new Handler();
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                getLocation();
                locationHandler.postDelayed(this, TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };
        locationHandler.postDelayed(locationRunnable, TimeConsts.WEATHER_LOAD_DELAY);


        loadfbGroupVideos();

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
                String path = Folders.SD_CARD.
                        concat(File.separator).
                        concat(Folders.BASE_FOLDER).
                        concat(File.separator).concat(Folders.VIDEO_FOLDER);

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
        Log.d("files", Arrays.toString(folderList));
        List<File> mp4Files = new ArrayList<>();

        for (File file : folderList) {
            if ("mp4".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                mp4Files.add(file);
            }
        }

        if (mp4Files.size() > 0) {
            playerView.setVideoURI(Uri.parse(mp4Files.get(videoIndex).getAbsolutePath()));
        } else {
            playerView.setVideoURI(embUri);
        }


        playerView.start();

        playerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextFile();
            }
        });


        mSlider = (SliderLayout) findViewById(R.id.slider);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(DevelopersKeys.TWITTER_KEY, DevelopersKeys.TWITTER_SECRET);
        Fabric.with(PlayerActivity.this, new Twitter(authConfig));
        final Handler twitterFeedHandler = new Handler();
        Runnable twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
                    Twitter.getInstance();
                    TwitterHelper.startLoadTweets(Twitter.getApiClient(), relativeLayout, PlayerActivity.this, isFirst);
                    isFirst = false;
                    twitterFeedHandler.postDelayed(this,
                            TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
                }
                else{
                    Toast.makeText(PlayerActivity.this,"Twitter is unavailable",Toast.LENGTH_SHORT).show();
                }
            }
        };

        twitterFeedHandler.postDelayed(twitterFeedRunnable, TimeConsts.TWITTER_LOAD_DELAY);

        final Handler instagramHandler = new Handler();
        Runnable instagramRunnable = new Runnable() {
            @Override
            public void run() {
                updateIGPhotos();
                instagramHandler.postDelayed(this, TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
            }
        };
        instagramHandler.postDelayed(instagramRunnable, TimeConsts.INSTAGRAM_LOAD_DELAY);


        final Handler currHandler = new Handler();
        Runnable currRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrencies();
                currHandler.postDelayed(this,
                        TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };
        currHandler.postDelayed(currRunnable, TimeConsts.CURRENCIES_LOAD_DELAY);


    }

    public void loadSlide(String tag) {
        String userName = instagram.getSession().getUser().username;
        mSlider.stopAutoCycle();
        mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);

        mSlider.setPresetTransformer(SliderLayout.Transformer.Fade);


        String path = Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.PHOTO_FOLDER);
        File[] folderList = new File(path).listFiles();

        if (folderList.length > 0) {
            for (int i = 0; i < folderList.length; i++) {

                final TextSliderView textSliderView = new TextSliderView(PlayerActivity.this);


                textSliderView.description("#".concat(tag)).
                        image(folderList[i]).setScaleType(DefaultSliderView.ScaleType.Fit);

                mSlider.addSlider(textSliderView);

                if (i == folderList.length - 1) {
                    mSlider.setDuration(TimeConsts.SLIDER_DURATION);
                    mSlider.startAutoCycle();
                }
            }


            mSlider.startAutoCycle();
        } else {
            Toast.makeText(this, "Instagram photos load fail", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateIGPhotos() {
        if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
            String tag = preferences.getString("instHashTag", "");
            final GetIGPhotosTask getTagPhotoIGTask = new GetIGPhotosTask(tag);
            getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());

            try {
                JSONObject igObject = new JSONObject(getTagPhotoIGTask.get());
                Log.d("object", igObject.toString());
                getIGPhotos(igObject);
            } catch (JSONException e) {
                Log.d("error", "json error");
            } catch (InterruptedException e) {
                Log.d("error", "interrupted error");
            } catch (ExecutionException e) {
                Log.d("error", "execution error");
            }
            String folder = Folders.SD_CARD.concat(File.separator).
                    concat(Folders.BASE_FOLDER).
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER);
            Log.d("photos", igPhotos.toString());

            InstagramDownloader instagramDownloader = new InstagramDownloader(PlayerActivity.this, folder, tag);
            instagramDownloader.download(igPhotos);
        }
        else{
            Toast.makeText(PlayerActivity.this,"Can't update instagram photos",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //loadSlide();


    private void getIGPhotos(JSONObject igObject) {
        if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
            igPhotos = new ArrayList<>();
            try {
                Log.d("object main", igObject.toString());
                final JSONArray igData = igObject.getJSONArray("data");

                for (int i = 0; i < igData.length(); i++) {

                    final JSONObject currObj = igData.getJSONObject(i)
                            .getJSONObject("images");
                    Log.d("images main", currObj.toString());
                    final String origURL = currObj.getJSONObject("standard_resolution")
                            .getString(URL);
                    Log.d("images url main", origURL);
                    Log.d("data", igData.toString());
                    String fsComm;
                    if (igData.getJSONObject(i).getJSONObject("comments").getInt("count") > 0) {
                        fsComm = igData.getJSONObject(i).getJSONObject("comments")
                                .getJSONArray("data").getJSONObject(0).getString("text");
                    } else {
                        fsComm = "";
                    }


                    igPhotos.add(new InstagramItem(igData.getJSONObject(i)
                            .getString("id"), currObj.getJSONObject("standard_resolution")
                            .getString(URL), origURL, fsComm, igData.getJSONObject(i).getJSONObject("user").getString("username")));


                }


            } catch (JSONException e) {
                Log.d("json", "json_error: ".concat(e.getMessage()));
            }
        }
        else{
           Toast.makeText(PlayerActivity.this,"Can't get photos from Instagram",Toast.LENGTH_SHORT).show();
        }
    }


    private void loadfbGroupVideos() {
        if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
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
//                                playerView.setVideoURI(Uri.parse(videoItems.get(videoIndex).getSource()));
//                                playerView.start();
//                                isStartVideo = false;
                                } else {
                                    videoItemsTmp = gson.fromJson(array.toString(), listType);

                                    if (!videoItems.equals(videoItemsTmp)) {
                                        videoItems.clear();
                                        videoItems.addAll(videoItemsTmp);
                                    }
                                }


                            }

                        }

                    }
            );
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,description,source");

            request.setParameters(parameters);
            request.executeAsync();
        }
        else{
            Toast.makeText(PlayerActivity.this,
                    "Can't update facebook videos",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d("location", String.valueOf(location.getLatitude()) + ":" + String.valueOf(location.getLongitude()));
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

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
        if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
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
                tempText.setText(String.valueOf(weatherInfo.getCurrentTemp()) + " " + "\u2103");

                weatherLayout.addView(conditionImage);
                weatherLayout.addView(tempText);


                Glide.with(this).load(Uri.parse(weatherInfo.getCurrentConditionIconURL())).into(conditionImage);
            } else {
                Toast.makeText(PlayerActivity.this, "Weather information is unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(PlayerActivity.this, "Weather information is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadCurrencyDashboard(CurrenciesList list, CurrenciesList yesterdayList) {
        if(NetMonitor.isNetworkAvailable((UpnewsOnlineApp)getApplication())) {
            String euro = "\u20ac" + " 0,0";
            String dollar = "\u0024" + " 0,0";
            String pound = "\u00a3" + " 0,0";
            String yena = "\u00a5" + " 0,0";

            DecimalFormat df = new DecimalFormat("#.0000");


            isEuroUp = true;
            isDollarUp = true;
            isPoundUp = true;
            isYenaUp = true;

            if (list != null && yesterdayList != null && list.currencies.size() > 0 && yesterdayList.currencies.size() > 0 && list.currencies.size() == yesterdayList.currencies.size()) {

                for (int i = 0; i < list.currencies.size(); i++) {
                    if (i < list.currencies.size() - 1) {
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

                            isPoundUp = tVal > yVal;


                            pound = "\u00a3" + " " + df.format(tVal);
                        } else if (list.currencies.get(i).getCurrCharCode().equals("USD")) {
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

                            isDollarUp = tVal >= yVal;

                            dollar = "\u0024" + " " + df.format(tVal);
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

                            isYenaUp = tVal >= yVal;

                            yena = "\u04b0" + " " + df.format(tVal);
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

                            isEuroUp = tVal >= yVal;

                            euro = "\u20ac" + " " + df.format(tVal);
                        }
                    } else {
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

                        TextView usdText = new TextView(this);
                        usdText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView eurText = new TextView(this);
                        eurText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView gbpText = new TextView(this);
                        gbpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        TextView cnyText = new TextView(this);
                        cnyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                        usdText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        eurText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        gbpText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        cnyText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                        dashLayout.addView(usdText);
                        dashLayout.addView(eurText);
                        dashLayout.addView(gbpText);
                        dashLayout.addView(cnyText);

                        if (isDollarUp) {
                            usdText.setTextColor(Color.GREEN);
                        } else {
                            usdText.setTextColor(Color.RED);
                        }
                        usdText.setText(dollar);

                        if (isEuroUp) {
                            eurText.setTextColor(Color.GREEN);
                        } else {
                            eurText.setTextColor(Color.RED);
                        }
                        eurText.setText(euro);

                        if (isPoundUp) {
                            gbpText.setTextColor(Color.GREEN);
                        } else {
                            gbpText.setTextColor(Color.RED);
                        }
                        gbpText.setText(pound);

                        if (isYenaUp) {
                            cnyText.setTextColor(Color.GREEN);
                        } else {
                            cnyText.setTextColor(Color.RED);
                        }
                        cnyText.setText(yena);


                    }
                }


            } else {
                Toast.makeText(this, "Currencies data unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Currencies data unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public Location getLocation() {
        try {
            LocationManager loc = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = loc
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = loc
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this,"Please turn GPS or network for location searching",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (isNetworkEnabled) {
                    loc.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0, this);
                    Log.d("Network", "Network Enabled");
                    if (loc != null) {
                        location = loc
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        loc.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0,
                                0, this);
                        Log.d("GPS", "GPS Enabled");
                        if (loc != null) {
                            location = loc
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(location!=null) {
            getLocationName(location);
        }
        else{
            Toast.makeText(this,"Can't define location",Toast.LENGTH_SHORT).show();
        }
        return location;
    }


    public String getLocationName(Location location) {

        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.ENGLISH);
        try {

            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(),
                    10);
            cityName = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(cityName!=null) {
            if (cityName.equals("Not Found")) {
                Toast.makeText(this, "Can't define current location name", Toast.LENGTH_SHORT).show();
            }

            else{
                mYahooWeather.setNeedDownloadIcons(true);
                mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
                mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
                mYahooWeather.queryYahooWeatherByPlaceName(this, cityName, this);
            }
        }
        else{
            Toast.makeText(this, "Can't define current location name", Toast.LENGTH_SHORT).show();
        }

        return cityName;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationHandler!=null){
            locationHandler.removeCallbacks(locationRunnable);
        }
//        if (proxyCache != null) {
//            proxyCache.shutdown();
//        }
//        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    protected void onResume() {
        if (playerView != null) {
            playerView.resume();  // <-- this will cause re-buffer.
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (playerView != null) {
            playerView.suspend();
        }
        super.onPause();
    }

    public void getCurrencies() {
        GetCurrencies getCurrencies = new GetCurrencies(this);
        getCurrencies.execute(new Date());
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
                            JSONObject resp = response.getJSONObject();
                            Log.d("icon url", resp.toString());


                            try {
                                Glide.with(PlayerActivity.this).load(Uri.parse(resp.getJSONObject("picture").getJSONObject("data").getString("url"))).override(90, 90).placeholder(R.drawable.facebook).error(R.drawable.facebook)
                                        .into(groupIconImageView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

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

        File videoDir=new File(path);
        Log.d("files", Arrays.toString(folderList));
        List<File> mp4Files = FileSystemHelper.getFileListByExts(videoDir,VIDEOS_EXTS);

//        for (File file : folderList) {
//            if ("mp4".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
//                mp4Files.add(file);
//            }
//        }

        Log.d("mp4", mp4Files.toString());
        if (mp4Files.size() > 0) {

            if (videoIndex < mp4Files.size()) {
                playerView.setVideoURI(Uri.parse(mp4Files.get(videoIndex).getAbsolutePath()));
                playerView.start();

                videoIndex++;

                easyTracker.send(MapBuilder.createEvent("playback",
                        "video_playback", "go to next video", null).build());
            } else {
                videoIndex = 0;
                playerView.setVideoURI(Uri.parse(mp4Files.get(videoIndex).getAbsolutePath()));
                playerView.start();

                easyTracker.send(MapBuilder.createEvent("playback",
                        "video_playback", "go to next video", null).build());
            }


        } else {
            playerView.setVideoURI(embUri);
            playerView.start();
            easyTracker.send(MapBuilder.createEvent("playback",
                    "video_playback", "go to next video", null).build());
        }

    }


}


