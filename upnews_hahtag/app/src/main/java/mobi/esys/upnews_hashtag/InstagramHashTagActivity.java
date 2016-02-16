package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
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

import net.londatiga.android.instagram.Instagram;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import mobi.esys.consts.ISConsts;
import mobi.esys.tasks.CheckInstaTagTask;
import mobi.esys.view.DrawProgress;


public class InstagramHashTagActivity extends Activity {
    private transient EditText hashTagEdit;
    private transient SharedPreferences preferences;
    private transient UNHApp mApp;
    private transient Instagram instagram;
    private transient EasyTracker easyTracker;
    private transient boolean isEdit = false;
    private transient boolean isFirstLaunch = true;
//    private transient ImageView iv_button_next_instagram_hash_tag;
    private transient Button enterHashTagBtn;

    private transient String prevHashTag;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        easyTracker = EasyTracker.getInstance(InstagramHashTagActivity.this);

//interface v.1
        setContentView(R.layout.activity_instagram_hash_tag);
        enterHashTagBtn = (Button) findViewById(R.id.enterHashTagBtn);
        final View.OnClickListener onClickListenerNext = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTagAndGo(false);
            }
        };
        enterHashTagBtn.setOnClickListener(onClickListenerNext);


////interface v.2
//
//        setContentView(R.layout.activity_instagram_hash_tag_2);
//        if (ISConsts.progressSizes.drawProgress == null) {
//            ISConsts.progressSizes.drawProgress = new DrawProgress(getResources().getColor(R.color.deep_gray), getResources().getColor(R.color.progress_color));
//        }
//
//        RelativeLayout rl_instagram_hash_tag = (RelativeLayout) findViewById(R.id.rl_instagram_hash_tag);
//
//        if (Build.VERSION.SDK_INT < 16) {
//            rl_instagram_hash_tag.setBackgroundDrawable(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
//        } else {
//            rl_instagram_hash_tag.setBackground(new BitmapDrawable(getResources(), ISConsts.progressSizes.drawProgress.getScreenBackground()));
//        }
//
//        //iv_button_next_instagram_hash_tag
//        iv_button_next_instagram_hash_tag = (ImageView) findViewById(R.id.iv_button_next_instagram_hash_tag);
//        iv_button_next_instagram_hash_tag.setTranslationX(ISConsts.progressSizes.progressDots[3] + ISConsts.progressSizes.progressDotSize);
//        final View.OnClickListener onClickListenerNext = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkTagAndGo(false);
//            }
//        };
//
//        TextView tv_instagram_hash_tag = (TextView) findViewById(R.id.tv_instagram_hash_tag);
//        tv_instagram_hash_tag.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 2 * ISConsts.progressSizes.progressDotSize);
//
//        LinearLayout ll_instagram_hash_tag = (LinearLayout) findViewById(R.id.ll_instagram_hash_tag);
//        ll_instagram_hash_tag.setTranslationY((int) (ISConsts.progressSizes.screenHeight / 2) + 3 * ISConsts.progressSizes.progressDotSize);
//
//        //iv_dot_instagram_hash_tag_1
//        ImageView iv_dot_instagram_hash_tag_1 = (ImageView) findViewById(R.id.iv_dot_instagram_hash_tag_1);
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lp.setMargins(ISConsts.progressSizes.progressDots[0] - ISConsts.progressSizes.progressLineSize,
//                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
//                0,
//                0);
//        iv_dot_instagram_hash_tag_1.setLayoutParams(lp);
//        iv_dot_instagram_hash_tag_1.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
//        // Animation iv_dot_instagram_hash_tag_1
//        Animation scale_up = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scale_up.setDuration((long) (ISConsts.progressSizes.animDuration*2/3));
//        scale_up.setFillAfter(true);
//        Animation scale_down = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scale_down.setDuration((long) (ISConsts.progressSizes.animDuration/3));
//        scale_down.setStartOffset((long) (10+ISConsts.progressSizes.animDuration*2/3));
//        scale_down.setFillAfter(true);
//        AnimationSet allDot1Animation = new AnimationSet(true);
//        allDot1Animation.addAnimation(scale_up);
//        allDot1Animation.addAnimation(scale_down);
//        allDot1Animation.setFillAfter(true);
//        iv_dot_instagram_hash_tag_1.startAnimation(allDot1Animation);
//
//        //iv_dot_instagram_hash_tag_2
//        ImageView iv_dot_instagram_hash_tag_2 = (ImageView) findViewById(R.id.iv_dot_instagram_hash_tag_2);
//        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lp2.setMargins(ISConsts.progressSizes.progressDots[0] - ISConsts.progressSizes.progressLineSize,
//                (int) (ISConsts.progressSizes.screenHeight / 2) - ISConsts.progressSizes.progressLineSize,
//                0,
//                0);
//        iv_dot_instagram_hash_tag_2.setLayoutParams(lp2);
//        iv_dot_instagram_hash_tag_2.setImageBitmap(ISConsts.progressSizes.drawProgress.getLittleDotProgress());
//        // Animation iv_dot_instagram_hash_tag_2
//        Animation animMove = new TranslateAnimation(0,
//                ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressDots[0], 0, 0);
//        Log.d("TAG", "move second dot to " + (ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressDots[0]) + " pixels");
//        animMove.setDuration(ISConsts.progressSizes.animDuration);
//        animMove.setFillAfter(true);
//        Animation scale_up_delayed = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f,
//                Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressDots[0] + ISConsts.progressSizes.progressLineSize,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scale_up_delayed.setDuration((long) (ISConsts.progressSizes.animDuration * 2 / 3));
//        scale_up_delayed.setStartOffset(10 + ISConsts.progressSizes.animDuration);
//        scale_up_delayed.setFillAfter(true);
//        Animation scale_down_delayed = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
//                Animation.ABSOLUTE, ISConsts.progressSizes.progressDots[1] - ISConsts.progressSizes.progressDots[0] + ISConsts.progressSizes.progressLineSize,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scale_down_delayed.setDuration((long) (ISConsts.progressSizes.animDuration / 3));
//        scale_down_delayed.setStartOffset(20 + ISConsts.progressSizes.animDuration + (long) (ISConsts.progressSizes.animDuration * 2 / 3));
//        scale_down_delayed.setFillAfter(true);
//        AnimationSet allDot2Animation = new AnimationSet(true);
//        allDot2Animation.addAnimation(animMove);
//        allDot2Animation.addAnimation(scale_up_delayed);
//        allDot2Animation.addAnimation(scale_down_delayed);
//        allDot2Animation.setFillAfter(true);
//        allDot2Animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                iv_button_next_instagram_hash_tag.setOnClickListener(onClickListenerNext);
//            }
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
//        iv_dot_instagram_hash_tag_2.startAnimation(allDot2Animation);
//
//        //iv_bar_instagram_hash_tag
//        ImageView iv_bar_instagram_hash_tag = (ImageView) findViewById(R.id.iv_bar_instagram_hash_tag);
//        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lp3.setMargins(ISConsts.progressSizes.progressDots[0],
//                (int) (ISConsts.progressSizes.screenHeight / 2) - (int)(ISConsts.progressSizes.progressLineSize*0.8),
//                0,
//                0);
//        iv_bar_instagram_hash_tag.setLayoutParams(lp3);
//        iv_bar_instagram_hash_tag.setImageBitmap(ISConsts.progressSizes.drawProgress.getProgressLine());
//        // Animation iv_bar_instagram_hash_tag
//        Animation scale_right = new ScaleAnimation(
//                1.0f,
//                (ISConsts.progressSizes.progressDots[1]-ISConsts.progressSizes.progressDots[0])/2,
//                1.0f,
//                1.0f,
//                Animation.RELATIVE_TO_SELF, 0,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scale_right.setDuration(ISConsts.progressSizes.animDuration);
//        scale_right.setFillAfter(true);
//        iv_bar_instagram_hash_tag.startAnimation(scale_right);
////

        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);

        mApp = (UNHApp) getApplicationContext();

        instagram = new Instagram(InstagramHashTagActivity.this,
                ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret,
                ISConsts.instagramconsts.instagram_redirect_uri);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        prevHashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, "");
        if (!prevHashTag.isEmpty()) {
            hashTagEdit.setText(prevHashTag);
            isFirstLaunch = false;
        } else {
            isFirstLaunch = true;
        }

        final Handler startHandler = new Handler();
        final Runnable twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isEdit && !isFirstLaunch) {
                    checkTagAndGo(true);
                }
            }
        };

        startHandler.postDelayed(twitterFeedRunnable, 10000);


        if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
            hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
        } else {
            hashTagEdit.setSelection(1);
        }

        hashTagEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                isEdit = true;
                startHandler.removeCallbacks(twitterFeedRunnable);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().startsWith("#")) {
                    String unSpaceStr = ("#" + s.toString()).replaceAll(" ",
                            "");
                    hashTagEdit.setText(unSpaceStr);
                }

                if (s.toString().length() == 1) {
                    hashTagEdit.setSelection(1);
                }
                isEdit = false;
            }

        });

        hashTagEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;

                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                    checkTagAndGo(false);
                    handled = true;
                }

                return handled;
            }
        });


    }

    public void checkTagAndGo(boolean twitterAutoStart) {
        isEdit = true;
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            Log.d("hash tag", prevHashTag);
            Log.d("hash tag", hashTagEdit.getEditableText().toString());

            if (!"".equals(prevHashTag) && !hashTagEdit.getEditableText().toString().equals(prevHashTag)) {
                clearFolder();
            }
            CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(hashTagEdit.getEditableText().toString(), mApp);
            checkInstaTagTask.execute(instagram.getSession().getAccessToken());
            try {
                if (checkInstaTagTask.get()) {
                    startActivity(new Intent(InstagramHashTagActivity.this, TwitterLoginActivity.class).putExtra("tas", twitterAutoStart));
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(ISConsts.prefstags.instagram_hashtag, hashTagEdit.getEditableText().toString());
                    editor.apply();
                    easyTracker.send(MapBuilder.createEvent("input_hashtag",
                            "instagram_input_hashtag", hashTagEdit.getEditableText().toString(), null).build());
                } else {
                    Toast.makeText(InstagramHashTagActivity.this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();
                    easyTracker.send(MapBuilder.createEvent("input_hashtag",
                            "instagram_input_hashtag", "hashtag_dont_allowed", null).build());

                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(InstagramHashTagActivity.this, getString(R.string.instagram_hashtag_required_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearFolder() {
        File[] igPhotosFileList = new File(Environment.getExternalStorageDirectory().
                getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name))
                .listFiles();
        Log.d("photo folder", Arrays.asList(igPhotosFileList).toString());

        for (File photoFile : igPhotosFileList) {
            photoFile.delete();
        }
    }
}
