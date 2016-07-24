package mobi.esys.upnews_tv.instagram;

import android.os.AsyncTask;
import android.util.Log;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.esys.upnews_tv.UpnewsOnlineApp;
import mobi.esys.upnews_tv.constants.Folders;
import mobi.esys.upnews_tv.eventbus.EventIgCheckingComplete;
import mobi.esys.upnews_tv.eventbus.EventIgLoadingComplete;
import mobi.esys.upnews_tv.net.NetMonitor;


public class CheckInstaTagTaskWeb extends AsyncTask<String, Void, String> {
    private transient String mHashTag;
    private EventBus bus = EventBus.getDefault();

    public CheckInstaTagTaskWeb(String hashTag) {
        mHashTag = hashTag;
    }


    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if (mHashTag.length() >= 2) {
            String edTag = mHashTag.toLowerCase(Locale.ENGLISH);

            try {
                String path = "https://www.instagram.com/explore/tags/" + edTag;
                URL url = new URL(path);
                Log.w("unTagCheckInstaTag", "request: " + path);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2 * 1000); // 2 sec timeout

                InputStream is = urlConnection.getInputStream();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String inputStr;
                String startPrefix = "<script type=\"text/javascript\">window._sharedData";
                while ((inputStr = streamReader.readLine()) != null) {
                    if (inputStr.startsWith(startPrefix)) {
                        break;
                    }
                }
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
                result = sb2.toString();

            } catch (Exception e) {
                Log.d("unTag_CheckInstaTag", "Error checking");
            }
        } else {
            Log.d("unTag_CheckInstaTag", "Tag too short");
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s.isEmpty()) {
            String folder = Folders.SD_CARD.concat(File.separator).
                    concat(Folders.BASE_FOLDER).
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER);
            File[] folderList = new File(folder).listFiles();
            if (folderList.length > 0) {
                Log.d("unTag_CheckInstaTag", "Can't load from instagram. Use cached.");
                bus.post(new EventIgLoadingComplete(mHashTag));
            }
        } else {
            bus.post(new EventIgCheckingComplete(s));
        }
        super.onPostExecute(s);
    }
}
