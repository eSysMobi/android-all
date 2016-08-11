package mobi.esys.eventbus;

import java.util.List;

import mobi.esys.instagram.InstagramItem;

/**
 * Created by ZeyUzh on 27.07.2016.
 */
public class EventGetTagsIGComplete {
    List<InstagramItem> igPhotos;


    public EventGetTagsIGComplete(List<InstagramItem> igPhotos) {
        this.igPhotos = igPhotos;
    }

    public List<InstagramItem> getIgPhotos() {
        return igPhotos;
    }
}
