package mobi.esys.dastarhan;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Stack;

import mobi.esys.dastarhan.database.Order;
import mobi.esys.dastarhan.database.OrderUpdateEvent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BaseFragment.FragmentNavigation {

    private final String TAG = "dtagMainActivity";
    private final FragmentManager fm = getSupportFragmentManager();
    private Stack<String> titles = new Stack<>();

    private final EventBus bus = EventBus.getDefault();
    private AppComponent component;

    private TextView title;
    private TextView cartCount;
    private LinearLayout llCartToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            cartCount = (TextView) toolbar.findViewById(R.id.toolbar_cart_count);
            llCartToolbar = (LinearLayout) toolbar.findViewById(R.id.llCartToolbar);
            llCartToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Fragment> fragments = fm.getFragments();
                    Fragment currentFragment = new BaseFragment();
                    if (fragments.size() > 0) {
                        currentFragment = fragments.get(fragments.size() - 1);
                    }
                    if (!(currentFragment instanceof BasketFragment)) {
                        BasketFragment fragment = BasketFragment.newInstance();
                        replaceFragment(fragment, "Корзина");
                    }
                }
            });
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        component = ((DastarhanApp) getApplication()).appComponent();

        //set cart count
        updateCartCount();

        replaceFragment(MainFragment.newInstance(), "Dastarhan");
    }

    @Override
    public void replaceFragment(Fragment fragment, String settingTitle) {
        String backStateName = fragment.getClass().getName();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.mainContainer, fragment);
        ft.addToBackStack(backStateName);
        ft.commit();
        if (title != null) {
            titles.push(settingTitle);
            title.setText(settingTitle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onOrderUpdateEvent(OrderUpdateEvent event) {
        updateCartCount();
    }

    private void updateCartCount(){
        List<Order> orders = component.cartRepository().getCurrentCartOrders();
        if(orders!= null){
            cartCount.setText(String.valueOf(orders.size()));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
            String popped = titles.pop();
            String peeked = titles.peek();
            if(peeked!= null){
                settingTitle(peeked);
            }
        }
    }

    void settingTitle(String incTitle){
        title.setText(incTitle);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        List<Fragment> fragments = fm.getFragments();
        Fragment currentFragment = new BaseFragment();
        if (fragments.size() > 0) {
            currentFragment = fragments.get(fragments.size() - 1);
        }

        if (id == R.id.nav_action_menu) {
            //check is current fragment NOT is MainFragment
            if (!(currentFragment instanceof MainFragment)) {
                MainFragment fragment = MainFragment.newInstance();
                replaceFragment(fragment, "Dastarhan");
            }
        } else if (id == R.id.nav_action_favorites) {
            //check is current fragment NOT is FavoriteFragment
            if (!(currentFragment instanceof FavoriteFragment)) {
                FavoriteFragment fragment = FavoriteFragment.newInstance();
                replaceFragment(fragment, "Избранное");
            }
        } else if (id == R.id.nav_action_bucket) {
            //check is current fragment NOT is BasketFragment
            if (!(currentFragment instanceof BasketFragment)) {
                BasketFragment fragment = BasketFragment.newInstance();
                replaceFragment(fragment, "Корзина");
            }
        } else if (id == R.id.nav_action_history) {
            //TODO history fragment
        } else if (id == R.id.nav_action_promo) {
            //check is current fragment NOT is PromoFragment
            if (!(currentFragment instanceof PromoFragment)) {
                PromoFragment fragment = PromoFragment.newInstance();
                replaceFragment(fragment, "Акции");
            }
        } else if (id == R.id.nav_action_settings) {
            //check is current fragment NOT is SettingFragment
            if (!(currentFragment instanceof SettingFragment)) {
                SettingFragment fragment = SettingFragment.newInstance();
                replaceFragment(fragment, "Настройки");
            }
        } else if (id == R.id.nav_action_info) {
            //TODO info fragment
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
