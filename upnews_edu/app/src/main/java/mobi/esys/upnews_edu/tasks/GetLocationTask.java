package mobi.esys.upnews_edu.tasks;

/**
 * Created by ZeyUzh on 24.07.2016.
 */

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

import mobi.esys.upnews_edu.eventbus.EventGetLocComplete;

public class GetLocationTask extends AsyncTask<Void, Void, Void> implements LocationListener {
    private Context ContextAsync;
    private EventBus bus = EventBus.getDefault();
    private String city = "";

    public GetLocationTask(Context context) {
        this.ContextAsync = context;
    }

    private String providerAsync;
    private LocationManager locationManagerAsync;
    double latAsync = 0.0;
    double lonAsync = 0.0;

    String AddressAsync = "";
    Geocoder GeocoderAsync;

    Location location;

    @Override
    protected Void doInBackground(Void... arg0) {
        locationManagerAsync = (LocationManager) ContextAsync.getSystemService(Context.LOCATION_SERVICE);

        providerAsync = LocationManager.NETWORK_PROVIDER;

        if (locationManagerAsync.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            providerAsync = LocationManager.NETWORK_PROVIDER;
        } else if (locationManagerAsync.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            providerAsync = LocationManager.PASSIVE_PROVIDER;
            //Toast.makeText(ContextAsync, "Switch On Data Connection!!!!", Toast.LENGTH_LONG).show();
        }

        location = locationManagerAsync.getLastKnownLocation(providerAsync);
        // Initialize the location fields
        if (location != null) {
            //  System.out.println("Provider " + provider + " has been selected.");
            latAsync = location.getLatitude();
            lonAsync = location.getLongitude();

        } else {
            //Toast.makeText(ContextAsync, " Locationnot available", Toast.LENGTH_SHORT).show();
        }

        List<Address> addresses = null;
        GeocoderAsync = new Geocoder(ContextAsync, Locale.ENGLISH);
        try {
            addresses = GeocoderAsync.getFromLocation(latAsync, lonAsync, 1);
            city = addresses.get(0).getAddressLine(1);
        } catch (Exception e) {
            e.printStackTrace();
            AddressAsync = "Refresh for the address";
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        onLocationChanged(location);
        if (city != null) {
            bus.post(new EventGetLocComplete(city));
        }
        locationManagerAsync.removeUpdates(this);
        super.onPostExecute(result);
    }


    @Override
    public void onLocationChanged(Location location) {
        locationManagerAsync.requestLocationUpdates(providerAsync, 0, 0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}