package mobi.esys.dastarhan;

import android.app.Application;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.database.RealmModule;
import mobi.esys.dastarhan.net.NetModule;
import mobi.esys.dastarhan.utils.FoodCheckElement;

/**
 * Created by ZeyUzh on 02.06.2016.
 */
public class DastarhanApp extends Application {

    private AppComponent component;
    private long lastCheck = 0;
    private List<FoodCheckElement> foodCheck;

    private static String sessionLogin;
    private static String sessionPass;

    public static void setSessionInfo(String sessionL, String sessionP){
        sessionLogin = sessionL;
        sessionPass = sessionP;
    }

    public static String getSessionLogin() {
        return sessionLogin;
    }

    public static String getSessionPass() {
        return sessionPass;
    }


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
        component = DaggerAppComponent
                .builder()
                .application(this)
                .realmModule(new RealmModule(0))
                .netModule(new NetModule())
                .build();

        foodCheck = new ArrayList<>();
        //add first element "All restaurants"
        foodCheck.add(new FoodCheckElement(-42,0));
    }

    public AppComponent appComponent(){
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
