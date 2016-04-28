package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 17.01.2016.
 */
public class CameraShotTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private String mVideoName;
    private Camera mCamera = null;
    private int currentCamID;
    private List<String> filesForCounting;
    private SurfaceHolder sHolder;
    private boolean allowToast = UNLConsts.ALLOW_TOAST;
    UNLApp mApp;
    static Clb callbackJPEG = null; //this callback must be static, otherwise GC kill it

    public CameraShotTask(SurfaceHolder mHolder, Context context, String videoName, UNLApp app) {
        mContext = context;
        sHolder = mHolder;
        mVideoName = videoName;
        mApp = app;
        filesForCounting = new ArrayList<>();
    }

    private class Clb implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("unTag_Camera", "Start JPEG callback from camera " + currentCamID);
            String tmpFilePath = UNLApp.getAppExtCachePath()
                    + UNLConsts.VIDEO_DIR_NAME
                    + UNLConsts.GD_STATISTICS_DIR_NAME    // or GD_LOGO_DIR_NAME
                    + "/"
                    + currentCamID + "_" + UNLConsts.STATISTICS_TEMP_PHOTO_FILE_NAME;//+ "tmp.jpg";
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

                filesForCounting.add(tmpFilePath);

                if (allowToast) {
                    Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                    intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_TOAST);
                    String toastText = "camera " + currentCamID + " saved foto";
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
                getPhotoAndSave(currentCamID);
            } else {
                finalCount();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (UNLApp.getCamerasID() != null && !mVideoName.isEmpty()) {
            if (!UNLApp.getIsCamerasWorking()) {
                if (UNLApp.getCamerasID()[0] != null) {
                    //check directory
                    String logoDirPath = UNLApp.getAppExtCachePath()
                            + UNLConsts.VIDEO_DIR_NAME
                            + UNLConsts.GD_STATISTICS_DIR_NAME
                            + "/";
                    File logoDir = new File(logoDirPath);
                    boolean successLogoDirCheck;
                    if (!logoDir.exists()) {
                        Log.d("unTag_Camera", "We haven't directory for pictures. Create directory.");
                        successLogoDirCheck = logoDir.mkdir();
                    } else {
                        successLogoDirCheck = true;
                    }
                    //start camera 0
                    if (successLogoDirCheck) {
                        UNLApp.setIsCamerasWorking(true);
                        currentCamID = UNLApp.getCamerasID()[0];
                        Log.d("unTag_Camera", "We have " + UNLApp.getCamerasID().length + " cameras. Start with id=" + currentCamID);
                        getPhotoAndSave(currentCamID);
                    }
                }
            } else {
                Log.d("unTag_Camera", "Cameras working in another thread");
            }
        } else {
            Log.d("unTag_Camera", "No cameras or name of video file is empty");
        }
        return null;
    }

    private void getPhotoAndSave(int cameraId) {
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
                getPhotoAndSave(currentCamID);
            } else {
                finalCount();
            }
        }
    }

    private void finalCount() {
        CameraCountTask ccTask = new CameraCountTask(mContext, mVideoName, mApp, filesForCounting);
        ccTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
