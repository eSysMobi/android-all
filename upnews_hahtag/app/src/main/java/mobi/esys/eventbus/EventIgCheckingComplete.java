package mobi.esys.eventbus;

import java.util.List;

import mobi.esys.instagram.model.InstagramPhoto;

/**
 * Created by ZeyUzh on 27.07.2016.
 */
public class EventIgCheckingComplete {
    private int photoCount;
    List<InstagramPhoto> igPhotos;


    public EventIgCheckingComplete(int photoCount, List<InstagramPhoto> igPhotos) {
        this.photoCount = photoCount;
        this.igPhotos = igPhotos;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public List<InstagramPhoto> getIgPhotos() {
        return igPhotos;
    }
}
