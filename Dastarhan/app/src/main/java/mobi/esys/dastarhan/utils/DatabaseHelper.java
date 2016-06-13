package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import mobi.esys.dastarhan.Constants;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = "dtagDatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");

        // create table cuisines
        Log.d(TAG, "Create table " + Constants.DB_TABLE_CUISINES);
        db.execSQL("create table " + Constants.DB_TABLE_CUISINES + " ("
                + "_id INTEGER primary key autoincrement,"
                + "server_id INTEGER,"
                + "ru_name TEXT,"
                + "en_name TEXT,"
                + "approved INTEGER"
                + ");");

        // create table restaurants
        Log.d(TAG, "Create table " + Constants.DB_TABLE_RESTAURANTS);
        db.execSQL("create table " + Constants.DB_TABLE_RESTAURANTS + " ("
                + "_id INTEGER primary key autoincrement,"
                + "server_id INTEGER,"
                + "ru_name TEXT,"
                + "en_name TEXT,"
                + "city_id INTEGER,"
                + "district_id INTEGER,"
                + "min_order INTEGER,"
                + "del_cost INTEGER,"
                + "schedule TEXT,"
                + "time1 TEXT,"
                + "time2 TEXT,"
                + "del_time TEXT,"
                + "payment_methods TEXT,"
                + "contact_name_ru TEXT,"
                + "contact_name_en TEXT,"
                + "phone TEXT,"
                + "mobile TEXT,"
                + "email1 TEXT,"
                + "email2 TEXT,"
                + "total_rating INTEGER,"
                + "total_votes INTEGER,"
                + "contact_email TEXT,"
                + "order_phone TEXT,"
                + "additional_ru TEXT,"
                + "additional_en TEXT,"
                + "picture TEXT,"
                + "vegetarian INTEGER,"
                + "featured INTEGER,"
                + "approved INTEGER,"
                + "cuisines TEXT"
                + ");");

        // create table food
        Log.d(TAG, "Create table " + Constants.DB_TABLE_FOOD);
        db.execSQL("create table " + Constants.DB_TABLE_FOOD + " ("
                + "_id INTEGER primary key autoincrement,"
                + "server_id INTEGER,"
                + "res_id INTEGER,"
                + "cat_id INTEGER,"
                + "ru_name TEXT,"
                + "en_name TEXT,"
                + "picture TEXT,"
                + "ru_descr TEXT,"
                + "en_descr TEXT,"
                + "price REAL,"
                + "min_amount INTEGER,"
                + "units TEXT,"
                + "ordered INTEGER,"
                + "offer INTEGER,"
                + "vegetarian INTEGER,"
                + "favorite INTEGER,"
                + "featured INTEGER,"
                + "in_order INTEGER"
                + ");");

        // create table orders
        Log.d(TAG, "Create table " + Constants.DB_TABLE_ORDERS);
        db.execSQL("create table " + Constants.DB_TABLE_ORDERS + " ("
                + "_id INTEGER primary key autoincrement,"
                + "id_order INTEGER,"
                + "id_food INTEGER,"
                + "count INTEGER,"
                + "price REAL,"
                + "notice TEXT"
                + ");");

        // create table promo
        Log.d(TAG, "Create table " + Constants.DB_TABLE_PROMO);
        db.execSQL("create table " + Constants.DB_TABLE_PROMO + " ("
                + "_id INTEGER primary key autoincrement,"
                + "server_id INTEGER,"
                + "res_id INTEGER,"
                + "condition INTEGER,"      //условие акции(1- Сумма заказа больше..., 2 - Покупка определенной группы блюд, 3 - Покупка одного блюда, 4 - покупка блюда из категории, 5 - промо-код, 6 - самовывоз)
                + "condition_par TEXT,"     //параметр для условия акции. Его форма зависит от значения condition:
                                            //1 - сумма заказа, при покупки на которую работает акция
                                            //2 - группа блюд, представленная в json формате, например ["3","4"], где 3, 4 - id блюд
                                            //3 - id блюда, при покупки которого наступает акция
                                            //4 - id категории, при покупки из которой наступает акция
                                            //5 - промокод(он будет скрыт)
                                            //6 - ничего
                + "time INTEGER,"   //0 - Акция действует в любое время, 1 - акция действует с time1 по time2(это время дня)
                + "time1 TEXT,"     //время дня начала действия акции
                + "time2 TEXT,"     //время дня конца действия акции
                + "days TEXT,"      //дни недели по номерам когда действует акция
                + "date INTEGER,"   //0 - Акция действует в любое время, 1 - акция действует с date1 по date2
                + "date1 TEXT,"     //дата начала действия акции
                + "date2 TEXT,"     //дата конца действия акции
                + "gift_type TEXT," //discount_percent_all - Скидка в процентах на весь заказ
                                    //discount_amount_all - Скидка в рублях на весь заказ
                                    //discount_percent_offer - Скидка в процентах на блюда, обозначенные в условиях акции
                                    //discount_amount_offer  - Скидка в рублях на блюда, обозначенные в условиях акции
                                    //gift_goods - Блюда, которые получают в подарок
                                    //free_delivery - бесплатная доставка
                + "gift TEXT,"
                + "gift_condition INTEGER" // Условия предоставление подарков в акции, 0 - И, 1 - ИЛИ
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}