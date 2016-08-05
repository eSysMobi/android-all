package mobi.esys.upnews_edu;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class TwitterLoginActivity extends Activity {
    private transient TwitterLoginButton loginButton;
    private transient SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        if (session == null) {
            setContentView(R.layout.fragment_twitterlogin);

            loginButton = (TwitterLoginButton) findViewById(R.id.twLgnBtn);
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("twAT", result.data.getAuthToken().token);
                    editor.putBoolean("needShowTwitter", true);
                    editor.apply();
                    startActivity(new Intent(TwitterLoginActivity.this, PlayerActivity.class));
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {

                }
            });

            Button twLgnBtnSkip = (Button) findViewById(R.id.twLgnBtnSkip);
            twLgnBtnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("needShowTwitter", false);
                    editor.apply();
                    startPlayer();
                }
            });

        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("needShowTwitter", true);
            editor.apply();
            startPlayer();
        }

    }

    private void startPlayer(){
        startActivity(new Intent(TwitterLoginActivity.this, PlayerActivity.class));
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
