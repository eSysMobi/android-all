package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Order extends RealmObject {

    @PrimaryKey
    private int id_order;
    private int id_food;
    private int count;
    private double price;

    /**
     * For Realm usage only
     */
    public Order() {
        //For Realm usage only
    }

    public Order(int id_order, int id_food, int count, double price) {
        this.id_order = id_order;
        this.id_food = id_food;
        this.count = count;
        this.price = price;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId_order() {
        return id_order;
    }

    public int getId_food() {
        return id_food;
    }

    public int getCount() {
        return count;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (id_order != order.id_order) return false;
        if (id_food != order.id_food) return false;
        if (count != order.count) return false;
        return Double.compare(order.price, price) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id_order;
        result = 31 * result + (int) (id_food ^ (id_food >>> 32));
        result = 31 * result + count;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
