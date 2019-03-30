package binus.mat.ics.myauto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public TextView appNameSidebar;
    public TextView toolbarTitle;
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // declare custom fonts
        Typeface serifFont = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGro.ttf");
        Typeface serifFontLight = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGroLig.ttf");
        Typeface serifFontBold = Typeface.createFromAsset(getAssets(),  "fonts/NeuzeitGroBold.ttf");

        // custom title
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( MainMenuActivity.this, "hello", Toast.LENGTH_LONG).show();
            }
        });
        toolbarTitle.setText(getSupportActionBar().getTitle() + " ▾");

        // disable default title, use custom title instead
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        // set custom font
        appNameSidebar = headerView.findViewById(R.id.appNameSidebar);
        appNameSidebar.setTypeface(serifFont);

        // select Timeline view
        navigationView.getMenu().getItem(0).setChecked(true);
        Fragment fragment = null;
        fragment = new TimelineFragment();

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set padding
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        appNameSidebar.setPadding(0,statusBarHeight+10,0,0);

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
            SharedPreferences mSharedPref = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            mSharedPref.edit().putBoolean("logged_in", false).commit();

            startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        //creating fragment object
        Fragment fragment = null;

        int id = item.getItemId();
        String tag = null;

        if (id == R.id.nav_timeline) {
            fragment = new TimelineFragment();

        } else if (id == R.id.nav_vehicle_information) {

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
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTitleChanged (CharSequence title, int color) {
        toolbarTitle.setText(this.getTitle() + " ▾");
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        Toast.makeText(MainMenuActivity.this, "jajaja", Toast.LENGTH_LONG).show();
//        getSupportActionBar().setTitle("asasas");
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//
//            MarketplaceFragment test = (MarketplaceFragment) getSupportFragmentManager().findFragmentByTag("marketplace");
//            if (test != null && test.isVisible()) {
//                test.onBackKeyPressed(KeyEvent.ACTION_DOWN);
//            } else {
//                //finish();
//            }
//            return true;
//        }
//        return false;
//    }
}
