package mobi.esys.upnews_play;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import mobi.esys.upnews_play.playback.VideoPlayback;

@Fullscreen
@WindowFeature({Window.FEATURE_NO_TITLE})
@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
    @ViewById
    VideoView video;
    @ViewById
    ImageView slider;
    @ViewById
    ImageView logo;

    private transient AlertDialog alert;
    private transient VideoPlayback playback;
    private transient App app;



    @AfterViews
    void init() {

        this.app=(App)getApplication();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String[] fileNames=app.getAppFolder().list();
        Log.d("file names", Arrays.toString(fileNames));
        File logoFileJPG=new File(app.getAppFolder()
                .getAbsolutePath(),"logo.jpg");
        File logoFilePNG=new File(app.getAppFolder()
                .getAbsolutePath(),"logo.png");



        File usbHost=new File("usb://");
        Log.d("usb host", Arrays.toString(usbHost.list()));


        Log.d("play files list", Arrays.toString(fileNames));
        if(fileNames!=null) {
            if (fileNames.length > 0) {
                startPlayBack();
                if (Arrays.asList(fileNames).contains(logoFileJPG.getName())) {
                    setLogo(logoFileJPG);
                } else if (Arrays.asList(fileNames).contains(logoFilePNG.getName())) {
                    setLogo(logoFilePNG);
                }
            } else {
                showIfFolderEmpty();
            }
        }
    }

    public void startPlayBack(){
        playback = new VideoPlayback(video, app.getAppFolder(),this,slider);
        playback.init();
        playback.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(playback!=null) {
            playback.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(playback!=null) {
            playback.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(app.getAppFolder()!=null&&app.getAppFolder().listFiles()!=null) {
            if (app.getAppFolder().listFiles().length != 0) {
                if (playback != null) {
                    playback.resume();
                }
            } else {
                showIfFolderEmpty();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alert!=null) {
            alert.dismiss();
        }
    }

    public void showIfFolderEmpty() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning")
                .setMessage(FilenameUtils.getBaseName(
                        app.getAppFolder().getAbsolutePath()).concat(" folder is empty!"))
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.dismiss();
                            }
                        });
        alert = builder.create();
        alert.show();
    }

    private void setLogo(File logoFile){
        Bitmap bitmap=null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            bitmap = BitmapFactory.decodeStream(
                    new FileInputStream(logoFile), null, options);

        } catch (FileNotFoundException ignored) {
        }

        logo.setImageBitmap(bitmap);
    }


}
