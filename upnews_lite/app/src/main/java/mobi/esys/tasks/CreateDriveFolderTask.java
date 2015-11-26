package mobi.esys.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.system.StremsUtils;
import mobi.esys.upnews_lite.DriveAuthActivity;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

public class CreateDriveFolderTask extends AsyncTask<Void, Void, Void> {
    private transient SharedPreferences prefs;
    private transient Context mContext;
    private transient boolean isAuthSuccess;
    private transient boolean mStartVideoOnSuccess;
    private static final String RSS_TITLE = UNLConsts.GD_RSS_FILE_TITLE;
    private static final String RSS_MIME_TYPE = UNLConsts.GD_RSS_FILE_MIME_TYPE;
    private static final String LOGO_TITLE = UNLConsts.GD_LOGO_FILE_TITLE;
    private static final String LOGO_MIME_TYPE = UNLConsts.GD_LOGO_FILE_MIME_TYPE;
    private static final String TAG = "unTag_CrDriveFolderTask";
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient ProgressDialog pd;
    private transient boolean mIsShowPD;

    public CreateDriveFolderTask(Context context, boolean isStartVideoOnSuccess, UNLApp app, boolean isShowPD) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        isAuthSuccess = true;
        mStartVideoOnSuccess = isStartVideoOnSuccess;
        drive = app.getDriveService();
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

                Files.List request = drive
                        .files()
                        .list()
                        .setQ(UNLConsts.GD_FOLDER_QUERY);
                FileList files = request.execute();

                for (File file : files.getItems()) {
                    fileName.add(file.getTitle());
                    Log.d(TAG, "Files in GD" + file.getTitle() + ":" + file.getId());
                }

//                Log.d(TAG, fileName.toString());
                Editor editor = prefs.edit();
                if (!fileName.contains(UNLConsts.GD_VIDEO_DIR_NAME)) {
                    File body = new File();
                    body.setTitle(UNLConsts.GD_VIDEO_DIR_NAME);
                    body.setMimeType("application/vnd.google-apps.folder");
                    File unlFolder = drive.files().insert(body).execute();
                    Log.d(TAG, "ID unlFolder in GD" + unlFolder.getId());
                    editor.putString("folderId", unlFolder.getId());

                    //Create rss.txt in Google Drive
                    //Prepare and create file
                    File file = new File();
                    file.setTitle(RSS_TITLE);
                    file.setDescription("file to configure rss reader");
                    file.setMimeType(RSS_MIME_TYPE);
                    file.setParents(Arrays.asList(new ParentReference().setId(unlFolder.getId())));
                    //Prepare and create temp file
                    java.io.File tmpFile = new java.io.File(Environment.getExternalStorageDirectory() + UNLConsts.VIDEO_DIR_NAME, "rss.txt");
//                    Log.d(TAG, "path to temp rss.txt file" +  tmpFile.getAbsolutePath());  //Path to temp file
                    InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.rss);
                    StremsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                    FileContent fileContent = new FileContent(RSS_MIME_TYPE, tmpFile);
                    file = drive.files().insert(file, fileContent).execute();

                    Log.d(TAG, "ID rss.txt file" + file.getId());

                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }

                    //Copy logo file in Google Drive
                    //Prepare and create file
                    File logoFile = new File();
                    logoFile.setTitle(LOGO_TITLE);
                    logoFile.setDescription("logo file");
                    logoFile.setMimeType(LOGO_MIME_TYPE);
                    logoFile.setParents(Arrays.asList(new ParentReference().setId(unlFolder.getId())));
                    //Prepare and create temp file
                    java.io.File logoTmpFile = new java.io.File(Environment.getExternalStorageDirectory() + UNLConsts.VIDEO_DIR_NAME, LOGO_TITLE);
