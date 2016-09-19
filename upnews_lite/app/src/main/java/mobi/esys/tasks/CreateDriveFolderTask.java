package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
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

import mobi.esys.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.system.StremsUtils;
import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

public class CreateDriveFolderTask extends AsyncTask<Void, Void, Void> {
    private static final String RSS_TITLE = UNLConsts.GD_RSS_FILE_TITLE;
    private static final String RSS_MIME_TYPE = UNLConsts.GD_RSS_FILE_MIME_TYPE;
    private static final String LOGO_TITLE = UNLConsts.GD_LOGO_FILE_TITLE;
    private static final String LOGO_MIME_TYPE = UNLConsts.GD_LOGO_FILE_MIME_TYPE;
    private static final String TAG = "unTag_CrDriveFolderTask";
    CreateDriveFolderCallback mCallback;
    private transient SharedPreferences prefs;
    private transient Context mContext;
    private transient boolean isAuthSuccess = false;
    private transient Drive drive;
    private transient UNLApp mApp;

    public CreateDriveFolderTask(Context context, CreateDriveFolderCallback callback, UNLApp app) {
        mContext = context;
        mCallback = callback;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        drive = UNLApp.getDriveService();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (drive != null) {
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
                        //create upnews lite folder in GD
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
                        java.io.File tmpFile = new java.io.File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME, "rss.txt");
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
                        java.io.File logoTmpFile = new java.io.File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME, LOGO_TITLE);
                        //                    Log.d(TAG, "path to temp rss.txt file" + logoTmpFile.getAbsolutePath());  //Path to temp file
                        InputStream logoTmpInStream = mContext.getResources().openRawResource(R.raw.logo_to_drive);
                        StremsUtils.copyInputStreamToFile(logoTmpInStream, logoTmpFile);
                        FileContent logoFileContent = new FileContent(LOGO_MIME_TYPE, logoTmpFile);
                        logoFile = drive.files().insert(logoFile, logoFileContent).execute();

                        Log.d(TAG, "ID logo file" + logoFile.getId());

                        if (logoTmpFile.exists()) {
                            logoTmpFile.delete();
                        }

                        //create upnews lite statistics folder in GD
                        File bodyStatistics = new File();
                        bodyStatistics.setTitle(UNLConsts.GD_STATISTICS_DIR_NAME);
                        bodyStatistics.setMimeType("application/vnd.google-apps.folder");
                        bodyStatistics.setParents(Arrays.asList(new ParentReference().setId(unlFolder.getId())));
                        File unlFolderStatistics = drive.files().insert(bodyStatistics).execute();
                        Log.d(TAG, "ID unlFolderStatistics in GD" + unlFolderStatistics.getId());

