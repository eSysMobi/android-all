package mobi.esys.dastarhan;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.DatabaseHelper;

public class CurrentFoodActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "dtagCurrentFood";
    private transient SharedPreferences prefs;
    private int current_order;
    private boolean order_executed;

    private boolean canOrdered = false;

    private SQLiteDatabase db;
    private Cursor cursor;

    private TextView mtvCurrFoodPrice;
    private TextView mtvCurrFoodName;
    private TextView mtvCurrFoodDescr;
    private Button mbCurrFoodAddShopping;
    private ImageView mivCurrFoodFavorite;
    private ImageView mivCurrFoodVegan;

    private int currentFoodID;
    private int res_id = 0;
    private int cat_id = 0;
    private String ru_name = "";
    private String en_name = "";
    private String picture = "";
    private String ru_descr = "";
    private String en_descr = "";
    private double price = 0;
    private int min_amount = 0;
    private String units = "";
    private int ordered = 0;
    private int offer = 0;
    private int vegetarian = 0;
    private int favorite = 0;
    private int featured = 0;
    private int in_order = 0;


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

        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);
        current_order = prefs.getInt(Constants.PREF_CURR_NUM_ORDER, 1);
        order_executed = prefs.getBoolean(Constants.PREF_CURR_ORDER_EXECUTED, false);

        currentFoodID = getIntent().getIntExtra("currentFoodID", -42);

        Log.d(TAG, "Choose FOOD ID from intent = " + currentFoodID);

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
                if (favorite != 0) {
                    favorite = 0;
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                } else {
                    favorite = 1;
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                }

                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("server_id", currentFoodID);
                cv.put("res_id", res_id);
                cv.put("cat_id", cat_id);
                cv.put("ru_name", ru_name);
                cv.put("en_name", en_name);
                cv.put("picture", picture);
                cv.put("ru_descr", ru_descr);
                cv.put("en_descr", en_descr);
                cv.put("price", price);
                cv.put("min_amount", min_amount);
                cv.put("units", units);
                cv.put("ordered", ordered);
                cv.put("offer", offer);
                cv.put("vegetarian", vegetarian);
                cv.put("favorite", favorite);
                cv.put("featured", featured);
                cv.put("in_order", in_order);

                db.update(Constants.DB_TABLE_FOOD, cv, "server_id=" + currentFoodID + " and res_id=" + res_id, null);
                db.close();
            }
        });

        //click order
        mbCurrFoodAddShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canOrdered) {
                    Intent intent = new Intent(CurrentFoodActivity.this, CurrentOrderActivity.class);
                    startActivityForResult(intent, 88);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        if (data == null) {
            return;
        }
        Log.d(TAG, "all ok");
        if (requestCode == 88 && resultCode == Activity.RESULT_OK) {
            int count = data.getIntExtra("count",1);
            String notice = data.getStringExtra("notice");
            Log.d(TAG, "Saving order " + current_order + " Count: " + count + " Notice: " + notice);

            canOrdered = false;
            if (order_executed) {
                current_order++;
                order_executed = false;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Constants.PREF_CURR_NUM_ORDER, current_order);
                editor.putBoolean(Constants.PREF_CURR_ORDER_EXECUTED, order_executed);
                editor.apply();
            }

            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            db = dbHelper.getWritableDatabase();
            ContentValues cv;

            cv = new ContentValues();
            cv.put("server_id", currentFoodID);
            cv.put("res_id", res_id);
            cv.put("cat_id", cat_id);
            cv.put("ru_name", ru_name);
            cv.put("en_name", en_name);
            cv.put("picture", picture);
            cv.put("ru_descr", ru_descr);
            cv.put("en_descr", en_descr);
            cv.put("price", price);
            cv.put("min_amount", min_amount);
            cv.put("units", units);
            cv.put("ordered", ordered);
            cv.put("offer", offer);
            cv.put("vegetarian", vegetarian);
            cv.put("favorite", favorite);
            cv.put("featured", featured);
            cv.put("in_order", current_order);
            db.update(Constants.DB_TABLE_FOOD, cv, "server_id=" + currentFoodID + " and res_id=" + res_id, null);

            cv = new ContentValues();
            cv.put("id_order", current_order);
            cv.put("id_food", currentFoodID);
            cv.put("count", count);
            cv.put("price", price);
            cv.put("notice", notice);
            long rowID = db.insert(Constants.DB_TABLE_ORDERS, null, cv);
            Log.d(TAG, "row order inserted, ID = " + rowID);

            db.close();

            mbCurrFoodAddShopping.setText(R.string.cant_order);
            Toast.makeText(getApplicationContext(), "Added to shopping list", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            db = dbHelper.getReadableDatabase();

            String selectQuery;
            String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
            if (currentFoodID != -42) {
                selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD + " WHERE server_id = " + currentFoodID;

                cursor = db.rawQuery(selectQuery, null);

                String name = "";
                String priceString = "";
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();

                    res_id = cursor.getInt(cursor.getColumnIndexOrThrow("res_id"));
                    cat_id = cursor.getInt(cursor.getColumnIndexOrThrow("cat_id"));
                    ru_name = cursor.getString(cursor.getColumnIndexOrThrow("ru_name"));
                    en_name = cursor.getString(cursor.getColumnIndexOrThrow("en_name"));
                    picture = cursor.getString(cursor.getColumnIndexOrThrow("picture"));
                    ru_descr = cursor.getString(cursor.getColumnIndexOrThrow("ru_descr"));
                    en_descr = cursor.getString(cursor.getColumnIndexOrThrow("en_descr"));
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    min_amount = cursor.getInt(cursor.getColumnIndexOrThrow("min_amount"));
                    units = cursor.getString(cursor.getColumnIndexOrThrow("units"));
                    ordered = cursor.getInt(cursor.getColumnIndexOrThrow("ordered"));
                    offer = cursor.getInt(cursor.getColumnIndexOrThrow("offer"));
                    vegetarian = cursor.getInt(cursor.getColumnIndexOrThrow("vegetarian"));
                    favorite = cursor.getInt(cursor.getColumnIndexOrThrow("favorite"));
                    featured = cursor.getInt(cursor.getColumnIndexOrThrow("featured"));
                    in_order = cursor.getInt(cursor.getColumnIndexOrThrow("in_order"));

                    if (in_order < current_order) {
                        canOrdered = true;
                    }

                    if (canOrdered) {
                        mbCurrFoodAddShopping.setText(R.string.to_order);
                    } else {
                        mbCurrFoodAddShopping.setText(R.string.cant_order);
                    }

                    if (locale.equals("ru")) {
                        name = ru_name;
                    } else {
                        name = en_name;
                    }

                    mtvCurrFoodName.setText(name);

                    priceString = String.valueOf(price); //String.valueOf(currentFoodPrice);
                    priceString += " " + Constants.CURRENCY_VERY_SHORT;
                    mtvCurrFoodPrice.setText(priceString);

                    String description = "";
                    if (locale.equals("ru")) {
                        description = ru_descr;
                    } else {
                        description = en_descr;
                    }
                    mtvCurrFoodDescr.setText(description);

                    //favorite
                    if (favorite != 0) {
                        mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                    }

                    if (vegetarian == 0) {
                        mivCurrFoodVegan.setVisibility(View.GONE);
                    }
                    cursor.close();

                    //get restaurant info
                    selectQuery = "SELECT * FROM " + Constants.DB_TABLE_RESTAURANTS + " WHERE server_id = " + res_id;

                    cursor = db.rawQuery(selectQuery, null);
                    FrameLayout mflCurrFoodDescr = (FrameLayout) findViewById(R.id.flCurrFoodDescr);
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();

                        TextView mtvCurrFoodRest = (TextView) findViewById(R.id.tvCurrFoodRest);
                        if (locale.equals("ru")) {
                            mtvCurrFoodRest.setText(cursor.getString(cursor.getColumnIndexOrThrow("ru_name")));
                        } else {
                            mtvCurrFoodRest.setText(cursor.getString(cursor.getColumnIndexOrThrow("en_name")));
                        }

                        ImageView mivCurrFoodRatingRest = (ImageView) findViewById(R.id.ivCurrFoodRatingRest);
                        int rating = cursor.getInt(cursor.getColumnIndexOrThrow("total_rating"));
                        switch (rating) {
                            case 0:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_0));
                                break;
                            case 1:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_1));
                                break;
                            case 2:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_2));
                                break;
                            case 3:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_3));
                                break;
                            case 4:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_4));
                                break;
                            case 5:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_5));
                                break;
                            case 6:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_6));
                                break;
                            case 7:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_7));
                                break;
                            case 8:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_8));
                                break;
                            case 9:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_9));
                                break;
                            case 10:
                                mivCurrFoodRatingRest.setImageDrawable(getResources().getDrawable(R.drawable.rating_10));
                                break;
                        }


                        if (mflCurrFoodDescr != null) {
                            mflCurrFoodDescr.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Get mo info about restaraunt");

                                    Intent intent = new Intent(CurrentFoodActivity.this, CurrentRestaurantActivity.class);
                                    intent.putExtra("restID", res_id);
                                    startActivity(intent);
                                }
                            });
                        }

                    } else {

                        if (mflCurrFoodDescr != null) {
                            mflCurrFoodDescr.setVisibility(View.GONE);
                        }
                        cursor.close();
                    }
                    cursor.close();
                } else {
                    cursor.close();
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
            db.close();
        } catch (Exception e) {
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
            Log.e(TAG, "Error with DB: " + e.toString());
            e.printStackTrace();
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
            Intent intent = new Intent(CurrentFoodActivity.this,BasketActivity.class);
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
