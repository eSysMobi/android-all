package mobi.esys.upnews_tv.cbr;

import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
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

import mobi.esys.upnews_tv.eventbus.EventCurrency;

public class GetCurrencies extends AsyncTask<Date, Void, Boolean> {
    private static final String TAG = "unTag_getCurr";
    private transient CurrenciesList yeasterdayList;
    private transient CurrenciesList todayList;
    private EventBus bus = EventBus.getDefault();

    // всегда к доллару: евро, британский фунт, японская ена, китайский юань
    //u20ac euro
    //u0024 dollar
    //u00a3 pound
    //u00a5 cny

    @Override
    protected Boolean doInBackground(final Date... params) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yeasterDay = cal.getTime();

        yeasterdayList = getCurrencyListByDate(yeasterDay);
        todayList = getCurrencyListByDate(params[0]);

        Boolean result = false;
        if (yeasterdayList != null && todayList != null) {
            result = true;
        }
        return result;
    }


    private CurrenciesList getCurrencyListByDate(final Date date) {
        CurrenciesList currenciesList = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String today = format.format(date);
            URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp?date_req=".concat(today));
            //or http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml for europe
            Log.d("curr ulr", url.toString());
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
        } catch (IOException e) {
            Log.e("SimpleTest_curr", e.getMessage());
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
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            bus.post(new EventCurrency(todayList, yeasterdayList));
        } else {
            Log.e(TAG, "Can't load currencies");
        }
    }

}
