package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.instagram.Instagram;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Arrays;

import mobi.esys.consts.ISConsts;
import mobi.esys.eventbus.EventGetCountTagsIGComplete;
import mobi.esys.filesystem.IOHelper;
import mobi.esys.tasks.GetCountTagIGTask;


public class InstagramHashTagActivity extends Activity {
    private transient EditText hashTagEdit;
    private transient Button enterHashBtn;
    private transient ProgressBar instHashtagPb;

    private transient UpnewsApp mApp;
    private transient Instagram instagram;

    private transient SharedPreferences preferences;
    private transient String prevHashtag;
    private transient boolean isEdit = false;
    private transient boolean isFirstLaunch = true;
    private transient boolean twitterAutoStart = false;

    private static final int MIN_EDITABLE_LENGTH = 2;

    //autostart
    private final Handler startHandler = new Handler();
    private Runnable twitterFeedRunnable = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_instagram_hash_tag);

        mApp = (UpnewsApp) getApplicationContext();
        instagram = new Instagram(this, ISConsts.instagramconsts.INSTAGRAM_CLIENT_ID,
                ISConsts.instagramconsts.INSTAGRAM_CLIENT_SECRET,
                ISConsts.instagramconsts.INSTAGRAM_REDIRECT_URI);

        instHashtagPb = (ProgressBar) findViewById(R.id.instHashtagPb);
        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        hashTagEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        //Button enter hashtag
        enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);
        enterHashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterAutoStart = false;
                checkTagAndGo();
            }
        });

        //EditText
        prevHashtag = preferences.getString(ISConsts.prefstags.instagram_hashtag, "");
        if (!prevHashtag.isEmpty()) {
            hashTagEdit.setText('#' + prevHashtag);
            isFirstLaunch = false;
        } else {
            isFirstLaunch = true;
        }

        twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isEdit && !isFirstLaunch) {
                    twitterAutoStart = true;
                    checkTagAndGo();
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isEdit = true;
                startHandler.removeCallbacks(twitterFeedRunnable);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    twitterAutoStart = false;
                    checkTagAndGo();
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (startHandler != null && twitterFeedRunnable != null) {
            startHandler.removeCallbacks(twitterFeedRunnable);
        }
    }

    public void checkTagAndGo() {
        isEdit = true;
        if (!hashTagEdit.getEditableText().toString().isEmpty() && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            lockUI();
            GetCountTagIGTask getCountTagIGTask = new GetCountTagIGTask(hashTagEdit.getEditableText().toString().substring(1), instagram.getSession().getAccessToken());
            getCountTagIGTask.execute();
        } else {
            Toast.makeText(this, "Input Instagram hashtag", Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe
    public void onEvent(EventGetCountTagsIGComplete event) {
        int result = event.getPhotoCount();
        if (result > 0) {
            String hashtag = hashTagEdit.getEditableText().toString().replace("#", "");
            Log.d("unTag_InstaHashtag", "Current tag = " + hashtag);
            Log.d("unTag_InstaHashtag", "Previous tag = " + prevHashtag);
            if (!hashtag.equals(prevHashtag)) {
                IOHelper.clearPhotoDir();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(ISConsts.prefstags.instagram_hashtag, hashTagEdit.getEditableText().toString().substring(1));
                editor.apply();
            }
            startActivity(new Intent(InstagramHashTagActivity.this, TwitterLoginActivity.class).putExtra("tas", twitterAutoStart));
            finish();
        } else {
            Toast.makeText(this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();
            unLockUI();
        }
    }


    private void lockUI() {
        hashTagEdit.setEnabled(false);
        enterHashBtn.setEnabled(false);
        enterHashBtn.setVisibility(View.GONE);
        instHashtagPb.setVisibility(View.VISIBLE);
    }

    private void unLockUI() {
        hashTagEdit.setEnabled(true);
        enterHashBtn.setEnabled(true);
        instHashtagPb.setVisibility(View.GONE);
        enterHashBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
