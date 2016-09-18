package mobi.esys.fileworks;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import mobi.esys.UNLConsts;
import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

public class DirectoryWorks {
    private transient String directoryPath;
    private static final String DIR_WORKS_TAG = "unTag_DirectoryWorks";

    public DirectoryWorks(String directoryPath) {
        this.directoryPath = directoryPath;
        checkDirs();
    }

    public boolean checkDirs() {
        boolean res = true;
        File videoDir = new File(UNLApp.getAppExtCachePath() + UNLConsts.VIDEO_DIR_NAME);
        if (!videoDir.exists()) {
            res = res && videoDir.mkdir();
        }
        File storageDir = new File(videoDir.getAbsolutePath() + UNLConsts.STORAGE_DIR_NAME);
        if (!storageDir.exists()) {
            res = res && storageDir.mkdir();
        }
        File logoDir = new File(videoDir.getAbsolutePath() + UNLConsts.LOGO_DIR_NAME);
        if (!logoDir.exists()) {
            res = res && logoDir.mkdir();
        }
        File statisticsDir = new File(videoDir.getAbsolutePath() + UNLConsts.STATISTICS_DIR_NAME);
        if (!statisticsDir.exists()) {
            res = res && statisticsDir.mkdir();
        }
        File statisticsNetDir = new File(videoDir.getAbsolutePath() + UNLConsts.NETWORK_STATISTICS_DIR_NAME);
        if (!statisticsNetDir.exists()) {
            res = res && statisticsNetDir.mkdir();
        }
        File rssDir = new File(videoDir.getAbsolutePath() + UNLConsts.RSS_DIR_NAME);
        if (!rssDir.exists()) {
            res = res && rssDir.mkdir();
        }
        return res;
    }

    public File getRSSFile() {
        File result = null;
        File rssFile = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.RSS_DIR_NAME + UNLConsts.RSS_FILE_NAME);
        if (rssFile.exists()) {
            result = rssFile;
        } else {
            try {
                boolean fileCreated = rssFile.createNewFile();
                if (fileCreated) {
                    result = rssFile;
                } else {
                    Log.d(DIR_WORKS_TAG, "Cant create empty rss file");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(DIR_WORKS_TAG, "Cant create empty rss file. IOException.");
            }
        }
        return result;
    }

