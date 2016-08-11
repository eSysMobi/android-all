package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.consts.ISConsts;


public class InstagramLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(this, ISConsts.instagramconsts.INSTAGRAM_CLIENT_ID,
                ISConsts.instagramconsts.INSTAGRAM_CLIENT_SECRET,
                ISConsts.instagramconsts.INSTAGRAM_REDIRECT_URI);

        if (instagram.getSession().getAccessToken() == null || instagram.getSession().getAccessToken().isEmpty()) {
            setContentView(R.layout.activity_instalogin);
            Button bIgLgBtn = (Button) findViewById(R.id.bIgLgBtn);
            bIgLgBtn.setOnClickListener(this);
        } else {
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashTagActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bIgLgBtn:
                instagram.authorize(igAuthListener);
                break;
        }

    }

    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashTagActivity.class));
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
