package mobi.esys.dastarhan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import mobi.esys.dastarhan.database.City;
import mobi.esys.dastarhan.database.CityRepository;
import mobi.esys.dastarhan.database.District;
import mobi.esys.dastarhan.database.DistrictRepository;
import mobi.esys.dastarhan.database.UserInfo;
import mobi.esys.dastarhan.database.UserInfoRepository;
import mobi.esys.dastarhan.net.APIAddress;
import mobi.esys.dastarhan.net.APIAuthorize;
import mobi.esys.dastarhan.net.APIDelivery;
import mobi.esys.dastarhan.utils.AppLocationService;
import mobi.esys.dastarhan.utils.CityOrDistrictChooser;
import mobi.esys.dastarhan.utils.LocationAddress;
import mobi.esys.dastarhan.utils.RVChoseCityAdapter;
import mobi.esys.dastarhan.utils.RVChoseDistrictAdapter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddAddressActivity extends AppCompatActivity implements CityOrDistrictChooser, LocationAddress.Callback {

    private final static String TAG = "dtagAddAddress";
    private final static int PERMISSION_REQUEST_CODE = 334;
    public final static String DELIVERY_REST_IDS = "delivery_rest_ids";
    final static String DELIVERY_MIN_ORDER = "delivery_min_order";
    final static String DELIVERY_TIME = "delivery_time";
    final static String DELIVERY_COST = "delivery_cost";

    @Inject
    UserInfoRepository userInfoRepo;
    @Inject
    CityRepository cityRepository;
    @Inject
    DistrictRepository districtRepository;
    @Inject
    Retrofit retrofit;
    private APIAddress apiAddress;
    private APIAuthorize apiAuth;
    private APIDelivery apiDelivery;

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

    private boolean isRuLocale;
    private boolean isAlreadyTryAuth = false;

    //delivery
    private HashMap<Integer, Double> restIDsAndCurrOrerSum;
    private HashMap<Integer, Double> deliveryMinOrderSums = new HashMap<>();
    private String deliveriesMaxTime = "";
    private double deliveryAllCost = 0;
    private volatile int completedDeliveryRequests = 0;
    private boolean failDeliveryResponseOccurs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((DastarhanApp) getApplication()).appComponent().inject(this);

        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

        restIDsAndCurrOrerSum = (HashMap<Integer, Double>) getIntent().getSerializableExtra(DELIVERY_REST_IDS);
        if (restIDsAndCurrOrerSum == null || restIDsAndCurrOrerSum.isEmpty()) {
            failResult();
        } else {

            final String locale = getResources().getConfiguration().locale.getLanguage();
            isRuLocale = locale.equals("ru");

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
            apiAuth = retrofit.create(APIAuthorize.class);
            apiDelivery = retrofit.create(APIDelivery.class);

            final TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().isEmpty()) {
                        bAddressToOrder.setVisibility(View.GONE);
                    } else {
                        checkRequiredFields();
                    }
                }
            };

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
            metAddressName.addTextChangedListener(textWatcher);

            metAddressPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return false;
                }
            });
            metAddressPhone.addTextChangedListener(textWatcher);

            mtvAddressCity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mrvAddressChooseCity.getVisibility() == View.VISIBLE) {
                        mrvAddressChooseCity.setVisibility(View.GONE);
                    } else {
                        if (mrvAddressChooseCity.getAdapter() == null) {
                            String locale = getResources().getConfiguration().locale.getLanguage();
                            RVChoseCityAdapter adapter = new RVChoseCityAdapter(AddAddressActivity.this, cityRepository.getCities(), locale);
                            mrvAddressChooseCity.setAdapter(adapter);
                        }
                        mrvAddressChooseCity.setVisibility(View.VISIBLE);
                    }
                }
            });

            mtvAddressDistrict.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mrvAddressChooseDistrict.getVisibility() == View.VISIBLE) {
                        mrvAddressChooseDistrict.setVisibility(View.GONE);
                    } else {
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
            metAddressHouse.addTextChangedListener(textWatcher);

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
            metAddressApartment.addTextChangedListener(textWatcher);

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

            mibAddressAutoLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getGeoLocation();
                }
            });

            //save address
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
                    //check is we have all requirements fields
                    if (!name.isEmpty()) {
                        if (!phone.isEmpty()) {
                            if (!city.isEmpty()) {
                                if (!district.isEmpty()) {
                                    if (!street.isEmpty()) {
                                        if (!house.isEmpty()) {
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
                                            //check is data from FORM equals data in DB and save if need
                                            if (userInfoFromDB == null) {
                                                userInfoFromDB = userInfoFromForm;
                                                userInfoRepo.update(userInfoFromDB);
                                            } else {
                                                boolean needUpdateInRepo = false;
                                                if (!userInfoFromForm.equalsByAddress(userInfoFromDB)) {
                                                    userInfoFromDB.updateAddress(
                                                            cityRepository.getCityByName(city).getCityID(), districtRepository.getDistrictByName(district).getDistrictID(),
                                                            street, house,
                                                            building, apartment,
                                                            porch, floor,
                                                            intercom
                                                    );
                                                    needUpdateInRepo = true;
                                                }
                                                if (!userInfoFromForm.equalsByUserInfo(userInfoFromDB)) {
                                                    userInfoFromDB.updateUserInfo(
                                                            name, phone,
                                                            needChange, notice
                                                    );
                                                    needUpdateInRepo = true;
                                                }
                                                if (needUpdateInRepo) {
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
                                        Toast.makeText(AddAddressActivity.this, R.string.need_choose_street, Toast.LENGTH_SHORT).show();
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
                                    failResult();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Error districts request: " + t.getMessage());
                                failResult();
                            }
                        });

                    } else {
                        Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error cities request: Response code is not 200");
                        failResult();
                    }
                }

                @Override
                public void onFailure(Call<JsonArray> call, Throwable t) {
                    Toast.makeText(AddAddressActivity.this, R.string.can_not_load_adress_book, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error cities request: " + t.getMessage());
                    failResult();
                }
            });
        }
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
                        List<UserInfo> serverAddresses = new ArrayList<>();
                        for (int i = 0; i < jsonResponse.size(); i++) {
                            try {
                                JsonObject jsonAddress = jsonResponse.get(i).getAsJsonObject();
                                if (jsonAddress.has("id")
                                        && jsonAddress.get("id") != null
                                        && jsonAddress.has("city_id")
                                        && jsonAddress.get("city_id") != null
                                        && jsonAddress.has("district_id")
                                        && jsonAddress.get("district_id") != null
                                        && jsonAddress.has("address")
                                        && jsonAddress.get("address") != null) {
                                    String addressLine = jsonAddress.get("address").getAsString();
                                    if (addressLine != null && !addressLine.isEmpty()) {
                                        final String[] splittedAddress = addressLine.split(", ");
                                        if (splittedAddress.length == 7) {
                                            for (int j = 0; j < splittedAddress.length; j++) {
                                                final String substring = splittedAddress[j].substring(splittedAddress[j].indexOf(": ") + 2);
                                                if (substring.isEmpty() || "-".equals(substring)) {
                                                    splittedAddress[j] = null;
                                                } else {
                                                    splittedAddress[j] = substring;
                                                }
                                            }

                                            //prepare data from response
                                            Integer addressIdInServer = jsonAddress.get("id").getAsInt();
                                            Integer city = jsonAddress.get("city_id").getAsInt();
                                            Integer district = jsonAddress.get("district_id").getAsInt();
                                            String street = splittedAddress[0];
                                            String house = splittedAddress[1];
                                            String building = splittedAddress[2];
                                            String apartment = splittedAddress[3];
                                            String porch = splittedAddress[4];
                                            String floor = splittedAddress[5];
                                            String intercom = splittedAddress[6];

                                            if (street != null && !street.isEmpty()
                                                    && house != null && !house.isEmpty()
                                                    && apartment != null && !apartment.isEmpty()) {

                                                UserInfo userInfoFromResponse = new UserInfo();
                                                userInfoFromResponse.updateAddress(
                                                        city, district,
                                                        street, house,
                                                        building, apartment,
                                                        porch, floor,
                                                        intercom
                                                );
                                                userInfoFromResponse.updateAddressID(addressIdInServer);

                                                serverAddresses.add(userInfoFromResponse);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Parsing error");
                            }
                        }

                        boolean isNeedSendNewAddressToServer = true;
                        for (UserInfo address : serverAddresses) {
                            if (address.equalsByAddress(userInfoFromDB)) {
                                userInfoFromDB.updateAddressID(address.getServerAddressID());
                                userInfoRepo.update(userInfoFromDB);
                                isNeedSendNewAddressToServer = false;
                                break;
                            }
                        }

                        if (isNeedSendNewAddressToServer) {
                            requestCreateNewUserAddress();
                        } else {
                            requestDeliveryCost();
                        }
                    } else if (response.code() == 400 && response.errorBody() != null) {
                        String error = "";
                        try {
                            error = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (error.contains("Invalid key")) {
                            if (isAlreadyTryAuth) {
                                Toast.makeText(AddAddressActivity.this, "Can't authorize on server.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Can't authorize on server.");
                                failResult();
                            } else {
                                final String userEmail = prefs.getString(Constants.PREF_SAVED_LOGIN, "");
                                final String userPass = prefs.getString(Constants.PREF_SAVED_PASS, "");
                                if (!userEmail.isEmpty() && !userPass.isEmpty()) {
                                    apiAuth.auth(userEmail, userPass).enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                            isAlreadyTryAuth = true;
                                            if (response != null && response.code() == 200) {
                                                // update token
                                                JsonObject authResponse = response.body();
                                                if (authResponse != null
                                                        && authResponse.has("apikey")
                                                        && authResponse.get("apikey").getAsString() != null
                                                        && !authResponse.get("apikey").getAsString().isEmpty()) {
                                                    final String token = authResponse.get("apikey").getAsString();
                                                    SharedPreferences.Editor editor = prefs.edit();
                                                    editor.putString(Constants.PREF_SAVED_AUTH_TOKEN, token);
                                                    editor.apply();
                                                    requestUserAddresses();
                                                } else {
                                                    Toast.makeText(AddAddressActivity.this, "Bad server auth response.", Toast.LENGTH_LONG).show();
                                                    Log.d(TAG, "Bad server auth response.");
                                                    failResult();
                                                }
                                            } else {
                                                Toast.makeText(AddAddressActivity.this, "Unknown server auth error.", Toast.LENGTH_LONG).show();
                                                Log.d(TAG, "Unknown server error when getting auth.");
                                                failResult();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<JsonObject> call, Throwable t) {
                                            isAlreadyTryAuth = true;
                                            Toast.makeText(AddAddressActivity.this, "Unknown server auth error.", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "Unknown server error when getting auth: " + t.getMessage());
                                            failResult();
                                        }
                                    });
                                } else {
                                    Toast.makeText(AddAddressActivity.this, "Can't request auth info, please authorize first", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "Can't request new auth token. No auth email and password");
                                    failResult();
                                }
                            }

                        } else {
                            Toast.makeText(AddAddressActivity.this, "Unknown server error: code 400.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Unknown server error when getting addresses request: code 400.");
                            failResult();
                        }
                    } else {
                        Toast.makeText(AddAddressActivity.this, "Server error, can't request address list.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error get addresses request: Response code is not 200");
                        failResult();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(AddAddressActivity.this, "Error, can't request address list.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error get addresses request: Response code is not 200");
                    failResult();
                }
            });
        } else {
            Toast.makeText(this, "Can't request address list, please authorize first", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Can't request address list for this user. No auth info");
            failResult();
        }
    }

    private void requestCreateNewUserAddress() {
        final int sendingUserID = prefs.getInt(Constants.PREF_SAVED_USER_ID, -1);
        final String sendingApiToken = prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "");
        final String sendingAddress = userInfoFromDB.getFullAddress();
        final Integer sendingCityID = userInfoFromDB.getCity();
        final Integer sendingDistrictID = userInfoFromDB.getDistrict();
        if (sendingUserID == -1
                || sendingApiToken.isEmpty()
                || sendingAddress.isEmpty()
                || sendingCityID == null
                || sendingDistrictID == null) {
            Toast.makeText(this, "Can't send new address to server, please reauthorize", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Can't send new address to server. Not enough data");
            failResult();
        } else {
            apiAddress
                    .getAddNewUserAddress(sendingUserID, sendingApiToken, sendingAddress, sendingCityID, sendingDistrictID)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response != null && response.code() == 200) {
                                //parse response and save address id in server
                                //example {"success":"Ok","data":{"address_id":110}}
                                int addressIDonServer = -1;
                                //parse
                                final JsonObject jsonResponse = response.body();
                                try {
                                    if (jsonResponse != null && jsonResponse.has("data")) {
                                        final JsonElement jsonDataElement = jsonResponse.get("data");
                                        if (jsonDataElement != null && jsonDataElement.isJsonObject()) {
                                            final JsonObject jsonData = jsonDataElement.getAsJsonObject();
                                            if (jsonData != null && jsonData.has("address_id")) {
                                                final JsonElement address_id = jsonData.get("address_id");
                                                if (address_id != null) {
                                                    addressIDonServer = address_id.getAsInt();
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AddAddressActivity.this, "Bad server response when sending new address", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "Bad server response when sending new address");
                                    failResult();
                                }

                                if (addressIDonServer > -1) {
                                    //save id
                                    userInfoFromDB.updateAddressID(addressIDonServer);
                                    userInfoRepo.update(userInfoFromDB);
                                    requestDeliveryCost();
                                } else {
                                    Toast.makeText(AddAddressActivity.this, "Unknown server response when sending new address", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "Unknown server response when sending new address");
                                    failResult();
                                }
                            } else {
                                Toast.makeText(AddAddressActivity.this, "Unknown server error when sending new address", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Unknown server error when sending new address");
                                failResult();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(AddAddressActivity.this, "Can't send new address to server.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Can't send new address to server: " + t.getMessage());
                            failResult();
                        }
                    });
        }
    }

    private void requestDeliveryCost() {
        final Integer addressID = userInfoRepo.get().getServerAddressID();
        final Set<Integer> restIDs = restIDsAndCurrOrerSum.keySet();
        if (addressID != null) {
            for (int restID : restIDs) {
                apiDelivery.getDeliveryInfoFromRestaurant(addressID, restID)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    String stringResponse = response.body().string();
                                    if (stringResponse == null || stringResponse.isEmpty()) {
                                        throw new IOException();
                                    } else {
                                        stringResponse = stringResponse.substring(stringResponse.indexOf("[") + 1, stringResponse.indexOf("]"));
                                        stringResponse = stringResponse.replace("\"", "");
                                        String[] splittedResponse = stringResponse.split(",");
                                        if (splittedResponse.length == 4) {
                                            completedDeliveryRequests++;
                                            Integer id = Integer.valueOf(splittedResponse[3]);
                                            okResult(
                                                    //min cost of order for delivery
                                                    Double.valueOf(splittedResponse[0]).doubleValue(),
                                                    //time of delivery
                                                    Double.valueOf(splittedResponse[1]).doubleValue(),
                                                    //cost of delivery
                                                    splittedResponse[2],
                                                    //id of restaurants
                                                    id.intValue());
                                        } else {
                                            throw new IOException();
                                        }
                                    }
                                } catch (NumberFormatException | IOException e) {
                                    failDeliveryResponseOccurs = true;
                                    completedDeliveryRequests++;
                                    result();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                failDeliveryResponseOccurs = true;
                                completedDeliveryRequests++;
                                result();
                            }
                        });
            }
        } else {
            Toast.makeText(AddAddressActivity.this, "Can't receive deletery info, no address ID info. Please choose adress again.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Can't receive deletery info, no address ID info.");
            failResult();
        }
    }

    private void okResult(double restsMinOrder, double deliveryCost, String deliveryTime, int restID) {
        deliveryMinOrderSums.put(restID, restsMinOrder);
        deliveryAllCost = deliveryAllCost + deliveryCost;
        if (deliveryTime.compareTo(deliveriesMaxTime) > 0) {
            deliveriesMaxTime = deliveryTime;
        }
        result();
    }

    private void result() {
        if (completedDeliveryRequests == restIDsAndCurrOrerSum.size()) {
            if (failDeliveryResponseOccurs) {
                Toast.makeText(AddAddressActivity.this, "Can't receive deletery info, bad server response", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Can't receive deletery info, bad server response");
                failResult();
            } else {
                Intent intent = new Intent();
                //HashMap as serializable
                intent.putExtra(DELIVERY_MIN_ORDER, deliveryMinOrderSums);
                intent.putExtra(DELIVERY_TIME, deliveriesMaxTime);
                intent.putExtra(DELIVERY_COST, deliveryAllCost);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private void failResult() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
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
        checkRequiredFields();
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
        checkRequiredFields();
    }

    private void checkRequiredFields() {
        if (metAddressName.length() > 0
                && metAddressPhone.getText().length() > 0
                && mtvAddressCity.getText().length() > 0
                && mtvAddressDistrict.getText().length() > 0
                && metAddressHouse.getText().length() > 0
                && metAddressApartment.getText().length() > 0) {
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
