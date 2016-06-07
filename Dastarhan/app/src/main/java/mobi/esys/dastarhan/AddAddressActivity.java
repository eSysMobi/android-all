package mobi.esys.dastarhan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import mobi.esys.dastarhan.utils.AppLocationService;
import mobi.esys.dastarhan.utils.LocationAddress;

public class AddAddressActivity extends AppCompatActivity {

    private final String TAG = "dtagAddAddress";
    private EditText metAddAddressCity;
    private EditText metAddAddressDistrict;
    private EditText metAddAddressExactAddress;
    private Button mbAddAddress;
    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        metAddAddressCity = (EditText) findViewById(R.id.etAddAddressCity);
        metAddAddressDistrict = (EditText) findViewById(R.id.etAddAddressDistrict);
        metAddAddressExactAddress = (EditText) findViewById(R.id.etAddAddressExactAddress);
        mbAddAddress = (Button) findViewById(R.id.bAddAddress);

        appLocationService = new AppLocationService(AddAddressActivity.this);
        Location location = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

        //you can hard-code the lat & long if you have issues with getting it
        //remove the below if-condition and use the following couple of lines
        //double latitude = 37.422005;
        //double longitude = -122.084095

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        } else {
            showSettingsAlert();
        }

        mbAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AddAddressActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        AddAddressActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress = "";
            String city = "";
            String street = "";
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    city = bundle.getString("city");
                    street = bundle.getString("street");

                    Log.d(TAG, "Get address bundle");

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

                    if (locationAddress == null || locationAddress.isEmpty()) {
                        metAddAddressCity.setText(prefs.getString("city",""));
                        metAddAddressDistrict.setText(prefs.getString("street",""));
                        metAddAddressExactAddress.setText(prefs.getString("address",""));
                    } else {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("city",city);
                        editor.putString("street",street);
                        editor.putString("address",locationAddress);
                        editor.apply();

                        metAddAddressCity.setText(city);
                        metAddAddressDistrict.setText(street);
                        metAddAddressExactAddress.setText(locationAddress);
                    }
                    break;
            }
        }
    }
}
