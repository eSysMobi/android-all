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
    public void closeCart() {
        realmTemplate.executeInRealm(new RealmTransactionCallback<Object>() {
            @Override
            public Object execute(Realm realm) {
                Cart searched = realm.where(Cart.class).findFirst();
                if (searched != null) {
                    searched.closeCart();
                }
                return null;
            }
        });
    }

    @Override
    public Cart get() {
        return realmTemplate.executeInRealm(new RealmTransactionCallback<Cart>() {
            @Override
            public Cart execute(Realm realm) {
                Cart searched = realm.where(Cart.class).findFirst();
                if (searched == null) {
                    Cart newCart = new Cart(true, 0, "");
                    realm.copyToRealmOrUpdate(newCart);
                    changeAlert();
                    return newCart;
                } else {
                    if(!searched.isOpened()){
                        searched.nextOrderID();
                        changeAlert();
                    }
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
                Cart cart = get();
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

    private void changeAlert(){
        if (uow.isStarted()) {
            uow.addEventToBroadcast(CartUpdateEvent.class.getName());
        } else {
            bus.post(new CartUpdateEvent());
        }
    }
}
