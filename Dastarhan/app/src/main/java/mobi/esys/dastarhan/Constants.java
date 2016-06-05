package mobi.esys.dastarhan;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Constants {
    //sharedPreferences
    public static final String APP_PREF = "dastarhan_preferences";
    public static final String PREF_SAVED_LOGIN = "saved_login";
    public static final String PREF_SAVED_PASS = "saved_pass";
    public static final String PREF_CURR_NUM_ORDER = "current_order";
    public static final String PREF_CURR_ORDER_EXECUTED = "order_executed";

    //callbacks
    public static final int CALLBACK_GET_CUISINES_SUCCESS = 42;
    public static final int CALLBACK_GET_CUISINES_FAIL = 41;
    public static final int CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR = 44;
    public static final int CALLBACK_GET_RESTAURANTS_SUCCESS = 52;
    public static final int CALLBACK_GET_RESTAURANTS_FAIL = 51;
    public static final int CALLBACK_GET_RESTAURANTS_SHOW_PROGRESS_BAR = 54;
    public static final int CALLBACK_GET_FOOD_SUCCESS = 62;
    public static final int CALLBACK_GET_FOOD_FAIL = 61;
    public static final int CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR = 64;
    public static final int CALLBACK_GET_PROMO_SUCCESS = 72;
    public static final int CALLBACK_GET_PROMO_FAIL = 71;
    public static final int CALLBACK_GET_PROMO_SHOW_PROGRESS_BAR = 74;

    //api
    public static final String URL_CUISINES = "http://dastarhan.net/index.php/user_api/cui/format/json";
    public static final String URL_RESTORANS = "http://dastarhan.net/index.php/user_api/rest/format/json";
    public static final String URL_FOOD = "http://dastarhan.net/index.php/user_api/food/format/json?resid=";
    public static final String URL_PROMO = "http://dastarhan.net/index.php/user_api/offers/format/json?res_id=";

    //DB
    public static final String DB_NAME = "dastarhanDB";
    public static final String DB_TABLE_CUISINES = "cuisines";
    public static final String DB_TABLE_RESTAURANTS = "restaurants";
    public static final String DB_TABLE_FOOD = "food";
    public static final String DB_TABLE_ORDERS = "orders";
    public static final String DB_TABLE_PROMO = "promo";
    public static final String DB_TABLE_GIFTS = "gifts";

    //
    public static final byte GIFT_TYPE_DISCOUNT_PERCENT_ALL = 1;
    public static final byte GIFT_TYPE_GOODS = 2;
    public static final byte GIFT_TYPE_HIDDEN = 3;

    //actions for food adapter
    public static final byte ACTION_GET_FOOD_FROM_RESTAURANTS = 2;
    public static final byte ACTION_GET_FOOD_FAVORITE = 4;
    public static final byte ACTION_GET_FOOD_CURR_ORDERED = 8;

}
