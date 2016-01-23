package mobi.esys.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
public class CameraShotTask extends Thread {
    private int MAX_FACES = 15;
    private Context mContext;
    private String mVideoName;
    private Camera mCamera = null;
    private int currentCamID;
    private SurfaceView svDriveAuth;
    private int count = 0;

    public CameraShotTask(Context context, String videoName) {
        mContext = context;
        mVideoName = videoName;
        svDriveAuth = new SurfaceView(context);
    }

    @Override
    public void run() {
        if (UNLApp.getCamerasID() != null && !mVideoName.isEmpty()) {
            if (UNLApp.getCamerasID()[0] != null) {
                int id = Integer.parseInt(UNLApp.getCamerasID()[0]);
                Log.d("unTag_Camera", "We have " + UNLApp.getCamerasID().length + " cameras. Start with id=" + id);
                currentCamID = id;
                getPhotoAndParce(id);
            }
        }
    }

    private void getPhotoAndParce(int cameraId) {
        boolean camOpened = false;

        SurfaceHolder sHolder = svDriveAuth.getHolder();
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        try {
//            mCamera = Camera.open(1);
            camOpened = safeCameraOpen(cameraId);
            mCamera.setPreviewDisplay(sHolder);
        } catch (IOException exception) {
            //mCamera.release();
            mCamera = null;
        }


        if (camOpened) {

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
                            + UNLConsts.GD_LOGO_DIR_NAME
                            + "/";
                    File logoDir = new File(logoDirPath);

                    if (!logoDir.exists()) {
                        logoDir.mkdir();
                    }

                    String tmpFilePath = UNLApp.getAppExtCachePath()
                            + UNLConsts.VIDEO_DIR_NAME
                            + UNLConsts.GD_LOGO_DIR_NAME
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

                    } catch (IOException e) {
                        //do something about it
                    }

                    releaseCameraAndPreview();

                    currentCamID = currentCamID + 1;
                    if (currentCamID < UNLApp.getCamerasID().length) {
                        getPhotoAndParce(currentCamID);
                    } else {
                        if (count > 0) {
                            finalCount();
                        }
                    }
                }
            };

            mCamera.takePicture(null, null, mCall);

        } else {
            currentCamID = currentCamID + 1;
            if (currentCamID < UNLApp.getCamerasID().length) {
                getPhotoAndParce(currentCamID);
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

        try {
            //parse last statistic file
            Scanner scanner = new Scanner(new FileInputStream(statisticFile));
            ArrayList<ArrayList<String>> collection = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] myList = line.split(";");
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
            int lastRow = collection.size()-1;
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
                    tmpString = tmpString + ";" + collection.get(i).get(j);
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


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
    //}


}
