package mobi.esys.dastarhan.database;

import java.util.List;

public interface OrderRepository {

    void add(Order order);

    List<Order> getById(long id_order);

}