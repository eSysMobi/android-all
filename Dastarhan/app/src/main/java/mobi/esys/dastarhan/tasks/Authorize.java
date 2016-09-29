package mobi.esys.dastarhan.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.database.Cuisine;
import mobi.esys.dastarhan.database.CuisineRepository;
import mobi.esys.dastarhan.database.UnitOfWork;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Authorize extends AsyncTask<String, Void, String> {
    private final String TAG = "dtagAuthorization";
    private AuthCallback callback;


    public Authorize(AuthCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onPrepared();
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";

        URL url;
        HttpURLConnection urlConnection = null;
        if (params.length == 2) {
            try {
                String urlString = Constants.URL_AUTHORIZATION + "?email=" + params[0] + "&pass=" + params[1];
                url = new URL(urlString);

                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setReadTimeout(Constants.CONNECTION_TIMEOUT);

                InputStream is = urlConnection.getInputStream();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                JSONObject authJson = new JSONObject(responseStrBuilder.toString());

                // Getting apikey
                if(authJson.has("apikey")) {
                    result = authJson.getString("apikey");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error IOException " + e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.isEmpty()) {
            callback.onFail();
        } else {
            callback.onSuccess(result);
        }
    }

    @Override
    protected void onCancelled() {
        callback.onFail();
        super.onCancelled();
    }

    public interface AuthCallback {
        void onPrepared();

        void onSuccess(String authToken);

        void onFail();
    }
}


