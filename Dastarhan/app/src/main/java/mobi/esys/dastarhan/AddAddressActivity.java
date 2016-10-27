package mobi.esys.dastarhan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import javax.inject.Inject;

import mobi.esys.dastarhan.database.City;
import mobi.esys.dastarhan.database.CityRepository;
import mobi.esys.dastarhan.database.District;
import mobi.esys.dastarhan.database.DistrictRepository;
import mobi.esys.dastarhan.database.UserInfo;
import mobi.esys.dastarhan.database.UserInfoRepository;
import mobi.esys.dastarhan.net.APIAddress;
import mobi.esys.dastarhan.utils.AppLocationService;
import mobi.esys.dastarhan.utils.CityOrDistrictChooser;
import mobi.esys.dastarhan.utils.LocationAddress;
import mobi.esys.dastarhan.utils.RVChoseCityAdapter;
import mobi.esys.dastarhan.utils.RVChoseDistrictAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddAddressActivity extends AppCompatActivity implements CityOrDistrictChooser {

    private final String TAG = "dtagAddAddress";
    private final static int PERMISSION_REQUEST_CODE = 334;

    @Inject
    UserInfoRepository userInfoRepo;
    private UserInfo userInfo;
    @Inject
    CityRepository cityRepository;
    @Inject
    DistrictRepository districtRepository;
    @Inject
    Retrofit retrofit;
    private APIAddress apiAddress;

    //layers
    private LinearLayout llAddressLoading;
    private ScrollView svAddressContent;

    private EditText metAddressName;
    private EditText metAddressPhone;

    private TextView mtvAddressCity;
    private TextView mtvAddressDistrict;
    private EditText metAddressStreet;
    private EditText metAddressHouse;
    private EditText metAddressBuilding;
    private EditText metAddressApartment;
    private EditText metAddressPorch;
    private EditText metAddressFloor;
    private EditText metAddressIntercom;
    private EditText metAddressNeedChange;
    private EditText metAddressNotice;
    private Button bAddressToOrder;
    private ProgressBar pbAddressToOrder;
    private RecyclerView mrvAddressChooseCity;
    private RecyclerView mrvAddressChooseDistrict;
    private ImageButton mibAddressAutoLocation;

    private City chosenCity;
    private District chosenDistrict;

    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((DastarhanApp)getApplication()).appComponent().inject(this);

        userInfo = userInfoRepo.get();

        llAddressLoading = (LinearLayout) findViewById(R.id.llAddressLoading);
        svAddressContent = (ScrollView) findViewById(R.id.svAddressContent);
        setUILoading(true);

        mtvAddressCity = (TextView) findViewById(R.id.tvAddressCity);
        mtvAddressDistrict = (TextView) findViewById(R.id.tvAddressDistrict);
        metAddressStreet = (EditText) findViewById(R.id.etAddressStreet);
        metAddressHouse = (EditText) findViewById(R.id.etAddressHouse);
        metAddressBuilding = (EditText) findViewById(R.id.etAddressBuilding);
        metAddressApartment = (EditText) findViewById(R.id.etAddressApartment);
        metAddressPorch = (EditText) findViewById(R.id.etAddressPorch);
        metAddressFloor = (EditText) findViewById(R.id.etAddressFloor);
        metAddressIntercom = (EditText) findViewById(R.id.etAddressIntercom);
        metAddressNeedChange = (EditText) findViewById(R.id.etAddressNeedChange);
        metAddressNotice = (EditText) findViewById(R.id.etAddressNotice);
        bAddressToOrder = (Button) findViewById(R.id.bAddressToOrder);
        pbAddressToOrder = (ProgressBar) findViewById(R.id.pbAddressToOrder);
        mrvAddressChooseCity = (RecyclerView) findViewById(R.id.rvAddressChooseCity);
        mrvAddressChooseDistrict = (RecyclerView) findViewById(R.id.rvAddressChooseDistrict);
        mibAddressAutoLocation = (ImageButton) findViewById(R.id.ibAddressAutoLocation);

        apiAddress = retrofit.create(APIAddress.class);

        mtvAddressCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mrvAddressChooseCity.getAdapter() == null) {
                    String locale = getResources().getConfiguration().locale.getLanguage();
                    RVChoseCityAdapter adapter = new RVChoseCityAdapter(AddAddressActivity.this, cityRepository.getCities(), locale);
                    mrvAddressChooseCity.setAdapter(adapter);
                }
                mrvAddressChooseCity.setVisibility(View.VISIBLE);
            }
        });

        mrvAddressChooseCity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mrvAddressChooseCity.setVisibility(View.GONE);
                }
            }
        });

        mtvAddressDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chosenCity==null){
                    Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                } else {
                    if (mrvAddressChooseDistrict.getAdapter() == null) {
                        String locale = getResources().getConfiguration().locale.getLanguage();
                        List<District> districts = districtRepository.getDistrictsOfCity(chosenCity.getCityID());
                        RVChoseDistrictAdapter adapter = new RVChoseDistrictAdapter(AddAddressActivity.this, districts, locale);
                        mrvAddressChooseDistrict.setAdapter(adapter);
                    }
                    mrvAddressChooseDistrict.setVisibility(View.VISIBLE);
                }
            }
        });

        mrvAddressChooseDistrict.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mrvAddressChooseDistrict.setVisibility(View.GONE);
                }
            }
        });

        metAddressStreet.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressHouse.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressHouse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressBuilding.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressBuilding.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressApartment.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressApartment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressPorch.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressPorch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressFloor.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressFloor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressIntercom.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressIntercom.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressNeedChange.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressNeedChange.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressNotice.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metAddressNotice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        mibAddressAutoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGeoLocation();
            }
        });

        //TODO set initial data from userInfo

        //TODO save address and send order
        bAddressToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!metAddressName.getText().toString().isEmpty()) {
                    if (!metAddressPhone.getText().toString().isEmpty()) {
                        if (!mtvAddressCity.getText().toString().isEmpty()) {
                            if (!mtvAddressDistrict.getText().toString().isEmpty()) {
                                if (!metAddressHouse.getText().toString().isEmpty()) {

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
                                Toast.makeText(AddAddressActivity.this, R.string.need_choose_district, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AddAddressActivity.this, R.string.need_choose_city, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddAddressActivity.this, R.string.need_enter_phone_number, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddAddressActivity.this, R.string.enter_your_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //set hiding choosers
        View.OnFocusChangeListener chooserHidingListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    v.setVisibility(View.GONE);
                }
            }
        };
        mrvAddressChooseCity.setOnFocusChangeListener(chooserHidingListener);
        mrvAddressChooseDistrict.setOnFocusChangeListener(chooserHidingListener);

        //download and save cities
        //TODO check this (endless cycle here)
        apiAddress.getCities().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() == 200) {
                    JsonArray jsonResponse = response.body().getAsJsonArray();
                    for (int i = 0; i < jsonResponse.size(); i++) {
                        JsonObject jsonCity = jsonResponse.get(i).getAsJsonObject();
                        if (jsonCity.has("id") && jsonCity.has("ru_name") && jsonCity.has("en_name")) {
                            City newCity = new City(jsonCity.get("id").getAsInt(), jsonCity.get("ru_name").getAsString(), jsonCity.get("en_name").getAsString());
                            cityRepository.createOrUpdate(newCity);
                        }
                    }

                    //download and save districts
                    apiAddress.getAllDistricts().enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.code() == 200 && response.body().has("0")) {
                                JsonArray jsonResponse = response.body().get("0").getAsJsonArray();
                                for (int i = 0; i < jsonResponse.size(); i++) {
                                    JsonObject jsonDistrict = jsonResponse.get(i).getAsJsonObject();
                                    if (jsonDistrict.has("id") && jsonDistrict.has("city_id") && jsonDistrict.has("ru_name") && jsonDistrict.has("en_name")) {
                                        District newDistrict =
                                                new District(
                                                        jsonDistrict.get("id").getAsInt(),
                                                        jsonDistrict.get("city_id").getAsInt(),
                                                        jsonDistrict.get("ru_name").getAsString(),
                                                        jsonDistrict.get("en_name").getAsString());
                                        districtRepository.createOrUpdate(newDistrict);
                                    }
                                }
                                setUILoading(false);
                            } else {
                                Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                } else {
                    Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    public void chooseCity(City city, String localizedName) {
        chosenCity = city;
        mtvAddressCity.setText(localizedName);
        mtvAddressDistrict.setClickable(true);
        mrvAddressChooseCity.setVisibility(View.GONE);
    }

    @Override
    public void chooseDistrict(District district, String localizedName) {
        chosenDistrict = district;
        mrvAddressChooseDistrict.setVisibility(View.GONE);
        metAddressStreet.setEnabled(true);
        metAddressHouse.setEnabled(true);
        metAddressBuilding.setEnabled(true);
        metAddressApartment.setEnabled(true);
        metAddressPorch.setEnabled(true);
        metAddressFloor.setEnabled(true);
        metAddressIntercom.setEnabled(true);
    }

    private void setUILoading(boolean isLoading) {
        if (isLoading) {
            llAddressLoading.setVisibility(View.VISIBLE);
            svAddressContent.setVisibility(View.GONE);
        } else {
            llAddressLoading.setVisibility(View.GONE);
            svAddressContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    unSaveGetGeoLocation();
                }
                break;
            }
        }
    }

    private void getGeoLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddAddressActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                unSaveGetGeoLocation();
            }
        } else {
            unSaveGetGeoLocation();
        }
    }

    private void unSaveGetGeoLocation() {
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
                    city = bundle.getString("city", "");
                    street = bundle.getString("street", "");

                    Log.d(TAG, "Get address bundle. City - " + city + ", street - " + street);

                    if (locationAddress == null || locationAddress.isEmpty()) {
                        Toast.makeText(AddAddressActivity.this, R.string.Geolocation_is_unavailable, Toast.LENGTH_SHORT).show();
                    } else {
                        //TODO find city and district in DB
                        mtvAddressCity.setText(city);
                        mtvAddressDistrict.setText(street);
                        metAddressStreet.setText("");
                        metAddressHouse.setText("");
                        metAddressBuilding.setText("");
                        metAddressApartment.setText("");
                        metAddressPorch.setText("");
                        metAddressFloor.setText("");
                        metAddressIntercom.setText("");
                    }
                    break;
            }
        }
    }
}
