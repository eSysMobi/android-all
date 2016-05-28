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
                + "_id integer primary key autoincrement,"
                + "server_id integer,"
                + "ru_name text,"
                + "en_name text,"
                + "approved integer"
                + ");");

        // create table restaurants
        Log.d(TAG, "Create table " + Constants.DB_TABLE_RESTAURANTS);
        db.execSQL("create table " + Constants.DB_TABLE_RESTAURANTS + " ("
                + "_id integer primary key autoincrement,"
                + "server_id integer,"
                + "ru_name text,"
                + "en_name text,"
                + "city_id integer,"
                + "district_id integer,"
                + "min_order integer,"
                + "del_cost integer,"
                + "schedule text,"
                + "time1 text,"
                + "time2 text,"
                + "del_time text,"
                + "payment_methods text,"
                + "contact_name_ru text,"
                + "contact_name_en text,"
                + "phone text,"
                + "mobile text,"
                + "email1 text,"
                + "email2 text,"
                + "total_rating integer,"
                + "total_votes integer,"
                + "contact_email text,"
                + "order_phone text,"
                + "additional_ru text,"
                + "additional_en text,"
                + "picture text,"
                + "vegetarian integer,"
                + "featured integer,"
                + "approved integer,"
                + "cuisines text"
                + ");");

        // create table food
        Log.d(TAG, "Create table " + Constants.DB_TABLE_FOOD);
        db.execSQL("create table " + Constants.DB_TABLE_FOOD + " ("
                + "_id integer primary key autoincrement,"
                + "server_id integer,"
                + "res_id integer,"
                + "cat_id integer,"
                + "ru_name text,"
                + "en_name text,"
                + "picture text,"
                + "ru_descr text,"
                + "en_descr text,"
                + "price real,"
                + "min_amount integer,"
                + "units text,"
                + "ordered integer,"
                + "offer integer,"
                + "vegetarian integer,"
                + "favorite integer,"
                + "featured integer"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}