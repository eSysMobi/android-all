package mobi.esys.upnews_edu;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class LoginActivity extends Activity {
    private transient CallbackManager callbackManager;
    private transient SharedPreferences prefs;
    private transient EasyTracker easyTracker;
    private transient String versionName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        prefs = getSharedPreferences("unoPref", MODE_PRIVATE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String fbAT = "";
        //check old version and new version
        versionName = BuildConfig.VERSION_NAME;
        String fbATversion = prefs.getString("fbATversion", "");
        if (versionName.equals(fbATversion)) {
            fbAT = prefs.getString("fbAT", "");
            Log.d("fbAt = ", fbAT);
        } else {
            if (!prefs.getString("fbAT", "").isEmpty()) {
                LoginManager.getInstance().logOut();
            }
        }

        if (fbAT.equals("")) {
            setContentView(R.layout.fragment_facebooklogin);

            easyTracker = EasyTracker.getInstance(LoginActivity.this);
            easyTracker.send(MapBuilder.createEvent("auth",
                    "start_app", "start_app at login activity", null).build());

            LoginButton loginBtn = (LoginButton) findViewById(R.id.fbLgBtn);

            loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("fbAT", loginResult.getAccessToken().getToken());
                    editor.putString("fbATversion", versionName);
                    editor.apply();
                    startActivity(new Intent(LoginActivity.this, FacebookGroupActivity.class));
                    finish();
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException exception) {

                }
            });
        } else {
            startActivity(new Intent(LoginActivity.this, FacebookGroupActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
