package mobi.esys.dastarhan.database;

import io.realm.Realm;
import io.realm.RealmConfiguration;

class RealmTemplate {

    private final RealmConfiguration configuration;
    private final RealmUnitOfWork uow;
    private boolean isTxLocal;

    RealmTemplate(RealmConfiguration configuration, UnitOfWork uow) {
        this.configuration = configuration;
        this.uow = (RealmUnitOfWork) uow;
    }

    final <T> T executeInRealm(RealmTransactionCallback<T> code) {
        Realm realm = getRealm();
        if (isTxLocal) {
            realm.beginTransaction();
        }
        try {
            T result = code.execute(realm);
            if (isTxLocal) {
                realm.commitTransaction();
            }
            return result;
        } catch (RuntimeException e) {
            if (isTxLocal) {
                realm.cancelTransaction();
            }
            throw e;
        } finally {
            if (isTxLocal) {
                realm.close();
            }
        }
    }

    final <T> T findInRealm(RealmTransactionCallback<T> code) {
        Realm realm = getRealm();
        try {
            return code.execute(realm);
        } finally {
            if (isTxLocal) {
                realm.close();
            }
        }
    }

    private Realm getRealm() {
        //get Realm from current UOW if it exists
        if (uow.isStarted()) {
            isTxLocal = false;
            return uow.getRealm();
        } else {
            isTxLocal = true;
            return Realm.getInstance(configuration);
        }
    }

}