package mobi.esys.dastarhan;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import mobi.esys.dastarhan.tasks.GetCuisines;
import mobi.esys.dastarhan.utils.DatabaseHelper;
import mobi.esys.dastarhan.utils.RVCuisinesAdapter;

public class Restorans extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagRestorans";
    private Handler handlerCuisines;
    ProgressBar mpbCuisines;
    RecyclerView mrvCuisines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restorans);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mpbCuisines = (ProgressBar) findViewById(R.id.pbCuisines);
        mrvCuisines = (RecyclerView) findViewById(R.id.rvCuisines);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mrvCuisines.setLayoutManager(llm);

        handlerCuisines = new HandleCuisines();


        GetCuisines gc = new GetCuisines(this, handlerCuisines);
        gc.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private class HandleCuisines extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_CUISINES_SUCCESS) {  //all ok
                //TODO refresh
                //TODO Hide progress bar and hide cuisines
                Log.d(TAG, "Cuisines data received");
                updateCuisines();
            }
            if (msg.what == Constants.CALLBACK_GET_CUISINES_FAIL) {  //not ok
                //TODO refresh
                //TODO Hide progress bar and hide cuisines
                Log.d(TAG, "Cuisines data NOT receive");
                updateCuisines();
            }
            if (msg.what == Constants.CALLBACK_GET_CUISINES_SHOW_PROGRESS_BAR) {  //show progress bar
                mrvCuisines.setVisibility(View.GONE);
                mpbCuisines.setVisibility(View.VISIBLE);
            }
            super.handleMessage(msg);
        }
    }

    private void updateCuisines() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        RVCuisinesAdapter adapter = new RVCuisinesAdapter(dbHelper, this);
        if (mrvCuisines.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvCuisines");
            mrvCuisines.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvCuisines");
            mrvCuisines.swapAdapter(adapter, true);
        }
        Log.w(TAG, "CLOSE DB");


        //TODO refresh RecyclerView
        mpbCuisines.setVisibility(View.GONE);
        mrvCuisines.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            // Handle the camera action
        } else if (id == R.id.nav_action_favorites) {

        } else if (id == R.id.nav_action_bucket) {

        } else if (id == R.id.nav_action_history) {

        } else if (id == R.id.nav_action_promo) {

        } else if (id == R.id.nav_action_settings) {

        } else if (id == R.id.nav_action_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
