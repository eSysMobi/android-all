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
import mobi.esys.dastarhan.database.Promo;
import mobi.esys.dastarhan.database.PromoRepository;
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.UnitOfWork;

/**
 * Created by ZeyUzh on 08.06.2016.
 */
public class GetPromo extends AsyncTask<Void, Void, Void> {
    private final String TAG = "dtagGetPromo";
    private Handler handler;
    private boolean result = false;
    private RealmComponent component;
    private Integer[] restaurantsID;

    public GetPromo(DastarhanApp dastarhanApp, Handler incHandler, Integer[] restaurantsID) {
        handler = incHandler;
        component = dastarhanApp.realmComponent();
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
                    UnitOfWork uow = component.getUow();
                    uow.startUOW();

                    int addedElementsToDB = 0;
                    try {
                        PromoRepository repo = component.promoRepository();
                        for (int j = 0; j < promoElements.length(); j++) {
                            JSONObject c = promoElements.getJSONObject(j);

                            int server_id = c.getInt("id");

                            Promo promo = repo.getById(server_id);

                            //check in db, if not exists - add
                            if (promo == null) {
                                String removed = c.getString("removed");
                                int res_id = c.getInt("res_id");
                                int condition = c.getInt("condition");
                                String condition_par = "";
                                if (condition == 2) {
                                    condition_par = c.getString("condition_par").replaceAll("\"", "").replace("[", "").replace("]", "");
                                } else {
                                    condition_par = c.getString("condition_par");
                                }
                                boolean limitedTime = (c.getInt("time") == 1);
                                String time1 = c.getString("time1");
                                String time2 = c.getString("time2");
                                String days = c.getString("days");
                                boolean limitedData = (c.getInt("date") == 1);
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
                                boolean gift_condition = (c.getInt("gift_condition") == 1);

                                promo = new Promo(server_id,
                                        condition,
                                        condition_par,
                                        limitedTime,
                                        time1,
                                        time2,
                                        days,
                                        limitedData,
                                        date1,
                                        date2,
                                        gift_type,
                                        gift,
                                        gift_condition);

                                repo.addOrUpdate(promo);
                                Log.d(TAG, "Prepare to adding promo id " + server_id);
                                addedElementsToDB++;
                            }
                        }
                        uow.commit();
                        Log.d(TAG, "Promos added: " + addedElementsToDB);
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
