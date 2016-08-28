package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Food extends RealmObject {

    @PrimaryKey
    private int server_id;
    private int res_id;
    private int cat_id;
    private String ru_name;
    private String en_name;
    private String picture;
    private String ru_descr;
    private String en_descr;
    private long price;
    private Integer min_amount;
    private String units;
    private Integer offer;
    private boolean vegetarian;
    private boolean favorite;
    private boolean featured;
    private boolean ordered;

    /**
     * For Realm usage only
     */
    public Food() {
        //For Realm usage only
    }

    public Food(int server_id,
                int res_id,
                int cat_id,
                String ru_name,
                String en_name,
                String picture,
                String ru_descr,
                String en_descr,
                long price,
                Integer min_amount,
                String units,
                Integer offer,
                boolean vegetarian,
                boolean featured) {
        this.server_id = server_id;
        this.res_id = res_id;
        this.cat_id = cat_id;
        this.ru_name = ru_name;
        this.en_name = en_name;
        this.picture = picture;
        this.ru_descr = ru_descr;
        this.en_descr = en_descr;
        this.price = price;
        this.min_amount = min_amount;
        this.units = units;
        this.offer = offer;
        this.vegetarian = vegetarian;
        this.featured = featured;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public int getServer_id() {
        return server_id;
    }

    public int getRes_id() {
        return res_id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public String getRu_name() {
        return ru_name;
    }

    public String getEn_name() {
        return en_name;
    }

    public String getPicture() {
        return picture;
    }

    public String getRu_descr() {
        return ru_descr;
    }

    public String getEn_descr() {
        return en_descr;
    }

    public long getPrice() {
        return price;
    }

    public Integer getMin_amount() {
        return min_amount;
    }

    public String getUnits() {
        return units;
    }

    public Integer getOffer() {
        return offer;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isFeatured() {
        return featured;
    }

    public boolean isOrdered() {
        return ordered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Food food = (Food) o;

        if (server_id != food.server_id) return false;
        if (res_id != food.res_id) return false;
        if (cat_id != food.cat_id) return false;
        if (price != food.price) return false;
        if (vegetarian != food.vegetarian) return false;
        if (favorite != food.favorite) return false;
        if (featured != food.featured) return false;
        if (ordered != food.ordered) return false;
        if (ru_name != null ? !ru_name.equals(food.ru_name) : food.ru_name != null) return false;
        if (en_name != null ? !en_name.equals(food.en_name) : food.en_name != null) return false;
        if (picture != null ? !picture.equals(food.picture) : food.picture != null) return false;
        if (ru_descr != null ? !ru_descr.equals(food.ru_descr) : food.ru_descr != null)
            return false;
        if (en_descr != null ? !en_descr.equals(food.en_descr) : food.en_descr != null)
            return false;
        if (min_amount != null ? !min_amount.equals(food.min_amount) : food.min_amount != null)
            return false;
        if (units != null ? !units.equals(food.units) : food.units != null) return false;
        return !(offer != null ? !offer.equals(food.offer) : food.offer != null);

    }

    @Override
    public int hashCode() {
        int result = server_id;
        result = 31 * result + res_id;
        result = 31 * result + cat_id;
        result = 31 * result + (ru_name != null ? ru_name.hashCode() : 0);
        result = 31 * result + (en_name != null ? en_name.hashCode() : 0);
        result = 31 * result + (picture != null ? picture.hashCode() : 0);
        result = 31 * result + (ru_descr != null ? ru_descr.hashCode() : 0);
        result = 31 * result + (en_descr != null ? en_descr.hashCode() : 0);
        result = 31 * result + (int) (price ^ (price >>> 32));
        result = 31 * result + (min_amount != null ? min_amount.hashCode() : 0);
        result = 31 * result + (units != null ? units.hashCode() : 0);
        result = 31 * result + (offer != null ? offer.hashCode() : 0);
        result = 31 * result + (vegetarian ? 1 : 0);
        result = 31 * result + (favorite ? 1 : 0);
        result = 31 * result + (featured ? 1 : 0);
        result = 31 * result + (ordered ? 1 : 0);
        return result;
    }
}
