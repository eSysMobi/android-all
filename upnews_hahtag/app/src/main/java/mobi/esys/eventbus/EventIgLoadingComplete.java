package mobi.esys.eventbus;

/**
 * Created by ZeyUzh on 28.07.2016.
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
