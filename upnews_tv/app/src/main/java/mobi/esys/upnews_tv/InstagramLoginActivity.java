package mobi.esys.upnews_tv;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.upnews_tv.constants.DevelopersKeys;

public class InstagramLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        instagram = new Instagram(this, DevelopersKeys.INSTAGRAM_CLIENT_ID,
                DevelopersKeys.INSTAGRAM_CLIENT_SECRET,
                DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        if (instagram.getSession().getAccessToken() == null || instagram.getSession().getAccessToken().isEmpty()) {
            setContentView(R.layout.fragment_instagramlogin);
            Button bIgLgBtn = (Button) findViewById(R.id.bIgLgBtn);
            bIgLgBtn.setOnClickListener(this);
            Button bIgLgBtnSkip = (Button) findViewById(R.id.bIgLgBtnSkip);
            bIgLgBtnSkip.setOnClickListener(this);
        } else {
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashtagActivity.class));
            finish();
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.bIgLgBtn:
                instagram.authorize(igAuthListener);
                break;
            case R.id.bIgLgBtnSkip:
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("needShowInstagram", false);
                editor.apply();
                startActivity(new Intent(InstagramLoginActivity.this, TwitterLoginActivity.class));
                finish();
                break;
        }

    }

    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("needShowInstagram", true);
            editor.apply();
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashtagActivity.class));
            finish();
        }

        @Override
        public void onError(final String error) {

        }

        @Override
        public void onCancel() {
        }
    };
}
