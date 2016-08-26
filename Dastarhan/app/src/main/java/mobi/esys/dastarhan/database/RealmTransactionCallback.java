package mobi.esys.dastarhan.database;

import io.realm.Realm;

interface RealmTransactionCallback<C> {
    C execute(Realm realm);
}