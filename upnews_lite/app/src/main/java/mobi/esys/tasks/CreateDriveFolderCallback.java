package mobi.esys.tasks;

/**
 * Created by ZeyUzh on 17.09.2016.
 */
public interface CreateDriveFolderCallback {
    void authIsFailed(boolean failWithException);

    void startVideoActivity();
}
