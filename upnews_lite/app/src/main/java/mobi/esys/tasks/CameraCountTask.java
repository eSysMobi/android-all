package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 27.04.2016.
 */
public class CameraCountTask extends AsyncTask<Void, Void, Void> {
    private int MAX_FACES = 15;
    private Context mContext;
    private String mVideoName;
    private List<String> filesForCounting;
    private int count = 0;
    private boolean allowToast = UNLConsts.ALLOW_TOAST;
    UNLApp mApp;

    public CameraCountTask(Context context, String videoName, UNLApp app, List<String> incFilesForCounting) {
        mContext = context;
        mVideoName = videoName;
        mApp = app;
        filesForCounting = incFilesForCounting;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("unTag_Camera", "Start counting faces");
        //count faces
        for (int i = 0; i < filesForCounting.size(); i++) {
            File tmpFileForFaceDetecting = new File(filesForCounting.get(i));

            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap background_image = BitmapFactory.decodeFile(tmpFileForFaceDetecting.getAbsolutePath(), bitmap_options);
            if (background_image != null) {
                FaceDetector face_detector = new FaceDetector(
                        background_image.getWidth(), background_image.getHeight(),
                        MAX_FACES);
                FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
                int face_count = face_detector.findFaces(background_image, faces);
                Log.d("unTag_Camera", "Faces detected: " + face_count + " (camera id " + i + ")");
                count = count + face_count;
                //clean trash
                background_image.recycle();
                background_image = null;

                if (allowToast) {
                    Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                    intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                    String toastText = "camera " + i + " detect " + face_count + " faces";
                    intentOut.putExtra("toastText", toastText);
                    mApp.sendBroadcast(intentOut);
                }
            }
        }
        //save to xml
        finalCount();

        return null;
    }


