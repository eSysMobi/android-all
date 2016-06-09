package mobi.esys.dastarhan;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.tasks.GetCuisines;
import mobi.esys.dastarhan.tasks.GetPromo;
import mobi.esys.dastarhan.tasks.GetRestaurants;
import mobi.esys.dastarhan.utils.DatabaseHelper;
import mobi.esys.dastarhan.utils.FoodCheckElement;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = "dtagSplashActivity";
    private Handler handler;
    private DastarhanApp dastarhanApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        dastarhanApp = (DastarhanApp) getApplication();

        handler = new HandleCuisines();
        GetCuisines gc = new GetCuisines(this, handler);
        gc.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }

    private class HandleCuisines extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_CUISINES_SUCCESS) {  //all ok
                Log.d(TAG, "Cuisines data received");
                getRestaurants();
            }
            if (msg.what == Constants.CALLBACK_GET_CUISINES_FAIL) {  //not ok
                Log.d(TAG, "Cuisines data NOT receive");
                getRestaurants();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_SUCCESS) {  //all ok
                Log.d(TAG, "Restaurants data received");
                getPromo();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_FAIL) {  //not ok
                Log.d(TAG, "Restaurants data NOT receive");
                getPromo();
            }
            if (msg.what == Constants.CALLBACK_GET_PROMO_SUCCESS) {  //all ok
                Log.d(TAG, "Promo data received");
                nextActivity();
            }
            if (msg.what == Constants.CALLBACK_GET_PROMO_FAIL) {  //not ok
                Log.d(TAG, "Promo data NOT receive");
                nextActivity();
            }

            super.handleMessage(msg);
        }
    }

    private void getRestaurants() {
        GetRestaurants gr = new GetRestaurants(this, handler);
        gr.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void getPromo() {
        String selectQuery = "SELECT * FROM " + Constants.DB_TABLE_RESTAURANTS;
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            List<Integer> restIDs = new ArrayList<>();
            do {
                int res_id = cursor.getInt(cursor.getColumnIndexOrThrow("server_id"));
                restIDs.add(res_id);
                //save restaurants id for checking time
                dastarhanApp.getCheckedFood().add(new FoodCheckElement(res_id,0));
            } while (cursor.moveToNext());
            Integer[] restaurantsID = restIDs.toArray(new Integer[restIDs.size()]);
            GetPromo gp = new GetPromo(this, handler, restaurantsID);
            gp.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
        cursor.close();
        db.close();
    }

    private void nextActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
