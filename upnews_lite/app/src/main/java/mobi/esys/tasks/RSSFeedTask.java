package mobi.esys.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import java.net.URL;

import mobi.esys.constants.UNLConsts;
import mobi.esys.net.NetWork;
import mobi.esys.rss.RSS;
import mobi.esys.upnews_lite.FirstVideoActivity;
import mobi.esys.upnews_lite.FullscreenActivity;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by Артем on 26.02.2015.
 */
public class RSSFeedTask extends AsyncTask<URL, Void, String> {
    private transient String mActName;
    private transient UNLApp mApp;
    private transient Handler handler;

    public RSSFeedTask(UNLApp app, Handler h, String actName) {
        mApp = app;
        handler = h;
        mActName = actName;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */

    @Override
    protected String doInBackground(URL... params) {
        String feed = "";
        if (NetWork.isNetworkAvailable(mApp)) {
            feed = RSS.parseRSSURL(params[0], " <font color='red'> | </font> ");
        } else {
            cancel(true);
        }
        return feed;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null && !s.isEmpty()) {
            if ("first".equals(mActName)) {
                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION_FIRST);
                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_START_RSS);
                intentOut.putExtra("rssToShow", s);
                mApp.sendBroadcast(intentOut);
            } else {
                Intent intentOut = new Intent(UNLConsts.BROADCAST_ACTION);
                intentOut.putExtra(UNLConsts.SIGNAL_TO_FULLSCREEN, UNLConsts.SIGNAL_START_RSS);
                intentOut.putExtra("rssToShow", s);
                mApp.sendBroadcast(intentOut);
            }
        }
        handler.sendEmptyMessage(42);
    }


}
