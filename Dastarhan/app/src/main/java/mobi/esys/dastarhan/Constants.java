package mobi.esys.dastarhan;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Constants {
    public static final int CALLBACK_GET_CUISINES_SUCCESS = 42;
    public static final int CALLBACK_GET_CUISINES_FAIL = 41;
    public static final int CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR = 44;
    public static final int CALLBACK_GET_CUISINES_HIDE_PROGRESS_BAR = 45;

    //api
    public static final String URL_CUISINES = "http://dastarhan.net/index.php/user_api/cui/format/json";
    public static final String URL_RESTORANS = "http://dastarhan.net/index.php/user_api/rest/format/json";

    //DB
    public static final String DB_NAME = "dastarhanDB";
    public static final String DB_TABLE_CUISINES = "cuisines";
}
