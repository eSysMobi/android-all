package mobi.esys.dastarhan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import mobi.esys.dastarhan.database.CityRepository;
import mobi.esys.dastarhan.database.DistrictRepository;
import mobi.esys.dastarhan.database.UserInfo;
import mobi.esys.dastarhan.database.UserInfoRepository;
import mobi.esys.dastarhan.utils.AppLocationService;
import mobi.esys.dastarhan.utils.LocationAddress;

public class AddAddressActivity extends AppCompatActivity {


    private final String TAG = "dtagAddAddress";

    @Inject
    UserInfoRepository userInfoRepo;
    private UserInfo userInfo;
    @Inject
    CityRepository cityRepository;
    @Inject
    DistrictRepository districtRepository;

    //layers
    private LinearLayout llAddressLoading;
    private ScrollView svAddressContent;

    private EditText metAddAddressCity;
    private EditText metAddAddressDistrict;
    private EditText metAddAddressHouse;
    private EditText metAddAddressApartment;
    private EditText metAddAddressNotice;
    private Button mbAddAddress;
    private ProgressBar mpbAddAddress;

    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        userInfo = userInfoRepo.get();

        llAddressLoading = (LinearLayout) findViewById(R.id.llAddressLoading);
        svAddressContent = (ScrollView) findViewById(R.id.svAddressContent);
        llAddressLoading.setVisibility(View.VISIBLE);
        svAddressContent.setVisibility(View.GONE);

        //TODO rewrite to new UI
        metAddAddressCity = (EditText) findViewById(R.id.etAddressName);
        metAddAddressDistrict = (EditText) findViewById(R.id.etAddressPhone);
        metAddAddressHouse = (EditText) findViewById(R.id.etAddAddressHouse);
        metAddAddressApartment = (EditText) findViewById(R.id.etAddAddressApartment);
        metAddAddressNotice = (EditText) findViewById(R.id.etAddAddressNotice);
        mbAddAddress = (Button) findViewById(R.id.bAddAddress);
        mpbAddAddress = (ProgressBar) findViewById(R.id.pbAddAddress);

        metAddAddressCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddAddressDistrict.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddAddressDistrict.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddAddressHouse.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddAddressHouse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddAddressApartment.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddAddressApartment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddAddressNotice.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddAddressNotice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        appLocationService = new AppLocationService(AddAddressActivity.this);
        Location location = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

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
                if (!metAddAddressCity.getText().toString().isEmpty()) {
                    if (!metAddAddressDistrict.getText().toString().isEmpty()) {
                        if (!metAddAddressHouse.getText().toString().isEmpty()) {

//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("city", metAddAddressCity.getText().toString());
//                            editor.putString("street", metAddAddressDistrict.getText().toString());
//                            editor.putString("house", metAddAddressHouse.getText().toString());
//                            editor.putString("apartment", metAddAddressApartment.getText().toString());
//                            editor.putString("notice_addr", metAddAddressNotice.getText().toString());
//                            editor.apply();
                            //TODO save user info from view to DB
                            //userInfo.update();
                            userInfoRepo.update(userInfo);

                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(AddAddressActivity.this, R.string.need_enter_house_num, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddAddressActivity.this, R.string.need_enter_district, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddAddressActivity.this, R.string.need_enter_city, Toast.LENGTH_SHORT).show();
                }
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

                    if (locationAddress == null || locationAddress.isEmpty()) {
                        //TODO get from DB
//                        metAddAddressCity.setText(prefs.getString("city", ""));
//                        metAddAddressDistrict.setText(prefs.getString("street", ""));
                    } else {
                        metAddAddressCity.setText(city);
                        metAddAddressDistrict.setText(street);

                    }
                    //TODO get from DB
//                        metAddAddressHouse.setText(prefs.getString("house", ""));
//                        metAddAddressApartment.setText(prefs.getString("apartment", ""));
//                        metAddAddressNotice.setText(prefs.getString("notice_addr", ""));
                    mpbAddAddress.setVisibility(View.GONE);
                    mbAddAddress.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
