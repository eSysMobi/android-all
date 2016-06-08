package mobi.esys.dastarhan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.DatabaseHelper;
import mobi.esys.dastarhan.utils.RVFoodAdapter;

public class BasketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagBasketActivity";
    private final static int PERMISSION_REQUEST_CODE = 334;

    private Handler handler;

    private RecyclerView mrvOrders;
    private Button mbBasketAddAddress;
    private Button mbBasketSendOrder;
    private TextView mtvBasketTotalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_basket_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        handler = new BasketHandler();

        mrvOrders = (RecyclerView) findViewById(R.id.rvOrders);
        mbBasketAddAddress = (Button) findViewById(R.id.bBasketAddAddress);
        mbBasketSendOrder = (Button) findViewById(R.id.bBasketSendOrder);
        mtvBasketTotalCost = (TextView) findViewById(R.id.tvBasketTotalCost);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mrvOrders.setLayoutManager(llm);

        mbBasketAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add address");
                Intent intent = new Intent(BasketActivity.this, AddAddressActivity.class);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                mbBasketAddAddress.setEnabled(true);
            }
        } else {
            mbBasketAddAddress.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        updateList();
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mbBasketAddAddress.setEnabled(true);
                }
                break;
            }
        }
    }

    protected void updateList() {
        Log.d(TAG, "Refresh RecyclerView");
        String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        RVFoodAdapter adapter = new RVFoodAdapter(dbHelper, this, locale, Constants.ACTION_GET_FOOD_CURR_ORDERED, null, handler);
        if (mrvOrders.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvOrders");
            mrvOrders.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvOrders");
            mrvOrders.swapAdapter(adapter, true);
        }
    }

    private class BasketHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 42:
                    //refresh list
                    updateList();
                    break;
                case 43:
                    //refresh total cost
                    Log.d(TAG, "Refresh total cost");
                    Bundle bundle = message.getData();
                    double totalCost = bundle.getDouble("totalCost", 0);
                    String text = getResources().getString(R.string.total_cost) + " " + totalCost + " " + getResources().getString(R.string.currency);
                    mtvBasketTotalCost.setText(text);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_basket_layout);
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
            Intent intent = new Intent(BasketActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            Intent intent = new Intent(BasketActivity.this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_bucket) {

        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(BasketActivity.this, PromoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_settings) {
            Intent intent = new Intent(BasketActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_basket_layout);
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
