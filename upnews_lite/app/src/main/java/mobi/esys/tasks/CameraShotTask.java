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
public class CameraShotTask implements Runnable{
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
    static Clb callbackJPEG = null; //this callback must be static, otherwise GC kill it

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

    private class Clb implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("unTag_Camera", "Start JPEG callback from camera " + currentCamID);
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
                    Log.d("unTag_Camera", "Photo from camera " + currentCamID + " is written on SD");

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
                    //clean trash
                    background_image.recycle();
                    background_image = null;

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
                    finalCount();
                }
            }
        }
    }

    @Override
    public void run() {
        if (UNLApp.getCamerasID() != null && !mVideoName.isEmpty()) {
            if (!UNLApp.getIsCamerasWorking()) {
                UNLApp.setIsCamerasWorking(true);
                try {
                    if (UNLApp.getCamerasID()[0] != null) {
                        int id = UNLApp.getCamerasID()[0];
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
            Log.d("unTag_Camera", "Camera " + currentCamID + " is open");

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

            callbackJPEG = new Clb();
            mCamera.takePicture(null, null, callbackJPEG);

        } else {
            Log.e("unTag_Camera", "Camera id " + currentCamID + " is not open. Next camera.");
            currentCamID = currentCamID + 1;
            if (currentCamID < UNLApp.getCamerasID().length) {
                getPhotoAndParse(currentCamID);
            } else {
                finalCount();
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

            //check old format
            if (collection.get(0).size()<3){
                collection = null;
                throw new IllegalAccessError("Olg format statistics file delete it!");
            } else {
                if (!collection.get(0).get(2).equals("All shown")){
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
            }else{
                int ind = collection.get(0).indexOf(mVideoName);
                if(ind+1<collection.get(0).size()){
                    if(!collection.get(0).get(ind+1).equals("Shown")){
                        collection.get(0).add(ind+1,"Shown");
                        for (int i = 1; i < collection.size(); i++) {
                            collection.get(i).add(ind+1,"0");
                        }
                    }
                } else{
                    collection.get(0).add("Shown");
                    for (int i = 1; i < collection.size(); i++) {
                        collection.get(i).add("0");
                    }
                }
            }

            //get column in the parsed data for writing info
            int colVidIndex = collection.get(0).indexOf(mVideoName);
            int colIndex = collection.get(0).indexOf(mVideoName)+1;
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
        } catch (IllegalAccessError e){
            e.printStackTrace();
            Log.e("unTag_Camera", "Old today statistics file! Delete it!");
            boolean statusDel = statisticFile.delete();
            Log.e("unTag_Camera", "Old today statistics file deleted = " + statusDel);
            finalCount();
        }
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