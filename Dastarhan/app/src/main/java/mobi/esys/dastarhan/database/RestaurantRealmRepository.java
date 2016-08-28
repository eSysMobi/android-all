package mobi.esys.dastarhan.database;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class RestaurantRealmRepository implements RestaurantRepository {
    private final RealmTemplate realmTemplate;

    @Inject
    RestaurantRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
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
    public Restaurant getByCuisine(final int cuisineID) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Restaurant>() {
            @Override
            public Restaurant execute(Realm realm) {
                Restaurant searched = realm.where(Restaurant.class).contains("cuisines", String.valueOf(cuisineID)).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
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
