package app.indvel.ibucheon;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class ScrollingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void schinfo(View V) {
        Intent i = new Intent(this, SchoolInfo.class);
        startActivity(i);
    }

    public void schnotice(View V) {
        Intent i = new Intent(this, SchoolNotice.class);
        startActivity(i);
    }

    public void schsch(View V) {
        Intent i = new Intent(this, SchoolSchedules.class);
        startActivity(i);
    }

    public void schfood(View V) {
        Intent i = new Intent(this, SchoolFood.class);
        startActivity(i);
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

        if (id == R.id.nav_app_info) {
            Intent i = new Intent(this, AppInfo.class);
            startActivity(i);
        } else if (id == R.id.nav_web) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ibucheon.hs.kr"));
            startActivity(i);
        } else if (id == R.id.nav_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "부천정보산업고등학교 앱");
            msg.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=app.indvel.ibucheon");
            msg.putExtra(Intent.EXTRA_TITLE, "정산고 앱");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "공유"));
        } else if (id == R.id.nav_another) {

            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=Infinity Grp."));
                startActivity(i);
            } catch(ActivityNotFoundException e) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Infinity Grp."));
                startActivity(i);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}