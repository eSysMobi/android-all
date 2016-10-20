package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


class DistrictRealmRepository implements DistrictRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    DistrictRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void createOrUpdate(final District district) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(district);
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
    public List<District> getDistrictsOfCity(final int cityID) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<District>>() {
            @Override
            public List<District> execute(Realm realm) {
                RealmResults<District> searched = realm.where(District.class).equalTo("cityID", cityID).findAll();
                List<District> districts = new ArrayList<>();
                for (District district : searched) {
                    districts.add(realm.copyFromRealm(district));
                }
                return districts;
            }
        });
    }
}