                        //create upnews lite statistics folder in GD
                        String serialNumber = UNLApp.getFullDeviceIdForStatistic();     //old version   Build.SERIAL;
                        File bodyDiveceStatistics = new File();
                        bodyDiveceStatistics.setTitle(serialNumber);
                        bodyDiveceStatistics.setMimeType("application/vnd.google-apps.folder");
                        bodyDiveceStatistics.setParents(Arrays.asList(new ParentReference().setId(unlFolderStatistics.getId())));
                        File deviceFolderIdStatistics = drive.files().insert(bodyDiveceStatistics).execute();
                        Log.d(TAG, "ID unlFolderStatistics in GD" + deviceFolderIdStatistics.getId());
                        editor.putString("deviceFolderIdStatistics", deviceFolderIdStatistics.getId());
                    } else {
                        //get folder upnewslite id in GD
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

                        Drive.Children.List fileList = drive.children().list(folderID);
                        ChildList children = fileList.execute();
                        String unlFolderStatisticsID = "";
                        for (ChildReference child : children.getItems()) {
                            File file = drive.files().get(child.getId()).execute();
                            if (!file.getExplicitlyTrashed()) {
                                fileNames.add(file.getTitle());
                                if (file.getTitle().equals(UNLConsts.GD_STATISTICS_DIR_NAME)
                                        && file.getMimeType().equals("application/vnd.google-apps.folder")) {
                                    unlFolderStatisticsID = child.getId();
                                }
                            }
                        }
                        Log.d(TAG, "fileNames in upnewslite folder:" + fileNames.toString());

                        //Checking rss.txt in GD
                        if (!fileNames.contains(RSS_TITLE)) {
                            //Create rss.txt in Google Drive
                            //Prepare and create file
                            File file = new File();
                            file.setTitle(RSS_TITLE);
                            file.setDescription("file to configure rss reader");
                            file.setMimeType(RSS_MIME_TYPE);
                            file.setParents(Arrays.asList(new ParentReference().setId(folderID)));

                            java.io.File tmpFile = new java.io.File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME, "rss.txt");
                            //Log.d(TAG, "path to temp rss.txt file" +  tmpFile.getAbsolutePath());  //Path to temp file
                            InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.rss);
                            StremsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                            FileContent fileContent = new FileContent(RSS_MIME_TYPE, tmpFile);
                            file = drive.files().insert(file, fileContent).execute();
                            Log.d(TAG, "ID rss.txt file" + file.getId());

                            if (tmpFile.exists()) {
                                tmpFile.delete();
                            }
                        }

                        //Checking upnews_logo_w2.png in GD
                        if (!fileNames.contains(LOGO_TITLE)) {
                            //Copy logo file in Google Drive
                            //Prepare and create file
                            File logoFile = new File();
                            logoFile.setTitle(LOGO_TITLE);
                            logoFile.setDescription("logo file");
                            logoFile.setMimeType(LOGO_MIME_TYPE);
                            logoFile.setParents(Arrays.asList(new ParentReference().setId(folderID)));
                            //Prepare and create temp file
                            java.io.File logoTmpFile = new java.io.File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME, LOGO_TITLE);
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

                        //Check upnews lite statistics folder in GD
                        String serialNumber = UNLApp.getFullDeviceIdForStatistic(); //old version   Build.SERIAL;
                        String unlFolderDeviceStatisticsID = "";
                        if (unlFolderStatisticsID.isEmpty()) {
                            File bodyStatistics = new File();
                            bodyStatistics.setTitle(UNLConsts.GD_STATISTICS_DIR_NAME);
                            bodyStatistics.setMimeType("application/vnd.google-apps.folder");
                            bodyStatistics.setParents(Arrays.asList(new ParentReference().setId(folderID)));
                            File unlFolderStatistics = drive.files().insert(bodyStatistics).execute();
                            unlFolderStatisticsID = unlFolderStatistics.getId();
                            Log.d(TAG, "ID unlFolderStatistics in GD" + unlFolderStatisticsID);
                        } else {
                            Drive.Children.List commonStatisticsFileList = drive.children().list(unlFolderStatisticsID);
                            ChildList commonStatisticsChildren = commonStatisticsFileList.execute();
                            for (ChildReference commonChild : commonStatisticsChildren.getItems()) {
                                File file = drive.files().get(commonChild.getId()).execute();
                                if (!file.getExplicitlyTrashed()) {
                                    fileNames.add(file.getTitle());
                                    if (file.getTitle().equals(serialNumber)
                                            && file.getMimeType().equals("application/vnd.google-apps.folder")) {
                                        unlFolderDeviceStatisticsID = commonChild.getId();
                                    }
                                }
                            }
                        }

                        //Check upnews lite device statistics folder in GD
                        if (unlFolderDeviceStatisticsID.isEmpty()) {
                            File bodyDeviceStatistics = new File();
                            bodyDeviceStatistics.setTitle(serialNumber);
                            bodyDeviceStatistics.setMimeType("application/vnd.google-apps.folder");
                            bodyDeviceStatistics.setParents(Arrays.asList(new ParentReference().setId(unlFolderStatisticsID)));
                            File deviceFolderIdStatistics = drive.files().insert(bodyDeviceStatistics).execute();
                            Log.d(TAG, "ID unlFolderStatistics in GD" + deviceFolderIdStatistics.getId());
                            editor.putString("deviceFolderIdStatistics", deviceFolderIdStatistics.getId());
                        } else {
                            editor.putString("deviceFolderIdStatistics", unlFolderDeviceStatisticsID);
                        }
                    }

                    editor.apply();
                    isAuthSuccess = true;
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "Error");
                    mCallback.authIsFailed(true);
                } catch (IOException e) {
                    Log.d(TAG, "Error: " + e.getLocalizedMessage());
                }
            } else {
                Log.d(TAG, "We have no Inet");
            }
        } else {
            Log.d(TAG, "We have no Google Drive");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        UNLApp.setIsCreatingDriveFolder(false);
        if (isAuthSuccess) {
            DirectoryWorks directoryWorks = new DirectoryWorks(
                    UNLConsts.VIDEO_DIR_NAME +
                            UNLConsts.GD_STORAGE_DIR_NAME +
                            "/");
            String[] files = directoryWorks.getDirFileList("if have files");
            if (files.length > 0) {
                Log.w(TAG, "We have files on device. All OK. Start MainActivity.");
                mCallback.startVideoActivity();
            } else {
                Log.w(TAG, "We have no files, start FirstVideoActivity");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("localMD5", "");
                editor.putString("localNames", "");
                editor.apply();
                mCallback.startVideoActivity();
            }
//                if (files.length > 0) {
//                    String[] localMD5 = prefs.getString("localMD5", "").split(",");
//                    String[] localNames = prefs.getString("localNames", "").split(",");
//                    int len = localNames.length;
//                    if (localMD5.length < len) {
//                        len = localMD5.length;
//                    }
//                    List<HashCache> tmp = new ArrayList<>();
//                    for (int i = 0; i < len; i++) {
//                        tmp.add(new HashCache(localNames[i], localMD5[i]));
//                    }
//                    UNLApp.setHashCaches(tmp);
//
//
//                    boolean haveVideoFile = false;
//                    for (int i = 0; i < files.length; i++) {
//                        for(int j=0; j<tmp.size();j++){
//                            //FileWorks fileWorks = new FileWorks(files[i]);
//                            if (tmp.get(j).getName().equals(files[i])) {    //  && Arrays.asList(UNLConsts.UNL_ACCEPTED_FILE_EXTS).contains(fileWorks.getFileExtension())
//                                haveVideoFile = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (haveVideoFile) {
//                        Log.w(TAG, "We have MP4 or AVI files on device. All OK. Start MainActivity.");
//                        mContext.startActivity(new Intent(mContext,
//                                MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                        ((DriveAuthActivity) mContext).finish();
//                    } else {
//                        Log.w(TAG, "We have no MP4 or AVI files or saved localNames not contain current files, start FirstVideoActivity");
//                        mContext.startActivity(new Intent(mContext,
//                                FirstVideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                        ((DriveAuthActivity) mContext).finish();
//                    }
//                } else {
//                    List<HashCache> tmp = new ArrayList<>();
//                    UNLApp.setHashCaches(tmp);
//                    Log.w(TAG, "We have no files, start FirstVideoActivity");
//                    mContext.startActivity(new Intent(mContext,
//                            FirstVideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                    ((DriveAuthActivity) mContext).finish();
//                }
        } else {
            Log.w(TAG, "Authentication is failed. Return to DriveAuthActivity.");
            mCallback.authIsFailed(false);
        }
    }

    @Override
    protected void onCancelled() {
        UNLApp.setIsCreatingDriveFolder(false);
        super.onCancelled();
    }

}