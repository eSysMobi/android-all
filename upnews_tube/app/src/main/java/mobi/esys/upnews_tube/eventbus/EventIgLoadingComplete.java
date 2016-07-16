package mobi.esys.upnews_tube.eventbus;

/**
 * Created by ZeyUzh on 15.07.2016.
 */
public class EventIgLoadingComplete {
    private String tag;

    public EventIgLoadingComplete(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
