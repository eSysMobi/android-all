package mobi.esys.dastarhan.database;

import java.util.List;

public interface OrderRepository {

    void addOrUpdate(Order order);

    List<Order> getById(int id_order);

}