package mobi.esys.dastarhan.database;

import android.util.Log;

/**
 * Created by ZeyUzh on 04.09.2016.
 */
public class CommonOperation {

    public static void createOrder(RealmComponent component, Food food) {
        CartRepository repo = component.cartRepository();
        OrderRepository orderRepository = component.orderRepository();
        Cart cart = repo.get();
        boolean cartIsOpened = cart.isOpened();

        Log.d("dtagCreateOrder", "Saving order in cart with ID: " + cart.getCurrentOrderID());

        if (!cartIsOpened) {
            cart = new Cart(true, cart.getCurrentOrderID() + 1, "");
            repo.createOrUpdate(cart);
        }

        Order order = new Order(cart.getCurrentOrderID(), food.getServer_id(), 1, food.getPrice());
        orderRepository.add(order);
    }

}
