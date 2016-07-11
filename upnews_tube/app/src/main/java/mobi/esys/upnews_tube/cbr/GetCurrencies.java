package mobi.esys.upnews_tube.cbr;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mobi.esys.upnews_tube.PlayerActivityYouTube;

public class GetCurrencies extends AsyncTask<Date, Void, CurrenciesList> {
    private static final String TAG = "unTag_getCurr";
    private transient Context context;
    private transient Date yeasterDay;
    private transient CurrenciesList yeasterdayList;

    // всегда к доллару: евро, британский фунт, японская ена, китайский юань
    //u20ac euro
    //u0024 dollar
    //u00a3 pound
    //u00a5 cny

    public GetCurrencies(final Context context) {
        this.context = context;
    }

    @Override
    protected CurrenciesList doInBackground(final Date... params) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        yeasterDay = cal.getTime();

        yeasterdayList = getCurrencyListByDate(yeasterDay);

        return getCurrencyListByDate(params[0]);
    }


    private CurrenciesList getCurrencyListByDate(final Date date) {
        CurrenciesList currenciesList = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String today = format.format(date);
            URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp?date_req=".concat(today));
            //or http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml for europe
            Log.d("curr ulr", url.toString());
            if (url != null) {
                InputStream is = getInputStream(url);
                if (is != null) {
                    Reader reader = new InputStreamReader(is);

                    Persister serializer = new Persister();
                    try {
                        currenciesList = serializer.read(CurrenciesList.class, reader, false);
                        Log.v("SimpleTest_curr", "stock: " + currenciesList.currencies.toString());
                    } catch (Exception e) {
                        Log.e("SimpleTest_curr", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return currenciesList;
    }

    private InputStream getInputStream(final URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(CurrenciesList currenciesList) {
        super.onPostExecute(currenciesList);
        if (currenciesList != null) {
            ((PlayerActivityYouTube) context).loadCurrencyDashboard(currenciesList, yeasterdayList);
        } else {
            Log.e(TAG, "Can't load currencies");
        }
    }
}
