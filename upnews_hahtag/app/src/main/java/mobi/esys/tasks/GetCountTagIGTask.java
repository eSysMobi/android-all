package mobi.esys.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.eventbus.EventGetCountTagsIGComplete;

public class GetCountTagIGTask extends AsyncTask<String, Void, Integer> {
    private final String mHashTag;
    private transient String mIgToken;

    private final String TAG = "unTag_GetCountIG";
    private EventBus bus = EventBus.getDefault();

    public GetCountTagIGTask(String tag, String igToken) {
        this.mHashTag = tag;
        mIgToken = igToken;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(final String... params) {
        Integer count = 0;
        try {
            final InstagramRequest request = new InstagramRequest(mIgToken);

            String edTag = mHashTag.toLowerCase();

            List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
            String response = request.requestGet("/tags/" + edTag, reqParams);

            if (isJSONValid(response)) {
                JSONObject resObject = new JSONObject(response);
                if (resObject.has("data")) {
                    JSONObject data = resObject.getJSONObject("data");
                    count = data.getInt("media_count");
                    Log.d(TAG, "Instagram tag is valid. We have " + count + " tags \"#" + edTag + "\"");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error checking");
        }
        return count;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        bus.post(new EventGetCountTagsIGComplete(result));
        super.onPostExecute(result);
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
