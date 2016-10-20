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
@Deprecated //need use retrofit APIVoteForRestaurant
public class SendVote extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "dtagSendVote";

    private CallbackNet callback;
    private long id;
    private String apikey;
    private long res;
    private int vote;

    private int errorCode = 0;

    public SendVote(CallbackNet callback, long id, String apikey, long res, int vote) {
        this.callback = callback;
        this.id = id;
        this.apikey = apikey;
        this.res = res;
        this.vote = vote;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onPrepared();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;

        URL url;
        HttpURLConnection urlConnection = null;
        if (params.length == 2) {
            try {
                String urlString = Constants.URL_VOTE_FOR_REST + "?id=" + id + "&apikey=11" + apikey + "&res=" + res + "&vote=" + vote;
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

                JSONObject voteResultJson = new JSONObject(responseStrBuilder.toString());

                // Getting apikey
                if (voteResultJson.has("success")) {
                    result = voteResultJson.getString("success").equals("Ok");
                } else if (voteResultJson.has("error")) {
                    if (voteResultJson.getString("success").equals("No data")) {
                        errorCode = Constants.RESULT_CODE_VOTE_ALREADY_VOTED;
                    } else {
                        errorCode = Constants.RESULT_CODE_ERROR;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error IOException " + e.getMessage());
                if (e.getMessage().startsWith("Unable to resolve host \"dastarhan.net\": No address associated with hostname")) {
                    errorCode = Constants.RESULT_CODE_NO_INET;
                } else {
                    errorCode = Constants.RESULT_CODE_ERROR;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error JSONException " + e.getMessage());
                errorCode = Constants.RESULT_CODE_ERROR;
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
            callback.onSuccessAuth();
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


