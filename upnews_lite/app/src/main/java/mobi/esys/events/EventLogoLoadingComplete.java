package mobi.esys.events;

/**
 * Created by ZeyUzh on 15.09.2016.
 */
public class EventLogoLoadingComplete {
    private boolean successful;

    public EventLogoLoadingComplete(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
