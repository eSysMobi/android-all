package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
//import android.view.SurfaceView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * Created by ZeyUzh on 17.01.2016.
 */
public class CameraShotTask implements Runnable {
    private int MAX_FACES = 15;
    private Context mContext;
    private String mVideoName;
    private Camera mCamera = null;
    private int currentCamID;
//    private SurfaceView svDriveAuth;
    private int count = 0;
    private SurfaceHolder sHolder;
    private boolean allowToast = UNLConsts.ALLOW_TOAST;
    UNLApp mApp;

    public CameraShotTask(SurfaceHolder mHolder, Context context, String videoName, UNLApp app) {
        mContext = context;
//        mVideoName = videoName;
//        svDriveAuth = new SurfaceView(context);
//        sHolder = svDriveAuth.getHolder();
//        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sHolder = mHolder;
        mVideoName = videoName;
        mApp = app;
    }

    @Override
    public void run() {
        if (UNLApp.getCamerasID() != null && !mVideoName.isEmpty()) {
            if (!UNLApp.getIsCamerasWorking()) {
                UNLApp.setIsCamerasWorking(true);
                try {
                    if (UNLApp.getCamerasID()[0] != null) {
                        int id = Integer.parseInt(UNLApp.getCamerasID()[0]);
                        Log.d("unTag_Camera", "We have " + UNLApp.getCamerasID().length + " cameras. Start with id=" + id);
                        currentCamID = id;
                        getPhotoAndParse(id);
                    }
                } finally {
                    Log.d("unTag_Camera", "Release camera thread!");
                    UNLApp.setIsCamerasWorking(false);
                }
            } else {
                Log.d("unTag_Camera", "Cameras working in another thread");
            }
        }
    }

    private void getPhotoAndParse(int cameraId) {
        boolean camOpened = safeCameraOpen(cameraId);
        if (camOpened) {
            try {
                mCamera.setPreviewDisplay(sHolder);
            } catch (IOException e) {
                e.printStackTrace();
                if (allowToast) {
                    Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                    intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                    String toastText = "Problem setPreviewDisplay camera id " + currentCamID;
                    intentOut.putExtra("toastText", toastText);
                    mApp.sendBroadcast(intentOut);
                }
            }

            if (allowToast) {
                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                String toastText = "camera " + currentCamID + " opened";
                intentOut.putExtra("toastText", toastText);
                mApp.sendBroadcast(intentOut);
            }

            Camera.Parameters parameters = mCamera.getParameters();

            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF))
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

