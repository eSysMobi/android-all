package mobi.esys;

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
    public static final String NETWORK_STATISTICS_DIR_NAME = "/statistics_net/";
    public static final String GD_NETWORK_STATISTICS_DIR_NAME = "Statistics_net";
    public static final String STATISTICS_MIME_TYPE = "text/csv";

    public static final String PREFIX_USER_VIDEOFILES = "dd";

    //RSS constants
    public static final int RSS_SIZE = 128;

    public static final String[] UNL_ACCEPTED_FILE_EXTS = {"mp4", "avi"};
    ;

    //time constants
    public final static int APP_START_DELAY = 10000;
    public final static int MACS_START_DELAY = 8000;
    public final static int MACS_CYCLE_DELAY = 2 * 60 * 1000;
    public static long START_OLD_PROFILE_DELAY = 10000;


    //permissions constants
    public final static int PERMISSIONS_REQUEST = 330;

    //statistics constants
    public static final String CSV_SEPARATOR = ",";
    public static final int NUM_STATISTICS_FILES = 7;

    //debug
    public static boolean ALLOW_TOAST = false;
    public static boolean ALLOW_NET_SCAN = false;
    public static boolean ALLOW_HIDEUI_DRIVEACTIVITY = false;

    public static final String MP_TOKEN = "5a24b4c7001b8850ace0573dc56dc1b9";
}
