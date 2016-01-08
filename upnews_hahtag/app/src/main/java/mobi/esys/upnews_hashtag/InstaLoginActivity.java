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
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
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


public class InstaLoginActivity extends Activity{
    private transient Instagram instagram;
    private transient EasyTracker easyTracker;
    private transient ImageView iv_button_next_instalogin;

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

            //iv_button_next_instalogin
            iv_button_next_instalogin = (ImageView) findViewById(R.id.iv_button_next_instalogin);
            iv_button_next_instalogin.setTranslationX(ISConsts.progressSizes.progressDots[3] + ISConsts.progressSizes.progressDotSize);
            final View.OnClickListener onClickListenerNext =new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instagram.authorize(igAuthListener);
                }
            };

            //positing tv_instalogin
            TextView tv_instalogin = (TextView) findViewById(R.id.tv_instalogin);
            tv_instalogin.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 2 * ISConsts.progressSizes.progressDotSize);

            //iv_dot_instalogin
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
            Animation scale_up = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_up.setDuration((long) (ISConsts.progressSizes.animDuration*2/3));
            scale_up.setFillAfter(true);
            Animation scale_down = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_down.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
            scale_down.setStartOffset((long) (10 + ISConsts.progressSizes.animDuration * 2 / 3));
            scale_down.setFillAfter(true);
            AnimationSet allDot1Animation = new AnimationSet(true);
            allDot1Animation.addAnimation(scale_up);
            allDot1Animation.addAnimation(scale_down);
            allDot1Animation.setFillAfter(true);
            allDot1Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    iv_button_next_instalogin.setOnClickListener(onClickListenerNext);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            iv_dot_instalogin.startAnimation(allDot1Animation);

//
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
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
