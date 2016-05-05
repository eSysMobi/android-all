package mobi.esys.upnews_tune;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.tasks.CreateDriveFolderTask;


public class DriveAuthActivity extends Activity implements View.OnClickListener {
    private transient SharedPreferences prefs;
    private transient GoogleAccountCredential credential;
    private transient static final int REQUEST_ACCOUNT_PICKER = 101;
    private transient static final int REQUEST_AUTHORIZATION = 102;
    private transient static final int REQUEST_AUTH_IF_ERROR = 103;

    private transient boolean isFirstAuth;
    private transient UNLApp mApp;
    private transient Drive drive;
    private transient String accName;

    private transient TextView mtvDriveAuthActivity;
    private transient ProgressBar mpbDriveAuthActivity;
    private transient Button gdAuthBtn;

    private transient boolean externalStorageIsAvailable = false;
    private transient boolean buttonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (UNLApp) getApplication();
        isFirstAuth = true;
        prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);

        accName = prefs.getString("accName", "");
        credential = GoogleAccountCredential.usingOAuth2(
                DriveAuthActivity.this, DriveScopes.DRIVE);

        createFolderIfNotExist();

        setContentView(R.layout.drive_auth_activity);
        mtvDriveAuthActivity = (TextView) findViewById(R.id.tvDriveAuthActivity);
        mpbDriveAuthActivity = (ProgressBar) findViewById(R.id.pbDriveAuthActivity);
        gdAuthBtn = (Button) findViewById(R.id.gdAuthBtn);

        gdAuthBtn.setOnClickListener(this);

        if (externalStorageIsAvailable) {
            if (NetWork.isNetworkAvailable(mApp)) {
                Log.d("unTag_DriveAuthActivity", "Account name: " + accName);
                if (accName.isEmpty()) {
                    //if we have not accName
                } else {
                    String txt = getString(R.string.autostart_accdrive_message_p1) + accName + getString(R.string.autostart_accdrive_message_p2);
                    mtvDriveAuthActivity.setText(txt);
                    //mtvDriveAuthActivity.setText(R.string.autostart_drive10);
                    gdAuthBtn.setText(R.string.change_profile);

                    final Handler startHandler = new Handler();
                    final Runnable loadOldAccName = new Runnable() {
                        @Override
                        public void run() {
                            if (!buttonPressed) {
                                //if we have accName then request
                                setLoadState(); //show loading screen
                                credential.setSelectedAccountName(accName);
                                drive = getDriveService(credential);
                                mApp.registerGoogle(drive);
                                createFolderInDriveIfDontExists();
                            }
                        }
                    };
                    startHandler.postDelayed(loadOldAccName, UNLConsts.START_OLD_PROFILE_DELAY);
                }
            } else {
                //in no internet connection then play saved files
                Log.d("unTag_DriveAuthActivity", "We have no inet");
                if (UNLConsts.ALLOW_TOAST) {
                    Toast.makeText(DriveAuthActivity.this, getResources().getText(R.string.no_inet), Toast.LENGTH_LONG).show();
                }
                startActivity(new Intent(DriveAuthActivity.this,
                        StartPlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                DriveAuthActivity.this.finish();
            }
        } else {
            Log.d("unTag_DriveAuthActivity", "External storage is not available");
            Toast.makeText(DriveAuthActivity.this, "External storage is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void setLoadState() {
        mtvDriveAuthActivity.setText(getResources().getText(R.string.geting_gd));
        mpbDriveAuthActivity.setVisibility(View.VISIBLE);
        gdAuthBtn.setVisibility(View.INVISIBLE);
    }

    private void setReadyToLoadState() {
        mtvDriveAuthActivity.setText(getResources().getText(R.string.add_gd));
        mpbDriveAuthActivity.setVisibility(View.GONE);
        gdAuthBtn.setVisibility(View.VISIBLE);
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
    }


    private void picker() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            setReadyToLoadState();  //hide loading spinner and show adding button
            gdAuthBtn.setText("OK");
        }
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d("accName", accountName);
                    if (accountName != null) {
                        Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.apply();

                        JSONObject props = new JSONObject();
                        try {
                            props.put("gd_account_add", "Google drive account has been added");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        credential.setSelectedAccountName(accountName);
                        drive = getDriveService(credential);
                        mApp.registerGoogle(drive);
                        if (isFirstAuth) {
                            createFolderInDriveIfDontExists();
                            isFirstAuth = false;
                        }
                    }
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    createFolderInDriveIfDontExists();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                    createFolderInDriveIfDontExists();
                }
                break;

            case REQUEST_AUTH_IF_ERROR:
                if (resultCode == Activity.RESULT_OK) {

                    String accountName = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.apply();

                        JSONObject props = new JSONObject();
                        try {
                            props.put("gd_account_add", "Google drive account has been added");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        credential.setSelectedAccountName(accountName);
                        drive = getDriveService(credential);
                        mApp.registerGoogle(drive);
                        createFolderInDriveIfDontExists();
                    } else {
                        picker();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void catchUSERException(Intent intent) {
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void createFolderInDriveIfDontExists() {
        if (!UNLApp.getIsCreatingDriveFolder()) {
            UNLApp.setIsCreatingDriveFolder(true);
            CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(DriveAuthActivity.this, true, mApp, true);
            createDriveFolderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void createFolderIfNotExist() {
        //checking availability external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageIsAvailable = true;
            UNLApp.setAppExtCachePath(Environment.getExternalStorageDirectory().getAbsolutePath());
            //checking existing all directories
            File videoDir = new File(UNLApp.getAppExtCachePath() + UNLConsts.DIR_NAME);
            if (!videoDir.exists()) {
                externalStorageIsAvailable = videoDir.mkdir();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (externalStorageIsAvailable) {
            //check inet
            if (NetWork.isNetworkAvailable(mApp)) {
                setLoadState(); //show loading screen
                buttonPressed = true;
                picker();
            } else {
                Log.d("unTag_DriveAuthActivity", "We have no inet");
                Toast.makeText(DriveAuthActivity.this, getResources().getText(R.string.no_inet), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("unTag_DriveAuthActivity", "External storage is not available");
            Toast.makeText(DriveAuthActivity.this, "External storage is not available", Toast.LENGTH_LONG).show();
        }
    }
}