    public File[] getNetworkStatisticsFiles() {
        File[] result = null;
        File statisticFolder = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.NETWORK_STATISTICS_DIR_NAME);
        if (statisticFolder.exists()) {
            result = statisticFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase().endsWith(".csv");
                }
            });
        }
        return result;
    }

    public File checkLastNetworkStatisticFile() {
        //check today statistic file
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateToday = df.format(Calendar.getInstance().getTime());
        String fileName = dateToday + "-net.csv";
        File statisticFile = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.NETWORK_STATISTICS_DIR_NAME, fileName);
        if (!statisticFile.exists()) {
            try {
                statisticFile.createNewFile();
                //write
                try {
                    BufferedWriter output = new BufferedWriter(new FileWriter(statisticFile, true));
                    output.append("Time,MAC");
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                deleteRedundantStatistics(new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.NETWORK_STATISTICS_DIR_NAME));
            }
        }
        return statisticFile;
    }


    public File[] getStatisticsFiles() {
        File[] result = null;
        File statisticFolder = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.STATISTICS_DIR_NAME);
        if (statisticFolder.exists()) {
            result = statisticFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase().endsWith(".csv");
                }
            });
        }
        return result;
    }

    public File checkLastStatisticFile(Context mContext) {
        //check today statistic file
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateToday = df.format(Calendar.getInstance().getTime());
        String fileName = dateToday + ".csv";
        File statisticFile = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.STATISTICS_DIR_NAME, fileName);
        if (!statisticFile.exists()) {
            try {
                statisticFile.createNewFile();
                InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.statistic_file_sample);
                if (tmpInStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(tmpInStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";

                    FileWriter logWriter = new FileWriter(statisticFile);
                    BufferedWriter out = new BufferedWriter(logWriter);
                    int numLine = 0;
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        if (numLine > 0) {
                            out.newLine();
                        }
                        out.write(receiveString);
                        //out.flush();
                        numLine++;
                    }
                    out.close();
                    tmpInStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                deleteRedundantStatistics(new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.STATISTICS_DIR_NAME));
            }
        }
        return statisticFile;
    }

    public File checkAllStatisticFile(Context mContext) {
        //check common statistic file
        File allStatisticFile = new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.STATISTICS_DIR_NAME, UNLConsts.ALL_STATISTICS_FINE_NAME);
        if (!allStatisticFile.exists()) {
            try {
                allStatisticFile.createNewFile();
                InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.statistic_file_sample);
                if (tmpInStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(tmpInStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";

                    FileWriter logWriter = new FileWriter(allStatisticFile);
                    BufferedWriter out = new BufferedWriter(logWriter);
                    int numLine = 0;
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        if (numLine > 0) {
                            out.newLine();
                        }
                        out.write(receiveString);
                        //out.flush();
                        numLine++;
                    }
                    out.close();
                    tmpInStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                deleteRedundantStatistics(new File(UNLApp.getAppExtCachePath() + "/" + UNLConsts.GD_VIDEO_DIR_NAME + UNLConsts.STATISTICS_DIR_NAME));
            }
        }
        return allStatisticFile;
    }

    public boolean deleteRedundantStatistics(File statisticDir) {
        boolean isDeleted = false;
        Log.d(DIR_WORKS_TAG, statisticDir.getAbsolutePath());
        if (statisticDir.exists()) {
            //get all files from statistics directory
            File[] files = statisticDir.listFiles();
            if (files.length > UNLConsts.NUM_STATISTICS_FILES) {
                List<File> statisticsList = Arrays.asList(files);
//                for (int i=0; i<statisticsList.size(); i++){
//                    if (statisticsList.get(i).exists() && statisticsList.get(i).getName().equals(UNLConsts.ALL_STATISTICS_FINE_NAME)){
//                        statisticsList.remove(i);
//                    }
//                }
                Collections.sort(statisticsList);
                Collections.reverse(statisticsList);
                //delete files older than 7 days
                for (int j = UNLConsts.NUM_STATISTICS_FILES; j < statisticsList.size(); j++) {
                    if (statisticsList.get(j).exists()) {
                        Log.d(DIR_WORKS_TAG, "Redundant Statistics file " + statisticsList.get(j).getName() + " deleted");
                        isDeleted = statisticsList.get(j).delete();
                    }
                }
            }
        } else {
            Log.d(DIR_WORKS_TAG, "folder don't exist");
            checkDirs();
        }
        return isDeleted;
    }

    public String[] getDirFileList(String mess) {
        checkDirs();
        File videoDir = new File(UNLApp.getAppExtCachePath().concat(this.directoryPath));
        List<String> filePaths = new ArrayList<>();
        //get all files from directory
        File[] files = videoDir.listFiles();
        for (File file : files) {
            if (file.exists()) {
                filePaths.add(file.getPath());
            }
        }
        Log.d(DIR_WORKS_TAG, mess + " " + filePaths.toString());

        return filePaths.toArray(new String[filePaths.size()]);
    }

    public String[] getOnlyVideoDirFileList(String mess) {
        checkDirs();
        File videoDir = new File(UNLApp.getAppExtCachePath().concat(this.directoryPath));
        List<String> filePaths = new ArrayList<>();
        //get only video files from directory
        File[] files = videoDir.listFiles();
        for (File file : files) {
            FileWorks fw = new FileWorks(file);
            if (file.exists() && Arrays.asList(UNLConsts.UNL_ACCEPTED_FILE_EXTS).contains(fw.getFileExtension())) {
                filePaths.add(file.getPath());
            }
        }
        Log.d(DIR_WORKS_TAG, mess + " " + filePaths.toString());

        return filePaths.toArray(new String[filePaths.size()]);
    }

    public void deleteFilesFromDir(List<String> maskList) {
        File videoDir = new File(UNLApp.getAppExtCachePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, "deleteFilesFromDir " + UNLApp.getAppExtCachePath().concat(this.directoryPath) + " Deleting " + maskList.size() + " files");
        Log.d(DIR_WORKS_TAG, "mask list task" + maskList.toString());
        if (videoDir.exists()) {
            File[] files = videoDir.listFiles();

            //check maskList
            if (maskList.size() == 1 && maskList.get(0).equals("unLiteDelAll")) {
                //delete all files in video directory excluded user files (with prefix dd)
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].getName().startsWith(UNLConsts.PREFIX_USER_VIDEOFILES)) {
                        files[i].delete();
                    }
                }
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (maskList.contains(files[i].getPath())) {

                        Date modDate = new Date(files[i].lastModified());
                        Calendar today = Calendar.getInstance();

                        long diff = today.getTimeInMillis() - modDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        if ((files[i].exists() && !files[i].getPath().equals(UNLApp.getCurPlayFile()))
                                ||
                                (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14)
                                ) {
                            files[i].delete();
                        }
                    }
                }
            }
        } else {
            Log.d(DIR_WORKS_TAG, "Folder don't exists");
            checkDirs();
            deleteFilesFromDir(maskList);
        }
        UNLApp.setIsDeleting(false);
    }

    public List<String> getMD5Sums() {
        String[] files = getDirFileList("getMD5SUM");
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            dirMD5s.add(fileWorks.getFileMD5());
        }
        return dirMD5s;
    }

    public List<String> getOnlyVideoMD5Sums() {
        String[] files = getOnlyVideoDirFileList("getMD5SUM");
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            dirMD5s.add(fileWorks.getFileMD5());
        }
        return dirMD5s;
    }

    public String getFileExtension(String fileName) {
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1);
        }
        Log.d(DIR_WORKS_TAG, "Getting ext is " + ext);
        return ext;
    }
}
