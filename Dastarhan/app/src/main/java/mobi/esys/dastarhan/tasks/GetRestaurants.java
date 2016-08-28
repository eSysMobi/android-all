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
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.database.UnitOfWork;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class GetRestaurants extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetRestaurants";
    private Handler handler;
    boolean result = false;
    private RealmComponent component;

    public GetRestaurants(DastarhanApp dastarhanApp, Handler incHandler) {
        handler = incHandler;
        component = dastarhanApp.realmComponent();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_SHOW_PROGRESS_BAR);
    }

    @Override
    protected Void doInBackground(Void... params) {


        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(Constants.URL_RESTORANS);

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
            JSONArray restaurantElements = jsonObject.getJSONArray("0");
            if (restaurantElements.length() > 0) {
                UnitOfWork uow = component.getUow();
                uow.startUOW();

                int addedElementsToDB = 0;
                try {
                    RestaurantRepository repo = component.restaurantRepository();
                    for (int i = 0; i < restaurantElements.length(); i++) {
                        JSONObject c = restaurantElements.getJSONObject(i);

                        int server_id = c.getInt("id");

                        Restaurant restaurant = repo.getById(server_id);

                        //check in db, if not exists - add
                        if (restaurant == null) {
                            int approved = c.getInt("approved");
                            String ru_name = c.getString("ru_name");
                            String en_name = c.getString("en_name");
                            int city_id = c.getInt("city_id");
                            int district_id = c.getInt("district_id");
                            int min_order = c.getInt("min_order");
                            int del_cost = c.getInt("del_cost");
                            String schedule = c.getString("schedule");
                            String time1 = c.getString("time1");
                            String time2 = c.getString("time2");
                            String del_time = c.getString("del_time");
                            String payment_methods = c.getString("payment_methods");
                            String contact_name_ru = c.getString("contact_name_ru");
                            String contact_name_en = c.getString("contact_name_en");
                            String phone = c.getString("phone");
                            String mobile = c.getString("mobile");
                            String email1 = c.getString("email1");
                            String email2 = c.getString("email2");
                            int total_rating = c.getInt("total_rating");
                            int total_votes = c.getInt("total_votes");
                            String contact_email = c.getString("contact_email");
                            String order_phone = c.getString("order_phone");
                            String additional_ru = c.getString("additional_ru");    //TODO check length
                            String additional_en = c.getString("additional_en");
                            String picture = c.getString("email2");
                            int vegetarian = c.getInt("vegetarian");
                            int featured = c.getInt("featured");
                            String cuisines = c.getString("cuisines");

                            restaurant = new Restaurant(server_id,
                                    ru_name,
                                    en_name,
                                    additional_ru,
                                    additional_en,
                                    picture,
                                    vegetarian,
                                    featured,
                                    approved,
                                    cuisines,
                                    city_id,
                                    district_id,
                                    schedule,
                                    time1,
                                    time2, contact_name_ru,
                                    contact_name_en,
                                    phone,
                                    mobile,
                                    email1,
                                    email2,
                                    contact_email,
                                    order_phone,
                                    min_order,
                                    payment_methods,
                                    total_rating,
                                    total_votes);

                            repo.addOrUpdate(restaurant);
                            Log.d(TAG, "Prepare to adding restaurant id " + server_id);
                            addedElementsToDB++;
                        }
                    }
                    uow.commit();
                    Log.d(TAG, "Restaurants added: " + addedElementsToDB);
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
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (result) {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_FAIL);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_FAIL);
    }
}
