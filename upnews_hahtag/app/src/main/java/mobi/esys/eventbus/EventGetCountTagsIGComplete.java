package mobi.esys.eventbus;

/**
 * Created by ZeyUzh on 27.07.2016.
 */
public class EventGetCountTagsIGComplete {
    private int photoCount;


    public EventGetCountTagsIGComplete(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getPhotoCount() {
        return photoCount;
    }
}
