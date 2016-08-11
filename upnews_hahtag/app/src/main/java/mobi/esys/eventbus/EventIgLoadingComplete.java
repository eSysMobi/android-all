package mobi.esys.eventbus;

/**
 * Created by ZeyUzh on 28.07.2016.
 */
public class EventIgLoadingComplete {
    int countLoadedFiles;

    public EventIgLoadingComplete(int countLoadedFiles) {
        this.countLoadedFiles = countLoadedFiles;
    }

    public int getCountLoadedFiles() {
        return countLoadedFiles;
    }
}
