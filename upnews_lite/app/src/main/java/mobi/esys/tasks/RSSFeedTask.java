package mobi.esys.tasks;

import android.os.AsyncTask;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import java.net.URL;

import mobi.esys.events.EventStartRSS;
import mobi.esys.net.NetWork;
import mobi.esys.rss.RSS;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by Артем on 26.02.2015.
 */
public class RSSFeedTask extends AsyncTask<URL, Void, String> {
    private final EventBus bus = EventBus.getDefault();
    private transient UNLApp mApp;
    private transient Handler handler;

    public RSSFeedTask(UNLApp app, Handler h) {
        mApp = app;
        handler = h;
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
            bus.post(new EventStartRSS(s));
        }
        handler.sendEmptyMessage(42);
    }


}
