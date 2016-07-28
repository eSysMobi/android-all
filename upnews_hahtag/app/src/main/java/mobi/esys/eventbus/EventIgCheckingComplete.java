package mobi.esys.eventbus;

/**
 * Created by ZeyUzh on 27.07.2016.
 */
public class EventIgCheckingComplete {
    private int photoCount;
    private String photoUrls = "";
    private boolean isCached;

    public EventIgCheckingComplete(int photoCount, String photoUrls, boolean isCached) {
        this.photoCount = photoCount;
        this.photoUrls = photoUrls;
        this.isCached = isCached;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public String getPhotoUrls() {
        return photoUrls;
    }

    public boolean isCached() {
        return isCached;
    }
}
