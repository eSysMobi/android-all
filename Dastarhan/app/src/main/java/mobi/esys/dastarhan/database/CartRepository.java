package mobi.esys.dastarhan.database;

import java.util.List;

public interface CartRepository {

    void closeCart();

    Cart get();

    List<Order> getCurrentCartOrders();
}