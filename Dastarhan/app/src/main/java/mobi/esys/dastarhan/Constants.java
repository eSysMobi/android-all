package mobi.esys.dastarhan;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Constants {
    //sharedPreferences
    public static final String APP_PREF = "dastarhan_preferences";
    public static final String PREF_SAVED_LOGIN = "saved_login";
    public static final String PREF_SAVED_PASS = "saved_pass";
    public static final String PREF_SAVED_AUTH_TOKEN = "saved_auth_token";
    public static final String PREF_SAVED_AUTH_IS_PERSIST = "saved_auth_is_persist";
    public static final String PREF_SAVED_USER_ID = "saved_user_id";

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
    public static final String API_BASE_URL = "http://dastarhan.net/";

    public static final String URL_AUTHORIZATION = "http://dastarhan.net/index.php/user_api/auth/format/json";
    public static final String URL_REGISTRATION = "http://dastarhan.net/index.php/user_api/register/format/json";
    public static final String URL_CUISINES = "http://dastarhan.net/index.php/user_api/cui/format/json";
    public static final String URL_RESTORANS = "http://dastarhan.net/index.php/user_api/rest/format/json";
    public static final String URL_FOOD = "http://dastarhan.net/index.php/user_api/food/format/json";
    public static final String URL_PROMO = "http://dastarhan.net/index.php/user_api/offers/format/json";
        //http://dastarhan.net/index.php/user_api/voteforrest/format/json?id=80&apikey=INVxnl733PjB6620jPYY&res=16&vote=8
    public static final String URL_VOTE_FOR_REST = "http://dastarhan.net/index.php/user_api/voteforrest/format/json";


    //promo gifts type
    public static final String GIFT_TYPE_DISCOUNT_PERCENT_ALL = "discount_percent_all";     //Скидка в процентах на весь заказ
    public static final String GIFT_TYPE_DISCOUNT_AMOUNT_ALL = "discount_amount_all";       //Скидка в рублях на весь заказ
    public static final String GIFT_TYPE_DISCOUNT_PERCENT_OFFER = "discount_percent_offer"; //Скидка в процентах на блюда, обозначенные в условиях акции
    public static final String GIFT_TYPE_DISCOUNT_AMOUNT_OFFER = "discount_amount_offer";   //Скидка в рублях на блюда, обозначенные в условиях акции
    public static final String GIFT_TYPE_GIFT_GOODS = "gift_goods";                         //Блюда, которые получают в подарок
    public static final String GIFT_TYPE_FREE_DELIVERY = "free_delivery";                   //Бесплатная доставка
    public static final String GIFT_TYPE_HIDDEN = "hidden";                                 //Скрыто, откроется после ввода промокода

    //currencies
    public static final String CURRENCY = "рублей";
    public static final String CURRENCY_SHORT = "руб.";
    public static final String CURRENCY_VERY_SHORT = "р.";

    //actions for food adapter
    public static final byte ACTION_GET_FOOD_FROM_RESTAURANTS = 2;
    public static final byte ACTION_GET_FOOD_FAVORITE = 4;
    public static final byte ACTION_GET_FOOD_CURR_ORDERED = 8;

    //time
    public static final int CONNECTION_TIMEOUT = 3 * 1000; //3 sec
    public static final int FOOD_CHECKING_INTERVAL = 15 * 60 * 1000; //15 min

    //request codes
    public static final int REQUEST_CODE_SPLASH = 89;
    public static final int REQUEST_CODE_SETTINGS = 99;
    public static final int REQUEST_CODE_VOTE_REST = 109;
    //result codes
    public static final int RESULT_CODE_NO_INET = 10;
    public static final int RESULT_CODE_ERROR = 9;
    public static final int RESULT_CODE_AUTH_ERROR = 11;
    public static final int RESULT_CODE_SIGNUP_ERROR = 12;
    public static final int RESULT_CODE_NO_USER_EXISTS = 13;
    public static final int RESULT_CODE_USER_ALREADY_EXISTS = 14;
    public static final int RESULT_CODE_VOTE_ALREADY_VOTED = 21;
}
