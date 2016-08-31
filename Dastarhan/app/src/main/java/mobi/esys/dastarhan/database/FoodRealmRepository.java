package mobi.esys.dastarhan.database;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;


class FoodRealmRepository implements FoodRepository {
    private final RealmTemplate realmTemplate;

    @Inject
    FoodRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
    }

    @Override
    public void addOrUpdate(final Food food) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(food);
                return null;
            }
        });
    }

    @Override
    public void updateFavorites(final int serverFoodID, final boolean fav) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                Food searched = realm.where(Food.class).equalTo("server_id", serverFoodID).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    searched.setFavorite(fav);
                }
                return null;
            }
        });
    }

    @Override
    public void updateOrdered(final int id, final boolean isOrdered) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                Food searched = realm.where(Food.class).equalTo("server_id", id).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    searched.setOrdered(isOrdered);
                }
                return null;
            }
        });
    }

    @Override
    public Food getById(final int id) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Food>() {
            @Override
            public Food execute(Realm realm) {
                Food searched = realm.where(Food.class).equalTo("server_id", id).findFirst();
                if (searched == null) {
                    return null;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }

    @Override
    public List<Food> getByIds(final Integer[] IDs) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmQuery<Food> query = realm.where(Food.class);
                for (int i = 0; i < IDs.length; i++) {
                    if (i == 0) {
                        query.equalTo("server_id", IDs[i]);
                    } else {
                        query.or().equalTo("server_id", IDs[i]);
                    }
                }
                RealmResults<Food> searched = query.findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }

    @Override
    public List<Food> getByRestaurantID(final int restID) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmResults<Food> searched = realm.where(Food.class).equalTo("res_id", restID).findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }

    @Override
    public List<Food> getByRestaurantIDs(final Integer[] restIDs) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmQuery<Food> query = realm.where(Food.class);
                for (int i = 0; i < restIDs.length; i++) {
                    if (i == 0) {
                        query.equalTo("res_id", restIDs[i]);
                    } else {
                        query.or().equalTo("res_id", restIDs[i]);
                    }
                }
                RealmResults<Food> searched = query.findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }

    @Override
    public List<Food> getByCategoryID(final int catID) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmResults<Food> searched = realm.where(Food.class).equalTo("cat_id", catID).findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }

    @Override
    public List<Food> getByFavorite() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmResults<Food> searched = realm.where(Food.class).equalTo("favorite", true).findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }

    @Override
    public List<Food> getAll() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Food>>() {
            @Override
            public List<Food> execute(Realm realm) {
                RealmResults<Food> searched = realm.where(Food.class).findAll();
                List<Food> foods = new ArrayList<>();
                for (Food food : searched) {
                    foods.add(realm.copyFromRealm(food));
                }
                return foods;
            }
        });
    }
}
