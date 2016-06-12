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
 * Created by ZeyUzh on 08.06.2016.
 */
public class GetPromo extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetPromo";
    private Handler handler;
    private boolean result = false;
    private Context context;
    private Integer[] restaurantsID;

    public GetPromo(Context incContext, Handler incHandler, Integer[] restaurantsID) {
        handler = incHandler;
        context = incContext;
        this.restaurantsID = restaurantsID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_PROMO_SHOW_PROGRESS_BAR);
    }

    @Override
    protected Void doInBackground(Void... params) {

        URL url;
        HttpURLConnection urlConnection = null;

        for (int i = 0; i < restaurantsID.length; i++) {
            try {
                url = new URL(Constants.URL_PROMO + restaurantsID[i]);

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
                JSONArray promoElements = jsonObject.getJSONArray("0");
                if (promoElements.length() > 0) {

                    // looping through All Cuisines
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    try {
                        for (int j = 0; j < promoElements.length(); j++) {
                            JSONObject c = promoElements.getJSONObject(j);

                            String removed = c.getString("removed");

                            if (removed.equals("null")) {
                                int server_id = c.getInt("id");
                                int res_id = c.getInt("res_id");
                                int condition = c.getInt("condition");
                                String condition_par = "";
                                if (condition == 2) {
                                    condition_par = c.getString("condition_par").replaceAll("\"", "").replace("[", "").replace("]", "");
                                } else {
                                    condition_par = c.getString("condition_par");
                                }
                                int time = c.getInt("time");
                                String time1 = c.getString("time1");
                                String time2 = c.getString("time2");
                                String days = c.getString("days");
                                int date = c.getInt("date");
                                String date1 = c.getString("date1");
                                String date2 = c.getString("date2");
                                //gifts
                                String gift_type;
                                String gift;
                                if (condition == 5) {
                                    gift_type = "hidden";
                                    gift = "hidden";
                                } else {
                                    String gifts = c.getString("gifts").replace("\"", "").replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace("\\", "");
                                    int nav = gifts.indexOf(":");
                                    gift_type = gifts.substring(0, nav);
                                    gift = gifts.substring(nav + 1);
                                }
                                int gift_condition = c.getInt("gift_condition");

                                Cursor cursor = db.query(Constants.DB_TABLE_PROMO, null, null, null, null, null, null);

                                //check rows in db
                                if (cursor.moveToFirst()) {
                                    int idColIndex = cursor.getColumnIndex("server_id");
                                    boolean needInsert = true;

                                    //check db for this id
                                    do {
                                        int idInDB = cursor.getInt(idColIndex);
                                        if (idInDB == server_id) {
                                            needInsert = false;
                                            break;
                                        }
                                    } while (cursor.moveToNext());

                                    if (needInsert) {
                                        Log.d(TAG, "This promo id not found, insert data");
                                        ContentValues cv = new ContentValues();
                                        cv.put("server_id", server_id);
                                        cv.put("res_id", res_id);
                                        cv.put("condition", condition);
                                        cv.put("condition_par", condition_par);
                                        cv.put("time", time);
                                        cv.put("time1", time1);
                                        cv.put("time2", time2);
                                        cv.put("days", days);
                                        cv.put("date", date);
                                        cv.put("date1", date1);
                                        cv.put("date2", date2);
                                        cv.put("gift_type", gift_type);
                                        cv.put("gift", gift);
                                        cv.put("gift_condition", gift_condition);

                                        // insert row
                                        long rowID = db.insert(Constants.DB_TABLE_PROMO, null, cv);
                                        Log.d(TAG, "row inserted, ID = " + rowID);
                                    }

                                } else {
                                    Log.d(TAG, "0 rows, insert data");
                                    ContentValues cv = new ContentValues();
                                    cv.put("server_id", server_id);
                                    cv.put("res_id", res_id);
                                    cv.put("condition", condition);
                                    cv.put("condition_par", condition_par);
                                    cv.put("time", time);
                                    cv.put("time1", time1);
                                    cv.put("time2", time2);
                                    cv.put("days", days);
                                    cv.put("date", date);
                                    cv.put("date1", date1);
                                    cv.put("date2", date2);
                                    cv.put("gift_type", gift_type);
                                    cv.put("gift", gift);
                                    cv.put("gift_condition", gift_condition);

                                    // insert row
                                    long rowID = db.insert(Constants.DB_TABLE_PROMO, null, cv);
                                    Log.d(TAG, "row inserted, ID = " + rowID);
                                }
                                cursor.close();
                            } else {
                                Log.d(TAG, "This promo removed! Not need adding!");
                            }
                        }
                    } finally {
                        //close bd
                        Log.d(TAG, "Close DB (promo)");
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
            Log.d(TAG, "Successfully getting info about promos from restaurant with id " + restaurantsID[i]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //super.onPostExecute(aVoid);
        if (result) {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_PROMO_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_PROMO_FAIL);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_PROMO_FAIL);
    }
}
