package mobi.esys.dastarhan;

import android.app.Application;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.database.DaggerRealmComponent;
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.RealmModule;
import mobi.esys.dastarhan.utils.FoodCheckElement;

/**
 * Created by ZeyUzh on 02.06.2016.
 */
public class DastarhanApp extends Application {

    RealmComponent component;
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
        component = DaggerRealmComponent
                .builder()
                .application(this)
                .realmModule(new RealmModule(0))
                .build();

        foodCheck = new ArrayList<>();
        //add first element "All restaurants"
        foodCheck.add(new FoodCheckElement(-42,0));
    }

    public RealmComponent realmComponent(){
        return component;
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
