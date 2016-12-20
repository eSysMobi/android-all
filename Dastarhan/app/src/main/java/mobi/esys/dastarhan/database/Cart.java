package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Cart extends RealmObject {

    @PrimaryKey
    private int inner_id = 42;
    private boolean opened;
    private int current_order_id;
    private String notice;

    /**
     * For Realm usage only
     */
    public Cart() {
        //For Realm usage only
    }

    public Cart(boolean opened, int current_order_id, String notice) {
        this.opened = opened;
        this.current_order_id = current_order_id;
        this.notice = notice;
    }

    public boolean isOpened() {
        return opened;
    }

    public int getCurrentOrderID() {
        return current_order_id;
    }

    public String getNotice() {
        return notice;
    }

    public void closeCart() {
        this.opened = false;
    }

    public void nextOrderID() {
        current_order_id = current_order_id++;
        opened = true;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cart cart = (Cart) o;

        if (opened != cart.opened) return false;
        if (current_order_id != cart.current_order_id) return false;
        return !(notice != null ? !notice.equals(cart.notice) : cart.notice != null);

    }

    @Override
    public int hashCode() {
        int result = (opened ? 1 : 0);
        result = 31 * result + (int) (current_order_id ^ (current_order_id >>> 32));
        result = 31 * result + (notice != null ? notice.hashCode() : 0);
        return result;
    }
}
