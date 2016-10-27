package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class CityRealmRepository implements CityRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    CityRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void createOrUpdate(final City city) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(city);
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(FoodUpdateEvent.class.getName());
        } else {
            bus.post(new FoodUpdateEvent());
        }
    }

    @Override
    public List<City> getCities() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<City>>() {
            @Override
            public List<City> execute(Realm realm) {
                RealmResults<City> searched = realm.where(City.class).findAll();
                List<City> cities = new ArrayList<>();
                for (City city : searched) {
                    cities.add(realm.copyFromRealm(city));
                }
                return cities;
            }
        });
    }

    @Override
    public City getCityByName(final String name) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<City>() {
            @Override
            public City execute(Realm realm) {
                City searched = realm.where(City.class)
                        .equalTo("cityRuName", name)
                        .or()
                        .equalTo("cityEnName", name)
                        .findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }
}
