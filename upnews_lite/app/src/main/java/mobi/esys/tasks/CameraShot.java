package mobi.esys.tasks;

import android.hardware.Camera;
import android.util.Log;

import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 17.01.2016.
 */
public class CameraShot implements Camera.FaceDetectionListener {
    int facesCount;
    private int currentCam;
    String camerasID[];
    Camera mCamera = null;

    public CameraShot() {
        facesCount = 0;
        currentCam = 0;
        camerasID = UNLApp.getCamerasID();
    }


    public void takeShot() {
        mCamera = Camera.open(Integer.parseInt(camerasID[currentCam]));
        mCamera.setFaceDetectionListener(this);
        mCamera.startFaceDetection();
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d("unTag_CameraShot", "Camera " + currentCam +" detect " + faces.length + "faces");
        facesCount = facesCount + faces.length;
        mCamera.stopFaceDetection();
        mCamera.release();
        currentCam++;
        if (currentCam < UNLApp.getCamerasID().length) {
            takeShot();
        }else{
            currentCam = 0;
            Log.d("unTag_CameraShot", "Final count faces detected: " + facesCount);
        }
    }

}
