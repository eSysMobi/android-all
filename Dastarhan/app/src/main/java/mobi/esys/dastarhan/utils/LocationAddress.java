package mobi.esys.dastarhan.utils;

/**
 * Created by ZeyUzh on 04.06.2016.
 */

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationAddress {
    private static final String TAG = "dtagLocationAddress";

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Callback callback) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                String city = "";
                String street = "";
                String result = "";
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        city = address.getLocality();
                        street = address.getThoroughfare();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");

                        }
                        sb.append(address.getLocality()).append("\n");
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    if (!result.isEmpty()) {
                        //result = result +" Latitude: " + latitude + " Longitude: " + longitude;
                        callback.receiveAddress(result, city, street);
                    } else {//result = " Unable to get address for Latitude: " + latitude + " Longitude: " + longitude;
                        callback.receiveAddress("", "", "");
                    }
                }
            }
        };
        thread.start();
    }

    public interface Callback {
        void receiveAddress(String address, String city, String street);
    }
}