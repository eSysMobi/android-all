package mobi.esys.upnews_tune;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.net.NetWork;
import mobi.esys.tasks.CreateDriveFolderTask;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DriveAuthActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    private transient UNLApp mApp;

    private transient TextView mtvDriveAuthActivity;
    private transient ProgressBar mpbDriveAuthActivity;
    private transient Button gdAuthBtn;

    private transient boolean externalStorageIsAvailable = false;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE};

    //autostart
    private transient final Handler startHandler = new Handler();
    private transient Runnable loadOldAccName = null;

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drive_auth_activity);
        mtvDriveAuthActivity = (TextView) findViewById(R.id.tvDriveAuthActivity);
        mpbDriveAuthActivity = (ProgressBar) findViewById(R.id.pbDriveAuthActivity);
        gdAuthBtn = (Button) findViewById(R.id.gdAuthBtn);

        gdAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoadState(); //show loading screen
                getResultsFromApi();
                if (loadOldAccName != null && startHandler != null) {
                    startHandler.removeCallbacks(loadOldAccName);
                }
            }
        });

        mApp = (UNLApp) getApplication();
        UNLApp.setmApp(mApp);

        createFolderIfNotExist();

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        if (externalStorageIsAvailable) {
            //check inet for set/change acc in GD
            if (NetWork.isNetworkAvailable(mApp)) {
                String accountName = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, "");
                if (!accountName.isEmpty()) {
                    gdAuthBtn.setText(getResources().getText(R.string.change_profile));
                    String txt = getString(R.string.autostart_accdrive_message_p1) + accountName + getString(R.string.autostart_accdrive_message_p2);
                    mtvDriveAuthActivity.setText(txt);
                    gdAuthBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setLoadState(); //show loading screen
                            chooseAccount(true);
                            if (loadOldAccName != null && startHandler != null) {
                                startHandler.removeCallbacks(loadOldAccName);
                            }
                        }
                    });
                    loadOldAccName = new Runnable() {
                        @Override
                        public void run() {
                            //if we have accName then request
                            setLoadState(); //show loading screen
                            getResultsFromApi();
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


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            setReadyToLoadState();
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(false);
        } else {
            createFolderInDriveIfDontExists(mCredential);
        }
    }


    private void createFolderInDriveIfDontExists(GoogleAccountCredential credential) {
        if (!UNLApp.getIsCreatingDriveFolder()) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            Drive mService = new Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("upnews | TUNE")
                    .build();
            mApp.registerGoogle(mService);
            UNLApp.setIsCreatingDriveFolder(true);
            CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(DriveAuthActivity.this, true, mApp, true);
            createDriveFolderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount(boolean needChangeAcc) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, "");
            if (!accountName.isEmpty() && needChangeAcc == false) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    String txtWarn = "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.";
                    Log.d("unTag_DriveAuthActivity", txtWarn);
                    Toast.makeText(DriveAuthActivity.this, txtWarn, Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                if(resultCode == RESULT_CANCELED){
                    setReadyToLoadState();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

//    /**
//     * Checks whether the device currently has a network connection.
//     *
//     * @return true if the device has a network connection, false otherwise.
//     */
//    private boolean isDeviceOnline() {
//        ConnectivityManager connMgr =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        return (networkInfo != null && networkInfo.isConnected());
//    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DriveAuthActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

//    /**
//     * An asynchronous task that handles the Drive API call.
//     * Placing the API calls in their own task ensures the UI stays responsive.
//     */
//    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
//        private com.google.api.services.drive.Drive mService = null;
//        private Exception mLastError = null;
//
//        public MakeRequestTask(GoogleAccountCredential credential) {
//            HttpTransport transport = AndroidHttp.newCompatibleTransport();
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//            mService = new com.google.api.services.drive.Drive.Builder(
//                    transport, jsonFactory, credential)
//                    .setApplicationName("upnews | TUNE")
//                    .build();
//        }
//
//        /**
//         * Background task to call Drive API.
//         *
//         * @param params no parameters needed for this task.
//         */
//        @Override
//        protected List<String> doInBackground(Void... params) {
//            try {
//                return getDataFromApi();
//            } catch (Exception e) {
//                mLastError = e;
//                cancel(true);
//                return null;
//            }
//        }
//
//        /**
//         * Fetch a list of up to 10 file names and IDs.
//         *
//         * @return List of Strings describing files, or an empty list if no files
//         * found.
//         * @throws IOException
//         */
//        private List<String> getDataFromApi() throws IOException {
//            // Get a list of up to 10 files.
//            List<String> fileInfo = new ArrayList<String>();
//            FileList result = mService.files().list()
//                    .setQ(UNLConsts.GD_FOLDER_QUERY)
//                            //.setPageSize(10)
//                            //.setFields("nextPageToken, items(id, name)")
//                    .execute();
//            List<File> files = result.getFiles();
//            if (files != null) {
//                for (File file : files) {
//                    fileInfo.add(String.format("%s (%s)\n",
//                            file.getName(), file.getId()));
//                }
//            }
//            return fileInfo;
//        }
//
//
//        @Override
//        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
//        }
//
//        @Override
//        protected void onPostExecute(List<String> output) {
//            mProgress.hide();
//            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
//            } else {
//                output.add(0, "Data retrieved using the Drive API:");
//                mOutputText.setText(TextUtils.join("\n", output));
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mProgress.hide();
//            if (mLastError != null) {
//                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                            ((GooglePlayServicesAvailabilityIOException) mLastError)
//                                    .getConnectionStatusCode());
//                } else if (mLastError instanceof UserRecoverableAuthIOException) {
//                    startActivityForResult(
//                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
//                            DriveAuthActivity.REQUEST_AUTHORIZATION);
//                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());
//                }
//            } else {
//                mOutputText.setText("Request cancelled.");
//            }
//        }
//    }

    public void catchUSERException(Intent intent) {
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void createFolderIfNotExist() {
        //checking availability external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageIsAvailable = true;
            //checking existing all directories
            java.io.File audioDir = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(UNLConsts.DIR_NAME));
            if (!audioDir.exists()) {
                externalStorageIsAvailable = audioDir.mkdir();
            }
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

    @Override
    protected void onPause() {
        super.onPause();
        if (loadOldAccName != null && startHandler != null) {
            startHandler.removeCallbacks(loadOldAccName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}