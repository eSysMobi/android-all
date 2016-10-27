package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;

class UserInfoRealmRepository implements UserInfoRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    UserInfoRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void update(final UserInfo userInfo) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(userInfo);
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(CartUpdateEvent.class.getName());
        } else {
            bus.post(new CartUpdateEvent());
        }
    }

    @Override
    public UserInfo get() {
        return realmTemplate.executeInRealm(new RealmTransactionCallback<UserInfo>() {
            @Override
            public UserInfo execute(Realm realm) {
                UserInfo searched = realm.where(UserInfo.class).findFirst();
                if (searched == null) {
                    UserInfo newUserInfo = new UserInfo();
                    realm.copyToRealm(newUserInfo);
                    return newUserInfo;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }
}
