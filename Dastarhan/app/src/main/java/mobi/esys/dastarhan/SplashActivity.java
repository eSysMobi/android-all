package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import mobi.esys.dastarhan.tasks.GetCuisines;
import mobi.esys.dastarhan.tasks.GetRestaurants;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = "dtagSplashActivity";
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

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
                nextActivity();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_FAIL) {  //not ok
                Log.d(TAG, "Restaurants data NOT receive");
                nextActivity();
            }

            super.handleMessage(msg);
        }
    }

    private void getRestaurants(){
        GetRestaurants gr = new GetRestaurants(this, handler);
        gr.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void nextActivity(){
        Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
