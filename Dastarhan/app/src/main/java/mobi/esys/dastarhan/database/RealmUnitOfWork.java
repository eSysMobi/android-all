package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;

class RealmUnitOfWork implements UnitOfWork {

    private final RealmConfiguration realmConfiguration;
    private final ThreadLocal<Realm> realmThreadLocal = new ThreadLocal<>();
    private ThreadLocal<List<String>> eventsForBroadcasts = new ThreadLocal<>();
    private final EventBus bus = EventBus.getDefault();

    @Inject
    RealmUnitOfWork(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    @Override
    public void commit() {
        Realm realm = realmThreadLocal.get();
        if (realm == null) {
            throw new IllegalStateException("You can't use commit() without use startUOW() first");
        } else {
            realm.commitTransaction();
            realm.close();
            realmThreadLocal.remove();
        }

        for (String event : eventsForBroadcasts.get()) {
            if (event.equals(CartUpdateEvent.class.getName())) {
                bus.post(new CartUpdateEvent());
            } else if (event.equals(CuisineUpdateEvent.class.getName())) {
                bus.post(new CuisineUpdateEvent());
            } else if (event.equals(FoodUpdateEvent.class.getName())) {
                bus.post(new FoodUpdateEvent());
            } else if (event.equals(OrderUpdateEvent.class.getName())) {
                bus.post(new OrderUpdateEvent());
            } else if (event.equals(PromoUpdateEvent.class.getName())) {
                bus.post(new PromoUpdateEvent());
            } else if (event.equals(RestaurantUpdateEvent.class.getName())) {
                bus.post(new RestaurantUpdateEvent());
            }
        }
        eventsForBroadcasts.remove();
    }

    @Override
    public void cancel() {
        Realm realm = realmThreadLocal.get();
        if (realm == null) {
            throw new IllegalStateException("You can't use cancel() without use startUOW() first");
        } else {
            realm.cancelTransaction();
            realm.close();
            realmThreadLocal.remove();
        }
        eventsForBroadcasts.remove();
    }

    @Override
    public void startUOW() {
        List<String> events = new ArrayList<>();
        eventsForBroadcasts.set(events);
        if (realmThreadLocal.get() == null) {
            realmThreadLocal.set(Realm.getInstance(realmConfiguration));
            realmThreadLocal.get().beginTransaction();
        } else {
            throw new IllegalStateException("UOW already started");
        }
    }

    @Override
    public void addEventToBroadcast(String eventName) {
        eventsForBroadcasts.get().add(eventName);
    }

    @Override
    public boolean isStarted() {
        return realmThreadLocal.get() != null;
    }

    final Realm getRealm() {
        return realmThreadLocal.get();
    }

}
