package mobi.esys.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.esys.consts.ISConsts;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.upnews_hashtag.UNHApp;

/**
 * Created by Артем on 29.04.2015.
 */
public class CheckInstaTagTask extends AsyncTask<String, Void, Integer> {
    private transient String mHashTag;

    public CheckInstaTagTask(String hashTag) {
        mHashTag = hashTag;
    }

    @Override
    protected Integer doInBackground(String... params) {
        int count = 0;
        String response = "";
        if (mHashTag.length() >= 2) {
            final InstagramRequest request = new InstagramRequest(params[0]);

            String edTag = mHashTag.substring(1).toLowerCase(Locale.ENGLISH);
            try {
                final List<NameValuePair> reqParams = new ArrayList<NameValuePair>(
                        1);
//                    reqParams.add(new BasicNameValuePair("count", String
//                            .valueOf(ISConsts.instagramconsts.instagram_page_count)));
//                    response = request.requestGet("/tags/" + edTag
//                            + "/media/recent", reqParams);
                response = request.requestGet("/tags/" + edTag, reqParams);
                if (isJSONValid(response)) {
                    JSONObject resObject = new JSONObject(response);
                    if (resObject.has("meta")
                            && resObject.getJSONObject("meta").get("code").equals(200)) {
                        count = (Integer) resObject.getJSONObject("data").get("media_count");
                    }
                }
            } catch (Exception e) {
                Log.e("unTag_CheckInstaTag", "Error: " + e.getMessage());
            }
        }
        return count;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
