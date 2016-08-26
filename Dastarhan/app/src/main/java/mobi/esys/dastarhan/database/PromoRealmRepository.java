package mobi.esys.dastarhan.database;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class PromoRealmRepository implements PromoRepository {
    private final RealmTemplate realmTemplate;

    @Inject
    PromoRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
    }

    @Override
    public void addOrUpdate(final Promo promo) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(promo);
                return null;
            }
        });
    }

    @Override
    public Promo getById(final long id) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Promo>() {
            @Override
            public Promo execute(Realm realm) {
                Promo searched = realm.where(Promo.class).equalTo("server_id", id).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }

    @Override
    public List<Promo> getAll() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Promo>>() {
            @Override
            public List<Promo> execute(Realm realm) {
                RealmResults<Promo> searched = realm.where(Promo.class).findAll();
                List<Promo> promos = new ArrayList<>();
                for (Promo promo : searched) {
                    promos.add(realm.copyFromRealm(promo));
                }
                return promos;
            }
        });
    }

}
