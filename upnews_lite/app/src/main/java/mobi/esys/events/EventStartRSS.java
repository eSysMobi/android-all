package mobi.esys.events;

/**
 * Created by ZeyUzh on 15.09.2016.
 */
public class EventStartRSS {
    private final String feed;

    public EventStartRSS(String feed) {
        this.feed = feed;
    }

    public String getFeed() {
        return feed;
    }
}
