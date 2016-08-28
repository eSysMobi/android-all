package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ZeyUzh on 25.08.2016.
 */
public class Cuisine extends RealmObject {

    @PrimaryKey
    private int server_id;
    private String ru_name;
    private String en_name;
    private boolean approved;

    /**
     * For Realm usage only
     */
    public Cuisine() {
        //For Realm usage only
    }

    public Cuisine(int server_id, String ru_name, String en_name, boolean approved) {
        this.server_id = server_id;
        this.ru_name = ru_name;
        this.en_name = en_name;
        this.approved = approved;
    }

    public int getServer_id() {
        return server_id;
    }

    public String getRu_name() {
        return ru_name;
    }

    public String getEn_name() {
        return en_name;
    }

    public boolean isApproved() {
        return approved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cuisine cuisine = (Cuisine) o;

        if (server_id != cuisine.server_id) return false;
        if (approved != cuisine.approved) return false;
        if (ru_name != null ? !ru_name.equals(cuisine.ru_name) : cuisine.ru_name != null)
            return false;
        return !(en_name != null ? !en_name.equals(cuisine.en_name) : cuisine.en_name != null);

    }

    @Override
    public int hashCode() {
        int result = server_id;
        result = 31 * result + (ru_name != null ? ru_name.hashCode() : 0);
        result = 31 * result + (en_name != null ? en_name.hashCode() : 0);
        result = 31 * result + (approved ? 1 : 0);
        return result;
    }
}
