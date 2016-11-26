package mobi.esys.dastarhan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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

public class AddAddressActivity extends AppCompatActivity implements CityOrDistrictChooser, LocationAddress.Callback {

    private final static String TAG = "dtagAddAddress";
    private final static int PERMISSION_REQUEST_CODE = 334;

    @Inject
    UserInfoRepository userInfoRepo;
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
    private RecyclerView.LayoutManager mLayoutManagerCity;
    private TextView mtvAddressDistrict;
    private RecyclerView.LayoutManager mLayoutManagerDistrict;
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
    private UserInfo userInfoFromDB;

    private boolean isLocatePlaceNow = false;

    private SharedPreferences prefs;

    boolean isRuLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((DastarhanApp) getApplication()).appComponent().inject(this);

        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

        String locale = getResources().getConfiguration().locale.getLanguage();
        if (locale.equals("ru")) {
            isRuLocale = true;
        } else {
            isRuLocale = false;
        }

        llAddressLoading = (LinearLayout) findViewById(R.id.llAddressLoading);
        svAddressContent = (ScrollView) findViewById(R.id.svAddressContent);
        setUILoading(true);

        metAddressName = (EditText) findViewById(R.id.etAddressName);
        metAddressPhone = (EditText) findViewById(R.id.etAddressPhone);

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
        mLayoutManagerCity = new LinearLayoutManager(this);
        mrvAddressChooseCity.setLayoutManager(mLayoutManagerCity);
        mrvAddressChooseDistrict = (RecyclerView) findViewById(R.id.rvAddressChooseDistrict);
        mLayoutManagerDistrict = new LinearLayoutManager(this);
        mrvAddressChooseDistrict.setLayoutManager(mLayoutManagerDistrict);
        mibAddressAutoLocation = (ImageButton) findViewById(R.id.ibAddressAutoLocation);

        apiAddress = retrofit.create(APIAddress.class);

