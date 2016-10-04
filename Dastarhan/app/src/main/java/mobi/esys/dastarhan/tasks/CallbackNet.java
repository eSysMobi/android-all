package mobi.esys.dastarhan.tasks;

/**
 * Created by ZeyUzh on 02.10.2016.
 */
public interface CallbackNet {

    void onPrepared();

    void onSuccessAuth();

    void onFail(int errorCode);
}
