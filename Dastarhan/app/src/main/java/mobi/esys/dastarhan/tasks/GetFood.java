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
public class GetFood extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetFood";
    private Handler handler;
    boolean result = false;
    private Context context;
    private Integer[] restID;

    public GetFood(Context incContext, Handler incHandler, Integer[] incRestID) {
        handler = incHandler;
        context = incContext;
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

        for(int i = 0; i<restID.length;i++) {

            try {
                if (restID[i] == -42) {
                    url = new URL(Constants.URL_FOOD + 0);
                } else {
                    url = new URL(Constants.URL_FOOD + restID[i]);
                }

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
                JSONArray foodElements = jsonObject.getJSONArray("0");
                if (foodElements.length() > 0) {

                    // looping through All Cuisines
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    try {
                        for (int j = 0; j < foodElements.length(); j++) {
                            JSONObject c = foodElements.getJSONObject(j);

                            //TODO add "removed"
                            //int approved = c.getString("removed");

                            int food_id = c.getInt("id");
                            int res_id = c.getInt("res_id");
                            int cat_id = c.getInt("cat_id");
                            String ru_name = c.getString("ru_name");
                            String en_name = c.getString("en_name");
                            String picture = c.getString("picture");
                            String ru_descr = c.getString("ru_descr");
                            String en_descr = c.getString("en_descr");
                            double price = c.getDouble("price");
                            int min_amount = c.getInt("min_amount");
                            String units = c.getString("units");
                            int ordered = c.getInt("ordered");
                            int offer = c.getInt("offer");
                            int vegetarian = c.getInt("vegetarian");
                            int featured = c.getInt("featured");

                            Cursor cursor = db.query(Constants.DB_TABLE_FOOD, null, null, null, null, null, null);

                            //check rows in db
                            if (cursor.moveToFirst()) {
                                int idColIndex = cursor.getColumnIndex("server_id");
                                boolean needInsert = true;

                                //check db for this id
                                do {
                                    int idInDB = cursor.getInt(idColIndex);
                                    if (idInDB == food_id) {
                                        needInsert = false;
                                        break;
                                    }
                                } while (cursor.moveToNext());

                                if (needInsert) {
                                    Log.d(TAG, "This food id not found, insert data");
                                    ContentValues cv = new ContentValues();
                                    cv.put("server_id", food_id);
                                    cv.put("res_id", res_id);
                                    cv.put("cat_id", cat_id);
                                    cv.put("ru_name", ru_name);
                                    cv.put("en_name", en_name);
                                    cv.put("picture", picture);
                                    cv.put("ru_descr", ru_descr);
                                    cv.put("en_descr", en_descr);
                                    cv.put("price", price);
                                    cv.put("min_amount", min_amount);
                                    cv.put("units", units);
                                    cv.put("ordered", ordered);
                                    cv.put("offer", offer);
                                    cv.put("vegetarian", vegetarian);
                                    cv.put("favorite", 0);
                                    cv.put("featured", featured);
                                    // insert row
                                    long rowID = db.insert(Constants.DB_TABLE_FOOD, null, cv);
                                    Log.d(TAG, "row inserted, ID = " + rowID);
                                }

                            } else {
                                Log.d(TAG, "0 rows, insert data");
                                ContentValues cv = new ContentValues();
                                cv.put("server_id", food_id);
                                cv.put("res_id", res_id);
                                cv.put("cat_id", cat_id);
                                cv.put("ru_name", ru_name);
                                cv.put("en_name", en_name);
                                cv.put("picture", picture);
                                cv.put("ru_descr", ru_descr);
                                cv.put("en_descr", en_descr);
                                cv.put("price", price);
                                cv.put("min_amount", min_amount);
                                cv.put("units", units);
                                cv.put("ordered", ordered);
                                cv.put("offer", offer);
                                cv.put("vegetarian", vegetarian);
                                cv.put("favorite", 0);
                                cv.put("featured", featured);
                                // insert row
                                long rowID = db.insert(Constants.DB_TABLE_FOOD, null, cv);
                                Log.d(TAG, "row inserted, ID = " + rowID);
                            }
                            cursor.close();

                        }
                    } finally {
                        //close bd
                        Log.d(TAG, "Close DB (food)");
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
            Log.d(TAG, "Sucessully getting info about food from restaurant with id "+ restID[i]);
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
}