            List<String> whiteBalance = parameters.getSupportedWhiteBalance();
            if (whiteBalance != null && whiteBalance.contains(Camera.Parameters.WHITE_BALANCE_AUTO))
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            if (sizes != null && sizes.size() > 0) {
                Camera.Size size = sizes.get(0);
                parameters.setPictureSize(size.width, size.height);
            }

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            if (previewSizes != null) {
                Camera.Size previewSize = previewSizes.get(previewSizes.size() - 1);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            mCamera.setParameters(parameters);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                mCamera.enableShutterSound(false);
            mCamera.startPreview();

            Camera.PictureCallback mCall = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    String logoDirPath = UNLApp.getAppExtCachePath()
                            + UNLConsts.VIDEO_DIR_NAME
                            + UNLConsts.GD_STATISTICS_DIR_NAME
                            + "/";
                    File logoDir = new File(logoDirPath);

                    boolean successLogoDirCheck = true;
                    if (!logoDir.exists()) {
                        successLogoDirCheck = logoDir.mkdir();
                    }

                    if (successLogoDirCheck) {
                        String tmpFilePath = UNLApp.getAppExtCachePath()
                                + UNLConsts.VIDEO_DIR_NAME
                                + UNLConsts.GD_STATISTICS_DIR_NAME    // or GD_LOGO_DIR_NAME
                                + "/"
                                + UNLConsts.STATISTICS_TEMP_PHOTO_FILE_NAME;//+ "tmp.jpg";
                        File tmpFileForFaceDetecting = new File(tmpFilePath);

                        if (tmpFileForFaceDetecting.exists()) {
                            tmpFileForFaceDetecting.delete();
                        }

                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(tmpFileForFaceDetecting);
                            fos.write(data);
                            //fos.flush();
                            fos.close();

                            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap background_image = BitmapFactory.decodeFile(tmpFileForFaceDetecting.getAbsolutePath(), bitmap_options);
                            FaceDetector face_detector = new FaceDetector(
                                    background_image.getWidth(), background_image.getHeight(),
                                    MAX_FACES);
                            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
                            int face_count = face_detector.findFaces(background_image, faces);
                            Log.d("unTag_Camera", "Faces detected: " + face_count + " (camera id " + currentCamID + ")");
                            count = count + face_count;

                            if (allowToast) {
                                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                                String toastText = "camera " + currentCamID + " detect " + face_count + " faces";
                                intentOut.putExtra("toastText", toastText);
                                mApp.sendBroadcast(intentOut);
                            }

                        } catch (IOException e) {
                            Log.d("unTag_Camera", "Problem with writing picture on SD from camera id " + currentCamID + ": " + e.toString());
                            if (allowToast) {
                                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                                String toastText = "Problem with writing picture on SD from camera id " + currentCamID;
                                intentOut.putExtra("toastText", toastText);
                                mApp.sendBroadcast(intentOut);
                            }
                        }

                        releaseCameraAndPreview();

                        currentCamID = currentCamID + 1;
                        if (currentCamID < UNLApp.getCamerasID().length) {
                            getPhotoAndParse(currentCamID);
                        } else {
                            if (count > 0) {
                                finalCount();
                            }
                        }
                    }
                }
            };

            mCamera.takePicture(null, null, mCall);

        } else {
            Log.e("unTag_Camera", "Camera id " + currentCamID + " is not open. Next camera.");
            currentCamID = currentCamID + 1;
            if (currentCamID < UNLApp.getCamerasID().length) {
                getPhotoAndParse(currentCamID);
            } else {
                if (count > 0) {
                    finalCount();
                }
            }
        }
    }

    private void finalCount() {
        Log.d("unTag_Camera", "Faces detected from all cameras: " + count);
        DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.STATISTICS_DIR_NAME);
        File statisticFile = directoryWorks.checkLastStatisticFile(mContext);
