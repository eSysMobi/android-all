package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnews_tune.UNLApp;

public class DownloadAudioTask extends AsyncTask<Void, Void, Void> {
    private transient Handler handler;
    private transient List<GDFile> gdFiles;
    private transient List<String> serverMD5;
    private transient int downCount;
    private transient List<GDFile> listWithoutDuplicates;
    private transient List<String> folderMD5;
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient SharedPreferences prefs;

    public DownloadAudioTask(UNLApp app) {
        downCount = 0;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        drive = UNLApp.getDriveService();
        serverMD5 = new ArrayList<>();

    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!UNLApp.getIsDownloadTaskRunning()) {
            if (!UNLApp.getIsDeleting()) {
                UNLApp.setIsDownloadTaskRunning(true);
                if (NetWork.isNetworkAvailable(mApp)) {
                    //get data from server
                    UNLServer server = new UNLServer(mApp);
                    String serverMD5String = server.getMD5FromServer();
                    String[] md5sAppArray = serverMD5String.split(",");
                    serverMD5 = Arrays.asList(md5sAppArray);
                    gdFiles = server.getGdFiles();

                    if (serverMD5.size() != 0) {
                        listWithoutDuplicates = new ArrayList<>(gdFiles);
                        //deleting duplicates for download
                        //get duplicates
                        HashSet<GDFile> delEntriesFromList = new HashSet<>();
                        for (int i = 0; i < listWithoutDuplicates.size(); i++) {
                            String curMD5 = listWithoutDuplicates.get(i).getGdFileMD5();
                            for (int j = i + 1; j < listWithoutDuplicates.size(); j++) {
                                if (curMD5.equals(listWithoutDuplicates.get(j).getGdFileMD5())) {
                                    delEntriesFromList.add(listWithoutDuplicates.get(j));
                                }
                            }
                        }
                        //delete duplicates
                        for (GDFile aDelEntriesFromList : delEntriesFromList) {
                            listWithoutDuplicates.remove(aDelEntriesFromList);
                        }
                        Log.d("unTag_down", "Drive files without duplicates (" + String.valueOf(listWithoutDuplicates.size()) + "): " + listWithoutDuplicates.toString());
                        Log.d("unTag_down", "Count all MD5 of drive files: " + String.valueOf(serverMD5.size()));

                        //get and save local file names and they md5's
                        DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.DIR_NAME);
                        String[] localFileNames = directoryWorks.getDirFileList(true);
                        folderMD5 = directoryWorks.getMD5Sums(true);
                        Log.d("unTag_down", "localFileNames (" + localFileNames.length + ")=" + Arrays.toString(localFileNames));
                        Log.d("unTag_down", "folderMD5 (" + folderMD5.size() + ")=" + folderMD5.toString());

                        //save local filenames and md5 for caching
                        SharedPreferences.Editor editor = prefs.edit();
                        String localMD5 = folderMD5.toString().substring(1, folderMD5.toString().length() - 1).replace(", ", ",");
                        editor.putString("localMD5", localMD5);
                        String localNames = Arrays.toString(localFileNames);
                        localNames = localNames.substring(1, localNames.length() - 1).replace(", ", ",");
                        editor.putString("localNames", localNames);
                        editor.apply();

                        //checking need download or not
                        Set<String> tmpSet = new HashSet<>();
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

        if (file.getSize() < (Environment.getExternalStorageDirectory().getUsableSpace() - 209715200)) {  // +200MB to Environment
            Log.d("unTag_down", "start down file number " + downCount);
            if (!folderMD5.contains(file.getMd5Checksum())) {
                if (file.getId() != null) {
                    try {
                        String root_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + UNLConsts.DIR_NAME;
                        String fileName = file.getName().replace(",", "").substring(0, file.getName().lastIndexOf(".")).concat(".").concat(UNLConsts.TEMP_FILE_EXT);

                        //checking duplicate name in different files
                        String fileTrueName = file.getName().replace(",", "");
                        java.io.File checkingFile = new java.io.File(root_dir, fileTrueName);
                        if (checkingFile.exists()) {
                            Log.d("unTag_down", "Another file with name " + fileName + " already exists. Rename new file.");
                            fileName = "copy_" + fileName;
                        }
                        checkingFile = null;

                        java.io.File downFile = new java.io.File(root_dir, fileName);   //file *.tmp

                        //Checking existing tmp file
                        //If file exists on SD we delete this file and write new on his place.
                        if (downFile.exists()) {
                            if (downFile.delete()) {
                                Log.d("unTag_down", "TMP file deleted download again");
                            }
                            downFile.createNewFile();
                        } else {
                            downFile.createNewFile();
                        }

                        //download
                        Log.d("unTag_down", "Start downloading in the " + fileName);
                        String fileId = file.getId();

                        FileOutputStream output = new FileOutputStream(downFile);
                        service.files().get(fileId).executeMediaAndDownloadTo(output);

//                        int bufferSize = 1024;
//                        byte[] buffer = new byte[bufferSize];
//                        int len = 0;
//                        while ((len = resp.getContent().read(buffer)) != -1) {
//                            output.write(buffer, 0, len);
//                        }
                        output.flush();
                        output.close();

                        //checking successful write     TODO is this checking really need?
                        FileWorks fileWorks = new FileWorks(downFile.getAbsolutePath());
                        String downloadedMD5 = fileWorks.getFileMD5();
                        if (serverMD5.contains(downloadedMD5)) {
                            //rename "tmp" to "mp4" or "avi"
                            String resultOfRenaming = fileWorks.renameFileExtension(file.getFileExtension());
                            downCount++;

                            //save
                            String localN = prefs.getString("localNames", "");
                            if (localN.isEmpty()) {
                                localN = resultOfRenaming;
                            } else {
                                localN = localN + "," + resultOfRenaming;
                            }
                            String localM = prefs.getString("localMD5", "");
                            if (localM.isEmpty()) {
                                localM = downloadedMD5;
                            } else {
                                localM = localM + "," + downloadedMD5;
                            }
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("localMD5", localM);
                            editor.putString("localNames", localN);
                            editor.apply();

                            Log.d("unTag_down", "Download complete: " + String.valueOf(downCount));
                            return;
                        } else {
                            downCount++;
                            Log.d("unTag_down", "Error saving down file: " + String.valueOf(downCount) + " MS5 not matched.");
                            return;
                        }
                    } catch (IOException e) {
                        downCount++;
                        Log.d("count exc", String.valueOf(downCount));
                        Log.d("exc", e.getLocalizedMessage());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("unTag_down", "Error when download file. Can't get file ID from GD");
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
        }
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        stopDownload();

        if (!UNLApp.getIsDeleting() && serverMD5 != null) {
            DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(mApp, serverMD5);
            brokeFilesTask.execute();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stopDownload();
    }

    private void stopDownload() {
        UNLApp.setIsDownloadTaskRunning(false);
    }

    public static void append(java.io.File file, byte[] bytes) throws Exception {
        long fileLength = file.length();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(fileLength);
        raf.write(bytes);
        raf.close();
    }


}
