package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ZeyUzh on 25.08.2016.
 */
public class Promo extends RealmObject {

    @PrimaryKey
    private long server_id;
    private Integer condition;      //условие акции(1- Сумма заказа больше..., 2 - Покупка определенной группы блюд, 3 - Покупка одного блюда, 4 - покупка блюда из категории, 5 - промо-код, 6 - самовывоз)
    private String condition_par;   //параметр для условия акции. Его форма зависит от значения condition:
                                    //1 - сумма заказа, при покупки на которую работает акция
                                    //2 - группа блюд, представленная в json формате, например ["3","4"], где 3, 4 - id блюд
                                    //3 - id блюда, при покупки которого наступает акция
                                    //4 - id категории, при покупки из которой наступает акция
                                    //5 - промокод(он будет скрыт)
                                    //6 - ничего
    private boolean limitedTime;    //0 (false) - Акция действует в любое время, 1 (true) - акция действует с time1 по time2(это время дня)
    private String time1;           //время дня начала действия акции
    private String time2;           //время дня конца действия акции
    private String days;            //дни недели по номерам когда действует акция
    private boolean limitedData;    //0 (false) - Акция действует в любое время, 1 (true) - акция действует с date1 по date2
    private String date1;           //дата начала действия акции
    private String date2;           //дата конца действия акции
    private String gift_type;       //discount_percent_all - Скидка в процентах на весь заказ
                                    //discount_amount_all - Скидка в рублях на весь заказ
                                    //discount_percent_offer - Скидка в процентах на блюда, обозначенные в условиях акции
                                    //discount_amount_offer  - Скидка в рублях на блюда, обозначенные в условиях акции
                                    //gift_goods - Блюда, которые получают в подарок
                                    //free_delivery - бесплатная доставка
    private String gift;
    private boolean gift_condition; //Условия предоставление подарков в акции, 0 (false) - И, 1 (true) - ИЛИ

    /**
     * For Realm usage only
     */
    public Promo() {
        //For Realm usage only
    }

    public Promo(long server_id,
                 Integer condition,
                 String condition_par,
                 boolean limitedTime,
                 String time1,
                 String time2,
                 String days,
                 boolean limitedData,
                 String date1,
                 String date2,
                 String gift_type,
                 String gift,
                 boolean gift_condition) {
        this.server_id = server_id;
        this.condition = condition;
        this.condition_par = condition_par;
        this.limitedTime = limitedTime;
        this.time1 = time1;
        this.time2 = time2;
        this.days = days;
        this.limitedData = limitedData;
        this.date1 = date1;
        this.date2 = date2;
        this.gift_type = gift_type;
        this.gift = gift;
        this.gift_condition = gift_condition;
    }

    public long getServer_id() {
        return server_id;
    }

    public Integer getCondition() {
        return condition;
    }

    public String getCondition_par() {
        return condition_par;
    }

    public boolean isLimitedTime() {
        return limitedTime;
    }

    public String getTime1() {
        return time1;
    }

    public String getTime2() {
        return time2;
    }

    public String getDays() {
        return days;
    }

    public boolean isLimitedData() {
        return limitedData;
    }

    public String getDate1() {
        return date1;
    }

    public String getDate2() {
        return date2;
    }

    public String getGift_type() {
        return gift_type;
    }

    public String getGift() {
        return gift;
    }

    public boolean isGift_condition() {
        return gift_condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Promo promo = (Promo) o;

        if (server_id != promo.server_id) return false;
        if (limitedTime != promo.limitedTime) return false;
        if (limitedData != promo.limitedData) return false;
        if (gift_condition != promo.gift_condition) return false;
        if (condition != null ? !condition.equals(promo.condition) : promo.condition != null)
            return false;
        if (condition_par != null ? !condition_par.equals(promo.condition_par) : promo.condition_par != null)
            return false;
        if (time1 != null ? !time1.equals(promo.time1) : promo.time1 != null) return false;
        if (time2 != null ? !time2.equals(promo.time2) : promo.time2 != null) return false;
        if (days != null ? !days.equals(promo.days) : promo.days != null) return false;
        if (date1 != null ? !date1.equals(promo.date1) : promo.date1 != null) return false;
        if (date2 != null ? !date2.equals(promo.date2) : promo.date2 != null) return false;
        if (gift_type != null ? !gift_type.equals(promo.gift_type) : promo.gift_type != null)
            return false;
        return !(gift != null ? !gift.equals(promo.gift) : promo.gift != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (server_id ^ (server_id >>> 32));
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (condition_par != null ? condition_par.hashCode() : 0);
        result = 31 * result + (limitedTime ? 1 : 0);
        result = 31 * result + (time1 != null ? time1.hashCode() : 0);
        result = 31 * result + (time2 != null ? time2.hashCode() : 0);
        result = 31 * result + (days != null ? days.hashCode() : 0);
        result = 31 * result + (limitedData ? 1 : 0);
        result = 31 * result + (date1 != null ? date1.hashCode() : 0);
        result = 31 * result + (date2 != null ? date2.hashCode() : 0);
        result = 31 * result + (gift_type != null ? gift_type.hashCode() : 0);
        result = 31 * result + (gift != null ? gift.hashCode() : 0);
        result = 31 * result + (gift_condition ? 1 : 0);
        return result;
    }
}