//                    Log.d(TAG, "path to temp rss.txt file" + logoTmpFile.getAbsolutePath());  //Path to temp file
                    InputStream logoTmpInStream = mContext.getResources().openRawResource(R.raw.logo_to_drive);
                    StremsUtils.copyInputStreamToFile(logoTmpInStream, logoTmpFile);
                    FileContent logoFileContent = new FileContent(LOGO_MIME_TYPE, logoTmpFile);
                    logoFile = drive.files().insert(logoFile, logoFileContent).execute();

                    Log.d(TAG, "ID logo file" + logoFile.getId());

                    if (logoTmpFile.exists()) {
                        logoTmpFile.delete();
                    }

                } else {
                    String folderID = files.getItems()
                            .get(fileName
                                    .indexOf(UNLConsts.GD_VIDEO_DIR_NAME))
                            .getId();
                    List<String> fileNames = new ArrayList<String>();
                    Log.d(TAG, "GD contain upnewslite folder. It ID is: " + folderID);
                    editor.putString(
                            "folderId",
                            files.getItems()
                                    .get(fileName
                                            .indexOf(UNLConsts.GD_VIDEO_DIR_NAME))
                                    .getId());

                    com.google.api.services.drive.Drive.Children.List fileList = drive.children().list(folderID);
                    ChildList children = fileList.execute();
                    for (ChildReference child : children.getItems()) {
                        File file = drive.files().get(child.getId()).execute();
                        if (!file.getExplicitlyTrashed()) {
                            fileNames.add(file.getTitle());
                        }
                    }

                    Log.d(TAG, "fileNames in upnewslite folder:" + fileNames.toString());

                    if (!fileNames.contains(RSS_TITLE)) {
                        File file = new File();
                        file.setTitle(RSS_TITLE);
                        file.setDescription("file to configure rss reader");
                        file.setMimeType(RSS_MIME_TYPE);
                        file.setParents(Arrays.asList(new ParentReference().setId(folderID)));

                        java.io.File tmpFile = new java.io.File(Environment.getExternalStorageDirectory() + UNLConsts.VIDEO_DIR_NAME, "rss.txt");
//                    Log.d(TAG, "path to temp rss.txt file" +  tmpFile.getAbsolutePath());  //Path to temp file
                        InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.rss);
                        StremsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                        FileContent fileContent = new FileContent(RSS_MIME_TYPE, tmpFile);
                        file = drive.files().insert(file, fileContent).execute();
                        Log.d(TAG, "ID rss.txt file" + file.getId());

                        if (tmpFile.exists()) {
                            tmpFile.delete();
                        }
                    }

                    if (!fileNames.contains(LOGO_TITLE)) {
                        //Copy logo file in Google Drive
                        //Prepare and create file
                        File logoFile = new File();
                        logoFile.setTitle(LOGO_TITLE);
                        logoFile.setDescription("logo file");
                        logoFile.setMimeType(LOGO_MIME_TYPE);
                        logoFile.setParents(Arrays.asList(new ParentReference().setId(folderID)));
                        //Prepare and create temp file
                        java.io.File logoTmpFile = new java.io.File(Environment.getExternalStorageDirectory() + UNLConsts.VIDEO_DIR_NAME, LOGO_TITLE);
//                      Log.d(TAG, "path to temp rss.txt file" + logoTmpFile.getAbsolutePath());  //Path to temp file
                        InputStream logoTmpInStream = mContext.getResources().openRawResource(R.raw.logo_to_drive);
                        StremsUtils.copyInputStreamToFile(logoTmpInStream, logoTmpFile);
                        FileContent logoFileContent = new FileContent(LOGO_MIME_TYPE, logoTmpFile);
                        logoFile = drive.files().insert(logoFile, logoFileContent).execute();

                        Log.d(TAG, "ID logo file" + logoFile.getId());

                        if (logoTmpFile.exists()) {
                            logoTmpFile.delete();
                        }
                    }
                }

                editor.apply();
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
        } else

        {
            isAuthSuccess = false;
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mStartVideoOnSuccess) {
            if (isAuthSuccess) {
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME +
                        UNLConsts.GD_STORAGE_DIR_NAME +
                        "/");
                if (directoryWorks.getDirFileList("if have files").length == 0) {
                    mContext.startActivity(new Intent(mContext,
                            FirstVideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ((DriveAuthActivity) mContext).finish();
                } else {
                    mContext.startActivity(new Intent(mContext,
                            FullscreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ((DriveAuthActivity) mContext).finish();
                }
            } else {
                mContext.startActivity(new Intent(mContext,
                        DriveAuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }

        if (mIsShowPD) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }

    }


}
