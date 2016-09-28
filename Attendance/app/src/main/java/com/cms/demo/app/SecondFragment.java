package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class SecondFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    String emp_id;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_info, container,
                false);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_id=prefs.getString("empid",null);


        mMapView = (MapView) v.findViewById(R.id.mapView);

      mMapView.onCreate(savedInstanceState);

        new MapRetrieve().execute(emp_id);
        // Perform any camera updates here
        return v;
    }

    private Bitmap writeTextOnDrawable(int drawableId) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);


        return  bm;
    }

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

        return null;
    }

 private class MapRetrieve extends AsyncTask<String,Void,String>{
     String line;
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
        progressDialog.cancel();
         try {
             if(aVoid.equals("no entry")){

                 mMapView.onResume();// needed to get the map to display immediately

                 MapsInitializer.initialize(getActivity().getApplicationContext());
                 googleMap = mMapView.getMap();
                 // latitude and longitude

                 // create marker

                 GPSTracker gps = new GPSTracker(getActivity());
                 MarkerOptions marker = new MarkerOptions().position(
                         new LatLng(gps.getLatitude(),gps.getLongitude())).title(location_details(gps.getLatitude(),gps.getLongitude()));

                 // Changing marker icon
                 marker.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_location_on_black_24dp)));

                 // adding marker
                 googleMap.addMarker(marker);


                 CameraPosition cameraPosition = new CameraPosition.Builder()
                         .target(new LatLng(gps.getLatitude(), gps.getLongitude())).zoom(17).build();
                 googleMap.animateCamera(CameraUpdateFactory
                         .newCameraPosition(cameraPosition));


             }else{
             JSONObject jObject = new JSONObject(aVoid);
             double lattitude_start = jObject.getDouble("lattitude_start");
             double longitude_start = jObject.getDouble("longitude_start");
             double lattitude_stop = jObject.getDouble("lattitude_stop");
             double longitude_stop =jObject.getDouble("longitude_stop");
             System.out.println("Lat" + lattitude_start + "Long" + longitude_start + ":attitude_stop" + lattitude_stop + "Longitude_stop" + longitude_stop);



         mMapView.onResume();// needed to get the map to display immediately

             MapsInitializer.initialize(getActivity().getApplicationContext());


         googleMap = mMapView.getMap();
         // latitude and longitude

         // create marker
         MarkerOptions marker = new MarkerOptions().position(
                 new LatLng(lattitude_start, longitude_start)).title(location_details(lattitude_start,longitude_start));

         // Changing marker icon
             marker.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_location_on_black_24dp)));

             // adding marker
         googleMap.addMarker(marker);

         MarkerOptions marker_destination = new MarkerOptions().position(new LatLng(lattitude_stop, longitude_stop)).title(location_details(lattitude_stop,longitude_stop));
             marker_destination.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_location_on_black_24dp)));

             googleMap.addMarker(marker_destination);


         CameraPosition cameraPosition = new CameraPosition.Builder()
                 .target(new LatLng(lattitude_start, longitude_start)).zoom(12).build();
         googleMap.animateCamera(CameraUpdateFactory
                 .newCameraPosition(cameraPosition));

             CameraPosition cameraPosition_destination = new CameraPosition.Builder()
                     .target(new LatLng(lattitude_stop, longitude_stop)).zoom(12).build();
             googleMap.animateCamera(CameraUpdateFactory
                     .newCameraPosition(cameraPosition_destination));
         Polyline line = googleMap.addPolyline(new PolylineOptions()
                 .add(new LatLng(lattitude_start, longitude_start), new LatLng(lattitude_stop, longitude_stop))
                 .width(10)
                 .color(Color.LTGRAY));
             }
     }catch (Exception e){
             Log.d("Exception","Maps"+e.getMessage());
         }

     }

     @Override
     protected String doInBackground(String... params) {
         try{
             String link="http://companytest.site88.net/attendance/maps.php";
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


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}