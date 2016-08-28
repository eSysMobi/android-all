package mobi.esys.dastarhan.database;

import java.util.List;

public interface PromoRepository {

    void addOrUpdate(Promo promo);

    Promo getById(int id);

    List<Promo> getAll();
}