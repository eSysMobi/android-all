package mobi.esys.dastarhan;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.DatabaseHelper;

public class CurrentRestaurantActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagCurrRestActivity";
    private TextView mtvCurrRestName;
    private ImageView mivCurrRestRating;
    private ImageView mivCurrRestImage;
    private ImageView mivCurrRestVegan;
    private FrameLayout mflCurrRestInfo;
    TextView tvCurrRestRecomendationCount;

    private SQLiteDatabase db;

    private String ru_name;
    private String en_name;
    private int restID;
    private int city_id;
    private int district_id;
    private int min_order;
    private int del_cost;
    private String schedule;
    private String time1;
    private String time2;
    private String del_time;
    private String payment_methods;
    private String contact_name_ru;
    private String contact_name_en;
    private String phone;
    private String mobile;
    private String email1;
    private String email2;
    private int total_rating;
    private int total_votes;
    private String contact_email;
    private String order_phone;
    private String additional_ru;
    private String additional_en;
    private String picture;
    private int vegetarian;
    private int featured;
    private int approved;
    private String cuisines;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_restaurant_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mtvCurrRestName = (TextView) findViewById(R.id.tvCurrRestName);
        mivCurrRestRating = (ImageView) findViewById(R.id.ivCurrRestRating);
        mivCurrRestImage = (ImageView) findViewById(R.id.ivCurrRestImage);
        mivCurrRestVegan = (ImageView) findViewById(R.id.ivCurrRestVegan);
        mflCurrRestInfo = (FrameLayout) findViewById(R.id.flCurrRestInfo);
        tvCurrRestRecomendationCount = (TextView) findViewById(R.id.tvCurrRestRecomendationCount);

        restID = getIntent().getIntExtra("restID", -42);
        Log.d(TAG, "Start getting info from DB about restaurant with id " + restID);

        if (restID != -42) {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                db = dbHelper.getReadableDatabase();
                String selectQuery;
                String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();

                selectQuery = "SELECT * FROM " + Constants.DB_TABLE_RESTAURANTS + " WHERE server_id = " + restID;
                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();

                    ru_name = cursor.getString(cursor.getColumnIndexOrThrow("ru_name"));
                    en_name = cursor.getString(cursor.getColumnIndexOrThrow("en_name"));
                    //restID;
                    city_id = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                    district_id = cursor.getInt(cursor.getColumnIndexOrThrow("district_id"));
                    min_order = cursor.getInt(cursor.getColumnIndexOrThrow("min_order"));
                    del_cost = cursor.getInt(cursor.getColumnIndexOrThrow("del_cost"));
                    schedule = cursor.getString(cursor.getColumnIndexOrThrow("schedule"));
                    time1 = cursor.getString(cursor.getColumnIndexOrThrow("time1"));
                    time2 = cursor.getString(cursor.getColumnIndexOrThrow("time2"));
                    del_time = cursor.getString(cursor.getColumnIndexOrThrow("del_time"));
                    payment_methods = cursor.getString(cursor.getColumnIndexOrThrow("payment_methods"));
                    contact_name_ru = cursor.getString(cursor.getColumnIndexOrThrow("contact_name_ru"));
                    contact_name_en = cursor.getString(cursor.getColumnIndexOrThrow("contact_name_en"));
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                    email1 = cursor.getString(cursor.getColumnIndexOrThrow("email1"));
                    email2 = cursor.getString(cursor.getColumnIndexOrThrow("email2"));
                    total_rating = cursor.getInt(cursor.getColumnIndexOrThrow("total_rating"));
                    total_votes = cursor.getInt(cursor.getColumnIndexOrThrow("total_votes"));
                    contact_email = cursor.getString(cursor.getColumnIndexOrThrow("contact_email"));
                    order_phone = cursor.getString(cursor.getColumnIndexOrThrow("order_phone"));
                    additional_ru = cursor.getString(cursor.getColumnIndexOrThrow("additional_ru"));
                    additional_en = cursor.getString(cursor.getColumnIndexOrThrow("additional_en"));
                    picture = cursor.getString(cursor.getColumnIndexOrThrow("picture"));
                    vegetarian = cursor.getInt(cursor.getColumnIndexOrThrow("vegetarian"));
                    featured = cursor.getInt(cursor.getColumnIndexOrThrow("featured"));
                    approved = cursor.getInt(cursor.getColumnIndexOrThrow("approved"));
                    cuisines = cursor.getString(cursor.getColumnIndexOrThrow("cuisines"));

                    String name = "";
                    if (locale.equals("ru")) {
                        name = ru_name;
                    } else {
                        name = en_name;
                    }
                    mtvCurrRestName.setText(name);

                    tvCurrRestRecomendationCount.setText(String.valueOf(total_votes));

                    setRatingInUI(total_rating);

                    if (vegetarian == 0) {
                        mivCurrRestVegan.setVisibility(View.GONE);
                    }

                    //ste "open hours"
                    TextView mtvCurrRestTime = (TextView) findViewById(R.id.tvCurrRestTime);
                    String openHours = "";
                    if (time1.length() > 5) {
                        openHours = time1.substring(0, 5) + " - ";
                    } else {
                        openHours = time1 + " - ";
                    }
                    if (time2.length() > 5) {
                        openHours = openHours + time2.substring(0, 5);
                    } else {
                        openHours = openHours + time2;
                    }
                    mtvCurrRestTime.setText(openHours);

                    //set schedule
                    TextView mtvCurrRestSchedule = (TextView) findViewById(R.id.tvCurrRestSchedule);
                    String setSchedule = "";
                    if (schedule.contains("1")) setSchedule = setSchedule + "Mon, ";
                    if (schedule.contains("2")) setSchedule = setSchedule + "Tue, ";
                    if (schedule.contains("3")) setSchedule = setSchedule + "Wed, ";
                    if (schedule.contains("4")) setSchedule = setSchedule + "Thu, ";
                    if (schedule.contains("5")) setSchedule = setSchedule + "Fri, ";
                    if (schedule.contains("6")) setSchedule = setSchedule + "Sat, ";
                    if (schedule.contains("7")) setSchedule = setSchedule + "Sun, ";
                    if (setSchedule.endsWith(", "))
                        setSchedule = setSchedule.substring(0, setSchedule.length() - 2);
                    mtvCurrRestSchedule.setText(setSchedule);

                    //set phone
                    TextView mtvCurrRestTel = (TextView) findViewById(R.id.tvCurrRestTel);
                    if (!phone.isEmpty() && !mobile.isEmpty()) {
                        String setPhone = phone;
                        setPhone = setPhone + ", " + mobile;
                        if (setPhone.endsWith(", "))
                            setPhone = setPhone.substring(0, setPhone.length() - 2);
                        mtvCurrRestTel.setText(setPhone);
                    } else {
                        mtvCurrRestTel.setVisibility(View.GONE);
                        TextView mtvCurrRestInfoTel = (TextView) findViewById(R.id.tvCurrRestInfoTel);
                        mtvCurrRestInfoTel.setVisibility(View.GONE);
                    }


                    TextView mtvCurrRestTelOrder = (TextView) findViewById(R.id.tvCurrRestTelOrder);
                    if (!order_phone.isEmpty()) {
                        mtvCurrRestTelOrder.setText(order_phone);
                    } else {
                        mtvCurrRestTelOrder.setVisibility(View.GONE);
                        TextView mtvCurrRestInfoTelOrder = (TextView) findViewById(R.id.tvCurrRestInfoTelOrder);
                        mtvCurrRestInfoTelOrder.setVisibility(View.GONE);
                    }

                    TextView mtvCurrRestEmail = (TextView) findViewById(R.id.tvCurrRestEmail);
                    if (!email1.isEmpty() && !email2.isEmpty()) {
                        mtvCurrRestEmail.setText(email1 + " " + email2);
                    } else {
                        mtvCurrRestEmail.setVisibility(View.GONE);
                        TextView mtvCurrRestInfoEmail = (TextView) findViewById(R.id.tvCurrRestInfoEmail);
                        mtvCurrRestInfoEmail.setVisibility(View.GONE);
                    }

                    TextView mtvCurrRestPayment = (TextView) findViewById(R.id.tvCurrRestPayment);
                    mtvCurrRestPayment.setText(payment_methods);

                    TextView mtvCurrRestDesrc = (TextView) findViewById(R.id.tvCurrRestDesrc);
                    String setDescr = "";
                    if (locale.equals("ru")) {
                        setDescr = additional_ru;
                    } else {
                        setDescr = additional_en;
                    }
                    mtvCurrRestDesrc.setText(setDescr);

                    cursor.close();
                } else {
                    hideNotFoundData();
                    cursor.close();
                }
                db.close();
            } catch (Exception e) {
                hideNotFoundData();
                Log.e(TAG, "Error with DB: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private void hideNotFoundData() {
        TextView mtvCurrRestNotFound = (TextView) findViewById(R.id.tvCurrRestNotFound);
        if (mtvCurrRestNotFound != null) {
            mtvCurrRestNotFound.setVisibility(View.VISIBLE);
        }
        mtvCurrRestName.setVisibility(View.GONE);
        mivCurrRestRating.setVisibility(View.GONE);
        mivCurrRestImage.setVisibility(View.GONE);
        mivCurrRestVegan.setVisibility(View.GONE);
        mflCurrRestInfo.setVisibility(View.GONE);
    }

    private void setRatingInUI(int rating) {
        switch (rating) {
            case 0:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_0));
                break;
            case 1:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_1));
                break;
            case 2:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_2));
                break;
            case 3:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_3));
                break;
            case 4:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_4));
                break;
            case 5:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_5));
                break;
            case 6:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_6));
                break;
            case 7:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_7));
                break;
            case 8:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_8));
                break;
            case 9:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_9));
                break;
            case 10:
                mivCurrRestRating.setImageDrawable(getResources().getDrawable(R.drawable.rating_10));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_restaurant_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_action_menu) {
            Intent intent = new Intent(CurrentRestaurantActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            Intent intent = new Intent(CurrentRestaurantActivity.this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_bucket) {
            Intent intent = new Intent(CurrentRestaurantActivity.this,BasketActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(CurrentRestaurantActivity.this,PromoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_settings) {
            Intent intent = new Intent(CurrentRestaurantActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_restaurant_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_btn, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getApplicationContext(), "Search pressed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
