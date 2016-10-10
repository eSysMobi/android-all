package mobi.esys.dastarhan.net;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import mobi.esys.dastarhan.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetModule {

    private static OkHttpClient httpClient = new OkHttpClient
            .Builder()
            .build();

    @Provides
    @Singleton
    public Retrofit retrofit() {
        return new Retrofit
                .Builder()
                .baseUrl(Constants.API_BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

}