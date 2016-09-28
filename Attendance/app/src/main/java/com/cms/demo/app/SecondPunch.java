package com.cms.demo.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SecondPunch extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    String emp_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_id=prefs.getString("empid",null);
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_info, container,
                false);
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



    private class MapRetrieve extends AsyncTask<String,Void,String>{
        String line;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            try {
                JSONObject myobj=new JSONObject(aVoid);
                org.json.JSONArray array=myobj.getJSONArray("location");
                double[] store_lattitude=new double[array.length()];
                double[] store_longitude=new double[array.length()];
                mMapView.onResume();// needed to get the map to display immediately

                MapsInitializer.initialize(getActivity().getApplicationContext());

                googleMap = mMapView.getMap();
                for(int i=0;i<store_lattitude.length;i++){
                    JSONObject asd=array.getJSONObject(i);

                    store_lattitude[i]=asd.getDouble("lattitude");
                    store_longitude[i]=asd.getDouble("longitude");
                    MarkerOptions marker = new MarkerOptions().position(
                            new LatLng(store_lattitude[i], store_longitude[i])).title(asd.getString("locationDetails"));

                    // Changing marker icon
                    marker.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_location_on_black_24dp)));

                    // adding marker
                    googleMap.addMarker(marker);



                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(store_lattitude[i], store_longitude[i])).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));

                    CameraPosition cameraPosition_destination = new CameraPosition.Builder()
                            .target(new LatLng(store_lattitude[0], store_longitude[0])).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition_destination));


                    if(i>0){
                        Polyline line = googleMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(store_lattitude[i-1], store_longitude[i-1]), new LatLng(store_lattitude[i], store_longitude[i]))
                                .width(10)
                                .color(Color.GRAY));
                    }

                    Log.d("Data","Lat"+store_lattitude[i]+"Long"+store_longitude[i]);
                }


                // latitude and longitude
                for(int i=0;i<store_lattitude.length;i++){

                    // create marker

                }
            }catch (Exception e){
                Log.d("Exception",aVoid+" Maps"+e.getMessage());
            }

        }

        @Override
        protected String doInBackground(String... params) {
            try{
                String link="http://companytest.site88.net/attendance/punches_map.php";
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