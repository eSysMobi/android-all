package mobi.esys.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import mobi.esys.upnews_lite.R;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 17.01.2016.
 */
public class CameraShotTask  {

    //TODO camera --------------------------------------------------------------------
    public android.hardware.Camera mCamera = null;
    ImageView tt2;

    public void getPhoto (ImageView tt, Context context) {
        tt2 = tt;
        //camera1 api
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 21) {
//            android.hardware.Camera mCamera = null;

            SurfaceView surfaceView = new SurfaceView(context);
            safeCameraOpen(0);
            try {
                mCamera.setPreviewDisplay(surfaceView.getHolder());
                mCamera.startPreview();
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        // get the bitmap from camera imageData
                        Bitmap bmpOfTheImageFromCamera = BitmapFactory.decodeByteArray(
                                data, 0, data.length);
                        tt2.setImageBitmap(bmpOfTheImageFromCamera);
                        releaseCameraAndPreview();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                releaseCameraAndPreview();
            }

        }

    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = android.hardware.Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
//            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
//        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;

        }
    }
//TODO camera --------------------------------------------------------------------

}
