package mobi.esys.dastarhan.database;

import java.util.List;

public interface PromoRepository {

    void addOrUpdate(Promo promo);

    Promo getById(long id);

    List<Promo> getAll();
}