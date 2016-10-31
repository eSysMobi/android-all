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

import mobi.esys.dastarhan.AppComponent;
import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.UnitOfWork;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
@Deprecated //need use retrofit
public class GetFood extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetFood";
    private Handler handler;
    boolean result = false;
    private AppComponent component;
    private Integer[] restID;

    public GetFood(DastarhanApp dastarhanApp, Handler incHandler, Integer[] incRestID) {
        //TODO
        handler = incHandler;
        component = dastarhanApp.appComponent();
        restID = incRestID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR);
    }

    @Override
    protected Void doInBackground(Void... params) {


        URL url;
        HttpURLConnection urlConnection = null;

        for (Integer aRestID : restID) {

            try {
                if (aRestID == -42) {
                    url = new URL(Constants.URL_FOOD + "?resid=0");
                } else {
                    url = new URL(Constants.URL_FOOD + "?resid=" + aRestID);
                }

                urlConnection = (HttpURLConnection) url.openConnection();
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
                JSONArray foodElements = jsonObject.getJSONArray("0");
                if (foodElements.length() > 0) {
                    UnitOfWork uow = component.getUow();
                    uow.startUOW();

                    int addedElementsToDB = 0;
                    try {
                        FoodRepository repo = component.foodRepository();
                        for (int j = 0; j < foodElements.length(); j++) {
                            JSONObject c = foodElements.getJSONObject(j);

                            int server_id = c.getInt("id");

                            Food food = repo.getById(server_id);

                            //check in db, if not exists - add
                            if (food == null) {
                                int res_id = c.getInt("res_id");
                                int cat_id = c.getInt("cat_id");
                                String ru_name = c.getString("ru_name");
                                String en_name = c.getString("en_name");
                                String origPicturePath = c.getString("origPicturePath");
                                String smallPicturePath = c.getString("smallPicturePath");
                                String ru_descr = c.getString("ru_descr");
                                String en_descr = c.getString("en_descr");
                                double price = c.getDouble("price");
                                int min_amount = c.getInt("min_amount");
                                String units = c.getString("units");
                                //int ordered = c.getInt("ordered");
                                int offer = c.getInt("offer");
                                boolean vegetarian = (c.getInt("vegetarian") == 1);
                                boolean featured = (c.getInt("featured") == 1);
                                boolean removed = (c.getString("removed") == null);

                                food = new Food(server_id,
                                        res_id,
                                        cat_id,
                                        ru_name,
                                        en_name,
                                        origPicturePath,
                                        smallPicturePath,
                                        ru_descr,
                                        en_descr,
                                        price,
                                        min_amount,
                                        units,
                                        offer,
                                        vegetarian,
                                        featured,
                                        removed
                                );

                                repo.addOrUpdate(food);
                                Log.d(TAG, "Prepare to adding cuisine id " + server_id);
                                addedElementsToDB++;
                            }
                        }

                        uow.commit();
                        Log.d(TAG, "Food added: " + addedElementsToDB);
                    } catch (Exception e) {
                        uow.cancel();
                    }
                }
                result = true;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error IOException " + e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error JSONException " + e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            Log.d(TAG, "Successfully getting info about food from restaurant with id " + aRestID);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //super.onPostExecute(aVoid);
        if (result) {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_FOOD_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_FOOD_FAIL);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_FOOD_FAIL);
    }
}
