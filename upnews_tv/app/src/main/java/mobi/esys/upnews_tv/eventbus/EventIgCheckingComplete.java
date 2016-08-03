package mobi.esys.upnews_tv.eventbus;

import java.util.List;

import mobi.esys.upnews_tv.instagram.InstagramItem;

/**
 * Created by ZeyUzh on 17.07.2016.
 */
public class EventIgCheckingComplete {
    private final List<InstagramItem> igPhotos;

    public EventIgCheckingComplete(List<InstagramItem> igPhotos) {
        this.igPhotos = igPhotos;
    }

    public List<InstagramItem> getIgPhotos() {
        return igPhotos;
    }
}