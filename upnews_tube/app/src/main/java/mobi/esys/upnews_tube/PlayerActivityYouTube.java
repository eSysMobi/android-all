package mobi.esys.upnews_tube;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import mobi.esys.upnews_tube.cbr.CurrenciesList;
import mobi.esys.upnews_tube.cbr.GetCurrencies;
import mobi.esys.upnews_tube.constants.DevelopersKeys;
import mobi.esys.upnews_tube.constants.Folders;
import mobi.esys.upnews_tube.constants.OtherConst;
import mobi.esys.upnews_tube.constants.TimeConsts;
import mobi.esys.upnews_tube.instagram.CheckInstaTagTask;
import mobi.esys.upnews_tube.instagram.InstagramDownloader;
import mobi.esys.upnews_tube.net.NetMonitor;
import mobi.esys.upnews_tube.twitter.TwitterHelper;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherExceptionListener;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;


public class PlayerActivityYouTube extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener, LocationListener, YahooWeatherInfoListener,
        YahooWeatherExceptionListener {

    static private final String DEVELOPER_KEY = DevelopersKeys.YOUTUBE_ANDROID_KEY; // AIzaSyAzDPv_OSQp73qI7VLPWmzkBEEHk0Thq2E
    //static private final String playlistID = "4SK0cUNMnMM";
    private String playlistID = "";

    private YouTubePlayerView youTubeView;
    private YouTubePlayer YPlayer;

    private static final String URL = "url";

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private transient LinearLayout clockLayout;
    private transient SliderLayout mSlider;
    private transient Location location;
    private transient LinearLayout dashLayout;
    private transient LinearLayout weatherLayout;
    private transient RelativeLayout rlForTwitter;

    private YahooWeather mYahooWeather = YahooWeather.getInstance(
            TimeConsts.WEATHER_REQUEST_TIMEOUT,
            TimeConsts.WEATHER_REQUEST_TIMEOUT, true);

    private transient Handler locationHandler;
    private transient Runnable locationRunnable;
    private transient Handler instagramHandler;
    private transient Runnable instagramRunnable;
    private transient Handler currHandler;
    private transient Runnable currRunnable;
    private transient Handler twitterFeedHandler;
    private transient Runnable twitterFeedRunnable;
    private transient Handler youtubeContinueHandler;
    private transient Runnable youtubeContinueRunnable;

    private final static int PERMISSION_REQUEST_CODE = 334;
    private transient boolean allowflag = false;

    private transient Handler handlerHideUI;
    private transient View decorView = null;
    private static transient int DELAY_NAV_HIDE = 2000;

    private final static String TAG = "unTag_PlayerActYT";

    boolean isEuroUp = true;
    boolean isDollarUp = true;
    boolean isPoundUp = true;
    boolean isRubUp = true;
    boolean isYenaUp = true;
    boolean isYanUp = true;

    private transient boolean isFirst = true;
    private transient boolean youtubePlaylist = true;
    private transient SharedPreferences preferences;
    private transient UpnewsTubeApp mApp;

    private transient boolean needIsnstagram;
    private transient boolean skip_twitter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_player_activity_you_tube);

        preferences = getSharedPreferences(OtherConst.APP_PREF, MODE_PRIVATE);
        needIsnstagram = preferences.getBoolean("instNeedShow", false);
        skip_twitter = preferences.getBoolean(OtherConst.APP_PREF_SKIP_TWITTER, true);
        playlistID = preferences.getString(OtherConst.APP_PREF_PLAYLIST, "");

        clockLayout = (LinearLayout) findViewById(R.id.clockLayout);
        weatherLayout = (LinearLayout) findViewById(R.id.weatherLayout);
        dashLayout = (LinearLayout) findViewById(R.id.dashLayout);

        mApp = (UpnewsTubeApp) getApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextClock digitalClock = new TextClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams dgLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(0, 0, 0, 0);
            dgLP.gravity = Gravity.CENTER_VERTICAL;
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            digitalClock.setId(R.id.clock);
            digitalClock.setFormat24Hour("k:mm");
            clockLayout.addView(digitalClock);
        } else {
            DigitalClock digitalClock = new DigitalClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams dgLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(0, 0, 0, 0);
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            digitalClock.setGravity(Gravity.CENTER_VERTICAL);
            dgLP.gravity = Gravity.CENTER_VERTICAL;
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setId(R.id.clock);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            clockLayout.addView(digitalClock);
        }

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mYahooWeather.setExceptionListener(this);

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
                getLocation();
                locationHandler.postDelayed(this, TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };
        //locationHandler.postDelayed(locationRunnable, TimeConsts.WEATHER_LOAD_DELAY);   //this is in onResume

        mSlider = (SliderLayout) findViewById(R.id.slider);


        rlForTwitter = (RelativeLayout) findViewById(R.id.rlForTwitter);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(DevelopersKeys.TWITTER_KEY, DevelopersKeys.TWITTER_SECRET);
        Fabric.with(PlayerActivityYouTube.this, new Twitter(authConfig));
        twitterFeedHandler = new Handler();
        twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                if (NetMonitor.isNetworkAvailable((UpnewsTubeApp) getApplication())) {
                    Twitter.getInstance();
                    TwitterHelper.startLoadTweets(Twitter.getApiClient(), rlForTwitter, PlayerActivityYouTube.this, isFirst);
                    isFirst = false;
                    twitterFeedHandler.postDelayed(this,
                            TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
                } else {
                    Toast.makeText(PlayerActivityYouTube.this, "Twitter is unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //twitterFeedHandler.postDelayed(twitterFeedRunnable, TimeConsts.TWITTER_LOAD_DELAY);   //this is in onResume

        instagramHandler = new Handler();
        instagramRunnable = new Runnable() {
            @Override
            public void run() {
                updateIGPhotos();
                instagramHandler.postDelayed(this, TimeConsts.TWITTER_AND_INSTAGRAM_REFRESH_INTERVAL);
            }
        };
        //instagramHandler.postDelayed(instagramRunnable, TimeConsts.INSTAGRAM_LOAD_DELAY);   //this is in onResume


        currHandler = new Handler();
        currRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrencies();
                currHandler.postDelayed(this,
                        TimeConsts.WEATHER_AND_CURR_REFRESH_INTERVAL);
            }
        };
        //currHandler.postDelayed(currRunnable, TimeConsts.CURRENCIES_LOAD_DELAY);  //this is in onResume

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

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(DEVELOPER_KEY, this);

        youtubeContinueHandler = new Handler();
        youtubeContinueRunnable = new Runnable() {
            @Override
            public void run() {
                checkPlaying();
                youtubeContinueHandler.postDelayed(this, TimeConsts.PLAYING_CHECK);
            }
        };
    }

    void checkPlaying() {
        try {
            if (youTubeView != null && YPlayer != null && !YPlayer.isPlaying()) {
                Log.d(TAG, "Start play again");
                YPlayer.play();
            }
        } catch (IllegalStateException e) {
            Log.d(TAG, "Error IllegalStateException. Can't start play again! " + e.getMessage());
        }
    }

    public void loadSlide(String tag) {
        mApp.setInstagramFiles("");
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

                final TextSliderView textSliderView = new TextSliderView(PlayerActivityYouTube.this);


                textSliderView.description("#".concat(tag)).
                        image(folderList[i]).setScaleType(DefaultSliderView.ScaleType.Fit);

                mSlider.addSlider(textSliderView);

                if (i == folderList.length - 1) {
                    mSlider.setDuration(TimeConsts.SLIDER_DURATION);
                    //mSlider.startAutoCycle();
                }
            }

            Log.w(TAG, "Start autocycle " + folderList.length + " photos");
            mSlider.startAutoCycle();
        } else {
            Toast.makeText(this, "Instagram photos load fail", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateIGPhotos() {
        if (NetMonitor.isNetworkAvailable(mApp)) {
            String tag = preferences.getString("instHashTag", "");

            String folder = Folders.SD_CARD.concat(File.separator).
                    concat(Folders.BASE_FOLDER).
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER);

            String urls = mApp.getInstagramFiles();
            if (urls.isEmpty()) {
                CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(tag, mApp);
                checkInstaTagTask.execute();
                try {
                    urls = checkInstaTagTask.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Log.w(TAG, "Loaded urls by tag #" + tag + " : " + urls);
            if (!urls.isEmpty()) {

                InstagramDownloader instagramDownloader = new InstagramDownloader(PlayerActivityYouTube.this, folder, tag);
                instagramDownloader.download(urls);
            }
        } else {
            Toast.makeText(PlayerActivityYouTube.this, "Can't update instagram photos. No Internet connection.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //loadSlide();

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
        if (NetMonitor.isNetworkAvailable((UpnewsTubeApp) getApplication())) {
            if (weatherInfo != null) {
                weatherLayout.removeAllViews();
                weatherLayout.setOrientation(LinearLayout.HORIZONTAL);
                weatherLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                weatherLayout.setBackgroundColor(getResources().getColor(R.color.rss_line));

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
                Toast.makeText(PlayerActivityYouTube.this, "Weather information is unavailable", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Weather information is unavailable. weatherInfo = null");
            }
        } else {
            Toast.makeText(PlayerActivityYouTube.this, "Weather information is unavailable. No inet.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Weather information is unavailable. No inet.");
        }
    }

    public void getCurrencies() {
        if (NetMonitor.isNetworkAvailable((UpnewsTubeApp) getApplication())) {
            GetCurrencies getCurrencies = new GetCurrencies(this);
            getCurrencies.execute(new Date());
        }
    }

    public void loadCurrencyDashboard(CurrenciesList list, CurrenciesList yesterdayList) {
        if (NetMonitor.isNetworkAvailable((UpnewsTubeApp) getApplication())) {
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

    public Location getLocation() {
        try {
            LocationManager loc = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = loc
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = loc
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "Please turn GPS or network for location searching",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (allowflag) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (location != null) {
            getLocationName(location);
        } else {
            Toast.makeText(this, "Can't define location", Toast.LENGTH_SHORT).show();
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
        return cityName;
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

    //YOUTUBE
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    "There was an error initializing the YouTubePlayer",
                    errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onInitializationSuccess(Provider provider, final YouTubePlayer player, boolean wasRestored) {
        YPlayer = player;
        if (!wasRestored) {
            //YPlayer.setPlayerStateChangeListener(playerStateChangeListener);
            YPlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                @Override
                public void onPlaying() {

                }

                @Override
                public void onPaused() {
                }

                @Override
                public void onStopped() {
                    if (!youtubePlaylist) {
                        Log.d(TAG, "Playlist flag reset");
                        youtubePlaylist = true;
                    }
                }

                @Override
                public void onBuffering(boolean b) {
                }

                @Override
                public void onSeekTo(int i) {
                }
            });
            YPlayer.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                @Override
                public void onPrevious() {
                }

                @Override
                public void onNext() {
                }

                @Override
                public void onPlaylistEnded() {
                    if (youtubePlaylist) {
                        youtubePlaylist = false;
                        Log.d(TAG, "Playlist is ended! Try start playlist again " + playlistID);
                        YPlayer.loadPlaylist(playlistID, 0, 0);
                    }
                }
            });
            YPlayer.setShowFullscreenButton(false);

            Log.d(TAG, "Play playlist =" + playlistID);
            YPlayer.loadPlaylist(playlistID);
        }
    }

    private static class mHandler extends Handler {
        //need check this! may be memory leak

//        WeakReference<FullscreenActivity> wrActivity;
//
//        public mHandler(FullscreenActivity activity) {
//            wrActivity = new WeakReference<FullscreenActivity>(activity);
//        }

        PlayerActivityYouTube wrActivity;

        public mHandler(PlayerActivityYouTube activity) {
            wrActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            FullscreenActivity activity = wrActivity.get();
//            if (activity != null)
//                activity.setUISmall();
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

    @Override
    protected void onResume() {
        super.onResume();
        currHandler.postDelayed(currRunnable, TimeConsts.CURRENCIES_LOAD_DELAY);
        locationHandler.postDelayed(locationRunnable, TimeConsts.WEATHER_LOAD_DELAY);
        if (needIsnstagram) {
            instagramHandler.postDelayed(instagramRunnable, TimeConsts.INSTAGRAM_LOAD_DELAY);
        } else {
            mSlider.setVisibility(View.GONE);
        }
        if (!skip_twitter) {
            rlForTwitter.setVisibility(View.VISIBLE);
            twitterFeedHandler.postDelayed(twitterFeedRunnable, TimeConsts.TWITTER_LOAD_DELAY);
        } else {
            rlForTwitter.setVisibility(View.GONE);
        }
        youtubeContinueHandler.postDelayed(youtubeContinueRunnable, TimeConsts.AUTOSTART);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Remove all handlers in onStop()");
        if (handlerHideUI != null) {
            handlerHideUI.removeMessages(32);
        }
        if (locationHandler != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
        if (instagramHandler != null) {
            instagramHandler.removeCallbacks(instagramRunnable);
        }
        if (currHandler != null) {
            currHandler.removeCallbacks(currRunnable);
        }
        if (twitterFeedHandler != null) {
            twitterFeedHandler.removeCallbacks(twitterFeedRunnable);
        }
        if (twitterFeedHandler != null) {
            twitterFeedHandler.removeCallbacks(twitterFeedRunnable);
        }
        if (YPlayer != null) {
            YPlayer.pause();
        }
        //instagramHandler.postDelayed(instagramRunnable, TimeConsts.INSTAGRAM_LOAD_DELAY);
        //currHandler.postDelayed(currRunnable, TimeConsts.CURRENCIES_LOAD_DELAY);
        // locationHandler.postDelayed(locationRunnable, TimeConsts.WEATHER_LOAD_DELAY);
    }

    @Override
    protected void onStop() {
        trimCache(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //do not return result in destroyed activity
        mYahooWeather.noNeedResult();
        if (YPlayer != null) {
            YPlayer.release();
            YPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                Log.d(TAG, "Start deleting cache");
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
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
