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
public class GetCuisines extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetCuisines";
    private Handler handler;
    boolean result = false;
    private Context context;

    public GetCuisines(Context incContext, Handler incHandler) {
        handler = incHandler;
        context = incContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR);
    }

    @Override
    protected Void doInBackground(Void... params) {


        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(Constants.URL_CUISINES);

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
            JSONArray cuisunesElements = jsonObject.getJSONArray("0");
            if (cuisunesElements.length() > 0) {

                // looping through All Cuisines
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                try {
                    for (int i = 0; i < cuisunesElements.length(); i++) {
                        JSONObject c = cuisunesElements.getJSONObject(i);

                        int approved = c.getInt("approved");

                        if (approved == 1) {
                            int cuisine_id = c.getInt("id");
                            String ru_name = c.getString("ru_name");
                            String en_name = c.getString("en_name");

                            Cursor cursor = db.query(Constants.DB_TABLE_CUISINES, null, null, null, null, null, null);

                            //check rows in db
                            if (cursor.moveToFirst()) {
                                int idColIndex = cursor.getColumnIndex("server_id");
                                boolean needInsert = true;

                                //check db for this id
                                do {
                                    int idInDB = cursor.getInt(idColIndex);
                                    if (idInDB == cuisine_id) {
                                        needInsert = false;
                                        break;
                                    }
                                } while (cursor.moveToNext());

                                if (needInsert) {
                                    Log.d(TAG, "This cuisine id not found, insert data");
                                    ContentValues cv = new ContentValues();
                                    cv.put("server_id", cuisine_id);
                                    cv.put("ru_name", ru_name);
                                    cv.put("en_name", en_name);
                                    cv.put("approved", approved);
                                    // insert row
                                    long rowID = db.insert(Constants.DB_TABLE_CUISINES, null, cv);
                                    Log.d(TAG, "row inserted, ID = " + rowID);
                                }

                            } else {
                                Log.d(TAG, "0 rows, insert data");
                                ContentValues cv = new ContentValues();
                                cv.put("server_id", cuisine_id);
                                cv.put("ru_name", ru_name);
                                cv.put("en_name", en_name);
                                cv.put("approved", approved);
                                // insert row
                                long rowID = db.insert(Constants.DB_TABLE_CUISINES, null, cv);
                                Log.d(TAG, "row inserted, ID = " + rowID);
                            }
                            cursor.close();
                        }
                    }
                } finally {
                    //close bd
                    Log.d(TAG, "Close DB (cuisines)");
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
            handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_SUCCESS);
        } else {
            handler.sendEmptyMessage(Constants.CALLBACK_GET_CUISINES_FAIL);
        }
    }
}
