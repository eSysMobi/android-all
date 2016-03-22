package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.ParentReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.system.StremsUtils;
import mobi.esys.taskmanager.TaskManager;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 27.01.2016.
 */
public class SendStatisticsToGD implements Runnable {
    private transient SharedPreferences prefs;
    private static final String FOLDER_STAT_NAME = UNLConsts.GD_STATISTICS_DIR_NAME;
    private static final String STAT_MIME_TYPE = UNLConsts.STATISTICS_MIME_TYPE;
    private static final String TAG = "unTag_SendStatGD";
    private transient Drive drive;
    private transient UNLApp mApp;
    private String statisticsGDDirID;
    private Handler handler;

    public SendStatisticsToGD(UNLApp app, Handler incHandler) {
        mApp = app;
        handler = incHandler;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        statisticsGDDirID = prefs.getString("deviceFolderIdStatistics", "");
        drive = app.getDriveService();
    }

    @Override
    public void run() {
        //super.run();
        if (NetWork.isNetworkAvailable(mApp)) {
            if (!statisticsGDDirID.isEmpty()) {
                Log.d(TAG, "Start sending statistics files in GD");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String dateToday = df.format(Calendar.getInstance().getTime());
                String todayStatFileName = dateToday + ".csv";
                String serialNumber = Build.SERIAL;

                java.io.File tmpFile = new java.io.File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME, "tmp.tmp");
                try {
                    List<com.google.api.services.drive.model.File> statFilesOnGD = new ArrayList<>();
                    Drive.Children.List commonStatisticsFileList = drive.children().list(statisticsGDDirID);
                    ChildList commonStatisticsChildren = commonStatisticsFileList.execute();
                    for (ChildReference commonChild : commonStatisticsChildren.getItems()) {
                        com.google.api.services.drive.model.File statFile = drive.files().get(commonChild.getId()).execute();
                        if (!statFile.getExplicitlyTrashed() && statFile.getMimeType().equals(STAT_MIME_TYPE)) {
                            statFilesOnGD.add(statFile);
                        }
                    }

                    DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.STATISTICS_DIR_NAME);
                    File[] statFilesOnDevice = directoryWorks.getStatisticsFiles();

                    for (int i = 0; i < statFilesOnDevice.length; i++) {
                        String idStatFileGD = "";
                        int num_idStatFileGD = 0;
                        //search statFilesOnDevice[i] file in GD
                        for (int j = 0; j < statFilesOnGD.size(); j++) {
                            if (statFilesOnGD.get(j).getTitle().equals(statFilesOnDevice[i].getName())) {
                                idStatFileGD = statFilesOnGD.get(j).getId();
                                num_idStatFileGD = j;
                                break;
                            }
                        }
                        //processing finding data
                        if (idStatFileGD.isEmpty()) {
                            //create *.csv file in GD
                            Log.d(TAG, "File " + statFilesOnDevice[i].getName() + " not exist in GD. Create it!");
                            //Prepare file which we adding
                            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
                            file.setTitle(statFilesOnDevice[i].getName());
                            file.setDescription("Statistics file from device with ID: " + serialNumber);
                            file.setMimeType(STAT_MIME_TYPE);
                            file.setParents(Arrays.asList(new ParentReference().setId(statisticsGDDirID)));
                            //Prepare and create temp file
                            InputStream tmpInStream = new FileInputStream(statFilesOnDevice[i]);
                            StremsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                            FileContent fileContent = new FileContent(STAT_MIME_TYPE, tmpFile);
                            //create
                            file = drive.files().insert(file, fileContent).execute();
                            tmpInStream.close();

                            Log.d(TAG, "File " + statFilesOnDevice[i].getName() + " with ID " + file.getId() + " created in GD");
                        } else {
                            //update *.csv file in GD
                            Log.d(TAG, "File " + statFilesOnDevice[i].getName() + " exist in GD.");
                            //checking is this today file
                            if (statFilesOnDevice[i].getName().equals(todayStatFileName)) {
                                //Prepare updatable file
                                com.google.api.services.drive.model.File file = statFilesOnGD.get(num_idStatFileGD);
                                //Prepare and create temp file
                                InputStream tmpInStream = new FileInputStream(statFilesOnDevice[i]);
                                StremsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                                FileContent fileContent = new FileContent(STAT_MIME_TYPE, tmpFile);
                                //update
                                file = drive.files().update(idStatFileGD, file, fileContent).execute();
                                tmpInStream.close();

                                if (file != null) {
                                    Log.d(TAG, "File " + statFilesOnDevice[i].getName() + " with ID " + file.getId() + " updated in GD");
                                } else {
                                    Log.d(TAG, "Error GD! File " + statFilesOnDevice[i].getName() + " no updated in GD!");
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error sending statistics in GD: " + e.toString());
                } finally {
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    sendEndingSignal();
                }
            } else {
                Log.d(TAG, "We have no statistics id folder");
                sendEndingSignal();
            }
        } else {
            Log.d(TAG, "Internet is offline");
            sendEndingSignal();
        }
    }

    private void sendEndingSignal(){
        handler.sendEmptyMessage(42);
    }
}
