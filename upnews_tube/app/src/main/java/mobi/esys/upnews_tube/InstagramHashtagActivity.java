package mobi.esys.upnews_tube;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.List;

import mobi.esys.upnews_tube.constants.DevelopersKeys;
import mobi.esys.upnews_tube.constants.Folders;
import mobi.esys.upnews_tube.constants.OtherConst;
import mobi.esys.upnews_tube.eventbus.EventIgCheckingComplete;
import mobi.esys.upnews_tube.instagram.CheckInstaTagTask;
import mobi.esys.upnews_tube.instagram.InstagramItem;

public class InstagramHashtagActivity extends Activity {

    private transient EditText hashTagEdit;
    private transient Button enterHashBtn;
    private transient Button igSkipBtn;
    private transient ProgressBar instHashtagPb;

    private transient SharedPreferences preferences;
    private transient String prevHashtag;
    private transient Instagram instagram;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fragment_instagramhashtag);

        instagram = new Instagram(this, DevelopersKeys.INSTAGRAM_CLIENT_ID,
                DevelopersKeys.INSTAGRAM_CLIENT_SECRET,
                DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        instHashtagPb = (ProgressBar) findViewById(R.id.instHashtagPb);
        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        hashTagEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        preferences = getSharedPreferences(OtherConst.APP_PREF, MODE_PRIVATE);

        //Button enter hashtag
        enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);
        enterHashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTagAndGo();
            }
        });

        //Button skip
        igSkipBtn = (Button) findViewById(R.id.igSkipBtn);
        igSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(OtherConst.APP_PREF_SKIP_INSTAGRAM, false);
                editor.apply();
                startActivity(new Intent(InstagramHashtagActivity.this, TwitterLoginActivity.class));
                finish();
            }
        });

        //EditText
        prevHashtag = preferences.getString("instHashTag", "");
        if (!prevHashtag.isEmpty()) {
            String text = "#".concat(prevHashtag);
            hashTagEdit.setText(text);
        }

        if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
            hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
        } else {
            hashTagEdit.setSelection(1);
        }

        hashTagEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
            }
        });

        hashTagEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    checkTagAndGo();
                    handled = true;
                }
                return handled;
            }
        });
    }

    public void checkTagAndGo() {
        if (!hashTagEdit.getEditableText().toString().isEmpty() && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            lockUI();
            CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(hashTagEdit.getEditableText().toString().substring(1), false);
            checkInstaTagTask.execute(instagram.getSession().getAccessToken());
        } else {
            Toast.makeText(this, "Input Instagram hashtag", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onEvent(EventIgCheckingComplete event) {
        unLockUI();
        List<InstagramItem> result = event.getIgPhotos();
        if (result.size() > 0) {
            String hashtag = hashTagEdit.getEditableText().toString().replace("#", "");
            Log.d("unTag_InstaHashtag", "curr tag = " + hashtag);
            Log.d("unTag_InstaHashtag", "prev tag = " + prevHashtag);
            if (!"".equals(prevHashtag) && !hashtag.equals(prevHashtag)) {
                clearFolder();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("instHashTag", hashtag);
            editor.putBoolean(OtherConst.APP_PREF_SKIP_INSTAGRAM, true);
            editor.apply();
            startActivity(new Intent(InstagramHashtagActivity.this,
                    TwitterLoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();

        }
    }

    public void clearFolder() {
        File tmpFolder = new File(Folders.SD_CARD
                .concat(File.separator)
                .concat(Folders.BASE_FOLDER)
                .concat(File.separator)
                .concat(Folders.PHOTO_FOLDER));

        if (tmpFolder.exists()) {
            File[] igPhotosFileList = tmpFolder.listFiles();
            Log.d("photo folder", Arrays.asList(igPhotosFileList).toString());

            for (File photoFile : igPhotosFileList) {
                photoFile.delete();
            }
        } else {
            tmpFolder.mkdir();
        }
    }

    private void lockUI() {
        hashTagEdit.setEnabled(false);
        igSkipBtn.setEnabled(false);
        enterHashBtn.setEnabled(false);
        enterHashBtn.setVisibility(View.GONE);
        instHashtagPb.setVisibility(View.VISIBLE);
    }

    private void unLockUI() {
        hashTagEdit.setEnabled(true);
        igSkipBtn.setEnabled(true);
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