package mobi.esys.dastarhan;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Constants {
    public static final String APP_PREF = "dastarhan_preferences";

    public static final int CALLBACK_GET_CUISINES_SUCCESS = 42;
    public static final int CALLBACK_GET_CUISINES_FAIL = 41;
    public static final int CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR = 44;
    public static final int CALLBACK_GET_RESTAURANTS_SUCCESS = 52;
    public static final int CALLBACK_GET_RESTAURANTS_FAIL = 51;
    public static final int CALLBACK_GET_RESTAURANTS_SHOW_PROGRESS_BAR = 54;
    public static final int CALLBACK_GET_FOOD_SUCCESS = 62;
    public static final int CALLBACK_GET_FOOD_FAIL = 61;
    public static final int CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR = 64;

    //api
    public static final String URL_CUISINES = "http://dastarhan.net/index.php/user_api/cui/format/json";
    public static final String URL_RESTORANS = "http://dastarhan.net/index.php/user_api/rest/format/json";
    public static final String URL_FOOD = "http://dastarhan.net/index.php/user_api/food/format/json?resid=";

    //DB
    public static final String DB_NAME = "dastarhanDB";
    public static final String DB_TABLE_CUISINES = "cuisines";
    public static final String DB_TABLE_RESTAURANTS = "restaurants";
    public static final String DB_TABLE_FOOD = "food";

}
