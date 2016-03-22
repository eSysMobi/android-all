package mobi.esys.constants;

public final class UNLConsts {

    //fileworks constants
    public static final String TEMP_FILE_EXT = "tmp";
    public static final String VIDEO_DIR_NAME = "/upnewslite/";
    public static final String GD_VIDEO_DIR_NAME = "upnewslite";
    public static final String LOGO_DIR_NAME = "/logo/";
    public static final String GD_LOGO_DIR_NAME = "logo";
    public static final String STORAGE_DIR_NAME = "/video/";
    public static final String GD_STORAGE_DIR_NAME = "video";
    public static final String APP_PREF = "UNLPref";
    public static final String GD_RSS_FILE_TITLE = "rss.txt";
    public static final String GD_RSS_FILE_MIME_TYPE = "text/plain";
    public static final String GD_LOGO_FILE_TITLE = "upnews_logo_w2.png";
    public static final String GD_LOGO_FILE_MIME_TYPE = "image/png";
    public static final String GD_FOLDER_QUERY = "'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed=false";
    public static final String RSS_DIR_NAME = "/rss/";
    public static final String GD_RSS_DIR_NAME = "rss";
    public static final String RSS_FILE_NAME = "rss.txt";
    public static final String STATISTICS_TEMP_PHOTO_FILE_NAME = "tmp.jpg";
    public static final String STATISTICS_DIR_NAME = "/statistics/";
    public static final String GD_STATISTICS_DIR_NAME = "Statistics";
    public static final String ALL_STATISTICS_FINE_NAME = "StatisticsAll.csv";
    public static final String STATISTICS_MIME_TYPE = "text/csv";

    public static final String PREFIX_USER_VIDEOFILES = "dd";

    //RSS constants
    public static final int RSS_SIZE = 128;

    //logo constants
    public static final String[] UNL_ACCEPTED_FILE_EXTS = {"mp4", "avi"};
    public static final String SIGNAL_TO_FULLSCREEN = "status";
    public static final byte GET_LOGO_STATUS_OK = 100;
    public static final byte GET_LOGO_STATUS_NOT_OK = 101;
    public static final byte STATUS_NEED_CHECK_LOGO = 102;
    public static final String BROADCAST_ACTION = "com.moby.esys.backbroadcast";
    public static final String BROADCAST_ACTION_FIRST = "com.moby.esys.backbroadcast.first";

    //signals
    public static final byte SIGNAL_TOAST = 90;
    public static final byte SIGNAL_CAMERASHOT = 80;
    public static final byte SIGNAL_START_RSS = 60;
    public static final byte SIGNAL_REC_TO_MP = 50;

    //time constants
    public final static int APP_START_DELAY = 10000;

    //permissions constants
    public final static int PERMISSIONS_REQUEST = 330;

    //statistics constants
    public static final String CSV_SEPARATOR = ",";
    public static final int NUM_STATISTICS_FILES = 7;

    //delays
    public static long START_OLD_PROFILE_DELAY = 10000;

    //debug
    public static boolean ALLOW_TOAST = false;

    public static final String MP_TOKEN = "5a24b4c7001b8850ace0573dc56dc1b9";
}
