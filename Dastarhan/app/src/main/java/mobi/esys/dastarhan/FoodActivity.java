package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import mobi.esys.dastarhan.tasks.GetFood;
import mobi.esys.dastarhan.tasks.GetRestaurants;
import mobi.esys.dastarhan.utils.DatabaseHelper;
import mobi.esys.dastarhan.utils.RVFoodAdapter;

public class FoodActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mrvFood;
    private ProgressBar mpbFood;
    private Handler handlerFood;
    private final String TAG = "dtagFood";

    private int restID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        handlerFood = new HandleFood();

        restID = getIntent().getIntExtra("restID", -42);
        Log.d(TAG, "Restaurant ID from intent = " + restID);

        GetFood gf = new GetFood(this, handlerFood, restID);
        gf.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private class HandleFood extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_FOOD_SUCCESS) {  //all ok
                Log.d(TAG, "Food data received");
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_FAIL) {  //not ok
                Log.d(TAG, "Food data NOT receive");
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR) {  //show progress bar
                mrvFood.setVisibility(View.GONE);
                mpbFood.setVisibility(View.VISIBLE);
            }
            super.handleMessage(msg);
        }
    }

    private void updateFood() {
        String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        RVFoodAdapter adapter = new RVFoodAdapter(dbHelper, this, locale, false, restID);
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
            Intent intent = new Intent(FoodActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            Intent intent = new Intent(FoodActivity.this,FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_bucket) {

        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {

        } else if (id == R.id.nav_action_settings) {

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
