package mobi.esys.dastarhan;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by ZeyUzh on 02.06.2016.
 */
public class DastarhanApp extends Application {

    private boolean needGetCuisinesFromServer = true;

    public void setNeedGetCuisinesFromServer(boolean needGetCuisinesFromServer) {
        this.needGetCuisinesFromServer = needGetCuisinesFromServer;
    }

    public boolean getNeedGetCuisinesFromServer() {
        return needGetCuisinesFromServer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
