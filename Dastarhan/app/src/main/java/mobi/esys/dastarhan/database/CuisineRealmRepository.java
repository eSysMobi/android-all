package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class CuisineRealmRepository implements CuisineRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    CuisineRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void addOrUpdate(final Cuisine cuisine) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(cuisine);
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(CuisineUpdateEvent.class.getName());
        } else {
            bus.post(new CuisineUpdateEvent());
        }
    }

    @Override
    public Cuisine getById(final int id) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Cuisine>() {
            @Override
            public Cuisine execute(Realm realm) {
                Cuisine searched = realm.where(Cuisine.class).equalTo("server_id", id).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }

    @Override
    public List<Cuisine> getAll() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Cuisine>>() {
            @Override
            public List<Cuisine> execute(Realm realm) {
                RealmResults<Cuisine> searched = realm.where(Cuisine.class).findAll();
                List<Cuisine> cuisines = new ArrayList<>();
                for (Cuisine cuisine : searched) {
                    cuisines.add(realm.copyFromRealm(cuisine));
                }
                return cuisines;
            }
        });
    }

}
