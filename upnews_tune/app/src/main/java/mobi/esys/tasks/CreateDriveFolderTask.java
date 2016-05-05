package mobi.esys.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.net.NetWork;
import mobi.esys.upnews_tune.DriveAuthActivity;
import mobi.esys.upnews_tune.StartPlayerActivity;
import mobi.esys.upnews_tune.UNLApp;

public class CreateDriveFolderTask extends AsyncTask<Void, Void, Void> {
    private transient SharedPreferences prefs;
    private transient Context mContext;
    private transient boolean isAuthSuccess = false;
    private transient boolean mStartPlayOnSuccess;
    private static final String TAG = "unTag_CrDriveFolderTask";
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient ProgressDialog pd;
    private transient boolean mIsShowPD;

    public CreateDriveFolderTask(Context context, boolean isStartVideoOnSuccess, UNLApp app, boolean isShowPD) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        mStartPlayOnSuccess = isStartVideoOnSuccess;
        drive = UNLApp.getDriveService();
        mIsShowPD = mIsShowPD;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mIsShowPD) {
            pd = new ProgressDialog(mContext);
            pd.setMessage("Обработка папки на Google Drive");
            if (!pd.isShowing()) {
                pd.show();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetWork.isNetworkAvailable(mApp)) {
            Log.d(TAG, "create folder");
            try {
                List<String> fileName = new ArrayList<>();

                //get list of files from GD
                Files.List request = drive
                        .files()
                        .list()
                        .setQ(UNLConsts.GD_FOLDER_QUERY);
                FileList files = request.execute();

                //convert GD FileList to List
                for (File file : files.getItems()) {
                    fileName.add(file.getTitle());
                    Log.d(TAG, "Folder in GD" + file.getTitle() + ":" + file.getId());
                }

                Editor editor = prefs.edit();
                //looking for folder "upnewstune"
                if (!fileName.contains(UNLConsts.GD_DIR_NAME)) {
                    //NOT FOUND. Create "upnewstune"  folder in GD
                    File body = new File();
                    body.setTitle(UNLConsts.GD_DIR_NAME);
                    body.setMimeType("application/vnd.google-apps.folder");
                    File unlFolder = drive.files().insert(body).execute();
                    Log.d(TAG, "ID unlFolder in GD" + unlFolder.getId());
                    editor.putString("folderId", unlFolder.getId());
                    editor.apply();
                } else {
                    //FOUND. Get folder upnewstune id in GD
                    String folderID = files.getItems()
                            .get(fileName.indexOf(UNLConsts.GD_DIR_NAME))
                            .getId();
                    Log.d(TAG, "GD contain upnewstune folder. It ID is: " + folderID);
                    editor.putString("folderId",
                                     files.getItems().get(fileName.indexOf(UNLConsts.GD_DIR_NAME)).getId()
                                    );
                    editor.apply();
                }
                isAuthSuccess = true;

            } catch (UserRecoverableAuthIOException e) {
                Log.d(TAG, "Error");
                if (mContext instanceof DriveAuthActivity) {
                    ((DriveAuthActivity) mContext).catchUSERException(e
                            .getIntent());
                }
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getLocalizedMessage());
            }
        } else {
            Log.d(TAG, "We have no Inet");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        UNLApp.setIsCreatingDriveFolder(false);
        if (mStartPlayOnSuccess) {
            if (isAuthSuccess) {
                Log.w(TAG, "All OK. Start StartPlayerActivity.");
                mContext.startActivity(new Intent(mContext,
                        StartPlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                ((DriveAuthActivity) mContext).finish();
            } else {
                Log.w(TAG, "Authentication is failed. Return to DriveAuthActivity.");
                mContext.startActivity(new Intent(mContext,
                        DriveAuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            //not need to do/ This is was just checking task
        }

        if (mIsShowPD) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    @Override
    protected void onCancelled() {
        UNLApp.setIsCreatingDriveFolder(false);
        super.onCancelled();
    }
}
