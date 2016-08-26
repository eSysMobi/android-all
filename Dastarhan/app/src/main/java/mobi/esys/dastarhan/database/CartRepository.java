package mobi.esys.dastarhan.database;

import java.util.List;

public interface CartRepository {

    void createOrUpdate(Cart cart);

    Cart get();

    List<Order> getCurrentCartOrders();
}