package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ZeyUzh on 25.08.2016.
 */
public class Cuisine extends RealmObject {

    @PrimaryKey
    private long server_id;
    private String ru_name;
    private String en_name;
    private boolean approved;

    /**
     * For Realm usage only
     */
    public Cuisine() {
        //For Realm usage only
    }

    public Cuisine(long server_id, String ru_name, String en_name, boolean approved) {
        this.server_id = server_id;
        this.ru_name = ru_name;
        this.en_name = en_name;
        this.approved = approved;
    }

    public long getServer_id() {
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
}
