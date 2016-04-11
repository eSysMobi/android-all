package mobi.esys.upnews_tv;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;

import mobi.esys.upnews_tv.constants.Folders;

public class FacebookGroupActivity extends Activity {
    private transient SharedPreferences preferences;
    private transient EditText fbGroupIDEdit;
    private transient String prevGroupID;
    private transient boolean isEdit = false;
    private transient boolean isFirstLaunch = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_facebookgroup);

        EasyTracker easyTracker = EasyTracker.getInstance(FacebookGroupActivity.this);
        easyTracker.send(MapBuilder.createEvent("auth",
                "start_app", "start_app at input facebook group id", null).build());


        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        prevGroupID = preferences.getString("fbGroupID", "");

        fbGroupIDEdit = (EditText) findViewById(R.id.fbGroupEdit);
        fbGroupIDEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        if (!prevGroupID.isEmpty()) {
            isFirstLaunch = false;
            fbGroupIDEdit.setText(prevGroupID);
        }

        Button fbGroupIDButtonEdit = (Button) findViewById(R.id.enterfbGroupBtn);

        fbGroupIDEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                isEdit = true;
                Log.d("is edit otc", String.valueOf(isEdit));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        fbGroupIDEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;

                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    goNext();
                    handled = true;
                }
                return handled;
            }
        });

        fbGroupIDButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        final Handler startHandler = new Handler();
        Runnable twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("is edit go", String.valueOf(isEdit));
                Log.d("is launch", String.valueOf(isFirstLaunch));
                if (!isEdit && !isFirstLaunch) {
                    startActivity(new Intent(FacebookGroupActivity.this, PlayerActivity.class));
                    finish();
                }
            }
        };

        startHandler.postDelayed(twitterFeedRunnable, 10000);
    }

    private void goNext() {
        isEdit = true;
        if (!fbGroupIDEdit.getText().toString().isEmpty() && !fbGroupIDEdit.getText().toString().equals("")) {
            checkAndGo(fbGroupIDEdit.getText().toString());
        } else {
            Toast.makeText(this, "Input facebook group id", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGroupID(String groupID) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("fbGroupID", groupID);
        editor.apply();
    }

    private void checkAndGo(String groupID) {
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + groupID + "/videos",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d("resp", response.toString());
                        if (!response.toString().contains("responseCode: 404")) {
                            if (!fbGroupIDEdit.getText().toString().equals(prevGroupID)) {
                                clearFolder();
                            }
                            saveGroupID(fbGroupIDEdit.getText().toString());
                            startActivity(new Intent(FacebookGroupActivity.this, InstagramLoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(FacebookGroupActivity.this, "This group id don't existed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,description,source");

        request.setParameters(parameters);
        request.executeAsync();
    }

    public void clearFolder() {
        File tmpFolder = new File(Folders.SD_CARD.
                concat(File.separator).
                concat(Folders.BASE_FOLDER).
                concat(File.separator).concat(Folders.VIDEO_FOLDER));

        if (tmpFolder.exists()) {
            File[] igPhotosFileList = tmpFolder.listFiles();

            for (File photoFile : igPhotosFileList) {
                photoFile.delete();
            }
        } else {
            tmpFolder.mkdir();
        }
    }
}
