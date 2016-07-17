package mobi.esys.upnews_tv.eventbus;

/**
 * Created by ZeyUzh on 17.07.2016.
 */
public class EventGetLocComplete {
    private String city;

    public EventGetLocComplete(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}