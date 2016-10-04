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
public class SignUp extends AsyncTask<String, Void, Void> {
    private final String TAG = "dtagSignUp";
    private CallbackAuth callback;

    private String login;
    private String password;

    private int errorCode = 0;

    public SignUp(CallbackAuth callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onPrepared();
    }

    @Override
    protected Void doInBackground(String... params) {
        URL url;
        HttpURLConnection urlConnection = null;
        if (params.length == 2) {
            try {
                String urlString = Constants.URL_REGISTRATION + "?email=" + params[0] + "&pass=" + params[1];
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

                // check
                if (authJson.has("id")) {
                    login = params[0];
                    password = params[1];
                } else {
                    errorCode = Constants.RESULT_CODE_USER_ALREADY_EXISTS;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error IOException " + e.getMessage());
                if (e.getMessage().startsWith("Unable to resolve host \"dastarhan.net\": No address associated with hostname")) {
                    errorCode = Constants.RESULT_CODE_NO_INET;
                } else {
                    errorCode = Constants.RESULT_CODE_SIGNUP_ERROR;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error JSONException " + e.getMessage());
                errorCode = Constants.RESULT_CODE_SIGNUP_ERROR;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (login != null && password != null) {
            callback.onSuccessSighUp(login, password);
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


