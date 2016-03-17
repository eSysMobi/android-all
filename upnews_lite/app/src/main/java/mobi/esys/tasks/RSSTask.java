package mobi.esys.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.UNLApp;

public class RSSTask extends AsyncTask<Void, Void, URL> {
    private transient Context mContext;
    private transient String mActName;
    private transient UNLServer server;
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient String rssString = "";

    private transient String TAG = "unTag_RSSTask";

    public RSSTask(Context context, String actName, UNLApp app) {
        mApp = app;
        mContext = context;
        mActName = actName;
        server = new UNLServer(app);
        drive = app.getDriveService();

    }

    @Override
    protected URL doInBackground(Void... params) {
        URL rssURL = null;
        DirectoryWorks dw = new DirectoryWorks(UNLConsts.RSS_DIR_NAME);
        File localRssFile = dw.getRSSFile();

        //check inet and get MD5 from gd RSS-file
        if (NetWork.isNetworkAvailable(mApp)) {
            GDFile gdRSS = server.getGdRSS();
            String gdRSSMD5 = gdRSS.getGdFileMD5();

            FileWorks fw = new FileWorks(localRssFile);
            String localMD5 = fw.getFileMD5();

            //check MD5 from local and gd RSS-files
            if (!localMD5.isEmpty() && localMD5.equals(gdRSSMD5)) {
                Log.d(TAG,"Local and GD rss.txt is identical");
                //all ok
            } else {
                //need download gdRSS to the local file
                Log.d(TAG,"Local and GD rss.txt NOT identical. Replace local...");
                try {
                    localRssFile.delete();
                    localRssFile.createNewFile();   //TODO is this really need?
                    if (gdRSS != null && gdRSS.getGdFileInst().getDownloadUrl() != null) {
                        com.google.api.client.http.HttpResponse resp = drive
                                .getRequestFactory()
                                .buildGetRequest(
                                        new GenericUrl(gdRSS.getGdFileInst().getDownloadUrl()))
                                .execute();
                        InputStream is = resp.getContent();
                        OutputStream output = new FileOutputStream(localRssFile);
                        try {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        } catch (Exception e) {
                            //Fail.
                            cancel(true);
                            e.printStackTrace();
                        } finally {
                            output.close();
                            is.close();
                            resp.disconnect();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
            }
        }else {
            Log.d(TAG,"No inet. Can't check GD rss.txt, use local version.");
        }
        //read local file
        try {
            FileReader fileReader = new FileReader(localRssFile);
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            StringBuilder bigLine = new StringBuilder();
            int countLines = -1;
            while ((countLines < UNLConsts.RSS_SIZE) && ((line = br.readLine()) != null)) {
                countLines++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                //check if this line is url
                if (countLines < 3) {
                    if (countLines == 0) {
                        if (line.startsWith("\uFEFF") || line.startsWith("#"))
                            continue;
                    }
                    try {
                        rssURL = new URL(line);
                        break;
                    } catch (MalformedURLException e) {
                        // this is not url
                    }
                }
                bigLine.append(line).append(" ").append(" <font color='red'> | </font> ").append(" ");
            }
            rssString = bigLine.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            cancel(true);
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
        }

        return rssURL;
    }


    @Override
    protected void onPostExecute(URL rssURL) {
        super.onPostExecute(rssURL);

        if (rssURL != null) {
            //we have url - call RSSFeedTask
            Log.d(TAG,"URL in rss.txt download XML in RSSFeedTask");
            RSSFeedTask rssFeedTask = new RSSFeedTask(mContext, mActName, mApp);
            rssFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rssURL);
        } else {
            //we have line - show rss in activity
            Log.d(TAG,"Strings in rss.txt. Show it.");
            if (!rssString.isEmpty()) {
                if ("first".equals(mActName)) {
                    ((FirstVideoActivity) mContext).startRSS(rssString);
                    ((FirstVideoActivity) mContext).recToMP("rss_start", "Start rss feed");
                } else {
                    ((FullscreenActivity) mContext).startRSS(rssString);
                    ((FullscreenActivity) mContext).recToMP("rss_start", "Start rss feed");

                }
            }
        }
    }
}
