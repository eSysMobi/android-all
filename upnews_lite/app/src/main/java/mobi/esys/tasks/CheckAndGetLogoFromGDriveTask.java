package mobi.esys.tasks;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobi.esys.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.events.EventLogoLoadingComplete;
import mobi.esys.net.NetWork;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 12.11.2015.
 */
public class CheckAndGetLogoFromGDriveTask extends AsyncTask<Void, Void, Void> {
    private UNLApp app;
    private GDFile newRemoteLogo;
    private transient Drive drive;
    private Handler handler;

    public CheckAndGetLogoFromGDriveTask(UNLApp incomingApp, Handler incHandler, GDFile remoteLogo) {
        app = incomingApp;
        drive = UNLApp.getDriveService();
        handler = incHandler;
        newRemoteLogo = remoteLogo;
    }


    @Override
    protected Void doInBackground(Void... params) {
        if (NetWork.isNetworkAvailable(app)) {
//            String newMD5 = newRemoteLogo.getGdFileMD5();
            if (!newRemoteLogo.getGdFileSize().equals("0")) {
                String logoDirPath = UNLApp.getAppExtCachePath()
                        + UNLConsts.VIDEO_DIR_NAME
                        + UNLConsts.GD_LOGO_DIR_NAME
                        + "/";
                File logoDir = new File(logoDirPath);

                if (!logoDir.exists()) {
                    logoDir.mkdir();
                }

                String oldFilePath = UNLApp.getAppExtCachePath()
                        + UNLConsts.VIDEO_DIR_NAME
                        + UNLConsts.GD_LOGO_DIR_NAME
                        + "/"
                        + UNLConsts.GD_LOGO_FILE_TITLE;
                File oldLocalLogo = new File(oldFilePath);

                if (oldLocalLogo.exists()) {
                    //Check and change old logo to new logo from Google Disk
//                FileWorks fw = new FileWorks(oldFilePath);
//                if (!fw.getFileMD5().equals(newMD5)) {
                    //Delete old logo and download new
                    try {
                        oldLocalLogo.delete();
                        File newLocalLogo = new File(oldFilePath);
                        if (newRemoteLogo != null && newRemoteLogo.getGdFileInst().getDownloadUrl() != null) {
                            com.google.api.client.http.HttpResponse resp = drive
                                    .getRequestFactory()
                                    .buildGetRequest(
                                            new GenericUrl(newRemoteLogo.getGdFileInst().getDownloadUrl()))
                                    .execute();
                            InputStream is = resp.getContent();
                            OutputStream output = new FileOutputStream(newLocalLogo);
                            try {
                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    output.write(buffer, 0, read);
                                }
                                output.flush();
                            } catch (Exception e) {
                                //Fail.
                                signalUseStandardLogo();
                                e.printStackTrace();
                            } finally {
                                is.close();
                                output.close();
                            }
                            //New logo downloaded, use it
                            signalUseNewLogo();
                        } else {
                            //Fail. New logo is corrupt, use standard
                            signalUseStandardLogo();
                        }
                    } catch (IOException e) {
                        //Fail.
                        signalUseStandardLogo();
                        e.printStackTrace();
                    }
//                } else {
//                    //New logo == old logo, not need to download, use old
//                    signalUseNewLogo();
//                }
                } else {
                    //Download new logo and save it in external store
                    try {
                        File newLocalLogo = new File(oldFilePath);
                        if (newRemoteLogo != null && newRemoteLogo.getGdFileInst().getDownloadUrl() != null) {
                            com.google.api.client.http.HttpResponse resp = drive
                                    .getRequestFactory()
                                    .buildGetRequest(
                                            new GenericUrl(newRemoteLogo.getGdFileInst().getDownloadUrl()))
                                    .execute();
                            InputStream is = resp.getContent();
                            OutputStream output = new FileOutputStream(newLocalLogo);
                            try {
                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    output.write(buffer, 0, read);
                                }
                                output.flush();
                            } catch (Exception e) {
                                //Fail.
                                signalUseStandardLogo();
                                e.printStackTrace();
                            } finally {
                                is.close();
                                output.close();
                            }
                            //New logo downloaded, use it
                            signalUseNewLogo();
                        } else {
                            //Fail. New logo is corrupt, use standard
                            signalUseStandardLogo();
                        }
                    } catch (IOException e) {
                        //Fail.
                        signalUseStandardLogo();
                        e.printStackTrace();
                    }
                }
            } else {
                //Fail. In Google Drive SUDDENLY no logo file, or it corrupt
                signalUseStandardLogo();
            }
        } else {
            //Fail. We have no inet
            signalUseStandardLogo();
        }
        return null;
    }

    private void signalUseNewLogo() {
        EventBus.getDefault().post(new EventLogoLoadingComplete(true));
        handler.sendEmptyMessage(42);
    }

    private void signalUseStandardLogo() {
        EventBus.getDefault().post(new EventLogoLoadingComplete(false));
        handler.sendEmptyMessage(42);
    }

}
