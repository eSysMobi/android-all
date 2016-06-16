package mobi.esys.upnews_tube.instagram;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import mobi.esys.upnews_tube.UpnewsTubeApp;
import mobi.esys.upnews_tube.net.NetMonitor;


public class CheckInstaTagTask extends AsyncTask<String, Void, String> {
    private transient String mHashTag;
    private transient UpnewsTubeApp mApp;

    public CheckInstaTagTask(String hashTag, UpnewsTubeApp app) {
        mHashTag = hashTag;
        mApp = app;
    }


    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if (mHashTag.length() >= 2) {

            String edTag = mHashTag.toLowerCase(Locale.ENGLISH);

            if (NetMonitor.isNetworkAvailable(mApp)) {

                try {
                    //TODO send request
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
                    int srt = inputStr.indexOf("}, \"nodes\": [")+2;
                    int end = inputStr.indexOf(", \"content_advisory");
                    inputStr = "{" + inputStr.substring(srt,end);

                    JSONObject jsonObject = new JSONObject(inputStr);

                    // Getting JSON Array node
                    StringBuilder sb2 = new StringBuilder();
                    boolean firstElement = true;
                    JSONArray instElements = jsonObject.getJSONArray("nodes");
                    if (instElements.length() > 0) {
                        for (int i = 0; i < instElements.length(); i++) {
                            JSONObject c = instElements.getJSONObject(i);

                            boolean is_video = c.getBoolean("is_video");

                            if(!is_video){
                                if(!firstElement){
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
                Log.d("unTag_CheckInstaTag", "No inet, can't check instagram tag");
            }
        } else {
            Log.d("unTag_CheckInstaTag", "Tag too short");
        }

        return result;
    }
}
