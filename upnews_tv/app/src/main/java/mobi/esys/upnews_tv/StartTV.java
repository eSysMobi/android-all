package mobi.esys.upnews_tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ZeyUzh on 08.12.2015.
 */
public class StartTV extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("esys_upn", "Start application from StartTV");
        startActivity(new Intent(StartTV.this, InAppBillingActivity.class));
        finish();
    }
}
