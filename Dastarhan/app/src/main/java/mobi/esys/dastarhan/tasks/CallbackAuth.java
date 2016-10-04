package mobi.esys.dastarhan.tasks;

/**
 * Created by ZeyUzh on 02.10.2016.
 */
public interface CallbackAuth {

    void onPrepared();

    void onSuccessAuth(String authToken);

    void onSuccessSighUp(String email, String pass);

    void onFail(int errorCode);
}
