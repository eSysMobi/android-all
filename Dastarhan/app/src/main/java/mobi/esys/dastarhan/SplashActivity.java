package mobi.esys.dastarhan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.tasks.GetCuisines;
import mobi.esys.dastarhan.tasks.GetPromo;
import mobi.esys.dastarhan.tasks.GetRestaurants;
import mobi.esys.dastarhan.utils.FoodCheckElement;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = "dtagSplashActivity";
    public final int REQUEST_CODE_SPLASH = 89;

    private Handler handler;
    private DastarhanApp dastarhanApp;
    private SharedPreferences prefs;

    private boolean readyCuisines = false;
    private boolean readyRestaurants = false;
    private boolean readyPromos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        dastarhanApp = (DastarhanApp) getApplication();
        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

        handler = new HandleCuisines();
        GetCuisines gc = new GetCuisines(dastarhanApp, handler);
        gc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        GetRestaurants gr = new GetRestaurants(dastarhanApp, handler);
        gr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class HandleCuisines extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_CUISINES_SUCCESS) {  //all ok
                Log.d(TAG, "Cuisines data received");
                readyCuisines = true;
                checkIsAllReady();
            }
            if (msg.what == Constants.CALLBACK_GET_CUISINES_FAIL) {  //not ok
                Log.d(TAG, "Cuisines data NOT receive");
                readyCuisines = true;
                checkIsAllReady();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_SUCCESS) {  //all ok
                Log.d(TAG, "Restaurants data received");
                readyRestaurants = true;
                getPromo();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_FAIL) {  //not ok
                Log.d(TAG, "Restaurants data NOT receive");
                readyRestaurants = true;
                getPromo();
            }
            if (msg.what == Constants.CALLBACK_GET_PROMO_SUCCESS) {  //all ok
                Log.d(TAG, "Promo data received");
                readyPromos = true;
                checkIsAllReady();
            }
            if (msg.what == Constants.CALLBACK_GET_PROMO_FAIL) {  //not ok
                Log.d(TAG, "Promo data NOT receive");
                readyPromos = true;
                checkIsAllReady();
            }

            super.handleMessage(msg);
        }
    }

    private void checkIsAllReady() {
        if (readyCuisines && readyRestaurants && readyPromos) {
            goToLoginActivity();
        }
    }

    private void getPromo() {
        //initialize timed check food list for each restaurant
        List<Restaurant> restaurants = dastarhanApp.component.restaurantRepository().getAll();
        List<Integer> restIDs = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            int res_id = restaurant.getServer_id();
            restIDs.add(res_id);
            //save restaurants id for checking time
            dastarhanApp.getCheckedFood().add(new FoodCheckElement(res_id, 0));
        }
        Integer[] restaurantsID = restIDs.toArray(new Integer[restIDs.size()]);
        GetPromo gp = new GetPromo(dastarhanApp, handler, restaurantsID);
        gp.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void goToLoginActivity() {
        boolean isAuthPersist = prefs.getBoolean(Constants.PREF_SAVED_AUTH_IS_PERSIST, false);
        if (isAuthPersist) {
            goToMainActivity();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREF_SAVED_LOGIN, "");
            editor.putString(Constants.PREF_SAVED_PASS, "");
            editor.putString(Constants.PREF_SAVED_AUTH_TOKEN, "");
            editor.apply();
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SPLASH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Login after splash requestCode " + requestCode + " resultCode " + resultCode);
        if (requestCode == REQUEST_CODE_SPLASH) {
            //if(resultCode == Activity.RESULT_OK){ }
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
