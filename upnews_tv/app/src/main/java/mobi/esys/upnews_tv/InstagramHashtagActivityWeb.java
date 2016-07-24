package mobi.esys.upnews_tv;

/**
 * Created by ZeyUzh on 24.07.2016.
 */

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
import android.widget.TextView;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Arrays;

import mobi.esys.upnews_tv.constants.Folders;
import mobi.esys.upnews_tv.eventbus.EventIgCheckingComplete;
import mobi.esys.upnews_tv.instagram.CheckInstaTagTaskWeb;

public class InstagramHashtagActivityWeb extends Activity {
    private transient UpnewsOnlineApp mApp;
    private transient EditText hashTagEdit;
    private transient SharedPreferences preferences;
    private transient String prevHashtag;
    private boolean isChecking;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fragment_instagramhashtag);

        mApp = (UpnewsOnlineApp) getApplicationContext();

        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        hashTagEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        Button enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);
        Button igSkipBtn = (Button) findViewById(R.id.instHashTagSkip);

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);
        prevHashtag = preferences.getString("instHashTag", "");
        if (!prevHashtag.isEmpty()) {
            hashTagEdit.setText("#" + prevHashtag);
        }

        if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
            hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
        } else {
            hashTagEdit.setSelection(1);
        }

        enterHashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTagAndGo();
            }
        });

        enterHashBtn.requestFocus();
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
                    String unSpaceStr = ("#" + s.toString()).replaceAll(" ",
                            "");
                    hashTagEdit.setText(unSpaceStr);
                }

                if (s.toString().length() == 1) {
                    hashTagEdit.setSelection(1);
                }

            }
        });

        igSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("instNeedShow", false);
                editor.apply();
                startActivity(new Intent(InstagramHashtagActivityWeb.this, TwitterLoginActivity.class));
                finish();
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
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            if (!isChecking) {
                CheckInstaTagTaskWeb checkInstaTagTask = new CheckInstaTagTaskWeb(hashTagEdit.getEditableText().toString().substring(1));
                checkInstaTagTask.execute();
                isChecking = true;
            } else {
                Log.d("unTag_IgHashtagAct", "Checking is running");
            }
        } else {
            Toast.makeText(this, "Input Instagram hashtag", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onEvent(EventIgCheckingComplete event) {
        isChecking = false;
        String result = event.getUrls();
        if (!result.isEmpty()) {
            String hashtag = hashTagEdit.getEditableText().toString().replace("#", "");
            Log.d("unTag_InstaHashtag", "curr tag = " + hashtag);
            Log.d("unTag_InstaHashtag", "prev tag = " + prevHashtag);
            if (!"".equals(prevHashtag) && !hashtag.equals(prevHashtag)) {
                clearFolder();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("instHashTag", hashtag);
            editor.putBoolean("needShowInstagram", true);
            editor.apply();
            startActivity(new Intent(InstagramHashtagActivityWeb.this,
                    TwitterLoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();

        }
    }

    public void clearFolder() {
        File tmpFolder = new File(Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.PHOTO_FOLDER));

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