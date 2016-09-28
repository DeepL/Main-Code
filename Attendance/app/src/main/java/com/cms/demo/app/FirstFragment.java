package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by NoName on 2/20/2016.
 */
public class FirstFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {
    Button start,stop;
    TextView details,stop_text;
    String emp_id;

    GPSTracker gps;
    boolean gpsenabled;
   public FirstFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView=inflater.inflate(R.layout.fragment_one,container,false);
        start=(Button)rootView.findViewById(R.id.start_time);
        stop=(Button)rootView.findViewById(R.id.stop_time);

        details=(TextView)rootView.findViewById(R.id.date_display);
        stop_text=(TextView)rootView.findViewById(R.id.stop_text);



        if(start.isClickable()){

            stop.setBackgroundColor(Color.parseColor("#BDBDBD"));
            stop.setTextColor(Color.parseColor("#CFD8DC"));
        }



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // gps enabled

                if(gps.canGetLocation()){ // gps enabled
                    if(!isMockSettingsON(getActivity()))
                        new Check_Internet().execute("start");
                    else{
                        Toast.makeText(getActivity(), "Please disable mock locations and then open the app", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }else
                    settingsrequest();


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // gps enabled

                if(!start.isClickable()){
                if(gps.canGetLocation()) { // gps enabled
                    if(!isMockSettingsON(getActivity()))
                    new Check_Internet().execute("stop");
                    else{
                        Toast.makeText(getActivity(), "Please disable mock locations", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }else
                    settingsrequest();;// gps.showSettingsAlert();
                }else{
                    Toast.makeText(getActivity(),"You can only stop once started.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    public void settingsrequest(){

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getActivity())
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
                                    getActivity(), 1000);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {

            if (resultCode==getActivity().RESULT_OK) {

                    Toast.makeText(getActivity(), "GPS enabled", Toast.LENGTH_LONG).show();

            } else {
                        Toast.makeText(getActivity(), "GPS is not enabled", Toast.LENGTH_LONG).show();
                    settingsrequest();

            }

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


    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    boolean check_internet;

    public  boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e("Internet", "Error checking internet connection", e);
            }
        } else {
            Log.d("Internet", "No network available!");
        }
        return false;
    }

    private class Check_Internet extends AsyncTask<String,Void,Boolean> {
        String status;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            check_internet=hasActiveInternetConnection(getActivity());
            status=params[0];
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result){


                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                String locationProvider = LocationManager.NETWORK_PROVIDER;
        if(status.equals("start")){

// Or use LocationManager.GPS_PROVIDER
            try{
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                double latitude =lastKnownLocation.getLatitude();;

                double longitude =lastKnownLocation.getLongitude();

                String lat=String.valueOf(latitude);
                String long_vale=String.valueOf(longitude);
                if(latitude==0.0 || longitude==0.0){
                    Toast.makeText(getActivity(),"Cannot find the location please try again later",Toast.LENGTH_SHORT).show();

                    start.setBackgroundColor(Color.parseColor("#BDBDBD"));
                    start.setTextColor(Color.parseColor("#263238"));

                    stop.setBackgroundColor(Color.parseColor("#BDBDBD"));
                    stop.setTextColor(Color.parseColor("#CFD8DC"));
                }else
                    new SendTime().execute(emp_id,"start",lat,long_vale);
            }catch(SecurityException e){
                e.printStackTrace();
            }



        }else if(status.equals("stop")) {

            try {
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                double latitude = lastKnownLocation.getLatitude();                ;

                double longitude = lastKnownLocation.getLongitude();
            String lat = String.valueOf(latitude);
            String long_vale = String.valueOf(longitude);
            if (latitude == 0.0 || longitude == 0.0) {
                Toast.makeText(getActivity(), "Cannot find the location please try again", Toast.LENGTH_SHORT).show();
            } else
                new SendTime().execute(emp_id, "stop", lat, long_vale);
        }catch(SecurityException e){

            }
        }else{
            new SendTime().execute(emp_id,"onStart","0","0");
         }
            }
            else{
                Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if(isMockSettingsON(getActivity())){
            getActivity().finish();
            Toast.makeText(getActivity(),"Please disable mock locations.",Toast.LENGTH_SHORT).show();
        }else{
            gps = new GPSTracker(getActivity());

            if(gps.canGetLocation()){

            SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
            emp_id=prefs.getString("empid",null);
                new Check_Internet().execute("onStart");

            }else
                settingsrequest();
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

    private class SendTime extends AsyncTask<String,Void,String> {
    String line,identifier;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            if(identifier.equals("start")){

                details.setText(aVoid);
                start.setClickable(false);
                start.setText("Started");
                start.setBackgroundColor(Color.parseColor("#616161"));
                start.setTextColor(Color.parseColor("#263238"));
                stop.setClickable(true);
                stop.setBackgroundColor(Color.parseColor("#FAFAFA"));
                stop.setTextColor(Color.parseColor("#263238"));

            }else if(aVoid.equals("noentry")){
             //   Toast.makeText(getActivity(),"No entries",Toast.LENGTH_SHORT).show();
                start.setClickable(true);
                start.setText("Start");
                stop.setClickable(false);
                stop.setText("Stop");
                start.setBackgroundColor(Color.parseColor("#263238"));
                stop.setBackgroundColor(Color.parseColor("#BDBDBD"));
                stop.setTextColor(Color.parseColor("#CFD8DC"));
            }else if(identifier.equals("stop")){
                    stop_text.setText(aVoid);
                    stop.setClickable(false);
                    stop.setText("Stopped");
                    stop.setBackgroundColor(Color.parseColor("#616161"));
                    stop.setTextColor(Color.parseColor("#FAFAFA"));
            }else{
              //  Toast.makeText(getActivity(), "Already Started", Toast.LENGTH_SHORT).show();

                Object obj = JSONValue.parse(aVoid);
                JSONObject jsonObject = (JSONObject) obj;
                String starting_time=(String)jsonObject.get("starting_time");
                String stopping_time=(String)jsonObject.get("stopping_time");
                start.setClickable(false);
                start.setText("Started");
                start.setBackgroundColor(Color.parseColor("#616161"));
                start.setTextColor(Color.parseColor("#FAFAFA"));
                if(stopping_time==null){
                    details.setText(starting_time);
                    stop.setClickable(true);
                    stop.setBackgroundColor(Color.parseColor("#FAFAFA"));
                    stop.setTextColor(Color.parseColor("#263238"));

                }else{
                    details.setText(starting_time);
                    stop_text.setText(stopping_time);

                    stop.setClickable(false);
                    stop.setText("Stopped");
                    stop.setBackgroundColor(Color.parseColor("#616161"));
                    stop.setTextColor(Color.parseColor("#FAFAFA"));
                }

                try {

                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                    double latitude = lastKnownLocation.getLatitude();
                    ;

                    double longitude = lastKnownLocation.getLongitude();

                    if(latitude==0.0 || longitude==0.0){
                        start.setClickable(false);
                        stop.setClickable(false);

                        stop.setBackgroundColor(Color.parseColor("#616161"));
                        stop.setTextColor(Color.parseColor("#FAFAFA"));

                        start.setBackgroundColor(Color.parseColor("#616161"));
                        start.setTextColor(Color.parseColor("#FAFAFA"));

                        Toast.makeText(getActivity(),"No GPS Available, Please try again later",Toast.LENGTH_SHORT).show();
                    }
                }catch (SecurityException e){
                    e.printStackTrace();
                }
            }

            progressDialog.cancel();
               }

        @Override
        protected String doInBackground(String... params) {

            try{
                identifier=params[1];
                String link="http://companytest.site88.net/attendance/store.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data += "&" + URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data +="&"+URLEncoder.encode("lattitude" , "UTF-8")+"="+URLEncoder.encode(params[2],"UTF-8");
                data +="&"+URLEncoder.encode("longitude" , "UTF-8")+"="+URLEncoder.encode(params[3],"UTF-8");
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
}