//        File allStatisticFile = directoryWorks.checkAllStatisticFile(mContext);

        try {
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
            //check video file name in the parsed data
            if (!collection.get(0).contains(mVideoName)) {
                collection.get(0).add(mVideoName);
                for (int i = 1; i < collection.size(); i++) {
                    collection.get(i).add("0");
                }
            }
            //get column in the parsed data for writing info
            int colIndex = collection.get(0).indexOf(mVideoName);
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
            int countFacesFromData = Integer.parseInt(collection.get(rowIndex).get(colIndex));
            countFacesFromData = countFacesFromData + count;
            ArrayList<String> tmp = collection.get(rowIndex);
            tmp.set(colIndex, String.valueOf(countFacesFromData));
            //adding data to all videos
            countFacesFromData = Integer.parseInt(collection.get(rowIndex).get(1));
            countFacesFromData = countFacesFromData + count;
            tmp.set(1, String.valueOf(countFacesFromData));
            collection.set(rowIndex, tmp);
            //adding data to current video in all time
            int lastRow = collection.size() - 1;
            countFacesFromData = Integer.parseInt(collection.get(lastRow).get(colIndex));
            countFacesFromData = countFacesFromData + count;
            ArrayList<String> tmp2 = collection.get(lastRow);
            tmp2.set(colIndex, String.valueOf(countFacesFromData));
            //adding data to all videos in all time
            countFacesFromData = Integer.parseInt(collection.get(lastRow).get(1));
            countFacesFromData = countFacesFromData + count;
            tmp2.set(1, String.valueOf(countFacesFromData));
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

//            //ALL STATISTICS FILE
//            //parse all statistic file
//            Scanner scannerAll = new Scanner(new FileInputStream(allStatisticFile));
//            ArrayList<ArrayList<String>> collectionAll = new ArrayList<>();
//            while (scannerAll.hasNextLine()) {
//                String line = scannerAll.nextLine();
//                String[] myList = line.split(UNLConsts.CSV_SEPARATOR);
//                ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(myList));
//                collectionAll.add(stringList);
//            }
//            scannerAll.close();
//            //check video file name in the parsed data
//            if (!collectionAll.get(0).contains(mVideoName)) {
//                collectionAll.get(0).add(mVideoName);
//                for (int i = 1; i < collectionAll.size(); i++) {
//                    collectionAll.get(i).add("0");
//                }
//            }
//            //get column in the parsed data for writing info
//            int colIndexAll = collectionAll.get(0).indexOf(mVideoName);
//            //get and parse current time
//            int rowIndexAll = 1;
//            String currTimeAll;
//            currTimeAll = df.format(Calendar.getInstance().getTime());
//            if (currTimeAll.length() == 1) {
//                currTimeAll = "0" + currTimeAll;
//            }
//            if (currTimeAll.equals("24")) {
//                currTimeAll = "00";
//            }
//            currTimeAll = currTimeAll + ":00-";
//            for (int i = 1; i < collectionAll.size(); i++) {
//                if (collectionAll.get(i).get(0).contains(currTimeAll)) {
//                    rowIndexAll = i;
//                }
//            }
//            //adding detected faced to the data
//            //adding data to current video
//            int countFacesFromDataAll = Integer.parseInt(collectionAll.get(rowIndexAll).get(colIndexAll));
//            countFacesFromDataAll = countFacesFromDataAll + count;
//            ArrayList<String> tmpAll = collectionAll.get(rowIndexAll);
//            tmpAll.set(colIndexAll, String.valueOf(countFacesFromDataAll));
//            //adding data to all videos
//            countFacesFromDataAll = Integer.parseInt(collectionAll.get(rowIndexAll).get(1));
//            countFacesFromDataAll = countFacesFromDataAll + count;
//            tmpAll.set(1, String.valueOf(countFacesFromDataAll));
//            collectionAll.set(rowIndexAll, tmpAll);
//            //adding data to current video in all time
//            int lastRowAll = collectionAll.size() - 1;
//            countFacesFromDataAll = Integer.parseInt(collectionAll.get(lastRowAll).get(colIndexAll));
//            countFacesFromDataAll = countFacesFromDataAll + count;
//            ArrayList<String> tmpAll2 = collectionAll.get(lastRowAll);
//            tmpAll2.set(colIndexAll, String.valueOf(countFacesFromDataAll));
//            //adding data to all videos in all time
//            countFacesFromDataAll = Integer.parseInt(collectionAll.get(lastRowAll).get(1));
//            countFacesFromDataAll = countFacesFromDataAll + count;
//            tmpAll2.set(1, String.valueOf(countFacesFromDataAll));
//            collectionAll.set(lastRowAll, tmpAll2);
//            //parse data to lines
//            ArrayList<String> tmpStringsAll = new ArrayList<>();
//            for (int i = 0; i < collectionAll.size(); i++) {
//                String tmpString = collectionAll.get(i).get(0);
//                for (int j = 1; j < collectionAll.get(i).size(); j++) {
//                    tmpString = tmpString + UNLConsts.CSV_SEPARATOR + collectionAll.get(i).get(j);
//                }
//                tmpStringsAll.add(tmpString);
//            }
//            //write data to file
//            FileWriter logWriterAll = new FileWriter(allStatisticFile);
//            BufferedWriter outAll = new BufferedWriter(logWriterAll);
//            for (int k = 0; k < tmpStringsAll.size(); k++) {
//                if (k > 0) {
//                    outAll.newLine();
//                }
//                outAll.write(tmpStringsAll.get(k));
//            }
//            tmpStringsAll.clear();
//            outAll.close();

            //sending statistics in GD
            signalSendStatToGD();
            //or
//            SendStatisticsToGD sstGD = new SendStatisticsToGD(mApp);
//            sstGD.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //if need send statistics to GD
    private void signalSendStatToGD() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_SEND_STATDATA_TO_GD);
        intentOut.putExtra("source", "CameraShotTask");
        mApp.sendBroadcast(intentOut);
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e("unTag_Camera", "failed to open Camera");
            mCamera = null;
            e.printStackTrace();
        }
        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
//            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;

        }
    }

}
