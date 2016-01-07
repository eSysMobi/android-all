package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.consts.ISConsts;
import mobi.esys.view.DrawProgress;


public class InstaLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;
    private transient Button instAuthBtn;
    private transient EasyTracker easyTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(InstaLoginActivity.this, ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret, ISConsts.instagramconsts.instagram_redirect_uri);
        easyTracker = EasyTracker.getInstance(InstaLoginActivity.this);

        // getting display size
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        //constants filling
        ISConsts.progressSizes.screenWidth = metrics.widthPixels;
        ISConsts.progressSizes.screenHeight = metrics.heightPixels;
        Log.d("TAG", "screenWidth = " + metrics.widthPixels + " pixels");
        Log.d("TAG", "screenHeight = " + metrics.heightPixels + " pixels");
        ISConsts.progressSizes.progressDotSize = (int) (ISConsts.progressSizes.screenWidth * ISConsts.progressSizes.progressDotDelta);
        ISConsts.progressSizes.progressLineSize = (int) (ISConsts.progressSizes.screenWidth * ISConsts.progressSizes.progressLineDelta);
        for (int i = 0; i < 4; i++) {
            int tmp = (int) (ISConsts.progressSizes.screenWidth / 3)
                    + ISConsts.progressSizes.progressDotSize
                    + i * (int)(((int) (ISConsts.progressSizes.screenWidth / 3) - 2 * ISConsts.progressSizes.progressDotSize) / 3);
            ISConsts.progressSizes.progressDots[i] = tmp;
            Log.d("TAG", "dot " + i + " = " + tmp + " pixels");
        }


        if (instagram.getSession().getAccessToken().isEmpty() || instagram.getSession().getAccessToken() == null) {
            setContentView(R.layout.activity_instalogin_2);

//
//interface v.2

            if (ISConsts.progressSizes.drawProgress == null){
                ISConsts.progressSizes.drawProgress = new DrawProgress(getResources().getColor(R.color.deep_gray), getResources().getColor(R.color.progress_color));
            }

            RelativeLayout rl_instalogin = (RelativeLayout) findViewById(R.id.rl_instalogin);

            if (Build.VERSION.SDK_INT < 16) {
                rl_instalogin.setBackgroundDrawable(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
            } else {
                rl_instalogin.setBackground(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
            }

            TextView tv_instalogin = (TextView) findViewById(R.id.tv_instalogin);
            tv_instalogin.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 2 * ISConsts.progressSizes.progressDotSize);

            ImageView iv_dot_instalogin = (ImageView) findViewById(R.id.iv_dot_instalogin);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(ISConsts.progressSizes.progressDots[0] - ISConsts.progressSizes.progressLineSize,
                    (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                    0,
                    0);
            iv_dot_instalogin.setLayoutParams(lp);
            iv_dot_instalogin.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
            Log.d("TAG", "w bitmap = " + ISConsts.progressSizes.drawProgress.getLittleDotProgress().getWidth());
            Log.d("TAG", "h bitmap = " + ISConsts.progressSizes.drawProgress.getLittleDotProgress().getHeight());

            // Animation
            Animation scale_up = AnimationUtils.loadAnimation(this, R.anim.inflate_dot);

//            Animation scale_up = new ScaleAnimation(1.0f, 4.0f, 1.0f, 4.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//            scale_up.setDuration(2000);
//            scale_up.setStartOffset(1000);
//            scale_up.setFillAfter(true);

            iv_dot_instalogin.startAnimation(scale_up);
//
//            AnimationSet allAnimation = new AnimationSet(true);
//            allAnimation.addAnimation(animMove);
//            allAnimation.addAnimation(scale_up);
//            allAnimation.setFillAfter(true);

//            iv_dot_instalogin.startAnimation(allAnimation);

//


            instAuthBtn = (Button) findViewById(R.id.instAuthBtn);
            instAuthBtn.setOnClickListener(this);
        } else {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "go_to_hashtag_input", null).build());
        }

    }



    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "success", null).build());
        }

        @Override
        public void onError(final String error) {
            Toast.makeText(InstaLoginActivity.this,
                    getString(R.string.instagram_auth_failure_message),
                    Toast.LENGTH_LONG).show();
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "error", null).build());
        }

        @Override
        public void onCancel() {
            Toast.makeText(InstaLoginActivity.this,
                    getString(R.string.instagram_auth_cancel_message),
                    Toast.LENGTH_LONG).show();
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "cancel", null).build());
        }
    };


    @Override
    public void onClick(View v) {
        instagram.authorize(igAuthListener);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
