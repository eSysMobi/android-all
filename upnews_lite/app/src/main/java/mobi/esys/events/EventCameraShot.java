package mobi.esys.events;

/**
 * Created by ZeyUzh on 18.09.2016.
 */
public class EventCameraShot {
    private final String fileName;

    public EventCameraShot(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
