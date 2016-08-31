package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.RVFoodAdapterFavorite;

public class FavoriteActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mrvFavorite;
    private final String TAG = "dtagFavorite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_favorite_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setEnabled(false);
//        navigationView.getMenu().getItem(1).setTitle("4564");

        mrvFavorite = (RecyclerView) findViewById(R.id.rvFavorite);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mrvFavorite.setLayoutManager(llm);

    }

    @Override
    protected void onResume() {
        updateFavoriteList();
        super.onResume();
    }

    private void updateFavoriteList(){
        String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterFavorite adapter = new RVFoodAdapterFavorite(this, (DastarhanApp) getApplication(), locale);
        if (mrvFavorite.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvFavorite");
            mrvFavorite.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvFavorite");
            mrvFavorite.swapAdapter(adapter, true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_favorite_layout);
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
            Intent intent = new Intent(FavoriteActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_favorites) {
            //not need
        } else if (id == R.id.nav_action_bucket) {
            Intent intent = new Intent(FavoriteActivity.this,BasketActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {
            Intent intent = new Intent(FavoriteActivity.this,PromoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_settings) {
            Intent intent = new Intent(FavoriteActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_favorite_layout);
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
