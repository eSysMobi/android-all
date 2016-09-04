package mobi.esys.dastarhan;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mobi.esys.dastarhan.database.CommonOperation;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.Order;
import mobi.esys.dastarhan.database.RealmComponent;

public class CurrentFoodActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagCurrentFood";
    private RealmComponent component;
    private FoodRepository foodRepository;
    private Food food;

    private transient SharedPreferences prefs;

    private boolean canOrdered = true;
    private TextView mtvCurrFoodPrice;
    private TextView mtvCurrFoodName;
    private TextView mtvCurrFoodDescr;
    private Button mbCurrFoodAddShopping;
    private ImageView mivCurrFoodFavorite;

    private ImageView mivCurrFoodVegan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_food_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        component = ((DastarhanApp) getApplication()).realmComponent();
        foodRepository = component.foodRepository();

        int currentFoodID = getIntent().getIntExtra("currentFoodID", -42);

        Log.d(TAG, "Choose FOOD ID from intent = " + currentFoodID);
        food = foodRepository.getById(currentFoodID);

        mtvCurrFoodPrice = (TextView) findViewById(R.id.tvCurrFoodPrice);
        mtvCurrFoodName = (TextView) findViewById(R.id.tvCurrFoodName);
        mtvCurrFoodDescr = (TextView) findViewById(R.id.tvCurrFoodDescr);
        mivCurrFoodFavorite = (ImageView) findViewById(R.id.ivCurrFoodFavorite);
        mivCurrFoodVegan = (ImageView) findViewById(R.id.ivCurrFoodVegan);
        mbCurrFoodAddShopping = (Button) findViewById(R.id.bCurrFoodAddShopping);

        //click favorite
        mivCurrFoodFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (food.isFavorite()) {
                    food.setFavorite(false);
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                } else {
                    food.setFavorite(true);
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                }

                //update fav in DB
                foodRepository.updateFavorites(food.getServer_id(), food.isFavorite());
            }
        });

        //click order
        mbCurrFoodAddShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canOrdered) {
                    canOrdered = false;

                    CommonOperation.createOrder(component, food);

                    mbCurrFoodAddShopping.setText(R.string.cant_order);
                    mbCurrFoodAddShopping.setBackground(getResources().getDrawable(R.drawable.button_to_basket_selector));
                    Toast.makeText(getApplicationContext(), "Added to shopping list", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    protected void onResume() {
        if (food != null) {
            //set data from DB to view

            String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();

            List<Order> orders = component.cartRepository().getCurrentCartOrders();
            for (Order order : orders) {
                if (order.getId_food() == food.getServer_id()) {
                    canOrdered = false;
                }
            }

            if (canOrdered) {
                mbCurrFoodAddShopping.setText(R.string.to_order);
            } else {
                mbCurrFoodAddShopping.setText(R.string.cant_order);
            }

            if (locale.equals("ru")) {
                mtvCurrFoodName.setText(food.getRu_name());
            } else {
                mtvCurrFoodName.setText(food.getEn_name());
            }

            String priceString = String.valueOf(food.getPrice());
            priceString += " " + Constants.CURRENCY_VERY_SHORT;
            mtvCurrFoodPrice.setText(priceString);

            if (locale.equals("ru")) {
                mtvCurrFoodDescr.setText(food.getRu_descr());
            } else {
                mtvCurrFoodDescr.setText(food.getEn_descr());
            }

            //favorite
            if (food.isFavorite()) {
                mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
            }

            if (food.isVegetarian()) {
                mivCurrFoodVegan.setVisibility(View.VISIBLE);
            } else {
                mivCurrFoodVegan.setVisibility(View.GONE);
            }
        } else {
            LinearLayout mllCurrentFood = (LinearLayout) findViewById(R.id.llCurrentFood);
            if (mllCurrentFood != null) {
                mllCurrentFood.setVisibility(View.GONE);
            }
            FrameLayout mflCurrentFood = (FrameLayout) findViewById(R.id.flCurrentFood);
            if (mflCurrentFood != null) {
                mflCurrentFood.setVisibility(View.GONE);
            }
            TextView mtvCurrFoodNotFound = (TextView) findViewById(R.id.tvCurrFoodNotFound);
            if (mtvCurrFoodNotFound != null) {
                mtvCurrFoodNotFound.setVisibility(View.VISIBLE);
            }
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_food_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_action_menu) {
            Intent intent = new Intent(CurrentFoodActivity.this, MainActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            Intent intent = new Intent(CurrentFoodActivity.this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_bucket) {
            Intent intent = new Intent(CurrentFoodActivity.this, BasketActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(CurrentFoodActivity.this, PromoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_settings) {
            Intent intent = new Intent(CurrentFoodActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_current_food_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
