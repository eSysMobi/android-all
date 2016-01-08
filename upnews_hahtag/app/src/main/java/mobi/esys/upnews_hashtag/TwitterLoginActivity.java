package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;
import mobi.esys.view.DrawProgress;

public class TwitterLoginActivity extends Activity {
    private transient TwitterAuthClient client;
    private transient SharedPreferences prefs;
    private transient EasyTracker easyTracker;
    private transient ImageView iv_button_next_twitlogin;
    private transient ImageView iv_button_skip_twitlogin;

    public TwitterLoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
        Fabric.with(this, new Twitter(authConfig));
        prefs = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        easyTracker = EasyTracker.getInstance(TwitterLoginActivity.this);


        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (session == null) {
            setContentView(R.layout.activity_twitterlogin_2);

//interface v.2
            if (ISConsts.progressSizes.drawProgress == null) {
                ISConsts.progressSizes.drawProgress = new DrawProgress(getResources().getColor(R.color.deep_gray), getResources().getColor(R.color.progress_color));
            }

            RelativeLayout rl_twitlogin = (RelativeLayout) findViewById(R.id.rl_twitlogin);

            if (Build.VERSION.SDK_INT < 16) {
                rl_twitlogin.setBackgroundDrawable(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
            } else {
                rl_twitlogin.setBackground(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
            }

            //iv_button_next_twitlogin
            iv_button_next_twitlogin = (ImageView) findViewById(R.id.iv_button_next_twitlogin);
            iv_button_next_twitlogin.setTranslationX(ISConsts.progressSizes.progressDots[3] + ISConsts.progressSizes.progressDotSize);
            final View.OnClickListener onClickListenerNext = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    twitterAllow();
                }
            };

            //iv_button_skip_twitlogin
            iv_button_skip_twitlogin = (ImageView) findViewById(R.id.iv_button_skip_twitlogin);
            BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(R.drawable.ic_close);
            int bd_width = bd.getBitmap().getWidth();
            iv_button_skip_twitlogin.setTranslationX(ISConsts.progressSizes.screenWidth / 3 - ISConsts.progressSizes.progressDotSize - bd_width);
            final View.OnClickListener onClickListenerSkip = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    twitterSkip();
                }
            };

            TextView tv_twitlogin = (TextView) findViewById(R.id.tv_twitlogin);
            tv_twitlogin.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 2 * ISConsts.progressSizes.progressDotSize);

            //iv_dot_twitlogin_1
            ImageView iv_dot_twitlogin_1 = (ImageView) findViewById(R.id.iv_dot_twitlogin_1);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(ISConsts.progressSizes.progressDots[0] - ISConsts.progressSizes.progressLineSize,
                    (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                    0,
                    0);
            iv_dot_twitlogin_1.setLayoutParams(lp);
            iv_dot_twitlogin_1.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
            // Animation iv_dot_instagram_hash_tag_1
            Animation scale_up_dot1 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_up_dot1.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
            scale_up_dot1.setFillAfter(true);
            Animation scale_down_dot_1 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_down_dot_1.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
            scale_down_dot_1.setStartOffset((long) (10 + ISConsts.progressSizes.animDuration * 2 / 3));
            scale_down_dot_1.setFillAfter(true);

            AnimationSet allDot1Animation = new AnimationSet(true);
            allDot1Animation.addAnimation(scale_up_dot1);
            allDot1Animation.addAnimation(scale_down_dot_1);
            allDot1Animation.setFillAfter(true);
            iv_dot_twitlogin_1.startAnimation(allDot1Animation);

            //iv_dot_twitlogin_2
            ImageView iv_dot_twitlogin_2 = (ImageView) findViewById(R.id.iv_dot_twitlogin_2);
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp2.setMargins(ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressLineSize,
                    (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                    0,
                    0);
            iv_dot_twitlogin_2.setLayoutParams(lp2);
            iv_dot_twitlogin_2.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
            // Animation iv_dot_twitlogin_2
            Animation scale_up_dot2 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_up_dot2.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
            scale_up_dot2.setStartOffset((long) (ISConsts.progressSizes.animDuration / 3));
            scale_up_dot2.setFillAfter(true);
            Animation scale_down_dot2 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_down_dot2.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
            scale_down_dot2.setStartOffset((long) (ISConsts.progressSizes.animDuration / 3) + 10 + (long) (ISConsts.progressSizes.animDuration * 2 / 3));
            scale_down_dot2.setFillAfter(true);
            AnimationSet allDot2Animation = new AnimationSet(true);
            allDot2Animation.addAnimation(scale_up_dot2);
            allDot2Animation.addAnimation(scale_down_dot2);
            allDot2Animation.setFillAfter(true);
            iv_dot_twitlogin_2.startAnimation(allDot2Animation);

            //iv_dot_twitlogin_3
            ImageView iv_dot_twitlogin_3 = (ImageView) findViewById(R.id.iv_dot_twitlogin_3);
            RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp3.setMargins(ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressLineSize,
                    (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                    0,
                    0);
            iv_dot_twitlogin_3.setLayoutParams(lp3);
            iv_dot_twitlogin_3.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
            // Animation iv_dot_twitlogin_3
            Animation animMove_dot3 = new TranslateAnimation(0,
                    ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressDots[1], 0, 0);
            Log.d("TAG", "move third dot to " + (ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressDots[1]) + " pixels");
            animMove_dot3.setDuration(ISConsts.progressSizes.animDuration);
            animMove_dot3.setStartOffset((long) (ISConsts.progressSizes.animDuration / 3));
            animMove_dot3.setFillAfter(true);
            Animation scale_up_dot3 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                    Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressDots[1] + ISConsts.progressSizes.progressLineSize,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_up_dot3.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
            scale_up_dot3.setStartOffset(10 + (long) (ISConsts.progressSizes.animDuration / 3) + ISConsts.progressSizes.animDuration);
            scale_up_dot3.setFillAfter(true);
            Animation scale_down_dot3 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                    Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressDots[1] + ISConsts.progressSizes.progressLineSize,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_down_dot3.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
            scale_down_dot3.setStartOffset(20 + (long) (ISConsts.progressSizes.animDuration / 3) + ISConsts.progressSizes.animDuration + (long) (ISConsts.progressSizes.animDuration * 2 / 3));
            scale_down_dot3.setFillAfter(true);
            AnimationSet allDot3Animation = new AnimationSet(true);
            allDot3Animation.addAnimation(animMove_dot3);
            allDot3Animation.addAnimation(scale_up_dot3);
            allDot3Animation.addAnimation(scale_down_dot3);
            allDot3Animation.setFillAfter(true);
            allDot1Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    iv_button_next_twitlogin.setOnClickListener(onClickListenerNext);
                    iv_button_skip_twitlogin.setOnClickListener(onClickListenerSkip);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            iv_dot_twitlogin_3.startAnimation(allDot3Animation);

            //iv_bar_instagram_hash_tag
            ImageView iv_bar_twitlogin = (ImageView) findViewById(R.id.iv_bar_twitlogin);
            RelativeLayout.LayoutParams lp_bar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp_bar.setMargins(ISConsts.progressSizes.progressDots[0],
                    (int) (ISConsts.progressSizes.screenHeight / 2) - (int) (ISConsts.progressSizes.progressLineSize * 0.8),
                    0,
                    0);
            iv_bar_twitlogin.setLayoutParams(lp_bar);
            iv_bar_twitlogin.setImageBitmap(ISConsts.progressSizes.drawProgress.getProgressLine());
            // Animation iv_bar_instagram_hash_tag
            Animation scale_bar_right_1 = new ScaleAnimation(
                    1.0f,
                    (ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressDots[0]) / 2,
                    1.0f,
                    1.0f,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_bar_right_1.setDuration(5);
            scale_bar_right_1.setFillAfter(true);
            Animation scale_bar_right_2 = new ScaleAnimation(
                    1.0f,
                    2.0f,
                    1.0f,
                    1.0f,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale_bar_right_2.setDuration(ISConsts.progressSizes.animDuration);
            scale_bar_right_2.setStartOffset(5 + (long) (ISConsts.progressSizes.animDuration / 3));
            scale_bar_right_2.setFillAfter(true);
            AnimationSet barAnimation = new AnimationSet(true);
            barAnimation.addAnimation(scale_bar_right_1);
            barAnimation.addAnimation(scale_bar_right_2);
            barAnimation.setFillAfter(true);
            iv_bar_twitlogin.startAnimation(barAnimation);
//

        } else {
            startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class).putExtra("tas", getIntent().getBooleanExtra("tas", false)));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "twitter_auth", "go_to_hashtag_input", null).build());
        }

    }

    private void twitterAllow() {
        client = new TwitterAuthClient();
        client.authorize(TwitterLoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                finish();
                startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(ISConsts.prefstags.twitter_allow, true);
                editor.apply();
                easyTracker.send(MapBuilder.createEvent("auth",
                        "twitter_auth", "success", null).build());
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(TwitterLoginActivity.this, getString(R.string.twitter_auth_failure_message).concat(e.getLocalizedMessage().toString()), Toast.LENGTH_SHORT).show();
                easyTracker.send(MapBuilder.createEvent("auth",
                        "twitter_auth", "failure", null).build());
            }
        });
    }

    private void twitterSkip() {
        final AlertDialog.Builder ad;
        final Context context = TwitterLoginActivity.this;
        String title = getString(R.string.twitter_skip_question);
        String buttonYesString = getString(R.string.yes);
        String buttonNoString = getString(R.string.no);

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setPositiveButton(buttonYesString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                finish();
                startActivity(new Intent(TwitterLoginActivity.this, SliderActivity.class).putExtra("tas", getIntent().getBooleanExtra("tas", false)));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(ISConsts.prefstags.twitter_allow, false);
                editor.apply();
                easyTracker.send(MapBuilder.createEvent("auth",
                        "twitter_auth", "skip", null).build());
            }
        });
        ad.setNegativeButton(buttonNoString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        client.onActivityResult(requestCode, resultCode, data);
    }
}
