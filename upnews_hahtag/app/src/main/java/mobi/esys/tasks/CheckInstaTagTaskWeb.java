package mobi.esys.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mobi.esys.consts.ISConsts;
import mobi.esys.eventbus.EventIgCheckingComplete;


public class CheckInstaTagTaskWeb extends AsyncTask<Void, Void, Boolean> {

    private EventBus bus = EventBus.getDefault();

    private transient String mHashTag;
    private SharedPreferences preferences;

    private int count;
    private String urls = "";

    public CheckInstaTagTaskWeb(String mHashTag, SharedPreferences preferences) {
        this.mHashTag = mHashTag;
        this.preferences = preferences;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;
        if (mHashTag.length() >= 2) {
            String edTag = mHashTag.toLowerCase(Locale.ENGLISH);

            String inputStr;
            try {
                String path = "https://www.instagram.com/explore/tags/" + edTag;
                URL url = new URL(path);
                Log.w("unTagCheckInstaTag", "request: " + path);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2 * 1000); // 2 sec timeout

                InputStream is = urlConnection.getInputStream();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String startPrefix = "<script type=\"text/javascript\">window._sharedData";
                while ((inputStr = streamReader.readLine()) != null) {
                    if (inputStr.startsWith(startPrefix)) {
                        break;
                    }
                }

                int srtCount = inputStr.indexOf("media\": {\"count\":") + 18;
                int endCount = inputStr.indexOf(",", srtCount);
                String countStr = inputStr.substring(srtCount, endCount);
                count = Integer.parseInt(countStr);

                int srt = inputStr.indexOf("}, \"nodes\": [") + 2;
                int end = inputStr.indexOf(", \"content_advisory");
                inputStr = "{" + inputStr.substring(srt, end);

                JSONObject jsonObject = new JSONObject(inputStr);

                // Getting JSON Array node
                StringBuilder sb2 = new StringBuilder();
                boolean firstElement = true;
                JSONArray instElements = jsonObject.getJSONArray("nodes");
                if (instElements.length() > 0) {
                    for (int i = 0; i < instElements.length(); i++) {
                        JSONObject c = instElements.getJSONObject(i);

                        boolean is_video = c.getBoolean("is_video");

                        if (!is_video) {
                            if (!firstElement) {
                                sb2.append(",");
                            }
                            sb2.append(c.getString("thumbnail_src"));
                            firstElement = false;
                        }
                    }
                }
                urls = sb2.toString();

                if (count > 0 && !urls.isEmpty()) {
                    result = true;
                }
            } catch (Exception e) {
                Log.d("unTag_CheckInstaTag", "Error checking");
            } finally {
                //clear
                inputStr = null;
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
            bus.post(new EventIgCheckingComplete(count, urls, false));
        } else {
            Log.d("unTag_CheckInstaTag", "Can't load from instagram. Use cached.");
            count = preferences.getInt(ISConsts.prefstags.instagram_photo_count, 0);
            bus.post(new EventIgCheckingComplete(count, "", true));
        }
    }
}