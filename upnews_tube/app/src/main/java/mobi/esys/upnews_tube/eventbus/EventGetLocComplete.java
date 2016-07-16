package mobi.esys.upnews_tube.eventbus;

/**
 * Created by ZeyUzh on 15.07.2016.
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
