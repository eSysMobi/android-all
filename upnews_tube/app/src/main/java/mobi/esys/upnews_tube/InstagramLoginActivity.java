package mobi.esys.upnews_tube;


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

import mobi.esys.upnews_tube.constants.DevelopersKeys;
import mobi.esys.upnews_tube.constants.OtherConst;

public class InstagramLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;
    private transient SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(this, DevelopersKeys.INSTAGRAM_CLIENT_ID,
                DevelopersKeys.INSTAGRAM_CLIENT_SECRET,
                DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        preferences = getSharedPreferences(OtherConst.APP_PREF, MODE_PRIVATE);

        if (instagram.getSession().getAccessToken().isEmpty() || instagram.getSession().getAccessToken() == null) {
            setContentView(R.layout.fragment_instagramlogin);
            Button igLoginBtn = (Button) findViewById(R.id.igLgBtn);
            igLoginBtn.setOnClickListener(this);
            Button igSkipBtn = (Button) findViewById(R.id.igSkipBtn);
            igSkipBtn.setOnClickListener(this);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(OtherConst.APP_PREF_SKIP_INSTAGRAM, false);//not skip
            editor.apply();
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashtagActivity.class));
            finish();
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case (R.id.igLgBtn):
                instagram.authorize(igAuthListener);
                break;
            case (R.id.igSkipBtn):
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(OtherConst.APP_PREF_SKIP_INSTAGRAM, true);
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
            editor.putBoolean(OtherConst.APP_PREF_SKIP_INSTAGRAM, false);//not skip
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
