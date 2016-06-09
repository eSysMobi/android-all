package mobi.esys.dastarhan;

import android.app.Application;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.utils.FoodCheckElement;

/**
 * Created by ZeyUzh on 02.06.2016.
 */
public class DastarhanApp extends Application {

    private long lastCheck = 0;
    private List<FoodCheckElement> foodCheck;

    public void setLastCheck(){
        lastCheck = System.currentTimeMillis();
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public List<FoodCheckElement> getCheckedFood(){
        return foodCheck;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        foodCheck = new ArrayList<>();
        //add first element "All restaurants"
        foodCheck.add(new FoodCheckElement(-42,0));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
