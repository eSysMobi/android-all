package mobi.esys.upnews_edu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

/**
 * Created by ZeyUzh on 08.12.2015.
 */
public class StartCommon extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("esys_upn","Start application from StartCommon");
        startActivity(new Intent(StartCommon.this, InAppBillingActivity.class));
        finish();
    }
}
