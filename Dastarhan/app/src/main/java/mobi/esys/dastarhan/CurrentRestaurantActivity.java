package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;

public class CurrentRestaurantActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagCurrRestActivity";
    private TextView mtvCurrRestName;
    private ImageView mivCurrRestRating;
    private ImageView mivCurrRestImage;
    private ImageView mivCurrRestVegan;
    private FrameLayout mflCurrRestInfo;
    private TextView tvCurrRestRecomendationCount;

    private RealmComponent component;
    private Restaurant restaurant;


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

        component = ((DastarhanApp) getApplication()).realmComponent();

        mtvCurrRestName = (TextView) findViewById(R.id.tvCurrRestName);
        mivCurrRestRating = (ImageView) findViewById(R.id.ivCurrRestRating);
        mivCurrRestImage = (ImageView) findViewById(R.id.ivCurrRestImage);
        mivCurrRestVegan = (ImageView) findViewById(R.id.ivCurrRestVegan);
        mflCurrRestInfo = (FrameLayout) findViewById(R.id.flCurrRestInfo);
        tvCurrRestRecomendationCount = (TextView) findViewById(R.id.tvCurrRestRecomendationCount);

        int restID = getIntent().getIntExtra("restID", -42);
        Log.d(TAG, "Start getting info from DB about restaurant with id " + restID);

        restaurant = component.restaurantRepository().getById(restID);
        if (restaurant != null) {
            String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
            TextView mtvCurrRestDesrc = (TextView) findViewById(R.id.tvCurrRestDesrc);

            if (locale.equals("ru")) {
                mtvCurrRestName.setText(restaurant.getRu_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_ru());
            } else {
                mtvCurrRestName.setText(restaurant.getEn_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_en());
            }

            tvCurrRestRecomendationCount.setText(String.valueOf(restaurant.getTotal_votes()));

            setRatingInUI(restaurant.getTotal_rating());

            if (restaurant.getVegetarian() == 1) {
                mivCurrRestVegan.setVisibility(View.GONE);
            }

            //ste "open hours"
            TextView mtvCurrRestTime = (TextView) findViewById(R.id.tvCurrRestTime);
            String openHours = "";
            if (restaurant.getTime1().length() > 5) {
                openHours = restaurant.getTime1().substring(0, 5) + " - ";
            } else {
                openHours = restaurant.getTime1() + " - ";
            }
            if (restaurant.getTime2().length() > 5) {
                openHours = openHours + restaurant.getTime2().substring(0, 5);
            } else {
                openHours = openHours + restaurant.getTime2();
            }
            mtvCurrRestTime.setText(openHours);

            //set schedule
            TextView mtvCurrRestSchedule = (TextView) findViewById(R.id.tvCurrRestSchedule);
            String schedule = restaurant.getSchedule();
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
            if (!restaurant.getPhone().isEmpty() && !restaurant.getMobile().isEmpty()) {
                String setPhone = restaurant.getPhone();
                setPhone = setPhone + ", " + restaurant.getMobile();
                if (setPhone.endsWith(", "))
                    setPhone = setPhone.substring(0, setPhone.length() - 2);
                mtvCurrRestTel.setText(setPhone);
            } else {
                mtvCurrRestTel.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTel = (TextView) findViewById(R.id.tvCurrRestInfoTel);
                mtvCurrRestInfoTel.setVisibility(View.GONE);
            }


            TextView mtvCurrRestTelOrder = (TextView) findViewById(R.id.tvCurrRestTelOrder);
            if (!restaurant.getOrder_phone().isEmpty()) {
                mtvCurrRestTelOrder.setText(restaurant.getOrder_phone());
            } else {
                mtvCurrRestTelOrder.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTelOrder = (TextView) findViewById(R.id.tvCurrRestInfoTelOrder);
                mtvCurrRestInfoTelOrder.setVisibility(View.GONE);
            }

            TextView mtvCurrRestEmail = (TextView) findViewById(R.id.tvCurrRestEmail);
            if (!restaurant.getEmail1().isEmpty() && !restaurant.getEmail2().isEmpty()) {
                mtvCurrRestEmail.setText(restaurant.getEmail1() + " " + restaurant.getEmail2());
            } else {
                mtvCurrRestEmail.setVisibility(View.GONE);
                TextView mtvCurrRestInfoEmail = (TextView) findViewById(R.id.tvCurrRestInfoEmail);
                mtvCurrRestInfoEmail.setVisibility(View.GONE);
            }

            TextView mtvCurrRestPayment = (TextView) findViewById(R.id.tvCurrRestPayment);
            mtvCurrRestPayment.setText(restaurant.getPayment_methods());
        } else {
            hideNotFoundData();
            Log.e(TAG, "No restaurant with this ID ");
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
            Intent intent = new Intent(CurrentRestaurantActivity.this, BasketActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(CurrentRestaurantActivity.this, PromoActivity.class);
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
