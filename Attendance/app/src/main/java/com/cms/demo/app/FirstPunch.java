package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by NoName on 2/20/2016.
 */
public class FirstPunch extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback{
    Button start,stop;
    TextView details,stop_text;

    GPSTracker gps;
    boolean gpsenabled;

    private List<StoreTime> timeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StoreTimeAdapter mAdapter;
    Button punch_start;
    String lat,long_vale;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeLayout;
    EditText message_punch;
    String emp_id;

    public FirstPunch(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView=inflater.inflate(R.layout.punch_one,container,false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        punch_start=(Button)rootView.findViewById(R.id.punch_button);
        mAdapter = new StoreTimeAdapter(timeList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        swipeLayout=(SwipeRefreshLayout)rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        message_punch=(EditText)rootView.findViewById(R.id.message_punch);
        gps = new GPSTracker(getActivity());


        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_id=prefs.getString("empid",null);

        new SendTime().execute(emp_id,"","","onStart","","");



        punch_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !isMockSettingsON(getActivity())){
                    if(gps.canGetLocation()){ // gps enabled
                        gpsenabled=true;


                        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        String locationProvider = LocationManager.NETWORK_PROVIDER;
                        try {
                            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

                            double latitude = gps.getLatitude();

                            double longitude = gps.getLongitude();
                            String data = message_punch.getText().toString();
                            Log.d("data", "" + data);

                            if (data != null)
                                Log.d("data", "" + data);
                            else {
                                data = "null";
                                Log.d("data", "" + data);
                            }
                            lat = String.valueOf(latitude);
                            long_vale = String.valueOf(longitude);
                            if (latitude == 0.0 || longitude == 0.0) {
                                Toast.makeText(getActivity(), "Cannot find the location please try again", Toast.LENGTH_SHORT).show();
                            } else {
                                new SendTime().execute(emp_id, lat, long_vale, "updated", data, location_details(latitude, longitude));
                            }
                        }catch (SecurityException e){

                        }
                    }else
                        settingsrequest();
                }else{
                    getActivity().finish();
                    Toast.makeText(getActivity(),"Please disable mock locations.",Toast.LENGTH_SHORT).show();
                }

            }

        });



        return rootView;
    }

    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
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
    public void onStart() {
        super.onStart();
        if(isMockSettingsON(getActivity())){
            getActivity().finish();
            Toast.makeText(getActivity(),"Please disable mock locations.",Toast.LENGTH_SHORT).show();
        }else{
            if(!gps.canGetLocation()){
                settingsrequest();
            }


        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
                new SendTime().execute(emp_id,"20","73","onStart","","");
            }
        }, 2000);
    }

    private void prepareMovieData(String start_time,String location_data,String message) {

        StoreTime time_store = new StoreTime(start_time,location_data,message);
        timeList.add(time_store);
        mAdapter.notifyDataSetChanged();

    }
    String[] time;

    public String location_details(double latitude,double longitude){
        try{
            Geocoder myLocation = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> myList = myLocation.getFromLocation(latitude,longitude, 1);
            Address address = (Address) myList.get(0);
            String addressStr = "";
            addressStr += address.getAddressLine(0) + ", ";
            addressStr += address.getAddressLine(2);

            return addressStr;
        }catch (IOException e){
            Log.d("ExceptionMap",""+e.getMessage());
        }
        return "Cannot find location details";
    }

    private class SendTime extends AsyncTask<String,Void,String> {
        String line;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("Please wait!");
            if(!progressDialog.isShowing())
                            progressDialog.show();
        }


        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.cancel();
            try{
                if(aVoid!=null){

                    org.json.JSONObject punch_details=new org.json.JSONObject(aVoid);
                    org.json.JSONArray details=punch_details.getJSONArray("punch_details");
                    Log.d("Details",""+details.length());
                    if(!(details.length()<1)){
                        time=new String[details.length()];
                        String[] message_array=new String[details.length()];
                        String[] location=new String[details.length()];
                        timeList.clear();
                        for(int i=0;i<time.length;i++){
                            org.json.JSONObject data=details.getJSONObject(i);
                            time[i]=data.getString("timestamp");
                            location[i]=data.getString("location");
                            message_array[i]=data.getString("message");
                            prepareMovieData(time[i],location[i],message_array[i]);
                            if(i==time.length-1){
                                Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();

                            }
                        }}else
                        Toast.makeText(getActivity(),"No punches", Toast.LENGTH_SHORT).show();
                  }else
                    Toast.makeText(getActivity(),"No punches", Toast.LENGTH_SHORT).show();

            }catch (JSONException e){
                Log.d("Exception",e.getMessage());
            }
            //          JSONObject jsonObject = (JSONObject) obj;
            //         String data=(String)jsonObject.get("date");
            //        String time=(String)jsonObject.get("time");

        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/punch.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data +="&"+URLEncoder.encode("lattitude" , "UTF-8")+"="+URLEncoder.encode(params[1],"UTF-8");
                data +="&"+URLEncoder.encode("longitude" , "UTF-8")+"="+URLEncoder.encode(params[2],"UTF-8");
                data+="&"+URLEncoder.encode("identifier","UTF-8")+"="+URLEncoder.encode(params[3],"UTF-8");
                data+="&"+URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode(params[4],"UTF-8");
                data+="&"+URLEncoder.encode("location","UTF-8")+"="+URLEncoder.encode(params[5],"UTF-8");


                URL url = new URL(link);



                HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Connection", "close");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();
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
                reader.close();
                wr.close();

            }

            catch(Exception e){
                System.out.println("Your ID"+e.getMessage()+" Output "+e.toString() );
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;


        }
    }
}
