package mobi.esys.dastarhan.utils;

/**
 * Created by ZeyUzh on 04.06.2016.
 */
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationAddress {
    private static final String TAG = "dtagLocationAddress";

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                String city = "";
                String street = "";
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = "";
                try {
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
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (!result.isEmpty()) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        //result = result +" Latitude: " + latitude + " Longitude: " + longitude;
                        bundle.putString("address", result);
                        bundle.putString("city", city);
                        bundle.putString("street", street);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        //result = " Unable to get address for Latitude: " + latitude + " Longitude: " + longitude;
                        bundle.putString("address", "");
                        bundle.putString("city", "");
                        bundle.putString("street", "");
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}