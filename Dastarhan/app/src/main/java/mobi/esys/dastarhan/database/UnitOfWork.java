package mobi.esys.dastarhan.database;

public interface UnitOfWork {
    void commit();

    void cancel();

    void startUOW();

    boolean isStarted();
}
