package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.database.CuisineRepository;
import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.tasks.GetFood;
import mobi.esys.dastarhan.utils.FoodCheckElement;
import mobi.esys.dastarhan.utils.RVFoodAdapterMain;

public class FoodActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagFoodActivity";
    private DastarhanApp dastarhanApp;

    private RecyclerView mrvFood;
    private ProgressBar mpbFood;
    private Handler handlerFood;

    private int cuisineID;
    private Integer[] restaurantsID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        dastarhanApp = (DastarhanApp) getApplication();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_food_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mrvFood = (RecyclerView) findViewById(R.id.rvFood);
        mpbFood = (ProgressBar) findViewById(R.id.pbFood);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mrvFood.setLayoutManager(llm);

        //TODO
        handlerFood = new HandleFood();

        cuisineID = getIntent().getIntExtra("cuisineID", -42);
        restaurantsID = new Integer[1];
        restaurantsID[0] = getIntent().getIntExtra("restID", -50);
        Log.d(TAG, "Cuisine ID from intent = " + cuisineID);
        Log.d(TAG, "Restaurant ID from intent = " + restaurantsID[0]);
    }

    @Override
    protected void onResume() {
        if (cuisineID != -50) {
            //get food from cuisine
            getFoodFromRestaurants();
            //no need, restaurants get in splash-screen
            //GetRestaurants gr = new GetRestaurants(this, handlerFood);
            //gr.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            //if we have that restaurant
            if (restaurantsID[0] != -50) {
                boolean needDownFoodFormServer = false;
                long currTime = System.currentTimeMillis();

                //check last downloaded time
                if (currTime > dastarhanApp.getCheckedFood().get(0).getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                    for (FoodCheckElement foodCheckElement : dastarhanApp.getCheckedFood()) {
                        if (restaurantsID[0].equals(foodCheckElement.getRestID())
                                && currTime > foodCheckElement.getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                            needDownFoodFormServer = true;
                            break;
                        }
                    }
                }

                if (needDownFoodFormServer) {
                    //get food from restaurant
                    GetFood gf = new GetFood(dastarhanApp, handlerFood, restaurantsID);
                    gf.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                } else {
                    Log.d(TAG, "Not need download food, time is not expired");
                    updateFood();
                }
            }
        }
        super.onResume();
    }

    private class HandleFood extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_FOOD_SUCCESS) {  //all ok
                Log.d(TAG, "Food data received");
                //update down time
                if (cuisineID == -42) {
                    //update first element "All restaurants"
                    dastarhanApp.getCheckedFood().get(0).setTimeCheck(System.currentTimeMillis());
                } else {
                    for (int i = 0; i < dastarhanApp.getCheckedFood().size(); i++) {
                        for (Integer restaurantID : restaurantsID) {
                            if (restaurantID.equals(dastarhanApp.getCheckedFood().get(i).getRestID())) {
                                dastarhanApp.getCheckedFood().get(i).setTimeCheck(System.currentTimeMillis());
                                break;
                            }
                        }
                    }
                }
                //update RecycleVew
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_FAIL) {  //not ok
                Log.d(TAG, "Food data NOT receive");
                //update RecycleVew
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR || msg.what == Constants.CALLBACK_GET_RESTAURANTS_SHOW_PROGRESS_BAR) {  //show progress bar
                mrvFood.setVisibility(View.GONE);
                mpbFood.setVisibility(View.VISIBLE);
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_SUCCESS) {  //all ok
                Log.d(TAG, "Restaurants data received");
                //update RecycleVew
                getFoodFromRestaurants();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_FAIL) {  //not ok
                Log.d(TAG, "Restaurants data NOT receive");
                //update RecycleVew
                getFoodFromRestaurants();
            }
            super.handleMessage(msg);
        }
    }

    private void getFoodFromRestaurants() {
        //get restaurants ID from cuisines
        CuisineRepository cuisineRepo = dastarhanApp.component.cuisineRepository();
        RestaurantRepository restaurantRepo = dastarhanApp.component.restaurantRepository();
        List<Restaurant> restaurants;
        if (cuisineID == -42) {
            restaurants = restaurantRepo.getAll();
        } else {
            restaurants = restaurantRepo.getByCuisine(cuisineID);
        }
        if (restaurants.size() > 0) {
            List<Integer> restIDs = new ArrayList<>();
            for (Restaurant restaurant : restaurants) {
                restIDs.add(restaurant.getServer_id());
            }
            restaurantsID = restIDs.toArray(new Integer[restIDs.size()]);
        }

        //check last downloaded time
        boolean needDownFoodFromServer = false;
        long currTime = System.currentTimeMillis();

        if (currTime > dastarhanApp.getCheckedFood().get(0).getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
            //checking first element "All restaurants"
            for (FoodCheckElement foodCheckElement : dastarhanApp.getCheckedFood()) {
                for (Integer restaurantID : restaurantsID) {
                    if (restaurantID.equals(foodCheckElement.getRestID())
                            && currTime > foodCheckElement.getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                        needDownFoodFromServer = true;
                        break;
                    }
                }
            }
        }

        if (needDownFoodFromServer) {
            if (restaurantsID.length > 0) {
                //get food from restaurant
                GetFood gf = new GetFood(dastarhanApp, handlerFood, restaurantsID);
                gf.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            } else {
                Log.d(TAG, "We have no restaurants with this cuisines");
                mpbFood.setVisibility(View.GONE);
                mrvFood.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(TAG, "Not need download food, time is not expired");
            updateFood();
        }
    }


    private void updateFood() {
        String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterMain adapter = new RVFoodAdapterMain(this, dastarhanApp, locale, restaurantsID);
        if (mrvFood.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvFood");
            mrvFood.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvFood");
            mrvFood.swapAdapter(adapter, true);
        }

        mpbFood.setVisibility(View.GONE);
        mrvFood.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_food_layout);
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
            Intent intent = new Intent(FoodActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            Intent intent = new Intent(FoodActivity.this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_bucket) {
            Intent intent = new Intent(FoodActivity.this, BasketActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(FoodActivity.this, PromoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_settings) {
            Intent intent = new Intent(FoodActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_food_layout);
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
