package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import mobi.esys.consts.ISConsts;
import mobi.esys.view.DrawProgress;


public class TweeterHashTagActivity extends Activity{
    private transient EditText hashTagEdit;
    private transient SharedPreferences preferences;
    private transient EasyTracker easyTracker;
    private transient ImageView iv_button_next_twitter_hash_tag;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_twitter_hashtag_2);

        //interface v.2
        if (ISConsts.progressSizes.drawProgress == null) {
            ISConsts.progressSizes.drawProgress = new DrawProgress(getResources().getColor(R.color.deep_gray), getResources().getColor(R.color.progress_color));
        }

        RelativeLayout rl_twitter_hash_tag = (RelativeLayout) findViewById(R.id.rl_twitter_hash_tag);

        if (Build.VERSION.SDK_INT < 16) {
            rl_twitter_hash_tag.setBackgroundDrawable(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
        } else {
            rl_twitter_hash_tag.setBackground(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
        }

        //iv_button_next_twitter_hash_tag
        iv_button_next_twitter_hash_tag = (ImageView) findViewById(R.id.iv_button_next_twitter_hash_tag);
        iv_button_next_twitter_hash_tag.setTranslationX(ISConsts.progressSizes.progressDots[3] + ISConsts.progressSizes.progressDotSize);
        final View.OnClickListener onClickListenerNext = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTagAndGo();
            }
        };

        TextView tv_twitter_hash_tag = (TextView) findViewById(R.id.tv_twitter_hash_tag);
        tv_twitter_hash_tag.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 2 * ISConsts.progressSizes.progressDotSize);

        LinearLayout ll_twitter_hash_tag = (LinearLayout) findViewById(R.id.ll_twitter_hash_tag);
        ll_twitter_hash_tag.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 3 * ISConsts.progressSizes.progressDotSize);

        //iv_dot_twitter_hash_tag_1
        ImageView iv_dot_twitter_hash_tag_1 = (ImageView) findViewById(R.id.iv_dot_twitter_hash_tag_1);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(ISConsts.progressSizes.progressDots[0] - ISConsts.progressSizes.progressLineSize,
                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                0,
                0);
        iv_dot_twitter_hash_tag_1.setLayoutParams(lp);
        iv_dot_twitter_hash_tag_1.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
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
        iv_dot_twitter_hash_tag_1.startAnimation(allDot1Animation);

        //iv_dot_twitter_hash_tag_2
        ImageView iv_dot_twitter_hash_tag_2 = (ImageView) findViewById(R.id.iv_dot_twitter_hash_tag_2);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressLineSize,
                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                0,
                0);
        iv_dot_twitter_hash_tag_2.setLayoutParams(lp2);
        iv_dot_twitter_hash_tag_2.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
        // Animation iv_dot_twitter_hash_tag_2
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
        iv_dot_twitter_hash_tag_2.startAnimation(allDot2Animation);

        //iv_dot_twitter_hash_tag_3
        ImageView iv_dot_twitter_hash_tag_3 = (ImageView) findViewById(R.id.iv_dot_twitter_hash_tag_3);
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp3.setMargins(ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressLineSize,
                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                0,
                0);
        iv_dot_twitter_hash_tag_3.setLayoutParams(lp3);
        iv_dot_twitter_hash_tag_3.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
        // Animation iv_dot_twitter_hash_tag_3
        Animation scale_up_dot3 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale_up_dot3.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
        scale_up_dot3.setStartOffset(2 * (long) (ISConsts.progressSizes.animDuration / 3));
        scale_up_dot3.setFillAfter(true);
        Animation scale_down_dot3 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale_down_dot3.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
        scale_down_dot3.setStartOffset(10 + 2 * ((long) (ISConsts.progressSizes.animDuration / 3)) + (long) (ISConsts.progressSizes.animDuration * 2 / 3));
        scale_down_dot3.setFillAfter(true);
        AnimationSet allDot3Animation = new AnimationSet(true);
        allDot3Animation.addAnimation(scale_up_dot3);
        allDot3Animation.addAnimation(scale_down_dot3);
        allDot3Animation.setFillAfter(true);
        iv_dot_twitter_hash_tag_3.startAnimation(allDot3Animation);

        //iv_dot_twitter_hash_tag_4
        ImageView iv_dot_twitter_hash_tag_4 = (ImageView) findViewById(R.id.iv_dot_twitter_hash_tag_4);
        RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp4.setMargins(ISConsts.progressSizes.progressDots[2] - ISConsts.progressSizes.progressLineSize,
                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
                0,
                0);
        iv_dot_twitter_hash_tag_4.setLayoutParams(lp4);
        iv_dot_twitter_hash_tag_4.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
        // Animation iv_dot_twitter_hash_tag_4
        Animation animMove_dot4 = new TranslateAnimation(0,
                ISConsts.progressSizes.progressDots[3] - ISConsts.progressSizes.progressDots[2], 0, 0);
        Log.d("TAG", "move third dot to " + (ISConsts.progressSizes.progressDots[3] - ISConsts.progressSizes.progressDots[2]) + " pixels");
        animMove_dot4.setDuration(ISConsts.progressSizes.animDuration);
        animMove_dot4.setStartOffset(2 * (long) (ISConsts.progressSizes.animDuration / 3));
        animMove_dot4.setFillAfter(true);
        Animation scale_up_dot4 = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
                Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[3] - ISConsts.progressSizes.progressDots[2] + ISConsts.progressSizes.progressLineSize,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale_up_dot4.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
        scale_up_dot4.setStartOffset(10 + 2 * (long) (ISConsts.progressSizes.animDuration / 3) + ISConsts.progressSizes.animDuration);
        scale_up_dot4.setFillAfter(true);
        Animation scale_down_dot4 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[3] - ISConsts.progressSizes.progressDots[2] + ISConsts.progressSizes.progressLineSize,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale_down_dot4.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
        scale_down_dot4.setStartOffset(20 + 2 * (long) (ISConsts.progressSizes.animDuration / 3) + ISConsts.progressSizes.animDuration + (long) (ISConsts.progressSizes.animDuration * 2 / 3));
        scale_down_dot4.setFillAfter(true);
        AnimationSet allDot4Animation = new AnimationSet(true);
        allDot4Animation.addAnimation(animMove_dot4);
        allDot4Animation.addAnimation(scale_up_dot4);
        allDot4Animation.addAnimation(scale_down_dot4);
        allDot4Animation.setFillAfter(true);
        allDot4Animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                iv_button_next_twitter_hash_tag.setOnClickListener(onClickListenerNext);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        iv_dot_twitter_hash_tag_4.startAnimation(allDot4Animation);

        //iv_bar_instagram_hash_tag
        ImageView iv_bar_twitter_hash_tag = (ImageView) findViewById(R.id.iv_bar_twitter_hash_tag);
        RelativeLayout.LayoutParams lp_bar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_bar.setMargins(ISConsts.progressSizes.progressDots[0],
                (int) (ISConsts.progressSizes.screenHeight / 2) - (int) (ISConsts.progressSizes.progressLineSize * 0.8),
                0,
                0);
        iv_bar_twitter_hash_tag.setLayoutParams(lp_bar);
        iv_bar_twitter_hash_tag.setImageBitmap(ISConsts.progressSizes.drawProgress.getProgressLine());
        // Animation iv_bar_twitter_hash_tag
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
        scale_bar_right_2.setDuration(5);
        scale_bar_right_2.setStartOffset(5);
        scale_bar_right_2.setFillAfter(true);
        Animation scale_bar_right_3 = new ScaleAnimation(
                1.0f,
                1.5f,
                1.0f,
                1.0f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale_bar_right_3.setDuration(ISConsts.progressSizes.animDuration);
        scale_bar_right_3.setStartOffset(10 + 2 * (long) (ISConsts.progressSizes.animDuration / 3));
        scale_bar_right_3.setFillAfter(true);
        AnimationSet barAnimation = new AnimationSet(true);
        barAnimation.addAnimation(scale_bar_right_1);
        barAnimation.addAnimation(scale_bar_right_2);
        barAnimation.addAnimation(scale_bar_right_3);
        barAnimation.setFillAfter(true);
        iv_bar_twitter_hash_tag.startAnimation(barAnimation);
//

        easyTracker = EasyTracker.getInstance(TweeterHashTagActivity.this);

        hashTagEdit = (EditText) findViewById(R.id.twHashTagEdit);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);
        String hashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, "");
        if (!hashTag.isEmpty()) {
            hashTagEdit.setText(hashTag);
        }

        if (!getIntent().getBooleanExtra("tas", false)) {
            if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
                hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
            } else {
                hashTagEdit.setSelection(1);
            }

            hashTagEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (!s.toString().startsWith("#")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("#").append(s.toString());
                        String unSpaceStr = sb.toString().replaceAll(" ",
                                "");
                        hashTagEdit.setText(unSpaceStr);
                    }

                    if (s.toString().length() == 1) {
                        hashTagEdit.setSelection(1);
                    }

                }
            });
            hashTagEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                    boolean handled = false;
                    if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                        checkTagAndGo();
                        handled = true;
                    }

                    return handled;
                }
            });

        } else {
            checkTagAndGo();
        }
    }

    public void checkTagAndGo() {
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            startActivity(new Intent(TweeterHashTagActivity.this, SliderActivity.class));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ISConsts.prefstags.twitter_hashtag, hashTagEdit.getEditableText().toString());
            editor.apply();
            easyTracker.send(MapBuilder.createEvent("input_hashtag",
                    "twitter_input_hashtag", hashTagEdit.getEditableText().toString(), null).build());
        } else {
            Toast.makeText(TweeterHashTagActivity.this, getString(R.string.twitter_hashtag_required_message), Toast.LENGTH_SHORT).show();
        }
    }
}

