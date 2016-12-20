package mobi.esys.dastarhan.net;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APISendOrder {
    //send order
    //    example http://dastarhan.net/index.php/user_api/ordplace/format/json?uid=59&apikey=ySdzeYpL3819VM2w7V34&addr=4&gift=2&promo=dsfsdf&payment=m
    //    uid - id юзера
    //    apikey - apikey
    //    payment_method - метод оплаты(m - деньги, c - карта)
    //    addr - id адреса
    //    gift - id блюда, выбранного как подарок
    //    promo - промо-код
    //    payment - метод оплаты(m - наличные, c - карта)
    //    itemX - food id, где X - 1,2,3,4...N
    //    qtX - количество этой еды

    @GET("/index.php/user_api/ordplace/format/json")
    Call<ResponseBody> sendOrder(@Query("uid") int userID,
                                 @Query("apikey") String apikey,
                                 @Query("addr") int addressID,
                                 @Query("payment") String payment,
                                 @QueryMap Map<String, String> foodOrders);

    @GET("/index.php/user_api/ordplace/format/json")
    Call<ResponseBody> sendOrder(@Query("uid") int userID,
                                 @Query("apikey") String apikey,
                                 @Query("addr") int addressID,
                                 @Query("gift") int giftID,
                                 @Query("promo") String promo,
                                 @Query("payment") String payment,
                                 @QueryMap Map<String, String> foodOrders);
}
