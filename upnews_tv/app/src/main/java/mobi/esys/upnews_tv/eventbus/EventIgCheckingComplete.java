package mobi.esys.upnews_tv.eventbus;

/**
 * Created by ZeyUzh on 17.07.2016.
 */
public class EventIgCheckingComplete {
    private String urls;

    public EventIgCheckingComplete(String urls) {
        this.urls = urls;
    }

    public String getUrls() {
        return urls;
    }
}