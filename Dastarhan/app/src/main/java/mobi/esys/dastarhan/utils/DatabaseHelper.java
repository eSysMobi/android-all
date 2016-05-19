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
        // create table
        db.execSQL("create table " + Constants.DB_TABLE_CUISINES + " ("
                + "_id integer primary key autoincrement,"
                //+ "id integer primary key autoincrement,"
                + "server_id integer,"
                + "ru_name text,"
                + "en_name text,"
                + "approved integer"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}