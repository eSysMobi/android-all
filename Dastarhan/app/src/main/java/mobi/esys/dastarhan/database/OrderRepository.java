package mobi.esys.dastarhan.database;

import java.util.List;

public interface OrderRepository {

    void add(Order order);

    void update(Order order);

    List<Order> getById(int id_order);

}