package mobi.esys.dastarhan.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import mobi.esys.dastarhan.utils.DatabaseHelper;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class GetRestaurants extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetRestaurants";
    private Handler handler;
    boolean result = false;
    private Context context;

    public GetRestaurants(Context incContext, Handler incHandler) {
        handler = incHandler;
        context = incContext;
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

                // looping through All Cuisines
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                try {
                    for (int i = 0; i < restaurantElements.length(); i++) {
                        JSONObject c = restaurantElements.getJSONObject(i);

                        int approved = c.getInt("approved");

                        if (approved == 1) {
                            int restaurant_id = c.getInt("id");
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

                            Cursor cursor = db.query(Constants.DB_TABLE_RESTAURANTS, null, null, null, null, null, null);

                            //check rows in db
                            if (cursor.moveToFirst()) {
                                int idColIndex = cursor.getColumnIndex("server_id");
                                boolean needInsert = true;

                                //check db for this id
                                do {
                                    int idInDB = cursor.getInt(idColIndex);
                                    if (idInDB == restaurant_id) {
                                        needInsert = false;
                                        break;
                                    }
                                } while (cursor.moveToNext());

                                if (needInsert) {
                                    Log.d(TAG, "This restaurant id not found, insert data");
                                    ContentValues cv = new ContentValues();
                                    cv.put("server_id", restaurant_id);
                                    cv.put("ru_name", ru_name);
                                    cv.put("en_name", en_name);
                                    cv.put("city_id", city_id);
                                    cv.put("district_id", district_id);
                                    cv.put("min_order", min_order);
                                    cv.put("del_cost", del_cost);
                                    cv.put("schedule", schedule);
                                    cv.put("time1", time1);
                                    cv.put("time2", time2);
                                    cv.put("del_time", del_time);
                                    cv.put("payment_methods", payment_methods);
                                    cv.put("contact_name_ru", contact_name_ru);
                                    cv.put("contact_name_en", contact_name_en);
                                    cv.put("phone", phone);
                                    cv.put("mobile", mobile);
                                    cv.put("email1", email1);
                                    cv.put("email2", email2);
                                    cv.put("total_rating", total_rating);
                                    cv.put("total_votes", total_votes);
                                    cv.put("contact_email", contact_email);
                                    cv.put("order_phone", order_phone);
                                    cv.put("additional_ru", additional_ru);
                                    cv.put("additional_en", additional_en);
                                    cv.put("picture", picture);
                                    cv.put("vegetarian", vegetarian);
                                    cv.put("featured", featured);
                                    cv.put("approved", approved);
                                    cv.put("cuisines", cuisines);
                                    // insert row
                                    long rowID = db.insert(Constants.DB_TABLE_RESTAURANTS, null, cv);
                                    Log.d(TAG, "row inserted, ID = " + rowID);
                                }

                            } else {
                                Log.d(TAG, "0 rows, insert data");
                                ContentValues cv = new ContentValues();
                                cv.put("server_id", restaurant_id);
                                cv.put("ru_name", ru_name);
                                cv.put("en_name", en_name);
                                cv.put("city_id", city_id);
                                cv.put("district_id", district_id);
                                cv.put("min_order", min_order);
                                cv.put("del_cost", del_cost);
                                cv.put("schedule", schedule);
                                cv.put("time1", time1);
                                cv.put("time2", time2);
                                cv.put("del_time", del_time);
                                cv.put("payment_methods", payment_methods);
                                cv.put("contact_name_ru", contact_name_ru);
                                cv.put("contact_name_en", contact_name_en);
                                cv.put("phone", phone);
                                cv.put("mobile", mobile);
                                cv.put("email1", email1);
                                cv.put("email2", email2);
                                cv.put("total_rating", total_rating);
                                cv.put("total_votes", total_votes);
                                cv.put("contact_email", contact_email);
                                cv.put("order_phone", order_phone);
                                cv.put("additional_ru", additional_ru);
                                cv.put("additional_en", additional_en);
                                cv.put("picture", picture);
                                cv.put("vegetarian", vegetarian);
                                cv.put("featured", featured);
                                cv.put("approved", approved);
                                cv.put("cuisines", cuisines);

                                // insert row
                                long rowID = db.insert(Constants.DB_TABLE_RESTAURANTS, null, cv);
                                Log.d(TAG, "row inserted, ID = " + rowID);
                            }
                            cursor.close();
                        }
                    }
                } finally {
                    //close bd
                    Log.d(TAG, "Close DB (restaurant)");
                    db.close();
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
        //super.onPostExecute(aVoid);
        if (result) {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_RESTAURANTS_FAIL);
        }
    }
}
