package mobi.esys.dastarhan.tasks;

import android.os.AsyncTask;
import android.os.Handler;
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
import mobi.esys.dastarhan.AppComponent;
import mobi.esys.dastarhan.database.UnitOfWork;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
@Deprecated //need use retrofit
public class GetCuisines extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "dtagGetCuisines";
    private Handler handler;
    private AppComponent component;


    public GetCuisines(DastarhanApp dastarhanApp, Handler incHandler) {
        handler = incHandler;
        component = dastarhanApp.appComponent();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;

        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(Constants.URL_CUISINES);

            urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setReadTimeout(Constants.CONNECTION_TIMEOUT);

            InputStream is = urlConnection.getInputStream();

            //Log.e(TAG, "result =  " + ConvertStreamToString.getString(is));

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

            // Getting JSON Array node
            JSONArray cuisunesElements = jsonObject.getJSONArray("0");
            if (cuisunesElements.length() > 0) {
                UnitOfWork uow = component.getUow();
                uow.startUOW();

                int addedElementsToDB = 0;
                try {
                    CuisineRepository repo = component.cuisineRepository();
                    for (int i = 0; i < cuisunesElements.length(); i++) {
                        JSONObject c = cuisunesElements.getJSONObject(i);

                        int server_id = c.getInt("id");

                        Cuisine cuisine = repo.getById(server_id);

                        //check in db, if not exists - add
                        if (cuisine == null) {
                            int approved = c.getInt("approved");
                            boolean appr = (approved == 1);
                            String ru_name = c.getString("ru_name");
                            String en_name = c.getString("en_name");

                            cuisine = new Cuisine(
                                    server_id,
                                    ru_name,
                                    en_name,
                                    appr
                            );

                            repo.addOrUpdate(cuisine);
                            Log.d(TAG, "Prepare to adding cuisine id " + server_id);
                            addedElementsToDB++;
                        }
                    }
                    uow.commit();
                    Log.d(TAG, "Cuisines added: " + addedElementsToDB);
                } catch (Exception e) {
                    uow.cancel();
                }
                //if we have cuisunes elements
                result = true;
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
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_FAIL);
        }
    }

    @Override
    protected void onCancelled() {
        handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_FAIL);
        super.onCancelled();
    }
}
