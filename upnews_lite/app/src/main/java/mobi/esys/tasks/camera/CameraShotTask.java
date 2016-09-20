package mobi.esys.tasks.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.UNLConsts;
import mobi.esys.events.EventToast;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 17.01.2016.
 */
public class CameraShotTask extends AsyncTask<Void, Void, Void> implements SaveCallback {
    private final EventBus bus = EventBus.getDefault();
    private Context mContext;
    private String mVideoName;
    private Camera mCamera = null;
    private int currentCamID;
    private SurfaceHolder sHolder;
    private boolean allowToast = UNLConsts.ALLOW_TOAST;
    private List<byte[]> datas = new ArrayList<>();
    private Clb callback = new Clb(this);

    public CameraShotTask(SurfaceHolder mHolder, Context context, String videoName) {
        mContext = context;
        sHolder = mHolder;
        mVideoName = videoName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!mVideoName.isEmpty()) {
            if (UNLApp.getCamerasID() != null && UNLApp.getCamerasID().length > 0) {
                if (!UNLApp.getIsCamerasWorking()) {
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
                    //start recursive camera shots from camera 0
                    if (successLogoDirCheck) {
                        UNLApp.setIsCamerasWorking(true);
                        Log.d("unTag_Camera", "We have " + UNLApp.getCamerasID().length + " cameras.");
                        currentCamID = UNLApp.getCamerasID()[0];
                        getNextPhoto();
                    }
                } else {
                    Log.d("unTag_Camera", "Cameras working in another thread");
                }
            } else {
                Log.d("unTag_Camera", "No cameras");
                finalCount();
            }
        } else {
            Log.d("unTag_Camera", "Name of video file is empty");
        }
        return null;
    }

    private void getNextPhoto() {
        if (currentCamID < UNLApp.getCamerasID().length) {
            getPhotoAndSave(currentCamID);
        } else {
            finalCount();
        }
    }

    private void getPhotoAndSave(int cameraId) {
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(cameraId);

            if (mCamera != null) {
                mCamera.setPreviewDisplay(sHolder);

                if (allowToast) {
                    String toastText = "camera " + cameraId + " opened";
                    bus.post(new EventToast(toastText));
                }
                Log.d("unTag_Camera", "Camera " + cameraId + " is open");

                //set parameters
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

                mCamera.takePicture(null, null, callback);

            } else {
                throw new IOException();
            }
        } catch (Exception e) {
            Log.e("unTag_Camera", "Camera id " + currentCamID + " is not open. Next camera. Error: " + e.getMessage());
            mCamera = null;
            if (allowToast) {
                String toastText = "Problem with camera id " + cameraId;
                bus.post(new EventToast(toastText));
            }
            currentCamID++;
            getNextPhoto();
        }
    }

    @Override
    public void prepareToSave(byte[] pict) {
        datas.add(pict);
        releaseCameraAndPreview();
        currentCamID++;
        getNextPhoto();
    }

    private class Clb implements Camera.PictureCallback {
        private SaveCallback callback;

        public Clb(SaveCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            callback.prepareToSave(data);
        }
    }

    private void finalCount() {
        //save and count
        CameraCountTask ccTask = new CameraCountTask(mContext, mVideoName, datas);
        ccTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
}
