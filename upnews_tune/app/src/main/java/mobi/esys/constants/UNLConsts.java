package mobi.esys.constants;

public final class UNLConsts {

    //fileworks constants
    public static final String TEMP_FILE_EXT = "tmp";
    public static final String DIR_NAME = "/upnewstune/";
    public static final String GD_DIR_NAME = "upnewstune";
    public static final String APP_PREF = "UNLPref";
    public static final String GD_FOLDER_QUERY = "'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed=false";
    public static final String PREFIX_USER_VIDEOFILES = "dd";

    public static final String[] UNL_ACCEPTED_FILE_EXTS = {"mp3", "wav", "ogg", "mid", "flac", "3gp"};
    public static final String SIGNAL_TO_FULLSCREEN = "status";
    public static final String BROADCAST_ACTION = "com.moby.esys.backbroadcast";

    //signals
    public static final String ACTION_PLAY = "mobi.esys.action.PLAY";
    public static final String ACTION_STOP = "mobi.esys.action.STOP";

    //time constants
    public final static int APP_START_DELAY = 10000;
    public static long START_OLD_PROFILE_DELAY = 10000;

    //permissions constants
    public final static int PERMISSIONS_REQUEST = 330;

    //debug
    public static boolean ALLOW_TOAST = false;
}