        metAddressName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metAddressPhone.requestFocus();
                    checkRequiredFields();
                    return true;
                }
                return false;
            }
        });

        metAddressPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    mtvAddressCity.requestFocus();
                    checkRequiredFields();
                    return true;
                }
                return false;
            }
        });

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
                if (chosenCity == null) {
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
                    checkRequiredFields();
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

        //save address and send order
        bAddressToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = metAddressName.getText().toString().trim();
                String phone = metAddressPhone.getText().toString().trim();
                String city = mtvAddressCity.getText().toString().trim();
                String district = mtvAddressDistrict.getText().toString().trim();
                String street = metAddressStreet.getText().toString().trim();
                String house = metAddressHouse.getText().toString().trim();
                String building = metAddressBuilding.getText().toString().trim();
                String apartment = metAddressApartment.getText().toString().trim();
                String porch = metAddressPorch.getText().toString().trim();
                String floor = metAddressFloor.getText().toString().trim();
                String intercom = metAddressIntercom.getText().toString().trim();
                String needChange = metAddressNeedChange.getText().toString().trim();
                String notice = metAddressNotice.getText().toString().trim();
                //check is we have all requirments fields
                if (!name.isEmpty()) {
                    if (!phone.isEmpty()) {
                        if (!city.isEmpty()) {
                            if (!district.isEmpty()
                                    || !street.isEmpty()) {
                                if (!metAddressHouse.getText().toString().trim().isEmpty()) {
                                    //prepare data from form
                                    UserInfo userInfoFromForm = new UserInfo();
                                    userInfoFromForm.update(
                                            name, phone,
                                            cityRepository.getCityByName(city).getCityID(), districtRepository.getDistrictByName(district).getDistrictID(),
                                            street, house,
                                            building, apartment,
                                            porch, floor,
                                            intercom, needChange, notice
                                    );
                                    //check is data from form equals date in DB and save if need
                                    if (userInfoFromDB == null) {
                                        userInfoFromDB = userInfoFromForm;
                                        userInfoRepo.update(userInfoFromDB);
                                    } else {
                                        if (!userInfoFromForm.equalsByAddressInfo(userInfoFromDB)) {
                                            //not equals
                                            //save user info from view to DB
                                            userInfoFromDB.update(
                                                    name, phone,
                                                    cityRepository.getCityByName(city).getCityID(), districtRepository.getDistrictByName(district).getDistrictID(),
                                                    street, house,
                                                    building, apartment,
                                                    porch, floor,
                                                    intercom, needChange, notice
                                            );
                                            userInfoRepo.update(userInfoFromDB);
                                        }
                                    }
                                    //check server address id
                                    Integer savedServerAddressID = userInfoFromDB.getServerAddressID();
                                    if (savedServerAddressID != null) {
                                        //send request for delivery cost
                                        requestDeliveryCost();
                                    } else {
                                        //send request for delivery cost
                                        requestUserAddresses();
                                    }
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

        //download and save cities and districts
        apiAddress.getCities().enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.code() == 200) {
                    JsonArray jsonResponse = response.body();
                    Log.d(TAG, "Cities request is ok. Received " + jsonResponse.size() + " cities");
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
                                Log.d(TAG, "Districts request is ok. Received " + jsonResponse.size() + " districts");
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
                                addressBookIsPrepared();
                            } else {
                                Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Error districts request: Response code is not 200");
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Error districts request: " + t.getMessage());
                            finish();
                        }
                    });

                } else {
                    Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error cities request: Response code is not 200");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error cities request: " + t.getMessage());
                finish();
            }
        });
    }

    private void requestUserAddresses() {
        String authToken = prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "");
        int userID = prefs.getInt(Constants.PREF_SAVED_USER_ID, -1);
        if (!authToken.isEmpty() || userID > -1) {
            //send request
            apiAddress.getAllUserAddresses(userID, authToken).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.code() == 200 && response.body().has("0")) {
                        JsonArray jsonResponse = response.body().get("0").getAsJsonArray();
                        Log.d(TAG, "User addesses request is ok. Received " + jsonResponse.size() + " addresses");
                        for (int i = 0; i < jsonResponse.size(); i++) {
                            JsonObject jsonAddress = jsonResponse.get(i).getAsJsonObject();
//                                    "id":108,
//                                    "user_id":80,
//                                    "city_id":1,
//                                    "district_id":3,
//                                    "address":"Адрес",
//                                    "chosen":0,
//                                    "removed":null
                            if (jsonAddress.has("id") && jsonAddress.has("city_id") && jsonAddress.has("district_id") && jsonAddress.has("address")) {
                                //prepare data from response
                                String name = null; //TODO
                                String phone = null; //TODO
                                Integer city = 0; //TODO
                                Integer district = 0; //TODO
                                String street = null; //TODO
                                String house = null; //TODO
                                String building = null; //TODO
                                String apartment = null; //TODO
                                String porch = null; //TODO
                                String floor = null; //TODO
                                String intercom = null; //TODO
                                String needChange = null; //TODO
                                String notice = null; //TODO

                                UserInfo userInfoFromResponse = new UserInfo();
                                userInfoFromResponse.update(
                                        name, phone,
                                        city, district,
                                        street, house,
                                        building, apartment,
                                        porch, floor,
                                        intercom, needChange, notice
                                );
                                userInfoFromResponse.updateAddressID(jsonAddress.get("id").getAsInt());
                                //check is data from form equals date in DB and save if need
                                if (userInfoFromDB == null) {
                                    userInfoFromDB = userInfoFromResponse;
                                    userInfoRepo.update(userInfoFromDB);
                                } else {
                                    userInfoFromDB.update(
                                            name, phone,
                                            city, district,
                                            street, house,
                                            building, apartment,
                                            porch, floor,
                                            intercom, needChange, notice
                                    );
                                    userInfoRepo.update(userInfoFromDB);
                                }
                            }
                        }
                        //TODO
                    } else {
                        Toast.makeText(AddAddressActivity.this, "Server error, can't request address list.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error get addresses request: Response code is not 200");
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(AddAddressActivity.this, "Error, can't request address list.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error get addresses request: Response code is not 200");
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Can't request address list, please authorize first", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Can't request address list for this user. No auth info");
            finish();
        }
    }

    private void requestCreateNewUserAddress() {
        //TODO
    }

    private void requestDeliveryCost() {
        //TODO
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void addressBookIsPrepared() {
        //set initial data from userInfo
        userInfoFromDB = userInfoRepo.get();
        if (userInfoFromDB != null) {
            if (userInfoFromDB.getName() != null) {
                metAddressName.setText(userInfoFromDB.getName());
            }
            if (userInfoFromDB.getPhone() != null) {
                metAddressPhone.setText(userInfoFromDB.getPhone());
            }
            Integer cityID = userInfoFromDB.getCity();
            if (cityID != null) {
                City cityFormDB = cityRepository.getCityByID(cityID);
                if (cityFormDB != null) {
                    String name;
                    if (isRuLocale) {
                        name = cityFormDB.getCityRuName();
                    } else {
                        name = cityFormDB.getCityEnName();
                    }
                    if (name != null) {
                        mtvAddressCity.setText(name);
                    }
                }
            }
            Integer districtID = userInfoFromDB.getDistrict();
            if (districtID != null) {
                District districtFromDB = districtRepository.getDistrictsByID(districtID);
                if (districtFromDB != null) {
                    String name;
                    if (isRuLocale) {
                        name = districtFromDB.getDistrictRuName();
                    } else {
                        name = districtFromDB.getDistrictEnName();
                    }
                    if (name != null) {
                        mtvAddressDistrict.setText(name);
                    }
                }
            }
            if (userInfoFromDB.getStreet() != null) {
                metAddressStreet.setText(userInfoFromDB.getStreet());
            }
            if (userInfoFromDB.getHouse() != null) {
                metAddressHouse.setText(userInfoFromDB.getHouse());
            }
            if (userInfoFromDB.getCorpBuilding() != null) {
                metAddressBuilding.setText(userInfoFromDB.getCorpBuilding());
            }
            if (userInfoFromDB.getApartmentOffice() != null) {
                metAddressApartment.setText(userInfoFromDB.getApartmentOffice());
            }
            if (userInfoFromDB.getEnterNum() != null) {
                metAddressPorch.setText(userInfoFromDB.getEnterNum());
            }
            if (userInfoFromDB.getFloor() != null) {
                metAddressFloor.setText(userInfoFromDB.getFloor());
            }
            if (userInfoFromDB.getDomophoneNum() != null) {
                metAddressIntercom.setText(userInfoFromDB.getDomophoneNum());
            }
            if (userInfoFromDB.getNeedChange() != null) {
                metAddressNeedChange.setText(userInfoFromDB.getNeedChange());
            }
            if (userInfoFromDB.getComment() != null) {
                metAddressNotice.setText(userInfoFromDB.getComment());
            }
        }
    }

    @Override
    public void chooseCity(City city, String localizedName) {
        chosenCity = city;
        mtvAddressCity.setText(localizedName);
        mtvAddressDistrict.setClickable(true);
        mrvAddressChooseCity.setVisibility(View.GONE);
        checkRequiredFields();
    }

    @Override
    public void chooseDistrict(District district, String localizedName) {
        chosenDistrict = district;
        mrvAddressChooseDistrict.setVisibility(View.GONE);
        mtvAddressDistrict.setText(localizedName);
        metAddressStreet.setEnabled(true);
        metAddressHouse.setEnabled(true);
        metAddressBuilding.setEnabled(true);
        metAddressApartment.setEnabled(true);
        metAddressPorch.setEnabled(true);
        metAddressFloor.setEnabled(true);
        metAddressIntercom.setEnabled(true);
        checkRequiredFields();
    }

    private void checkRequiredFields() {
        if (metAddressName.length() > 0
                && metAddressPhone.length() > 0
                && mtvAddressDistrict.getText().length() > 0
                && mtvAddressDistrict.length() > 0
                && metAddressHouse.length() > 0) {
            bAddressToOrder.setVisibility(View.VISIBLE);
        } else {
            bAddressToOrder.setVisibility(View.GONE);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    unSafeGetGeoLocation();
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
                unSafeGetGeoLocation();
            }
        } else {
            unSafeGetGeoLocation();
        }
    }

    private void unSafeGetGeoLocation() {
        if (!isLocatePlaceNow) {
            isLocatePlaceNow = true;
            AppLocationService appLocationService = new AppLocationService(AddAddressActivity.this);
            Location location = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LocationAddress.getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), this);
            } else {
                showSettingsAlert();
            }
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

    @Override
    public void receiveAddress(String address, String city, String street) {
        isLocatePlaceNow = false;
        Log.d(TAG, "Get address bundle. City - " + city + ", street - " + street);

        if (address.isEmpty()) {
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
    }
}