    private void finalCount() {
        Log.d("unTag_Camera", "Faces detected from all cameras: " + count);
        DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.STATISTICS_DIR_NAME);
        File statisticFile = directoryWorks.checkLastStatisticFile(mContext);
        if (!UNLApp.getIsStatFileWriting()) {
            try {
                //lock statistic file
                UNLApp.setIsStatFileWriting(true);

                //LAST STATISTICS FILE
                //parse last statistic file
                Scanner scanner = new Scanner(new FileInputStream(statisticFile));
                ArrayList<ArrayList<String>> collection = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] myList = line.split(UNLConsts.CSV_SEPARATOR);
                    ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(myList));
                    collection.add(stringList);
                }
                scanner.close();

                //check old format
                if (collection.get(0).size() < 3) {
                    collection = null;
                    throw new IllegalAccessError("Olg format statistics file delete it!");
                } else {
                    if (!collection.get(0).get(2).equals("All shown")) {
                        collection = null;
                        throw new IllegalAccessError("Olg format statistics file delete it!");
                    }
                }

                //check video file name in the parsed data
                if (!collection.get(0).contains(mVideoName)) {
                    collection.get(0).add(mVideoName);
                    for (int i = 1; i < collection.size(); i++) {
                        collection.get(i).add("0");
                    }
                    collection.get(0).add("Shown");
                    for (int i = 1; i < collection.size(); i++) {
                        collection.get(i).add("0");
                    }
                } else {
                    int ind = collection.get(0).indexOf(mVideoName);
                    if (ind + 1 < collection.get(0).size()) {
                        if (!collection.get(0).get(ind + 1).equals("Shown")) {
                            collection.get(0).add(ind + 1, "Shown");
                            for (int i = 1; i < collection.size(); i++) {
                                collection.get(i).add(ind + 1, "0");
                            }
                        }
                    } else {
                        collection.get(0).add("Shown");
                        for (int i = 1; i < collection.size(); i++) {
                            collection.get(i).add("0");
                        }
                    }
                }

                //get column in the parsed data for writing info
                int colVidIndex = collection.get(0).indexOf(mVideoName);
                int colIndex = collection.get(0).indexOf(mVideoName) + 1;
                //get and parse current time
                int rowIndex = 1;
                SimpleDateFormat df = new SimpleDateFormat("k");
                String currTime;
                currTime = df.format(Calendar.getInstance().getTime());
                if (currTime.length() == 1) {
                    currTime = "0" + currTime;
                }
                if (currTime.equals("24")) {
                    currTime = "00";
                }
                currTime = currTime + ":00-";
                for (int i = 1; i < collection.size(); i++) {
                    if (collection.get(i).get(0).contains(currTime)) {
                        rowIndex = i;
                    }
                }
                //adding detected faced to the data
                //adding data to current video
                int countVidFromData = Integer.parseInt(collection.get(rowIndex).get(colVidIndex)) + 1;
                int countFacesFromData = Integer.parseInt(collection.get(rowIndex).get(colIndex));
                countFacesFromData = countFacesFromData + count;
                ArrayList<String> tmp = collection.get(rowIndex);
                tmp.set(colVidIndex, String.valueOf(countVidFromData));
                tmp.set(colIndex, String.valueOf(countFacesFromData));
                //adding data to all videos
                countVidFromData = Integer.parseInt(collection.get(rowIndex).get(1)) + 1;
                countFacesFromData = Integer.parseInt(collection.get(rowIndex).get(2));
                countFacesFromData = countFacesFromData + count;
                tmp.set(1, String.valueOf(countVidFromData));
                tmp.set(2, String.valueOf(countFacesFromData));
                collection.set(rowIndex, tmp);
                //adding data to current video in all time
                int lastRow = collection.size() - 1;
                countVidFromData = Integer.parseInt(collection.get(lastRow).get(colVidIndex)) + 1;
                countFacesFromData = Integer.parseInt(collection.get(lastRow).get(colIndex));
                countFacesFromData = countFacesFromData + count;
                ArrayList<String> tmp2 = collection.get(lastRow);
                tmp2.set(colVidIndex, String.valueOf(countVidFromData));
                tmp2.set(colIndex, String.valueOf(countFacesFromData));
                //adding data to all videos in all time
                countVidFromData = Integer.parseInt(collection.get(lastRow).get(1)) + 1;
                countFacesFromData = Integer.parseInt(collection.get(lastRow).get(2));
                countFacesFromData = countFacesFromData + count;
                tmp2.set(1, String.valueOf(countVidFromData));
                tmp2.set(2, String.valueOf(countFacesFromData));
                collection.set(lastRow, tmp2);
                //parse data to lines
                ArrayList<String> tmpStrings = new ArrayList<>();
                for (int i = 0; i < collection.size(); i++) {
                    String tmpString = collection.get(i).get(0);
                    for (int j = 1; j < collection.get(i).size(); j++) {
                        tmpString = tmpString + UNLConsts.CSV_SEPARATOR + collection.get(i).get(j);
                    }
                    tmpStrings.add(tmpString);
                }
                //write data to file
                FileWriter logWriter = new FileWriter(statisticFile);
                BufferedWriter out = new BufferedWriter(logWriter);
                for (int k = 0; k < tmpStrings.size(); k++) {
                    if (k > 0) {
                        out.newLine();
                    }
                    out.write(tmpStrings.get(k));
                }
                tmpStrings.clear();
                out.close();

                if (allowToast) {
                    Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                    intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                    String toastText = "cameras detect " + count + " faces";
                    intentOut.putExtra("toastText", toastText);
                    mApp.sendBroadcast(intentOut);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessError e) {
                e.printStackTrace();
                Log.e("unTag_Camera", "Old today statistics file! Delete it!");
                boolean statusDel = statisticFile.delete();
                Log.e("unTag_Camera", "Old today statistics file deleted = " + statusDel);
                finalCount();
            } finally {
                Log.d("unTag_Camera", "Release camera thread!");
                UNLApp.setIsCamerasWorking(false);
                UNLApp.setIsStatFileWriting(false);
            }
        } else {
            Log.w("unTag_Camera", "Do not write in statistics files because statistics is sending");
        }
    }
}
