package mobi.esys.upnews_tube.eventbus;

import java.util.List;

import mobi.esys.upnews_tube.instagram.InstagramItem;

public class EventIgCheckingComplete {
    private final List<InstagramItem> igPhotos;

    public EventIgCheckingComplete(List<InstagramItem> igPhotos) {
        this.igPhotos = igPhotos;
    }

    public List<InstagramItem> getIgPhotos() {
        return igPhotos;
    }
}