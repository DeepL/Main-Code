package com.cms.demo.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import java.util.ArrayList;
import java.util.List;

public class Department extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {


    android.support.v7.widget.Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    String departmentId;
    SharedPreferences prefs;
    public static Context main;
    @Override
    public void onResult(@NonNull Result result) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        SharedPreferences.Editor editor=prefs.edit();
        editor.remove("main_id");
        editor.remove("assign_emp");
        editor.remove("check");
        editor.commit();
        Intent start_previous=new Intent(Department.this,MainScreen.class);
        startActivity(start_previous);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:

                SharedPreferences.Editor editor=prefs.edit();
                editor.remove("main_id");
                editor.remove("assign_emp");
                editor.remove("check");
                editor.commit();
                Intent start_previous=new Intent(Department.this,MainScreen.class);
                startActivity(start_previous);
                finish();
                return true;

            default:        return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lisiinventory);
        main=getApplicationContext();
        prefs= PreferenceManager.getDefaultSharedPreferences(Department.this);
        departmentId=String.valueOf(prefs.getInt("departmentId",0));
        toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.inventory_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager=(ViewPager)findViewById(R.id.inventory_viewPager);
        setupViewPager(viewPager);
        tabLayout=(TabLayout)findViewById(R.id.inventory_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }
    @Override
    protected void onStart(){
        super.onStart();

        //   runFadeInAnimation();
        if(isMockSettingsON(getApplicationContext())){

            finish();
            //   Toast.makeText(getApplicationContext(),"Please disable mock locations",Toast.LENGTH_SHORT).show();


        }

    }







    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FirstFragment.REQUEST_CHECK_SETTINGS) {

            if (resultCode==RESULT_OK){
                Toast.makeText(getApplicationContext(),"GPS Enabled",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext()," GPS Disabled",Toast.LENGTH_SHORT).show();
              //  settingsrequest();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    void setupViewPager(ViewPager vpager){

        ViewPagerAdapter adapter=new ViewPagerAdapter(getSupportFragmentManager());
        String department=null;
        if(departmentId.equals("1")){
            department="OHS";
        }
        if(departmentId.equals("2")){
            department="Admin";
        }
        if(departmentId.equals("3"))
            department="IT";
        if(departmentId.equals("4"))
            department="DT";
        adapter.addFragment(new FirstManager(), department);
       // adapter.addFragment(new SecondManager(), "Admin");
        //adapter.addFragment(new ThirdManager(), "IT");
        adapter.addFragment(new AssignInventory(),"Assign Inventory");
        vpager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }


    public void selectPage(int page) {
        viewPager.setCurrentItem(page);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {


        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(android.support.v4.app.FragmentManager fragment){
            super(fragment);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
