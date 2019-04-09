package binus.mat.ics.myauto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import binus.mat.ics.myauto.structures.CarResponseStructure;
import binus.mat.ics.myauto.structures.ConnectionErrorFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public TextView appNameSidebar;
    public TextView toolbarTitle;
    public TextView toolbatSubtitle;
    public LinearLayout toolbarTitleLayout;
    public ImageView toolbarArrowDown;
    public Toolbar toolbar;
    private int timelineId;
    private int vehicleInfoId;
    private ShimmerFrameLayout mShimmerViewContainer;
    private ShimmerFrameLayout mShimmerTitleView;


    // OkHttp
    public static final MediaType JSON = MediaType.get("application/json");
    String GetProfileUrl = "http://wendrian.duckdns.org/stanley/myauto/api/getuserinfo.php";
    String PostUrl = "http://wendrian.duckdns.org/stanley/myauto/api/vehiclelist.php";

    CarResponseStructure[] responseArray;
    Gson gson = new Gson();

    OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build();

    boolean shouldExecuteOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        toolbatSubtitle = findViewById(R.id.toolbarSubTitle);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitleLayout = findViewById(R.id.toolbarTitleLayout);
        toolbarArrowDown = findViewById(R.id.toolbarArrowDown);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerTitleView = findViewById(R.id.title_shimmer_layout);

        setSupportActionBar(toolbar);

        mShimmerViewContainer.startShimmerAnimation();
        mShimmerTitleView.startShimmerAnimation();
        shouldExecuteOnResume = false;

        // declare custom fonts
        Typeface serifFont = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGro.ttf");
        Typeface serifFontLight = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGroLig.ttf");
        Typeface serifFontBold = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGroBold.ttf");

        // custom title (change car)
        toolbarTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if array ready
                if (responseArray != null && responseArray[0].result == 1) {
                    //Toast.makeText(MainMenuActivity.this, "hello", Toast.LENGTH_LONG).show();
                    // setup the alert builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
                    builder.setTitle(getString(R.string.choose_vehicle));

                    ArrayList<CharSequence> carType = new ArrayList<>();
                    for (CarResponseStructure temp : responseArray) {
                        carType.add(temp.brand + " " + temp.type + " (" + temp.license_plate + ")");
                    }

                    carType.add(getString(R.string.action_manage_vehicle));

                    CharSequence[] cs = carType.toArray(new CharSequence[carType.size()]);

                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == cs.length-1) {
                                // make ArrayList
                                ArrayList<CarResponseStructure> vehicles = new ArrayList<>(Arrays.asList(responseArray));
                                // make bundle
                                Bundle extras = new Bundle();

                                extras.putSerializable("vehicle_data", vehicles);

                                Intent intent = new Intent(getApplicationContext(), ManageVehicleActivity.class);
                                intent.putExtra("bundle", extras);
                                startActivity(intent);
                            } else {
                                MainMenuActivity.this.setTitle(responseArray[which].brand + " " + responseArray[which].type);
                                toolbatSubtitle.setText(responseArray[which].license_plate);
                                toolbarArrowDown.setVisibility(View.VISIBLE);
                                toolbatSubtitle.setVisibility(View.VISIBLE);
                                refreshFragment(which);
                            }
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        toolbarTitle.setText(getSupportActionBar().getTitle());

        // disable default title, use custom title instead
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        // set fragment id
        timelineId = navigationView.getMenu().getItem(0).getItemId();
        vehicleInfoId = navigationView.getMenu().getItem(1).getItemId();

        // set custom font
        appNameSidebar = headerView.findViewById(R.id.appNameSidebar);
        appNameSidebar.setTypeface(serifFont);


        // set padding
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // convert dp to px
        float dip = 5f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        appNameSidebar.setPadding(0,statusBarHeight+(int)px,0,0);

        requestVehicleData();

    }

    private void requestVehicleData() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        // make JSON to get vehicle data
        SharedPreferences mSharedPref = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        Map<String, String> postParam = new HashMap<>();
        postParam.put("user_id", mSharedPref.getString("user_id", "null"));
        postParam.put("login_hash", mSharedPref.getString("user_hash", "null"));


        // Convert Map to JSONObject
        JSONObject jObj = new JSONObject(postParam);

        // Get vehicle data
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(PostUrl).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(getApplicationContext().toString(), e.toString());
                call.cancel();
                runOnUiThread(() -> {
                    // stop shimmer
                    stopShimmer();

                    Fragment fragment = new ConnectionErrorFragment();

                    if (fragment != null) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        ft.replace(R.id.content_frame, fragment);
                        ft.commit();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseArray = gson.fromJson(response.body().string(), CarResponseStructure[].class);
                runOnUiThread(() -> {
                    if(responseArray[0].result == 0) {
                        Log.e("MyAuto", "Token invalid! Logging out...");
                        Toast.makeText(MainMenuActivity.this, getString(R.string.session_expired), Toast.LENGTH_LONG).show();
                        doLogout();
                    } else if (responseArray[0].result == 2) {
                        // user has no vehicle registered. handle the problem
                        // stop shimmer
                        stopShimmer();

                        //replacing the fragment
                        // select Timeline view
                        navigationView.getMenu().getItem(0).setChecked(true);
                        Fragment fragment = new NoVehicleFragment();

                       setTitle(getString(R.string.app_name));

                        toolbarArrowDown.setVisibility(View.GONE);
                        toolbatSubtitle.setVisibility(View.GONE);

                        if (fragment != null) {
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                            ft.replace(R.id.content_frame, fragment);
                            ft.commit();
                        }

                    } else if (responseArray[0].result == 1){
                        SharedPreferences mSharedPref = getSharedPreferences("MainMenuActivity", Context.MODE_PRIVATE);

                        if (responseArray.length-1 < mSharedPref.getInt("current_index", 0)) {
                            mSharedPref.edit()
                                    .remove("current_index")
                                    .commit();
                        }

                        setTitle(responseArray[mSharedPref.getInt("current_index", 0)].brand + " " + responseArray[mSharedPref.getInt("current_index", 0)].type);
                        toolbatSubtitle.setText(responseArray[mSharedPref.getInt("current_index", 0)].license_plate);

                        toolbarArrowDown.setVisibility(View.VISIBLE);
                        toolbatSubtitle.setVisibility(View.VISIBLE);

                        // stop shimmer
                        stopShimmer();

                        //replacing the fragment
                        // select Timeline view
                        navigationView.getMenu().getItem(0).setChecked(true);
                        Fragment fragment = null;
                        fragment = TimelineFragment.newInstance(mSharedPref.getInt("current_index", 0));

                        if (fragment != null) {
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                            ft.replace(R.id.content_frame, fragment);
                            ft.commit();
                        }
                    } else {
                        Log.e("MyAuto", "Unknown error. Error code: " + responseArray[0].errorString);
                        Toast.makeText(MainMenuActivity.this, getString(R.string.unknown_error) + " " + responseArray[0].errorString, Toast.LENGTH_LONG).show();
                        doLogout();
                    }
                });
            }
        });
    }

    private void stopShimmer() {
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerTitleView.stopShimmerAnimation();

        mShimmerViewContainer.setVisibility(View.GONE);
        mShimmerTitleView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(shouldExecuteOnResume){
            // refresh cars
            requestVehicleData();
        } else {
            shouldExecuteOnResume = true;
        }
    }

    private void refreshFragment(int index) {
        Fragment fragment = null;

        NavigationView n = findViewById(R.id.nav_view);

        if (n.getCheckedItem() != null) {
            if (n.getCheckedItem().getItemId() == timelineId) {
                fragment = TimelineFragment.newInstance(index);
            } else if (n.getCheckedItem().getItemId() == vehicleInfoId) {
                fragment = VehicleInfoFragment.newInstance(index);
            }
        }

        SharedPreferences.Editor sp = getSharedPreferences("MainMenuActivity", Context.MODE_PRIVATE).edit();
        sp.putInt("current_index", index);
        sp.apply();

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            MarketplaceFragment test = (MarketplaceFragment) getSupportFragmentManager().findFragmentByTag("marketplace");
            NewsFragment test2 = (NewsFragment) getSupportFragmentManager().findFragmentByTag("news");

            if ((test != null && test.isVisible()) || (test2 != null && test2.isVisible())) {
                if (test != null && test.isVisible()) {
                    test.onBackKeyPressed(KeyEvent.KEYCODE_BACK);
                } else {
                    test2.onBackKeyPressed(KeyEvent.KEYCODE_BACK);
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            doLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doLogout() {
        SharedPreferences mSharedPref = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        SharedPreferences mSharedP = getSharedPreferences("MainMenuActivity", Context.MODE_PRIVATE);
        mSharedPref.edit().clear().commit();
        mSharedP.edit().clear().commit();

        startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));

        finish();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        // if item hasn't been loaded
        if (responseArray != null) {
            //creating fragment object
            Fragment fragment = null;

            int id = item.getItemId();
            String tag = null;
            SharedPreferences mSharedPref = getSharedPreferences("MainMenuActivity", Context.MODE_PRIVATE);

            if (id == R.id.nav_timeline) {
                fragment = TimelineFragment.newInstance(mSharedPref.getInt("current_index", 0));
                tag = "timeline";

            } else if (id == R.id.nav_vehicle_information) {
                fragment = VehicleInfoFragment.newInstance(mSharedPref.getInt("current_index", 0));
                tag = "vehicle_info";

            } else if (id == R.id.nav_report) {

            } else if (id == R.id.nav_services_nearby) {

            } else if (id == R.id.nav_emergency_services) {

            } else if (id == R.id.nav_marketplace) {
                fragment = new MarketplaceFragment();
                tag = "marketplace";

            } else if (id == R.id.nav_news) {
                fragment = new NewsFragment();
                tag = "news";

            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));

            } else if (id == R.id.nav_about) {

            }

            //replacing the fragment
            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                ft.replace(R.id.content_frame, fragment, tag);
                ft.commit();
            }
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }
    }

    @Override
    public void onTitleChanged (CharSequence title, int color) {
        toolbarTitle.setText(this.getTitle());
    }
}
