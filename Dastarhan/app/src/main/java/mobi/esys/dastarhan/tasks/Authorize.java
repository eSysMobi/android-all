package mobi.esys.dastarhan.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mobi.esys.dastarhan.Constants;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class Authorize extends AsyncTask<String, Void, Boolean> {
    private final String TAG = "dtagAuthorization";
    private CallbackAuth callback;

    private int errorCode = 0;
    private String apiKey;
    private Integer userID;

    public Authorize(CallbackAuth callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onPrepared();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean result = false;

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
                if (authJson.has("apikey") && authJson.has("id")) {
                    apiKey = authJson.getString("apikey");
                    userID = authJson.getInt("id");
                    result = true;
                } else {
                    errorCode = Constants.RESULT_CODE_NO_USER_EXISTS;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error IOException " + e.getMessage());
                if (e.getMessage().startsWith("Unable to resolve host \"dastarhan.net\": No address associated with hostname")) {
                    errorCode = Constants.RESULT_CODE_NO_INET;
                } else {
                    errorCode = Constants.RESULT_CODE_AUTH_ERROR;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error JSONException " + e.getMessage());
                errorCode = Constants.RESULT_CODE_AUTH_ERROR;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (apiKey!=null && !apiKey.isEmpty() && userID != null) {
                callback.onSuccessAuth(apiKey, userID);
            } else {
                callback.onFail(errorCode);
            }
        } else {
            callback.onFail(errorCode);
        }
    }

    @Override
    protected void onCancelled() {
        errorCode = 0;
        callback.onFail(errorCode);
        super.onCancelled();
    }
}


