package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import static com.cms.demo.app.R.id.mapView;

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {
    String username_async,emp_id;
    View bottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    MapView m;
    GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_mainscreen);
     bottomSheet =findViewById( R.id.bottom_sheet );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        emp_id=prefs.getString("empid",null);
        if(emp_id==null || username_async==null)
        new GetDetails().execute(emp_id);
        final TextView attendance_start=(TextView)findViewById(R.id.attendance_button);
        final TextView punch_start=(TextView)findViewById(R.id.punch_button);
        final TextView inventory_start=(TextView)findViewById(R.id.inventory_button);
        final TextView location_start=(TextView)findViewById(R.id.location_button);

        inventory_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start=new Intent(MainScreen.this,InventoryMain.class);
                startActivity(start);
                finish();
            }
        });
            attendance_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent start=new Intent(MainScreen.this,MainActivity.class);
                    startActivity(start);
                    finish();
                }
            });


            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        m = (MapView) findViewById(mapView);
        m.onCreate(savedInstanceState);
// Gets to GoogleMap from the MapView and does initialization stuff
        map = m.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);


        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        map.setMyLocationEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(false);
        MapsInitializer.initialize(getApplicationContext());


        punch_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start=new Intent(MainScreen.this,Punch.class);
                startActivity(start);
                finish();
//               // Toast.makeText(getApplicationContext(),"Disabled",Toast.LENGTH_SHORT).show();


            }
        });

        location_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                    double latitude = lastKnownLocation.getLatitude();
                    ;

                    double longitude = lastKnownLocation.getLongitude();

                    if (latitude == 0.0 || longitude == 0.0) {
                        Toast.makeText(getApplicationContext(), "No GPS Available, Please try again later", Toast.LENGTH_SHORT).show();
                    }else{

                        // Updates the location and zoom of the MapView

                        MarkerOptions marker = new MarkerOptions().position(
                                new LatLng(latitude, longitude)).title(location_details(latitude,longitude));

                        // Changing marker icon
                        marker.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_location_on_black_24dp)));

                        // adding marker
                        map.addMarker(marker);


                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude)).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));

                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }

                }catch (SecurityException e){
                    e.printStackTrace();
                }
            }
        });

       final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                TextView employee=(TextView)drawerView.findViewById(R.id.drawer_username);
                employee.setText(username_async);

                TextView username=(TextView)drawerView.findViewById(R.id.drawer_id);
                username.setText(emp_id);

            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




    }


    public String location_details(double latitude,double longitude){
        try{
            Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> myList = myLocation.getFromLocation(latitude,longitude, 1);
            Address address = (Address) myList.get(0);
            String addressStr = "";
            addressStr += address.getAddressLine(0) + ", ";
            addressStr += address.getAddressLine(2);

            return addressStr;
        }catch (IOException e){
            Log.d("ExceptionMap",""+e.getMessage());
        }

        return null;
    }

    private Bitmap writeTextOnDrawable(int drawableId) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);


        return  bm;
    }
    @Override
    public void onResume() {
        m.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        m.onLowMemory();
    }

    public void showDialog(View c){

     /*   if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_HIDDEN){

            mMapView = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.mapView)).getMap();
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);*/



    }


    private class GetDetails extends AsyncTask<String,Void,String>{
        String id,line;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            username_async=s;
            SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit=prefs.edit();
            edit.putString("username",s);
            edit.commit();

        }

        @Override
        protected String doInBackground(String... params) {


            try{
                String link="http://companytest.site88.net/attendance/details.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.d("sent",""+data);
                wr.write(data);
                wr.flush();
                Log.d("writeen",""+data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();

                // Read Server Response
                while((line = reader.readLine()) != null){
                    Log.d("Receiving", "" + line);
                    sb.append(line);

                    //sb.append(line);
                    break;
                }
                //     return sb.toString();
            }
            catch(Exception e){
                System.out.println("Your ID"+e.getMessage());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;

        }
    }

    public void settingsrequest(){

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.

                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainScreen.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    static int REQUEST_CHECK_SETTINGS = 1000;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {


            switch(resultCode){

                case RESULT_OK:
                    Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_LONG).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
                    settingsrequest();
                    break;
            }

            /*if (resultCode == RESULT_OK) {

                Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
             settingsrequest();

            }*/

        }
    }

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

    public void openMessage(View v){
        Intent send_sms=new Intent(MainScreen.this,SendSms.class);
        startActivity(send_sms);
    }

    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    boolean check_internet;

    public  boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
            }
        } else {
        }
        return false;
    }

    private class Check_Internet extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            check_internet=hasActiveInternetConnection(getApplicationContext());
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result){



            }else{
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
               finish();
            }
        }
    }




    @Override
    protected void onStart(){
        super.onStart();
        GPSTracker gps = new GPSTracker(this);

     if(isMockSettingsON(getApplicationContext())){
         finish();
         Toast.makeText(MainScreen.this, "Please disable mock locations and then open the app", Toast.LENGTH_SHORT).show();

     }else{
        if(gps.canGetLocation()){
            //runFadeInAnimation();
            new Check_Internet().execute();
        }else{
            settingsrequest();
        }

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



    private class GetInfo extends AsyncTask<String,Void,String>{
        String id,line;
        String identifier;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(MainScreen.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.cancel();
            if(s.equals("Registered")){
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor edit=prefs.edit();
                edit.putString("username",s);
                edit.commit();

                if(identifier.equals("manager")){

                    Intent start_login=new Intent(MainScreen.this,Manager.class);
                    startActivity(start_login);
                    finish();
                }
                if(identifier.equals("department")){
                    Intent start_login=new Intent(MainScreen.this,Department.class);
                    startActivity(start_login);
                    finish(); 
                }

                }else
                Toast.makeText(getApplicationContext(),"You are not eligibile !",Toast.LENGTH_SHORT).show();

        }

        @Override
        protected String doInBackground(String... params) {

            identifier=params[1];
            try{
                String link="http://companytest.site88.net/attendance/check_details.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.d("sent",""+data);
                wr.write(data);
                wr.flush();
                Log.d("writeen",""+data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();

                // Read Server Response
                while((line = reader.readLine()) != null){
                    Log.d("Receiving", "" + line);
                    sb.append(line);

                    //sb.append(line);
                    break;
                }
                //     return sb.toString();
            }
            catch(Exception e){
                System.out.println("Your ID"+e.toString());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.END);
        }else if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
                System.exit(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.manager) {
            // Handle the camera action
            new GetInfo().execute(emp_id,"manager");
        } else if (id == R.id.logout) {

            SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit=prefs.edit();
            edit.putBoolean("login",false);
            edit.commit();
            Intent start_login=new Intent(MainScreen.this,Login.class);
            startActivity(start_login);
            finish();
        }else if(id==R.id.department){
            new GetInfo().execute(emp_id,"department");
        }

         DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
