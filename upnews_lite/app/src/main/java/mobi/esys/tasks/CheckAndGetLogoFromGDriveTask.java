package mobi.esys.tasks;

import android.content.Intent;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 12.11.2015.
 */
public class CheckAndGetLogoFromGDriveTask extends Thread {
    private UNLApp app;
    private UNLServer server;
    private transient Drive drive;

    public CheckAndGetLogoFromGDriveTask(UNLApp incomingApp) {
        app = incomingApp;
        drive = app.getDriveService();
    }


    @Override
    public void run() {

        if (NetWork.isNetworkAvailable(app)) {
            server = new UNLServer(app);

            GDFile newRemoteLogo = server.getGdLogo();

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
        }
    }

    private void signalUseNewLogo() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_OK);
        app.sendBroadcast(intentOut);
    }

    private void signalUseStandardLogo() {
        Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
        intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.GET_LOGO_STATUS_NOT_OK);
        app.sendBroadcast(intentOut);
    }
}
