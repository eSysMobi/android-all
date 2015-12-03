package mobi.esys.constants;

public final class UNLConsts {

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


    public static final String[] UNL_ACCEPTED_FILE_EXTS = {"mp4", "avi"};
    public static final String STATUS_GET_LOGO = "status";
    public static final byte STATUS_OK = 100;
    public static final byte STATUS_NOT_OK = 101;
    public static final byte STATUS_NEED_CHECK_LOGO = 102;
    public static final String BROADCAST_ACTION = "com.moby.esys.backbroadcast";


    public final static int APP_START_DELAY = 10000;
    public final static int RSS_REFRESH_INTERVAL = 1000 * 60 * 5;
    public final static int RSS_TASK_START_DELAY = 6000;

    public final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 333;

    public static final String MP_TOKEN = "5a24b4c7001b8850ace0573dc56dc1b9";
}
