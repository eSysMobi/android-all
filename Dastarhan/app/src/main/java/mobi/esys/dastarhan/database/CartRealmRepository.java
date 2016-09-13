package mobi.esys.dastarhan.database;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class CartRealmRepository implements CartRepository {
    private final RealmTemplate realmTemplate;
    private UnitOfWork uow;
    private EventBus bus = EventBus.getDefault();

    @Inject
    CartRealmRepository(RealmConfiguration config, UnitOfWork uow) {
        realmTemplate = new RealmTemplate(config, uow);
        this.uow = uow;
    }

    @Override
    public void createOrUpdate(final Cart cart) {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                realm.copyToRealmOrUpdate(cart);
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
    public Cart get() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<Cart>() {
            @Override
            public Cart execute(Realm realm) {
                Cart searched = realm.where(Cart.class).findFirst();
                if (searched == null) {
                    Cart newCart = new Cart(true, 0, "");
                    createOrUpdate(newCart);
                    return newCart;
                } else {
                    return realm.copyFromRealm(searched);
                }
            }
        });
    }

    @Override
    public List<Order> getCurrentCartOrders() {
        return realmTemplate.findInRealm(new RealmTransactionCallback<List<Order>>() {
            @Override
            public List<Order> execute(Realm realm) {
                List<Order> orders = new ArrayList<>();
                Cart cart = realm.where(Cart.class).findFirst();
                if (cart == null) {
                    return null;
                } else {
                    RealmResults<Order> searched = realm.where(Order.class).equalTo("id_order", cart.getCurrentOrderID()).findAll();
                    for (Order order : searched) {
                        orders.add(realm.copyFromRealm(order));
                    }
                }
                return orders;
            }
        });
    }
}
