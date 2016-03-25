package mobi.esys.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.io.ByteStreams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

public class DownloadVideoTask extends AsyncTask<Void, Void, Void> {
    private transient Handler handler;
    private transient List<GDFile> gdFiles;
    private transient static FileOutputStream output;
    private transient List<String> serverMD5;
    private transient int downCount;
    private transient List<GDFile> listWithoutDuplicates;
    private transient List<String> folderMD5;
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient String actName;
    private transient DirectoryWorks directoryWorks;

    public DownloadVideoTask(UNLApp app, Handler incHandler, List<GDFile> incGDFiles, String incServerMD5, String actName) {
        downCount = 0;
        mApp = app;
        handler = incHandler;
        drive = app.getDriveService();
        this.gdFiles = incGDFiles;
        this.actName = actName;
        serverMD5 = new ArrayList<>();
        String[] md5sAppArray = incServerMD5.split(",");
        serverMD5 = Arrays.asList(md5sAppArray);
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!UNLApp.getIsDownloadTaskRunning()) {
            if (!UNLApp.getIsDeleting()) {
                UNLApp.setIsDownloadTaskRunning(true);
                if (NetWork.isNetworkAvailable(mApp)) {
                    if (serverMD5.size() != 0) {
                        directoryWorks = new DirectoryWorks(
                                UNLConsts.VIDEO_DIR_NAME +
                                        UNLConsts.GD_STORAGE_DIR_NAME +
                                        "/");

                        //deleting duplicates
                        listWithoutDuplicates = new ArrayList<>(gdFiles);
                        ArrayList<Integer> delEntriesFromList = new ArrayList<>();
                        for (int i = 0; i < listWithoutDuplicates.size(); i++) {
                            String curMD5 = listWithoutDuplicates.get(i).getGdFileMD5();
                            for (int j = i + 1; j < listWithoutDuplicates.size(); j++) {
                                if (curMD5.equals(listWithoutDuplicates.get(j).getGdFileMD5())) {
                                    delEntriesFromList.add(j);
                                }
                            }
                        }
                        for (int i = delEntriesFromList.size() - 1; i >= 0; i--) {
                            int removePos = delEntriesFromList.get(i);
                            listWithoutDuplicates.remove(removePos);
                        }

                        Log.d("unTag_drive files", String.valueOf(listWithoutDuplicates.size()));
                        Log.d("unTag_md5", String.valueOf(serverMD5.size()));

                        Collections.sort(listWithoutDuplicates, new Comparator<GDFile>() {
                            @Override
                            public int compare(GDFile lhs, GDFile rhs) {
                                return lhs.getGdFileName().compareTo(rhs.getGdFileName());
                            }
                        });

                        Log.d("unTag_files", listWithoutDuplicates.toString());

                        folderMD5 = directoryWorks.getMD5Sums();
                        Set<String> tmpSet = new HashSet<String>();
                        tmpSet.addAll(serverMD5);
                        if (folderMD5.containsAll(tmpSet) && folderMD5.size() == tmpSet.size()) {
                            Log.d("unTag_down", "Not need down file, all files already exists. Cancel download task");
                            tmpSet = null;
                            cancel(true);
                        } else {
                            tmpSet = null;
                            while (downCount < listWithoutDuplicates.size() && !isCancelled()) {
                                try {
                                    downloadFile(drive, listWithoutDuplicates.get(downCount).getGdFileInst());
                                } catch (Exception e) {
                                    Log.d("unTag_exc", e.getLocalizedMessage());
                                    downCount++;
                                }
                            }
                        }
                    } else {
                        Log.d("unTag_down", "We have empty file-list from GoogleDrive");
                        cancel(true);
                    }
                } else {
                    Log.d("unTag_down", "Cancel download task because we have no Internet");
                    cancel(true);
                }
            } else {
                Log.d("unTag_down", "Cancel download task because running deleting process");
                cancel(true);
            }
        } else {
            Log.d("unTag_down", "Cancel download task because running another download process");
            cancel(true);
        }

