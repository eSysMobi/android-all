package mobi.esys.dastarhan.database;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;

class RealmUnitOfWork implements UnitOfWork {

    private final RealmConfiguration realmConfiguration;
    private final ThreadLocal<Realm> realmThreadLocal = new ThreadLocal<>();

    @Inject
    RealmUnitOfWork(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    @Override
    public void commit() {
        Realm realm = realmThreadLocal.get();
        if (realm == null) {
            throw new IllegalStateException("You can't use commit() without use startUOW() first");
        } else {
            realm.commitTransaction();
            realm.close();
            realmThreadLocal.remove();
        }
    }

    @Override
    public void cancel() {
        Realm realm = realmThreadLocal.get();
        if (realm == null) {
            throw new IllegalStateException("You can't use cancel() without use startUOW() first");
        } else {
            realm.cancelTransaction();
            realm.close();
            realmThreadLocal.remove();
        }
    }

    @Override
    public void startUOW() {
        if (realmThreadLocal.get() == null) {
            realmThreadLocal.set(Realm.getInstance(realmConfiguration));
            realmThreadLocal.get().beginTransaction();
        } else {
            throw new IllegalStateException("UOW already started");
        }
    }

    @Override
    public boolean isStarted() {
        return realmThreadLocal.get() != null;
    }

    final Realm getRealm() {
        return realmThreadLocal.get();
    }

}
