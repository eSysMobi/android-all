package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class RestaurantRealmRepository implements RestaurantRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    RestaurantRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void addOrUpdate(final Restaurant restaurant) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(restaurant);
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(RestaurantUpdateEvent.class.getName());
        } else {
            bus.post(new RestaurantUpdateEvent());
        }
    }

    @Override
    public Restaurant getById(final int id) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Restaurant>() {
            @Override
            public Restaurant execute(Realm realm) {
                Restaurant searched = realm.where(Restaurant.class).equalTo("server_id", id).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }

    @Override
    public List<Restaurant> getByCuisine(final int cuisineID) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Restaurant>>() {
            @Override
            public List<Restaurant> execute(Realm realm) {
                List<Restaurant> searched = realm.where(Restaurant.class).contains("cuisines", String.valueOf(cuisineID)).findAll();
                List<Restaurant> restaurants = new ArrayList<>();
                for (Restaurant restaurant : searched) {
                    restaurants.add(realm.copyFromRealm(restaurant));
                }
                return restaurants;
            }
        });
    }

    @Override
    public List<Restaurant> getAll() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Restaurant>>() {
            @Override
            public List<Restaurant> execute(Realm realm) {
                RealmResults<Restaurant> searched = realm.where(Restaurant.class).findAll();
                List<Restaurant> restaurants = new ArrayList<>();
                for (Restaurant restaurant : searched) {
                    restaurants.add(realm.copyFromRealm(restaurant));
                }
                return restaurants;
            }
        });
    }

}
