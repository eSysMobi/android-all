package mobi.esys.upnews_tube.eventbus;

import mobi.esys.upnews_tube.cbr.CurrenciesList;

/**
 * Created by ZeyUzh on 15.07.2016.
 */
public class EventCurrency {
    private CurrenciesList today;
    private CurrenciesList yesterday;

    public EventCurrency(CurrenciesList today, CurrenciesList yesterday) {
        this.today = today;
        this.yesterday = yesterday;
    }

    public CurrenciesList getToday() {
        return today;
    }

    public CurrenciesList getYesterday() {
        return yesterday;
    }
}
