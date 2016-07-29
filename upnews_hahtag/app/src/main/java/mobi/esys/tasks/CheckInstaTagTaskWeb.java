package mobi.esys.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.esys.consts.ISConsts;
import mobi.esys.eventbus.EventIgCheckingComplete;
import mobi.esys.instagram.model.InstagramPhoto;


public class CheckInstaTagTaskWeb extends AsyncTask<Void, Void, Boolean> {

    private EventBus bus = EventBus.getDefault();

    private String mHashTag;
    private boolean needOnlyCount;
    private SharedPreferences preferences;

    private int count;
    private List<InstagramPhoto> igPhotos;

    public CheckInstaTagTaskWeb(String mHashTag, boolean needOnlyCount, SharedPreferences preferences) {
        this.mHashTag = mHashTag;
        this.needOnlyCount = needOnlyCount;
        this.preferences = preferences;
        igPhotos = new ArrayList<>();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;
        if (mHashTag.length() >= 2) {
            String edTag = mHashTag.toLowerCase();

            String inputStr;
            HttpURLConnection urlConnection = null;
            try {
                String path = "https://www.instagram.com/explore/tags/" + edTag;
                URL url = new URL(path);
                Log.w("unTagCheckInstaTag", "request: " + path);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2 * 1000); // 2 sec timeout

                InputStream is = urlConnection.getInputStream();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String startPrefix = "<script type=\"text/javascript\">window._sharedData";
                while ((inputStr = streamReader.readLine()) != null) {
                    if (inputStr.startsWith(startPrefix)) {
                        break;
                    }
                }
                is.close();

                int srtCount = inputStr.indexOf("media\": {\"count\":") + 18;
                int endCount = inputStr.indexOf(",", srtCount);
                String countStr = inputStr.substring(srtCount, endCount);
                count = Integer.parseInt(countStr);


                if (!needOnlyCount) {
                    int srt = inputStr.indexOf("}, \"nodes\": [") + 2;
                    int end = inputStr.indexOf(", \"content_advisory");
                    inputStr = "{" + inputStr.substring(srt, end);

                    JSONObject jsonObject = new JSONObject(inputStr);

                    // Getting JSON Array node
                    JSONArray instElements = jsonObject.getJSONArray("nodes");
                    if (instElements.length() > 0) {
                        for (int i = 0; i < instElements.length(); i++) {
                            JSONObject c = instElements.getJSONObject(i);

                            boolean is_video = c.getBoolean("is_video");
                            if (!is_video) {

                                int indexCache = 0;
                                //thumb url
                                String igThumbURL = c.getString("thumbnail_src");
                                indexCache = igThumbURL.indexOf('?');
                                if (indexCache > 0) {
                                    igThumbURL = igThumbURL.substring(0, indexCache);
                                }
                                //original url
                                String igOrigURL = c.getString("display_src");
                                indexCache = igOrigURL.indexOf('?');
                                if (indexCache > 0) {
                                    igOrigURL = igOrigURL.substring(0, indexCache);
                                }
                                //id
                                String igID = c.getString("id");
                                //likes
                                int igLikes = c.getJSONObject("likes").getInt("count");

                                igPhotos.add(new InstagramPhoto(igID, igThumbURL, igOrigURL, igLikes));
                            }
                        }
                    }
                }

                if (count > 0 && igPhotos.size() > 0) {
                    result = true;
                }
            } catch (Exception e) {
                Log.d("unTag_CheckInstaTag", "Error checking");
            } finally {
                //clear
                inputStr = null;
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } else {
            Log.d("unTag_CheckInstaTag", "Tag too short");
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(ISConsts.prefstags.instagram_photo_count, count);
            editor.apply();
            bus.post(new EventIgCheckingComplete(count, igPhotos));
        } else {
            Log.d("unTag_CheckInstaTag", "Can't load from instagram. Use cached.");
            count = preferences.getInt(ISConsts.prefstags.instagram_photo_count, 0);
            bus.post(new EventIgCheckingComplete(count, igPhotos));
        }
    }
}