        return null;
    }

    private void downloadFile(Drive service, File file) {

        if (file.getFileSize() < Environment.getExternalStorageDirectory().getUsableSpace()) {
            Log.d("unTag_down", "start down file number " + downCount);
            if (!folderMD5.contains(file.getMd5Checksum())) {
                if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
                    try {
                        HttpResponse resp = service
                                .getRequestFactory()
                                .buildGetRequest(
                                        new GenericUrl(file.getDownloadUrl()))
                                .execute();
                        String root_dir = UNLApp.getAppExtCachePath()
                                + UNLConsts.VIDEO_DIR_NAME
                                + UNLConsts.GD_STORAGE_DIR_NAME
                                + "/";
                        String path = file.getTitle().substring(0, file.getTitle().lastIndexOf(".")).concat(".").concat(UNLConsts.TEMP_FILE_EXT);
                        java.io.File downFile = new java.io.File(root_dir, path);
                        FileWorks fileWorks = new FileWorks(downFile.getAbsolutePath());
                        Log.d("unTag_down", downFile.getAbsolutePath());
                        //if file do not exists on SD
                        if (!downFile.exists()) {
                            output = new FileOutputStream(downFile);
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            int len = 0;
                            while ((len = resp.getContent().read(buffer)) != -1) {
                                output.write(buffer, 0, len);
                            }
                            output.flush();
                            output.close();
                            if (serverMD5.contains(fileWorks.getFileMD5())) {
                                fileWorks.renameFileExtension(file.getFileExtension());
                                downCount++;
                                Log.d("unTag_down", "Download complete: " + String.valueOf(downCount));
                                return;
                            } else {
                                downCount++;
                                return;
                            }
                        }
                        //if file exists on SD and his extension is "tmp" we delete this file and write new on his place.
                        else if (downFile.exists() && UNLConsts.TEMP_FILE_EXT.equals(fileWorks.getFileExtension())) {
                            if (downFile.delete()) {
                                Log.d("unTag_down", "TMP file deleted download again");
                                output = new FileOutputStream(downFile);
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len = 0;
                                while ((len = resp.getContent().read(buffer)) != -1) {
                                    output.write(buffer, 0, len);
                                }
                                output.flush();
                                output.close();
                                if (serverMD5.contains(fileWorks.getFileMD5())) {
                                    fileWorks.renameFileExtension(file.getFileExtension());
                                    downCount++;
                                    Log.d("unTag_down", "Download complete: " + String.valueOf(downCount));
                                    return;
                                } else {
                                    downCount++;
                                    return;
                                }
                            }
                        }
//                            else if (downFile.exists() && UNLConsts.TEMP_FILE_EXT.equals(fileWorks.getFileExtension()) && serverMD5.contains(fileWorks.getFileMD5()) && downFile.length() < file.getFileSize()) {
//                                Log.d("unTag_down_tag", fileWorks.getFileExtension());
//
//                                output = new FileOutputStream(downFile, true);
//                                int bufferSize = 1024;
//                                byte[] buffer = new byte[bufferSize];
//                                int len = 0;
//                                InputStream inputStream = resp.getContent();
//
//                                long skipped = inputStream.skip(file.getFileSize() - downFile.length());
//                                Log.d("down_tag", String.valueOf(file.getFileSize() - downFile.length()) + ":" + String.valueOf(skipped));
//                                if (skipped < file.getFileSize() - downFile.length()) {
//                                    append(downFile, ByteStreams.toByteArray(inputStream));
//                                } else {
//                                    downFile.delete();
//                                }
//
//                                if (serverMD5.contains(fileWorks.getFileMD5())) {
//                                    fileWorks.renameFileExtension(file.getFileExtension());
//                                    downCount++;
//
//                                    Log.d("unTag_countDownComplete",
//                                            String.valueOf(downCount));
//                                    return;
//                                } else {
//                                    downCount++;
//                                    return;
//                                }
//                            }
                    } catch (IOException e) {
                        downCount++;
                        Log.d("count exc", String.valueOf(downCount));
                        Log.d("exc", e.getLocalizedMessage());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("unTag_down", "Error when download file. Can't get Download Url from GD");
                    downCount++;
                    return;
                }

            } else {
                Log.d("unTag_down", "File already exists, not need down.");
                downCount++;
                return;
            }
        } else {
            cancel(true);
            Log.d("unTag_down", "In the external storage is not more available memory. New files are not downloaded until you free up space.");

            if ("first".equals(actName)) {
                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                intentOut.putExtra("toastText", "In the external storage is not more available memory. New files are not downloaded until you free up space.");
                mApp.sendBroadcast(intentOut);
            } else {
                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                intentOut.putExtra("toastText", "In the external storage is not more available memory. New files are not downloaded until you free up space.");
                mApp.sendBroadcast(intentOut);
            }
        }
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        stopDownload();

        if ("first".equals(actName)) {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "download_done");
            intentOut.putExtra("recToMP_message", "Download ends fine");
            mApp.sendBroadcast(intentOut);
        } else {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "download_done");
            intentOut.putExtra("recToMP_message", "Download ends fine");
            mApp.sendBroadcast(intentOut);
        }

        if (!UNLApp.getIsDeleting() && serverMD5 != null) {
            DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(mApp, serverMD5, actName);
            brokeFilesTask.execute();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stopDownload();

        if ("first".equals(actName)) {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "download_error");
            intentOut.putExtra("recToMP_message", "Download canceled");
            mApp.sendBroadcast(intentOut);
        } else {
            Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
            intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_REC_TO_MP);
            intentOut.putExtra("recToMP_tag", "download_error");
            intentOut.putExtra("recToMP_message", "Download canceled");
            mApp.sendBroadcast(intentOut);
        }
    }

    private void stopDownload() {
        UNLApp.setIsDownloadTaskRunning(false);
        handler.sendEmptyMessage(42);
    }

    public static void append(java.io.File file, byte[] bytes) throws Exception {
        long fileLength = file.length();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(fileLength);
        raf.write(bytes);
        raf.close();
    }


}
