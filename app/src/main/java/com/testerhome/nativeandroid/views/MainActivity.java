package com.testerhome.nativeandroid.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.testerhome.nativeandroid.Config;
import com.testerhome.nativeandroid.R;
import com.testerhome.nativeandroid.auth.TesterHomeAccountService;
import com.testerhome.nativeandroid.fragments.HomeFragment;
import com.testerhome.nativeandroid.fragments.SettingsFragment;
import com.testerhome.nativeandroid.fragments.TopicsListFragment;
import com.testerhome.nativeandroid.models.TesterUser;
import com.testerhome.nativeandroid.views.base.BaseActivity;
import com.testerhome.nativeandroid.views.widgets.ThemeUtils;

import butterknife.Bind;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    private Fragment homeFragment;
    private Fragment jobFragment;
    private Fragment topicFragment;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    private ImageView navBackGround;
    // 是否启用夜间模式
    private String appTheme;
    private ImageView darkImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        appTheme = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.KEY_PREF_THEME, "0");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();

        setupWX();
    }

    @Override
    public boolean enableTheme() {
        return true;
    }


    private void setupWX(){
        IWXAPI api = WXAPIFactory.createWXAPI(this, Config.APP_ID, true);
        api.registerApp(Config.APP_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserInfo();
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.KEY_PREF_THEME, "0").equals(appTheme)) {
            ThemeUtils.recreateActivity(this);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity","i destroy");
    }

    private void setupView() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.realtabcontent, homeFragment).commit();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);



        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        darkImage = (ImageView)headerLayout.findViewById(R.id.main_nav_btn_theme_dark);
        navBackGround = (ImageView)headerLayout.findViewById(R.id.main_nav_img_top_background);
        mAccountAvatar = (SimpleDraweeView) headerLayout.findViewById(R.id.sdv_account_avatar);
        mAccountAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAvatarClick();
            }
        });

        darkImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onDarkClick();
            }
        });

        mAccountUsername = (TextView) headerLayout.findViewById(R.id.tv_account_username);
        mAccountEmail = (TextView) headerLayout.findViewById(R.id.tv_account_email);
        navBackGround.setVisibility(appTheme.equals("1") ? View.INVISIBLE:View.VISIBLE);
        darkImage.setImageResource(appTheme.equals("1") ? R.drawable.ic_wb_sunny_white_24dp : R.drawable.ic_brightness_3_white_24dp);

    }


    SearchView searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null){
            searchView.setOnQueryTextListener(this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Log.e("search","search clicked");
            searchView.onActionViewExpanded();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        hideAllFragment(fragmentTransaction);


        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
                fragmentTransaction.add(R.id.realtabcontent, homeFragment);
            }

            fragmentTransaction.show(homeFragment);
            if (toolbar != null) {
                toolbar.setTitle("社区");
            }
        } else if (id == R.id.nav_topic) {
            if (topicFragment == null) {
                topicFragment = TopicsListFragment.newInstance(Config.TOPICS_TYPE_LAST_ACTIVED);
                fragmentTransaction.add(R.id.realtabcontent, topicFragment);

            }
            fragmentTransaction.show(topicFragment);
            if (toolbar != null) {
                toolbar.setTitle("话题");
            }
        } else if (id == R.id.nav_job) {
            if (jobFragment == null) {
                jobFragment = TopicsListFragment.newInstance(Config.TOPIC_JOB_NODE_ID);
                fragmentTransaction.add(R.id.realtabcontent, jobFragment);

            }
            fragmentTransaction.show(jobFragment);
            if (toolbar != null) {
                toolbar.setTitle("招聘");
            }
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        drawer.closeDrawer(GravityCompat.START);

        fragmentTransaction.commit();
        return true;
    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (homeFragment != null) {
            fragmentTransaction.hide(homeFragment);
        }
        if (topicFragment != null) {
            fragmentTransaction.hide(topicFragment);
        }
        if (jobFragment != null) {
            fragmentTransaction.hide(jobFragment);
        }
    }

    SimpleDraweeView mAccountAvatar;

    TextView mAccountUsername;

    TextView mAccountEmail;



    void onAvatarClick() {
        if (mTesterHomeAccount != null && !TextUtils.isEmpty(mTesterHomeAccount.getLogin())) {
            startActivity(new Intent(this, UserInfoActivity.class).putExtra("loginName",mTesterHomeAccount.getLogin()));
        } else {
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    TesterUser mTesterHomeAccount;

    private void updateUserInfo() {
        mTesterHomeAccount = TesterHomeAccountService.getInstance(this).getActiveAccountInfo();
        if (!TextUtils.isEmpty(mTesterHomeAccount.getLogin())) {
            mAccountAvatar.setImageURI(Uri.parse(Config.getImageUrl(mTesterHomeAccount.getAvatar_url())));
            mAccountUsername.setText(mTesterHomeAccount.getName());
            mAccountEmail.setText(mTesterHomeAccount.getEmail());
        } else {
            mAccountAvatar.setImageResource(R.mipmap.ic_launcher);
            mAccountUsername.setText("未登录");
            mAccountEmail.setText("点击头像登录TesterHome");
        }
    }

    private static long back_pressed;

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerVisible(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show();
            }
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.e("search", "search:" + query);

        if (!TextUtils.isEmpty(query)){
            startActivity(new Intent(this, SearchActivity.class).putExtra("keyword", query));
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // do nothing
        return false;
    }
}
