package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class OrderRealmRepository implements OrderRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    OrderRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void add(final Order order) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealm(order);
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(OrderUpdateEvent.class.getName());
        } else {
            bus.post(new OrderUpdateEvent());
        }
    }

    @Override
    public void update(final Order order) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                Order searched = realm.where(Order.class).equalTo("id_order", order.getId_order()).equalTo("id_food", order.getId_food()).findFirst();
                if (searched != null) {
                    if (order.getCount() > 0) {
                        searched.setCount(order.getCount());
                    } else {
                        searched.deleteFromRealm();
                    }
                }
                return null;
            }
        });
        if (uow.isStarted()) {
            uow.addEventToBroadcast(OrderUpdateEvent.class.getName());
        } else {
            bus.post(new OrderUpdateEvent());
        }
    }

    @Override
    public List<Order> getById(final int id_order) {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Order>>() {
            @Override
            public List<Order> execute(Realm realm) {
                RealmResults<Order> searched = realm.where(Order.class).equalTo("id_order", id_order).findAll();
                List<Order> orders = new ArrayList<>();
                for (Order order : searched) {
                    orders.add(realm.copyFromRealm(order));
                }
                return orders;
            }
        });
    }

}
